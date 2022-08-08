package org.cryptimeleon.incentive.crypto.crypto;

import org.cryptimeleon.incentive.crypto.BilinearGroupChoice;
import org.cryptimeleon.incentive.crypto.Helper;
import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.Util;
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

import java.math.BigInteger;

public class TestSuite {
    static public final IncentivePublicParameters pp = IncentiveSystem.setup(128, BilinearGroupChoice.Debug);
    static public final IncentiveSystem incentiveSystem = new IncentiveSystem(pp);
    static public final ProviderKeyPair providerKeyPair = incentiveSystem.generateProviderKeyPair();
    static public final UserPreKeyPair userPreKeyPair = incentiveSystem.generateUserPreKeyPair();
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
        return Helper.generateToken(pp, userKeyPair, providerKeyPair, promotionParameters, points);
    }
}
