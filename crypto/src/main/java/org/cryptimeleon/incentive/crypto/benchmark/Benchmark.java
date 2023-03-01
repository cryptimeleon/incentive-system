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
    private static final String userInfo = "Benchmark User #1";

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
        long[] tRegistrationStoreRequest = new long[benchmarkConfig.iterations];
        long[] tRegistrationStoreResponse = new long[benchmarkConfig.iterations];
        long[] tRegistrationProviderRequest = new long[benchmarkConfig.iterations];
        long[] tRegistrationProviderResponse = new long[benchmarkConfig.iterations];
        long[] tRegistrationHandleResponse = new long[benchmarkConfig.iterations];
        long[] tJoinStoreRequest = new long[benchmarkConfig.iterations];
        long[] tJoinStoreResponse = new long[benchmarkConfig.iterations];
        long[] tJoinProviderRequest = new long[benchmarkConfig.iterations];
        long[] tJoinProviderResponse = new long[benchmarkConfig.iterations];
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
        EarnProviderRequest earnProviderRequest;
        EarnStoreResponse earnStoreResponse;
        SPSEQSignature earnResponse;
        SpendStoreRequest spendStoreRequest;
        SpendStoreResponse spendCouponSignature;
        SpendProviderRequest spendProviderRequest;
        SpendProviderResponse spendProviderResponse;
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
            feedbackFunction.accept(BenchmarkState.REGISTRATION, i);

            // Nothing to do here, just send upb+userInfo
            tRegistrationStoreRequest[i] = 0;

            start = Instant.now();
            var registrationCoupon = incentiveSystem.signRegistrationCoupon(
                    storeKeyPair,
                    upk,
                    userInfo
            );
            finish = Instant.now();
            tRegistrationStoreResponse[i] = Duration.between(start, finish).toNanos();

            start = Instant.now();
            incentiveSystem.verifyRegistrationCoupon(
                    registrationCoupon,
                    (storePublicKey) -> true
            );
            finish = Instant.now();
            tRegistrationProviderRequest[i] = Duration.between(start, finish).toNanos();

            start = Instant.now();
            var registrationToken = incentiveSystem.verifyRegistrationCouponAndIssueRegistrationToken(
                    providerKeyPair,
                    registrationCoupon,
                    storePublicKey -> true,
                    registrationCoupon1 -> {
                    }
            );
            finish = Instant.now();
            tRegistrationProviderResponse[i] = Duration.between(start, finish).toNanos();

            start = Instant.now();
            incentiveSystem.verifyRegistrationToken(
                    ppk,
                    registrationToken,
                    upk
            );
            finish = Instant.now();
            tRegistrationHandleResponse[i] = Duration.between(start, finish).toNanos();
        }

        for (int i = 0; i < benchmarkConfig.iterations; i++) {
            feedbackFunction.accept(BenchmarkState.ISSUE_JOIN, i);

            tJoinStoreRequest[i] = 0;
            tJoinStoreResponse[i] = 0;

            start = Instant.now();
            joinFirstStepOutput = incentiveSystem.generateJoinRequest(
                    ppk,
                    userKeyPair
            );
            finish = Instant.now();
            tJoinProviderRequest[i] = Duration.between(start, finish).toNanos();

            start = Instant.now();
            joinResponse =
                    incentiveSystem.generateJoinRequestResponse(
                            promotionParameters,
                            providerKeyPair,
                            joinFirstStepOutput.getJoinRequest()
                    );
            finish = Instant.now();
            tJoinProviderResponse[i] = Duration.between(start, finish).toNanos();

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
            earnStoreResponse = incentiveSystem.signEarnCoupon(
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
            earnProviderRequest = incentiveSystem.generateEarnRequest(
                    token,
                    ppk,
                    userKeyPair,
                    EARN_SPEND_AMOUNT,
                    earnStoreResponse
            );
            finish = Instant.now();
            tEarnProviderRequest[i] = Duration.between(start, finish).toNanos();

            start = Instant.now();
            earnResponse = incentiveSystem.generateEarnResponse(
                    earnProviderRequest,
                    promotionParameters,
                    providerKeyPair,
                    new BenchmarkTransactionDBHandler(),
                    storePublicKey -> true
            );
            finish = Instant.now();
            tEarnProviderResponse[i] = Duration.between(start, finish).toNanos();

            start = Instant.now();
            token = incentiveSystem.handleEarnResponse(
                    earnProviderRequest,
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
            spendProviderRequest = new SpendProviderRequest(spendStoreRequest, spendCouponSignature);
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
                tRegistrationStoreRequest,
                tRegistrationStoreResponse,
                tRegistrationProviderRequest,
                tRegistrationProviderResponse,
                tRegistrationHandleResponse,
                tJoinStoreRequest,
                tJoinStoreResponse,
                tJoinProviderRequest,
                tJoinProviderResponse,
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
