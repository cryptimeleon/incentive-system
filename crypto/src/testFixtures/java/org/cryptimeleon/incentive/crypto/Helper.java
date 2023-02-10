package org.cryptimeleon.incentive.crypto;

import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.incentive.crypto.model.*;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.cryptimeleon.math.structures.rings.cartesian.RingElementVector;

import java.math.BigInteger;

/**
 * Class that creates some random mathematical objects. Used to shorten tests and sometimes system code.
 */
public class Helper {
    /**
     * Generates a sound empty (i.e. no points) user token as output by a sound execution of the Issue-Join protocol.
     */
    public static Token generateToken(IncentivePublicParameters pp,
                                      UserKeyPair userKeyPair,
                                      ProviderKeyPair providerKeyPair,
                                      PromotionParameters promotionParameters) {
        return generateToken(pp,
                userKeyPair,
                providerKeyPair,
                promotionParameters,
                Vector.iterate(BigInteger.valueOf(0), v -> v, promotionParameters.getPointsVectorSize()));
    }

    /**
     * Generates a valid user token, as output by a sound execution of the Issue-Join protocol followed by an execution of Credit-Earn with the passed earn vector.
     */
    public static Token generateToken(IncentivePublicParameters pp,
                                      UserKeyPair userKeyPair,
                                      ProviderKeyPair providerKeyPair,
                                      PromotionParameters promotionParameters,
                                      Vector<BigInteger> points) {
        var vectorH = providerKeyPair.getPk().getH(pp, promotionParameters);
        var zp = pp.getBg().getZn();
        // Manually create a token since issue-join is not yet implemented
        var dsid = zp.getUniformlyRandomElement();
        var dsrd = zp.getUniformlyRandomElement();
        var z = zp.getUniformlyRandomElement();
        var t = zp.getUniformlyRandomElement();
        var pointsVector = RingElementVector.fromStream(points.stream().map(e -> pp.getBg().getZn().createZnElement(e)));
        var exponents = RingElementVector.of(
                t, userKeyPair.getSk().getUsk(), dsid, dsrd, z
        );
        exponents = exponents.concatenate(pointsVector);
        var c1 = vectorH.innerProduct(exponents).compute();
        var c2 = pp.getG1Generator();

        return new Token(
                c1,
                dsid,
                dsrd,
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
