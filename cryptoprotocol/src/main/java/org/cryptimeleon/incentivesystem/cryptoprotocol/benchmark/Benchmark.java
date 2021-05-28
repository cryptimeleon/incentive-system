package org.cryptimeleon.incentivesystem.cryptoprotocol.benchmark;


import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.incentivesystem.cryptoprotocol.IncentiveSystem;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.*;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.provider.ProviderSecretKey;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.user.UserKeyPair;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.user.UserPublicKey;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.user.UserSecretKey;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.messages.JoinRequest;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.messages.JoinResponse;

import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.function.BiConsumer;

public class Benchmark {

    private static final BigInteger EARN_SPEND_AMOUNT = BigInteger.TEN;

    public static BenchmarkResult runBenchmark(
            BenchmarkConfig benchmarkConfig
    ) {
        return runBenchmark(
                benchmarkConfig,
                (benchmarkState, integer) -> {
                }
        );
    }

    public static BenchmarkResult runBenchmark(
            BenchmarkConfig benchmarkConfig,
            BiConsumer<BenchmarkState, Integer> feedbackFunction
    ) {
        long[] tJoinRequest = new long[benchmarkConfig.getIterations()];
        long[] tJoinResponse = new long[benchmarkConfig.getIterations()];
        long[] tJoinHandleResponse = new long[benchmarkConfig.getIterations()];
        long[] tEarnRequest = new long[benchmarkConfig.getIterations()];
        long[] tEarnResponse = new long[benchmarkConfig.getIterations()];
        long[] tEarnHandleResponse = new long[benchmarkConfig.getIterations()];
        long[] tSpendRequest = new long[benchmarkConfig.getIterations()];
        long[] tSpendResponse = new long[benchmarkConfig.getIterations()];
        long[] tSpendHandleResponse = new long[benchmarkConfig.getIterations()];

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

        for (int i = 0; i < benchmarkConfig.getIterations(); i++) {
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

        for (int i = 0; i < benchmarkConfig.getIterations(); i++) {
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

        for (int i = 0; i < benchmarkConfig.getIterations(); i++) {
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
