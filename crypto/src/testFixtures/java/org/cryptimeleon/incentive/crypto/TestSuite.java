package org.cryptimeleon.incentive.crypto;

import org.cryptimeleon.incentive.crypto.callback.IDsidBlacklistHandler;
import org.cryptimeleon.incentive.crypto.callback.ITransactionDBHandler;
import org.cryptimeleon.incentive.crypto.model.*;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.store.StoreKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPreKeyPair;
import org.cryptimeleon.math.hash.UniqueByteRepresentable;
import org.cryptimeleon.math.misc.ByteArrayImpl;
import org.cryptimeleon.math.structures.cartesian.Vector;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class TestSuite {
    static public final IncentivePublicParameters pp = IncentiveSystem.setup(128, BilinearGroupChoice.Debug);
    static public final IncentiveSystem incentiveSystem = new IncentiveSystem(pp);
    static public final StoreKeyPair storeKeyPair = incentiveSystem.generateStoreKeyPair();
    static public final ProviderKeyPair providerKeyPair = incentiveSystem.generateProviderKeyPair();
    static public final UserPreKeyPair userPreKeyPair = incentiveSystem.generateUserPreKeyPair();
    static public final UserKeyPair userKeyPair = Util.addRegistrationSignatureToUserPreKeys(userPreKeyPair, providerKeyPair, pp);
    static public final IncentiveSystemRestorer incentiveSystemRestorer = new IncentiveSystemRestorer(pp);
    static public final UniqueByteRepresentable context = new ByteArrayImpl("contex".getBytes());

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

    public static EarnStoreResponse getEarnCouponForPromotion(Token token, Vector<BigInteger> earnAmount, UUID basketId, BigInteger promotionId) {
        EarnStoreRequest earnStoreRequest = incentiveSystem.generateEarnCouponRequest(token, userKeyPair);
        return incentiveSystem.signEarnCoupon(storeKeyPair, earnAmount, earnStoreRequest, basketId, promotionId, new TestRedeemedHandler());
    }

    public static class TestDsidBlacklist implements IDsidBlacklistHandler {
        final HashMap<Zn.ZnElement, Zn.ZnElement> dsMap = new HashMap<>();

        @Override
        public boolean containsDsidWithDifferentGamma(Zn.ZnElement doubleSpendingId, Zn.ZnElement gamma) {
            return dsMap.containsKey(doubleSpendingId) && !dsMap.get(doubleSpendingId).equals(gamma);
        }

        @Override
        public void addEntryIfDsidNotPresent(Zn.ZnElement doubleSpendingId, Zn.ZnElement gamma) {
            dsMap.putIfAbsent(doubleSpendingId, gamma);
        }
    }

    public static class TestTransactionDbHandler implements ITransactionDBHandler {
        final HashMap<EarnProviderRequest, byte[]> earnData = new HashMap<>();
        final ArrayList<SpendTransactionData> spendData = new ArrayList<>();

        @Override
        public void addEarnData(EarnProviderRequest earnProviderRequest, byte[] h) {
            earnData.put(earnProviderRequest, h);
        }

        @Override
        public void addSpendData(SpendTransactionData spendTransactionData) {
            spendData.add(spendTransactionData);
        }
    }
}
