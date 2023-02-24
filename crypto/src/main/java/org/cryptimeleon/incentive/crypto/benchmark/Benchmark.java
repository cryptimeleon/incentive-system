package org.cryptimeleon.incentive.crypto.benchmark;


import org.cryptimeleon.craco.common.ByteArrayImplementation;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.callback.IStoreBasketRedeemedHandler;
import org.cryptimeleon.incentive.crypto.model.*;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderSecretKey;
import org.cryptimeleon.incentive.crypto.model.keys.store.StoreKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.store.StorePublicKey;
import org.cryptimeleon.incentive.crypto.model.keys.store.StoreSecretKey;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPublicKey;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserSecretKey;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.math.structures.cartesian.Vector;

import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * Contains benchmark code.
 */
public class Benchmark {

    /**
     * Increase/decrease used for earn/spend in benchmark
     */
    private static final Vector<BigInteger> EARN_SPEND_AMOUNT = Vector.of(BigInteger.valueOf(10L), BigInteger.valueOf(10L));

    /**
     * Run a benchmark with the given benchmarkConfig
     *
     * @param benchmarkConfig a configuration object that defines parameters of the benchmark
     * @return a BenchmarkResult object that contains all data collected throught the benchmark
     */
    public static BenchmarkResult runBenchmark(
            BenchmarkConfig benchmarkConfig
    ) {
        return runBenchmark(
                benchmarkConfig,
                (benchmarkState, integer) -> {
                }
        );
    }

    /**
     * Run a benchmark with the given benchmarkConfig and side effects for logging progress, updating an UI.
     *
     * @param benchmarkConfig  a configuration object that defines parameters of the benchmark
     * @param feedbackFunction a consumer function that takes the state of the benchmark and the iteration as an
     *                         argument and has some side effects, e.g. logging or updating an UI
     * @return a BenchmarkResult object that contains all data collected through the benchmark
     */
    public static BenchmarkResult runBenchmark(
            BenchmarkConfig benchmarkConfig,
            BiConsumer<BenchmarkState, Integer> feedbackFunction
    ) {
        long[] tJoinRequest = new long[benchmarkConfig.iterations];
        long[] tJoinResponse = new long[benchmarkConfig.iterations];
        long[] tJoinHandleResponse = new long[benchmarkConfig.iterations];
        long[] tEarnStoreRequest = new long[benchmarkConfig.iterations];
        long[] tEarnStoreResponse = new long[benchmarkConfig.iterations];
        long[] tEarnProviderRequest = new long[benchmarkConfig.iterations];
        long[] tEarnProviderResponse = new long[benchmarkConfig.iterations];
        long[] tEarnHandleResponse = new long[benchmarkConfig.iterations];
        long[] tSpendStoreRequest = new long[benchmarkConfig.iterations];
        long[] tSpendStoreResponse = new long[benchmarkConfig.iterations];
        long[] tSpendProviderRequest = new long[benchmarkConfig.iterations];
        long[] tSpendProviderResponse = new long[benchmarkConfig.iterations];
        long[] tSpendHandleResponse = new long[benchmarkConfig.iterations];

        JoinFirstStepOutput joinFirstStepOutput;
        JoinResponse joinResponse;
        EarnStoreRequest earnStoreRequest;
        EarnRequestECDSA earnRequestECDSA;
        EarnStoreCouponSignature earnStoreCouponSignature;
        SPSEQSignature earnResponse;
        SpendCouponRequest spendStoreRequest;
        SpendCouponSignature spendCouponSignature;
        SpendRequestECDSA spendProviderRequest;
        SpendResponseECDSA spendProviderResponse;
        Token token = null;
        UUID basketId = UUID.randomUUID();

        IncentiveSystem incentiveSystem = benchmarkConfig.incentiveSystem;
        ProviderPublicKey ppk = benchmarkConfig.ppk;
        ProviderSecretKey psk = benchmarkConfig.psk;
        UserPublicKey upk = benchmarkConfig.upk;
        UserSecretKey usk = benchmarkConfig.usk;
        StorePublicKey spk = benchmarkConfig.spk;
        StoreSecretKey ssk = benchmarkConfig.ssk;
        PromotionParameters promotionParameters = IncentiveSystem.generatePromotionParameters(EARN_SPEND_AMOUNT.length());
        SpendDeductTree spendDeductTree = BenchmarkSpendDeductZkp.getBenchmarkSpendDeductTree(
                promotionParameters,
                EARN_SPEND_AMOUNT);

        var userKeyPair = new UserKeyPair(upk, usk);
        var providerKeyPair = new ProviderKeyPair(psk, ppk);
        var storeKeyPair = new StoreKeyPair(ssk, spk);
        var context = new ByteArrayImplementation("Context".getBytes());
        Instant start, finish;

        for (int i = 0; i < benchmarkConfig.iterations; i++) {
            feedbackFunction.accept(BenchmarkState.ISSUE_JOIN, i);
            start = Instant.now();
            joinFirstStepOutput = incentiveSystem.generateJoinRequest(
                    ppk,
                    userKeyPair
            );
            finish = Instant.now();
            tJoinRequest[i] = Duration.between(start, finish).toNanos();
            start = Instant.now();
            joinResponse =
                    incentiveSystem.generateJoinRequestResponse(
                            promotionParameters,
                            providerKeyPair,
                            joinFirstStepOutput.getJoinRequest()
                    );
            finish = Instant.now();
            tJoinResponse[i] = Duration.between(start, finish).toNanos();
            start = Instant.now();
            token = incentiveSystem.handleJoinRequestResponse(
                    promotionParameters,
                    ppk,
                    joinFirstStepOutput,
                    joinResponse
            );
            finish = Instant.now();
            tJoinHandleResponse[i] = Duration.between(start, finish).toNanos();
        }

        for (int i = 0; i < benchmarkConfig.iterations; i++) {
            feedbackFunction.accept(BenchmarkState.CREDIT_EARN, i);
            start = Instant.now();
            earnStoreRequest = incentiveSystem.generateEarnCouponRequest(
                    token,
                    userKeyPair
            );
            finish = Instant.now();
            tEarnStoreRequest[i] = Duration.between(start, finish).toNanos();

            start = Instant.now();
            earnStoreCouponSignature = incentiveSystem.signEarnCoupon(
                    storeKeyPair,
                    EARN_SPEND_AMOUNT,
                    earnStoreRequest,
                    basketId,
                    promotionParameters.getPromotionId(),
                    (basketId1, promotionId, hash) -> IStoreBasketRedeemedHandler.BasketRedeemState.BASKET_NOT_REDEEMED
            );
            finish = Instant.now();
            tEarnStoreResponse[i] = Duration.between(start, finish).toNanos();

            start = Instant.now();
            earnRequestECDSA = incentiveSystem.generateEarnRequest(
                    token,
                    ppk,
                    userKeyPair,
                    EARN_SPEND_AMOUNT,
                    earnStoreCouponSignature
            );
            finish = Instant.now();
            tEarnProviderRequest[i] = Duration.between(start, finish).toNanos();

            start = Instant.now();
            earnResponse = incentiveSystem.generateEarnResponse(
                    earnRequestECDSA,
                    promotionParameters,
                    providerKeyPair,
                    new BenchmarkTransactionDBHandler(),
                    storePublicKey -> true
            );
            finish = Instant.now();
            tEarnProviderResponse[i] = Duration.between(start, finish).toNanos();

            start = Instant.now();
            token = incentiveSystem.handleEarnResponse(
                    earnRequestECDSA,
                    earnResponse,
                    promotionParameters,
                    token,
                    userKeyPair,
                    ppk
            );
            finish = Instant.now();
            tEarnHandleResponse[i] = Duration.between(start, finish).toNanos();
        }

        for (int i = 0; i < benchmarkConfig.iterations; i++) {
            feedbackFunction.accept(BenchmarkState.SPEND_DEDUCT, i);
            start = Instant.now();
            assert token != null;
            var newPoints = Vector.fromStreamPlain(token.getPoints().stream().map(p -> p.asInteger().subtract(EARN_SPEND_AMOUNT.get(0))));
            spendStoreRequest = incentiveSystem.generateStoreSpendRequest(
                    userKeyPair,
                    ppk,
                    token,
                    promotionParameters,
                    basketId,
                    newPoints,
                    spendDeductTree,
                    context
            );

            finish = Instant.now();
            tSpendStoreRequest[i] = Duration.between(start, finish).toNanos();

            start = Instant.now();
            spendCouponSignature = incentiveSystem.signSpendCoupon(
                    storeKeyPair,
                    ppk,
                    basketId,
                    promotionParameters,
                    spendStoreRequest,
                    spendDeductTree,
                    context,
                    (basketId1, promotionId, hash) -> IStoreBasketRedeemedHandler.BasketRedeemState.BASKET_NOT_REDEEMED,
                    new BenchmarkBlacklist(),
                    new BenchmarkTransactionDBHandler()
            );
            finish = Instant.now();
            tSpendStoreResponse[i] = Duration.between(start, finish).toNanos();

            start = Instant.now();
            incentiveSystem.verifySpendCouponSignature(spendStoreRequest, spendCouponSignature, promotionParameters, basketId);
            spendProviderRequest = new SpendRequestECDSA(spendStoreRequest, spendCouponSignature);
            finish = Instant.now();
            tSpendProviderRequest[i] = Duration.between(start, finish).toNanos();

            start = Instant.now();
            spendProviderResponse = incentiveSystem.verifySpendRequestAndIssueNewToken(
                    providerKeyPair,
                    promotionParameters,
                    spendProviderRequest,
                    basketId,
                    spendDeductTree,
                    context,
                    storePublicKey -> true,
                    new BenchmarkBlacklist()
            );
            finish = Instant.now();
            tSpendProviderResponse[i] = Duration.between(start, finish).toNanos();

            start = Instant.now();
            token = incentiveSystem.retrieveUpdatedTokenFromSpendResponse(userKeyPair,
                    ppk,
                    token,
                    promotionParameters,
                    newPoints,
                    spendProviderRequest,
                    spendProviderResponse
                    );
            finish = Instant.now();
            tSpendHandleResponse[i] = Duration.between(start, finish).toNanos();
        }
        return new BenchmarkResult(
                tJoinRequest,
                tJoinResponse,
                tJoinHandleResponse,
                tEarnStoreRequest,
                tEarnStoreResponse,
                tEarnProviderRequest,
                tEarnProviderResponse,
                tEarnHandleResponse,
                tSpendStoreRequest,
                tSpendStoreResponse,
                tSpendProviderRequest,
                tSpendProviderResponse,
                tSpendHandleResponse
        );
    }
}
