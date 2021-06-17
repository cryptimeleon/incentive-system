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
import org.cryptimeleon.incentive.crypto.model.messages.JoinRequest;
import org.cryptimeleon.incentive.crypto.model.messages.JoinResponse;

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
    private static final BigInteger EARN_SPEND_AMOUNT = BigInteger.TEN;

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

        JoinRequest joinRequest;
        JoinResponse joinResponse;
        EarnRequest earnRequest;
        SPSEQSignature earnResponse;
        SpendRequest spendRequest;
        SpendProviderOutput spendResponseTuple;
        Token token = null;

        IncentiveSystem incentiveSystem = benchmarkConfig.incentiveSystem;
        IncentivePublicParameters pp = benchmarkConfig.pp;
        ProviderPublicKey ppk = benchmarkConfig.ppk;
        ProviderSecretKey psk = benchmarkConfig.psk;
        UserPublicKey upk = benchmarkConfig.upk;
        UserSecretKey usk = benchmarkConfig.usk;

        var userKeyPair = new UserKeyPair(upk, usk);
        var providerKeyPair = new ProviderKeyPair(psk, ppk);
        Instant start, finish;

        for (int i = 0; i < benchmarkConfig.iterations; i++) {
            feedbackFunction.accept(BenchmarkState.ISSUE_JOIN, i);
            start = Instant.now();
            joinRequest = incentiveSystem.generateJoinRequest(
                    pp,
                    ppk,
                    userKeyPair
            );
            finish = Instant.now();
            tJoinRequest[i] = Duration.between(start, finish).toNanos();
            start = Instant.now();
            joinResponse =
                    incentiveSystem.generateJoinRequestResponse(
                            pp,
                            providerKeyPair,
                            upk.getUpk(),
                            joinRequest
                    );
            finish = Instant.now();
            tJoinResponse[i] = Duration.between(start, finish).toNanos();
            start = Instant.now();
            token = incentiveSystem.handleJoinRequestResponse(
                    pp,
                    ppk,
                    userKeyPair,
                    joinRequest,
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
                    earnRequest,
                    EARN_SPEND_AMOUNT,
                    providerKeyPair
            );
            finish = Instant.now();
            tEarnResponse[i] = Duration.between(start, finish).toNanos();
            start = Instant.now();
            token = incentiveSystem.handleEarnRequestResponse(
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
            var tid = incentiveSystem.pp.getBg().getZn().getUniformlyRandomElement();
            start = Instant.now();
            assert token != null;
            spendRequest = incentiveSystem.generateSpendRequest(
                    token,
                    ppk,
                    EARN_SPEND_AMOUNT,
                    userKeyPair,
                    tid
            );

            finish = Instant.now();
            tSpendRequest[i] = Duration.between(start, finish).toNanos();
            start = Instant.now();
            spendResponseTuple = incentiveSystem.generateSpendRequestResponse(
                    spendRequest,
                    providerKeyPair,
                    EARN_SPEND_AMOUNT,
                    tid
            );
            finish = Instant.now();
            tSpendResponse[i] = Duration.between(start, finish).toNanos();
            start = Instant.now();
            token = incentiveSystem.handleSpendRequestResponse(
                    spendResponseTuple.getSpendResponse(),
                    spendRequest,
                    token,
                    EARN_SPEND_AMOUNT,
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
