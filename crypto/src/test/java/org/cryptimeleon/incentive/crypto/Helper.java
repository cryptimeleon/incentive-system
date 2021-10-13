package org.cryptimeleon.incentive.crypto;

import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.PromotionParameters;
import org.cryptimeleon.incentive.crypto.model.Token;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.cryptimeleon.math.structures.rings.cartesian.RingElementVector;

import java.math.BigInteger;

public class Helper {
    static Token generateToken(IncentivePublicParameters pp,
                               UserKeyPair userKeyPair,
                               ProviderKeyPair providerKeyPair,
                               PromotionParameters promotionParameters) {
        return generateToken(pp,
                userKeyPair,
                providerKeyPair,
                promotionParameters,
                Vector.iterate(BigInteger.valueOf(0), v -> v, promotionParameters.getStoreSize()));
    }

    static Token generateToken(IncentivePublicParameters pp,
                               UserKeyPair userKeyPair,
                               ProviderKeyPair providerKeyPair,
                               PromotionParameters promotionParameters,
                               Vector<BigInteger> points) {
        var vectorH = Util.constructH(providerKeyPair.getPk(), pp, promotionParameters);
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
                pointsVector,
                (SPSEQSignature) pp.getSpsEq().sign(
                        providerKeyPair.getSk().getSkSpsEq(),
                        c1,
                        c2
                )
        );

    }

}
