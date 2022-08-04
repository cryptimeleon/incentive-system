package org.cryptimeleon.incentive.crypto;

import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.PromotionParameters;
import org.cryptimeleon.incentive.crypto.model.Token;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderSecretKey;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPreKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPublicKey;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserSecretKey;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.cryptimeleon.math.structures.rings.cartesian.RingElementVector;

import java.math.BigInteger;

public class TestSuite {
    static public final IncentivePublicParameters pp = IncentiveSystem.setup(128, Setup.BilinearGroupChoice.Debug);
    static public final IncentiveSystem incentiveSystem = new IncentiveSystem(pp);
    static public final ProviderKeyPair providerKeyPair = incentiveSystem.generateProviderKeys();
    static public final UserPreKeyPair userPreKeyPair = incentiveSystem.generateUserKeys();
    static public final UserKeyPair userKeyPair = Util.addGenesisSignatureToUserKeys(userPreKeyPair, providerKeyPair, pp);
    static public final ProviderSecretKey providerSecretKey = providerKeyPair.getSk();
    static public final ProviderPublicKey providerPublicKey = providerKeyPair.getPk();
    static public final UserSecretKey userSecretKey = userKeyPair.getSk();
    static public final UserPublicKey userPublicKey = userKeyPair.getPk();

    /**
     * Generates a sound empty (i.e. no points) user token as output by a sound execution of the Issue-Join protocol.
     */
    public static Token generateToken(PromotionParameters promotionParameters) {
        return generateToken(
                promotionParameters,
                Vector.iterate(BigInteger.valueOf(0), v -> v, promotionParameters.getPointsVectorSize())
        );
    }

    /**
     * Generates a valid user token, as output by a sound execution of the Issue-Join protocol followed by an execution of Credit-Earn with the passed earn vector.
     */
    public static Token generateToken(PromotionParameters promotionParameters,
                                      Vector<BigInteger> points) {
        var vectorH = providerKeyPair.getPk().getH(pp, promotionParameters);
        var zp = pp.getBg().getZn();
        // Manually create a token since issue-join is not yet implemented
        var encryptionSecretKey = zp.getUniformlyRandomNonzeroElement();
        var dsrd1 = zp.getUniformlyRandomElement();
        var dsrd2 = zp.getUniformlyRandomElement();
        var z = zp.getUniformlyRandomElement();
        var t = zp.getUniformlyRandomElement();
        var pointsVector = RingElementVector.fromStream(points.stream().map(e -> pp.getBg().getZn().createZnElement(e)));
        var exponents = RingElementVector.of(
                t, userKeyPair.getSk().getUsk(), encryptionSecretKey, dsrd1, dsrd2, z
        );
        exponents = exponents.concatenate(pointsVector);
        var c1 = vectorH.innerProduct(exponents).compute();
        var c2 = pp.getG1Generator();

        return new Token(
                c1,
                c2,
                encryptionSecretKey,
                dsrd1,
                dsrd2,
                z,
                t,
                promotionParameters.getPromotionId(),
                pointsVector,
                (SPSEQSignature) pp.getSpsEq().sign(
                        providerKeyPair.getSk().getSkSpsEq(),
                        c1,
                        c2,
                        c2.pow(promotionParameters.getPromotionId())
                )
        );
    }
}
