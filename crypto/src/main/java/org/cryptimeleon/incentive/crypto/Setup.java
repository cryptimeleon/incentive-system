package org.cryptimeleon.incentive.crypto;


import org.cryptimeleon.craco.protocols.arguments.sigma.schnorr.setmembership.SetMembershipPublicParameters;
import org.cryptimeleon.craco.sig.SignatureKeyPair;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQPublicParameters;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignatureScheme;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSigningKey;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQVerificationKey;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderSecretKey;
import org.cryptimeleon.incentive.crypto.model.keys.user.*;
import org.cryptimeleon.math.hash.impl.SHA256HashFunction;
import org.cryptimeleon.math.prf.PrfKey;
import org.cryptimeleon.math.prf.zn.HashThenPrfToZn;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.groups.cartesian.GroupElementVector;
import org.cryptimeleon.math.structures.groups.debug.DebugBilinearGroup;
import org.cryptimeleon.math.structures.groups.elliptic.BilinearGroup;
import org.cryptimeleon.math.structures.groups.elliptic.type3.bn.BarretoNaehrigBilinearGroup;
import org.cryptimeleon.math.structures.rings.cartesian.RingElementVector;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.cryptimeleon.math.structures.rings.zn.Zn.ZnElement;
import org.cryptimeleon.mclwrap.bn254.MclBilinearGroup;

import java.math.BigInteger;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * a class implementing the incentive system algorithms (of the incentive system from the 2020 paper) that are not part of any protocol
 * (namely Setup, P.KeyGen, U.KeyGen)
 */
public class Setup {
    // Which AES version to use for the PRF-to-ZN
    public static final int HASH_THEN_PRF_AES_KEY_LENGTH = 256;
    // The size of the metadata base vector of a token
    public static final int H_SIZE_WITHOUT_POINTS = 6;
    // (1/2)^OVERSUBSCRIPTION is a lower bound for probability of the PRF failing
    private static final int HASH_THEN_PRF_OVERSUBSCRIPTION = 128;
    // Base for esk decomposition. Requires as many signed digits in the provider's public key, but reduces digits and
    // hence verifications required in the Spend-Deduct protocol for the encryption secret key
    public static final long ESK_DEC_BASE = 256;
    // ESK_DEC_BASE^MAX_POINTS_BASE_POWER determines the maximum number of points a user can have.
    // This is due to the CCS range proof used for v>=k in Spend-Deduct
    private static final int MAX_POINTS_BASE_POWER = 6;
    // Determines the maximum size of a promotion's points vector under a provider public key
    private static final int MAX_POINTS_VECTOR_SIZE = 10;

    /**
     * Generates public parameters from security parameter
     *
     * @param securityParameter   integer representation of security parameter
     * @param bilinearGroupChoice determines which group to use. Use Debug for testing only since it is not secure!
     * @return public parameters as object representation
     */
    public static IncentivePublicParameters trustedSetup(int securityParameter, BilinearGroupChoice bilinearGroupChoice) {
        // generate a bilinear group from the security parameter depending on the group choice
        BilinearGroup bg;
        switch (bilinearGroupChoice) {
            case Debug:
                bg = new DebugBilinearGroup(BilinearGroup.Type.TYPE_3);
                break;
            case BarretoNaehrig:
                bg = new BarretoNaehrigBilinearGroup(securityParameter);
                break;
            case Herumi_MCL:
                bg = new MclBilinearGroup();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + bilinearGroupChoice);
        }


        /*
         * note: in trusted setup (as in this implementation), it does not matter how w and h7 are generated
         */

        // compute w (base of user public keys)
        GroupElement w = bg.getG1().getUniformlyRandomElement().compute(); // w=e not a problem, see discord #questions 2.2.21

        // compute seventh base for user token (h7 in 2020 incsys paper)
        GroupElement h7 = bg.getG1().getUniformlyRandomElement().compute();

        // PrfToZn instantiation
        HashThenPrfToZn prfToZn = new HashThenPrfToZn(HASH_THEN_PRF_AES_KEY_LENGTH, bg.getZn(), new SHA256HashFunction(), HASH_THEN_PRF_OVERSUBSCRIPTION);

        // instantiate SPS-EQ scheme used in this instance of the incentive system
        SPSEQSignatureScheme spsEq = new SPSEQSignatureScheme(new SPSEQPublicParameters(bg));

        // draw generators for groups in used bilinear group at random
        GroupElement g1 = bg.getG1().getGenerator().compute();
        GroupElement g2 = bg.getG2().getGenerator().compute();

        // prepare encryptionSecretKey decomposition for ZKP in SpendDeduct
        var eskBaseSet = LongStream.range(0, ESK_DEC_BASE).mapToObj(BigInteger::valueOf).collect(Collectors.toSet());
        var eskBaseSetMembershipPublicParameters = SetMembershipPublicParameters.generate(bg, eskBaseSet);

        // wrap up all values
        return new IncentivePublicParameters(bg, w, h7, g1, g2, prfToZn, spsEq, bg.getZn().valueOf(ESK_DEC_BASE), MAX_POINTS_BASE_POWER, eskBaseSetMembershipPublicParameters);
    }

    /**
     * Generates a user key pair from public parameters
     *
     * @param pp object representation of public parameters
     * @return UserKeyPair
     */
    public static UserPreKeyPair userKeyGen(IncentivePublicParameters pp) {
        // draw random exponent for the user secret key
        Zn usedZn = pp.getBg().getZn(); // the remainder class ring used in this instance of the incentive system
        ZnElement usk = usedZn.getUniformlyRandomElement(); // secret exponent

        // generate the user key for the PRF used to generate pseudorandomness in the system
        PrfKey betaUsr = pp.getPrfToZn().generateKey(); // key the user uses to generate pseudorandomness

        // compute user public key from secret exponent
        GroupElement upkElem = pp.getW().pow(usk).compute();

        // wrap up values
        UserPreSecretKey uSk = new UserPreSecretKey(usk, betaUsr);
        UserPublicKey uPk = new UserPublicKey(upkElem);
        return new UserPreKeyPair(uPk, uSk);
    }

    /**
     * Generates a provider key pair from public parameters
     *
     * @param pp object representation of public parameters
     * @return ProviderKeyPair
     */
    public static ProviderKeyPair providerKeyGen(IncentivePublicParameters pp) {
        // draw the dlogs of the bases used in the Pedersen commitment in the token
        RingElementVector q = pp.getBg().getZn().getUniformlyRandomElements(6 + MAX_POINTS_VECTOR_SIZE);

        // compute above bases
        GroupElementVector h = pp.getG1Generator().pow(q).compute();

        // generate PRF key for provider
        PrfKey betaProv = pp.getPrfToZn().generateKey();

        // generate SPS-EQ key pairs
        SignatureKeyPair<SPSEQVerificationKey, SPSEQSigningKey> tokenSpsEqKeyPair = pp.getSpsEq().generateKeyPair(3);
        SignatureKeyPair<SPSEQVerificationKey, SPSEQSigningKey> genesisSpsEqKeyPair = pp.getSpsEq().generateKeyPair(2);

        // wrap up values
        ProviderPublicKey pk = new ProviderPublicKey(tokenSpsEqKeyPair.getVerificationKey(), genesisSpsEqKeyPair.getVerificationKey(), h);
        ProviderSecretKey sk = new ProviderSecretKey(tokenSpsEqKeyPair.getSigningKey(), genesisSpsEqKeyPair.getSigningKey(), q, betaProv);
        return new ProviderKeyPair(sk, pk);
    }


    // Enum for choosing the bilinear group
    public enum BilinearGroupChoice {
        Debug,
        BarretoNaehrig,
        Herumi_MCL
    }
}
