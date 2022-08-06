package org.cryptimeleon.incentive.crypto.benchmark;


import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.model.*;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderSecretKey;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPublicKey;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserSecretKey;
import org.cryptimeleon.incentive.crypto.model.JoinRequest;
import org.cryptimeleon.incentive.crypto.model.JoinResponse;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.math.structures.cartesian.Vector;

import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
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
        long[] tEarnRequest = new long[benchmarkConfig.iterations];
        long[] tEarnResponse = new long[benchmarkConfig.iterations];
        long[] tEarnHandleResponse = new long[benchmarkConfig.iterations];
        long[] tSpendRequest = new long[benchmarkConfig.iterations];
        long[] tSpendResponse = new long[benchmarkConfig.iterations];
        long[] tSpendHandleResponse = new long[benchmarkConfig.iterations];

        GenerateIssueJoinOutput generateIssueJoinOutput;
        JoinResponse joinResponse;
        EarnRequest earnRequest;
        SPSEQSignature earnResponse;
        SpendRequest spendRequest;
        DeductOutput spendResponseTuple;
        Token token = null;

        IncentiveSystem incentiveSystem = benchmarkConfig.incentiveSystem;
        ProviderPublicKey ppk = benchmarkConfig.ppk;
        ProviderSecretKey psk = benchmarkConfig.psk;
        UserPublicKey upk = benchmarkConfig.upk;
        UserSecretKey usk = benchmarkConfig.usk;
        PromotionParameters promotionParameters = IncentiveSystem.generatePromotionParameters(EARN_SPEND_AMOUNT.length());
        SpendDeductTree spendDeductTree = BenchmarkSpendDeductZkp.getBenchmarkSpendDeductTree(
                promotionParameters,
                EARN_SPEND_AMOUNT);

        var userKeyPair = new UserKeyPair(upk, usk);
        var providerKeyPair = new ProviderKeyPair(psk, ppk);
        Instant start, finish;

        for (int i = 0; i < benchmarkConfig.iterations; i++) {
            feedbackFunction.accept(BenchmarkState.ISSUE_JOIN, i);
            start = Instant.now();
            generateIssueJoinOutput = incentiveSystem.generateJoinRequest(
                    ppk,
                    userKeyPair,
                    promotionParameters
            );
            finish = Instant.now();
            tJoinRequest[i] = Duration.between(start, finish).toNanos();
            start = Instant.now();
            joinResponse =
                    incentiveSystem.generateJoinRequestResponse(
                            promotionParameters,
                            providerKeyPair,
                            generateIssueJoinOutput.getJoinRequest()
                    );
            finish = Instant.now();
            tJoinResponse[i] = Duration.between(start, finish).toNanos();
            start = Instant.now();
            token = incentiveSystem.handleJoinRequestResponse(
                    promotionParameters,
                    ppk,
                    userKeyPair,
                    generateIssueJoinOutput,
                    joinResponse
            );
            finish = Instant.now();
            tJoinHandleResponse[i] = Duration.between(start, finish).toNanos();
        }

        for (int i = 0; i < benchmarkConfig.iterations; i++) {
            feedbackFunction.accept(BenchmarkState.CREDIT_EARN, i);
            start = Instant.now();
            earnRequest = incentiveSystem.generateEarnRequest(
                    token,
                    ppk,
                    userKeyPair
            );
            finish = Instant.now();
            tEarnRequest[i] = Duration.between(start, finish).toNanos();
            start = Instant.now();
            earnResponse = incentiveSystem.generateEarnRequestResponse(
                    promotionParameters,
                    earnRequest,
                    EARN_SPEND_AMOUNT,
                    providerKeyPair
            );
            finish = Instant.now();
            tEarnResponse[i] = Duration.between(start, finish).toNanos();
            start = Instant.now();
            token = incentiveSystem.handleEarnRequestResponse(
                    promotionParameters,
                    earnRequest,
                    earnResponse,
                    EARN_SPEND_AMOUNT,
                    token,
                    ppk,
                    userKeyPair
            );
            finish = Instant.now();
            tEarnHandleResponse[i] = Duration.between(start, finish).toNanos();
        }

        for (int i = 0; i < benchmarkConfig.iterations; i++) {
            feedbackFunction.accept(BenchmarkState.SPEND_DEDUCT, i);
            var tid = incentiveSystem.getPp().getBg().getZn().getUniformlyRandomElement();
            start = Instant.now();
            assert token != null;
            var newPoints = Vector.fromStreamPlain(token.getPoints().stream().map(p -> p.asInteger().subtract(EARN_SPEND_AMOUNT.get(0))));
            spendRequest = incentiveSystem.generateSpendRequest(
                    promotionParameters,
                    token,
                    ppk,
                    newPoints,
                    userKeyPair,
                    tid,
                    spendDeductTree
            );

            finish = Instant.now();
            tSpendRequest[i] = Duration.between(start, finish).toNanos();
            start = Instant.now();
            spendResponseTuple = incentiveSystem.generateSpendRequestResponse(
                    promotionParameters,
                    spendRequest,
                    providerKeyPair,
                    tid,
                    spendDeductTree,
                    tid // using tid as user choice TODO change this once user choice generation is properly implemented, see issue 75
            );
            finish = Instant.now();
            tSpendResponse[i] = Duration.between(start, finish).toNanos();
            start = Instant.now();
            token = incentiveSystem.handleSpendRequestResponse(
                    promotionParameters,
                    spendResponseTuple.getSpendResponse(),
                    spendRequest,
                    token,
                    newPoints,
                    ppk,
                    userKeyPair
            );
            finish = Instant.now();
            tSpendHandleResponse[i] = Duration.between(start, finish).toNanos();
        }
        return new BenchmarkResult(
                tJoinRequest,
                tJoinResponse,
                tJoinHandleResponse,
                tEarnRequest,
                tEarnResponse,
                tEarnHandleResponse,
                tSpendRequest,
                tSpendResponse,
                tSpendHandleResponse
        );
    }
}
