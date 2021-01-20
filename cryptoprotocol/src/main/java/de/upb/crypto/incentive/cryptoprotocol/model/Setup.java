package de.upb.crypto.incentive.cryptoprotocol.model;


import de.upb.crypto.craco.prf.PrfKey;
import de.upb.crypto.craco.prf.aes.AesPseudorandomFunction;
import de.upb.crypto.craco.sig.sps.eq.SPSEQPublicParameters;
import de.upb.crypto.craco.sig.sps.eq.SPSEQSignatureScheme;
import de.upb.crypto.craco.sig.sps.eq.SPSEQSigningKey;
import de.upb.crypto.craco.sig.sps.eq.SPSEQVerificationKey;
import de.upb.crypto.incentive.cryptoprotocol.model.keys.provider.ProviderKeyPair;
import de.upb.crypto.incentive.cryptoprotocol.model.keys.provider.ProviderPublicKey;
import de.upb.crypto.incentive.cryptoprotocol.model.keys.provider.ProviderSecretKey;
import de.upb.crypto.incentive.cryptoprotocol.model.keys.user.UserKeyPair;
import de.upb.crypto.incentive.cryptoprotocol.model.keys.user.UserPublicKey;
import de.upb.crypto.incentive.cryptoprotocol.model.keys.user.UserSecretKey;
import de.upb.crypto.math.interfaces.hash.HashIntoStructure;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.pairings.generic.BilinearGroup;
import de.upb.crypto.math.pairings.type3.bn.BarretoNaehrigBilinearGroup;
import de.upb.crypto.math.structures.zn.Zn;
import de.upb.crypto.math.structures.zn.Zn.ZnElement;

/**
 * a class implementing the incentive system algorithms (of the incentive system from the 2020 paper) that are not part of any protocol
 */
public class Setup {
    public static final int PRF_KEY_LENGTH = 256;

    /**
     * generates public parameters from security parameter
     * @param securityParameter integer representation of security parameter
     * @return public parameters as object representation
     */
    public static PublicParameters trustedSetup(int securityParameter) {
        // generate a bilinear group from the security parameter (type 3, Barreto-Naehrig)
        BilinearGroup bg = new BarretoNaehrigBilinearGroup(securityParameter);

        HashIntoStructure hf = bg.getHashIntoG1(); // hash function {0,1}^* -> G_1

        // compute w (base used in double spending protection, see 2020 incsys paper)
        // TODO: can element be cast to a group element? Jan: yes should work
        GroupElement w = (GroupElement) hf.hashIntoStructure("w" + bg.toString()); // TODO: ... .toString ok? in PR anmerken

        // compute seventh base for user token (h7 in 2020 incsys paper)
        GroupElement h7 = (GroupElement) hf.hashIntoStructure("h7" + bg.toString());

        // instantiate prf used in this instance of the incentive system with a proper key length
        AesPseudorandomFunction prf = new AesPseudorandomFunction(PRF_KEY_LENGTH);

        // instantiate SPS-EQ scheme used in this instance of the incentive system
        SPSEQSignatureScheme spsEq = new SPSEQSignatureScheme(new SPSEQPublicParameters(bg)); // TODO: correct bilinear group?

        // wrap up all values
        return new PublicParameters(bg, w, h7, prf, spsEq);
    }

    /**
     * generates a user key pair from public parameters
     * @param pp object representation of public parameters
     * @return object representation of a user key pair (see 2020 inc sys paper)
     */
    public static UserKeyPair userKeyGen(PublicParameters pp)
    {
        // draw random exponent for the user secret key
        Zn usedZn = pp.getBG().getZn(); // the remainder class ring used in this instance of the incentive system
        ZnElement usk = usedZn.getUniformlyRandomElement(); // secret exponent

        // generate the user key for the PRF used to generate pseudorandomness in the system
        PrfKey betaUsr = pp.getPrf().generateKey(); // key the user uses to generate pseudorandomness

        // compute user public key from secret exponent
        GroupElement upkElem = pp.getW().pow(usk);

        // wrap up values
        UserSecretKey USK = new UserSecretKey(usk, betaUsr);
        UserPublicKey upk = new UserPublicKey(upkElem);
        return new UserKeyPair(upk, USK);
    }


    public static ProviderKeyPair providerKeyGen(PublicParameters pp)
    {
        // TODO: generate key pair for the regular signature scheme (or retrieve it like the SPS key pair)

        // draw the dlogs of the first 6 bases used in the Pedersen commitment in the token
        ZnElement[] q = new ZnElement[6];
        for(int i=0; i < q.length; i++)
        {
            q[i] = pp.getBG().getZn().getUniformlyRandomElement();
        }

        // compute above first 6 bases
        GroupElement[] h = new GroupElement[6];
        GroupElement g1Generator = pp.getBG().getG1().getGenerator();
        for(int i = 0; i < h.length; i++)
        {
            h[i] =g1Generator.pow(q[i]);
        }

        // generate PRF key for provider
        PrfKey betaProv = pp.getPrf().generateKey();

        // wrap up values
        SPSEQVerificationKey spseqVerificationKey = pp.getSpsEq().getVerificationKey();
        SPSEQSigningKey spseqSigningKey = pp.getSpsEq().getSigningKey();
        ProviderPublicKey pk = new ProviderPublicKey(spseqVerificationKey, sigmaPublicKey, h);
        ProviderSecretKey sk = new ProviderSecretKey(spseqSigningKey, sigmaSigningKey, q, betaProv);
        return new ProviderKeyPair(sk, pk);
    }
}
