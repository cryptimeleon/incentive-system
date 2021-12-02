package org.cryptimeleon.incentive.crypto;

import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.incentive.crypto.model.*;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPublicKey;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.cryptimeleon.math.structures.groups.Group;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.PromotionParameters;
import org.cryptimeleon.incentive.crypto.model.Token;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.cryptimeleon.math.structures.rings.cartesian.RingElementVector;

import java.math.BigInteger;

/**
 * Class that creates some random mathematic objects. Used to shorten tests.
 */
public class Helper {
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

    public static Token generateToken(IncentivePublicParameters pp,
                                      UserKeyPair userKeyPair,
                                      ProviderKeyPair providerKeyPair,
                                      PromotionParameters promotionParameters,
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

    /**
     * Creates and returns a transaction with spend amount 1 and random other fields.
     * @param valid whether the generated transaction shall be valid or not
     */
    // TODO: make spend amount random once basket server endpoint is implemented
    public static Transaction generateTransaction(IncentivePublicParameters pp, boolean valid) {
        Zn usedZn = pp.getBg().getZn();
        Group usedG1 = pp.getBg().getG1();

        return new Transaction(
            valid,
            usedZn.getUniformlyRandomElement(),
            BigInteger.ONE,
            new DoubleSpendingTag(
                    usedZn.getUniformlyRandomElement(),
                    usedZn.getUniformlyRandomElement(),
                    usedZn.getUniformlyRandomElement(),
                    usedZn.getUniformlyRandomElement(),
                    usedG1.getUniformlyRandomElements(3),
                    usedG1.getUniformlyRandomElements(3)
            )
        );
    }

    /**
     * Creates random user info.
     * @return UserInfo
     */
    public static UserInfo generateUserInfo(IncentivePublicParameters pp) {
        Zn usedZn = pp.getBg().getZn();
        Group usedG1 = pp.getBg().getG1();

        return new UserInfo(
                new UserPublicKey(usedG1.getUniformlyRandomElement()),
                usedZn.getUniformlyRandomElement(),
                usedZn.getUniformlyRandomElement()
        );
    }

    /**
     * Helper method to shorten code, returns a serialized representation of the passed representable.
     */
    public static String computeSerializedRepresentation(Representable r) {
        JSONConverter jsonConverter = new JSONConverter();
        return jsonConverter.serialize(
                r.getRepresentation()
        );
    }

}
