package org.cryptimeleon.incentivesystem.cryptoprotocol;


import org.cryptimeleon.craco.prf.PrfKey;
import org.cryptimeleon.craco.prf.aes.AesPseudorandomFunction;
import org.cryptimeleon.craco.sig.SignatureKeyPair;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQPublicParameters;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignatureScheme;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSigningKey;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQVerificationKey;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.IncentivePublicParameters;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.provider.ProviderSecretKey;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.user.UserKeyPair;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.user.UserPublicKey;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.user.UserSecretKey;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.groups.cartesian.GroupElementVector;
import org.cryptimeleon.math.structures.groups.elliptic.BilinearGroup;
import org.cryptimeleon.math.structures.groups.elliptic.type3.bn.BarretoNaehrigBilinearGroup;
import org.cryptimeleon.math.structures.rings.cartesian.RingElementVector;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.cryptimeleon.math.structures.rings.zn.Zn.ZnElement;

/**
 * a class implementing the incentive system algorithms (of the incentive system from the 2020 paper) that are not part of any protocol
 * (namely Setup, P.KeyGen, U.KeyGen)
 */
public class Setup {
    public static final int PRF_KEY_LENGTH = 128;

    /**
     * generates public parameters from security parameter
     *
     * @param securityParameter integer representation of security parameter
     * @return public parameters as object representation
     */
    public static IncentivePublicParameters trustedSetup(int securityParameter) {
        // generate a bilinear group from the security parameter (type 3, Barreto-Naehrig)
        BilinearGroup bg = new BarretoNaehrigBilinearGroup(securityParameter);

        // TODO: rewrite computation of w and h7 once proper hashing of bilinear groups is possible
        // compute w (base used in double spending protection, see 2020 incsys paper)
        GroupElement w = bg.getG1().getUniformlyRandomElement(); // w=e not a problem, see discord #questions 2.2.21

        // compute seventh base for user token (h7 in 2020 incsys paper)
        GroupElement h7 = bg.getG1().getUniformlyRandomElement();

        // instantiate prf used in this instance of the incentive system with a proper key length
        AesPseudorandomFunction prf = new AesPseudorandomFunction(PRF_KEY_LENGTH);

        // instantiate SPS-EQ scheme used in this instance of the incentive system
        SPSEQSignatureScheme spsEq = new SPSEQSignatureScheme(new SPSEQPublicParameters(bg));

        // Random generators
        GroupElement g1 = bg.getG1().getGenerator();
        GroupElement g2 = bg.getG2().getGenerator();

        // wrap up all values
        return new IncentivePublicParameters(bg, w, h7, g1, g2, prf, spsEq);
    }

    /**
     * generates a user key pair from public parameters
     *
     * @param pp object representation of public parameters
     * @return object representation of a user key pair (see 2020 inc sys paper)
     */
    public static UserKeyPair userKeyGen(IncentivePublicParameters pp) {
        // draw random exponent for the user secret key
        Zn usedZn = pp.getBg().getZn(); // the remainder class ring used in this instance of the incentive system
        ZnElement usk = usedZn.getUniformlyRandomElement(); // secret exponent

        // generate the user key for the PRF used to generate pseudorandomness in the system
        PrfKey betaUsr = pp.getPrf().generateKey(); // key the user uses to generate pseudorandomness

        // compute user public key from secret exponent
        GroupElement upkElem = pp.getW().pow(usk);

        // wrap up values
        UserSecretKey uSk = new UserSecretKey(usk, betaUsr);
        UserPublicKey uPk = new UserPublicKey(upkElem);
        return new UserKeyPair(uPk, uSk);
    }


    public static ProviderKeyPair providerKeyGen(IncentivePublicParameters pp) {
        // draw the dlogs of the first 6 bases used in the Pedersen commitment in the token
        RingElementVector q = pp.getBg().getZn().getUniformlyRandomElements(6);

        // compute above first 6 bases
        GroupElementVector h = pp.getG1().pow(q);

        // generate PRF key for provider
        PrfKey betaProv = pp.getPrf().generateKey();

        // generate SPS-EQ key pair
        SignatureKeyPair<SPSEQVerificationKey, SPSEQSigningKey> spsEqKeyPair = pp.getSpsEq().generateKeyPair(2);
        SPSEQVerificationKey spseqVerificationKey = spsEqKeyPair.getVerificationKey();
        SPSEQSigningKey spseqSigningKey = spsEqKeyPair.getSigningKey();

        // wrap up values
        ProviderPublicKey pk = new ProviderPublicKey(spseqVerificationKey, h);
        ProviderSecretKey sk = new ProviderSecretKey(spseqSigningKey, q, betaProv);
        return new ProviderKeyPair(sk, pk);
    }
}
