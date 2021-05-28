package org.cryptimeleon.incentivesystem.cryptoprotocol.benchmark;

import lombok.Value;

import java.util.logging.Logger;
import java.util.stream.LongStream;

@Value
public class BenchmarkResult {
    long[] joinRequestTime;
    long[] joinResponseTime;
    long[] joinHandleResponseTime;
    long[] earnRequestTime;
    long[] earnResponseTime;
    long[] earnHandleResponseTime;
    long[] spendRequestTime;
    long[] spendResponseTime;
    long[] spendHandleResponseTime;

    public void printReport(Logger logger) {
        var joinRequestAvg = computeAverage(joinRequestTime);
        var joinResponseAvg = computeAverage(joinResponseTime);
        var joinHandleResponseAvg = computeAverage(joinHandleResponseTime);

        var earnRequestAvg = computeAverage(earnRequestTime);
        var earnResponseAvg = computeAverage(earnResponseTime);
        var earnHandleResponseAvg = computeAverage(earnHandleResponseTime);

        var spendRequestAvg = computeAverage(spendRequestTime);
        var spendResponseAvg = computeAverage(spendResponseTime);
        var spendHandleResponseAvg = computeAverage(spendHandleResponseTime);

        var joinTotalAvg = joinRequestAvg + joinResponseAvg + joinHandleResponseAvg;
        var earnTotalAvg = earnRequestAvg + earnResponseAvg + earnHandleResponseAvg;
        var spendTotalAvg = spendRequestAvg + spendResponseAvg + spendHandleResponseAvg;
        var totalAvg = joinTotalAvg + earnTotalAvg + spendTotalAvg;

        System.out.println("****************************************************************************************************");
        System.out.printf("** Total ** %fms%n", totalAvg);
        System.out.printf("** Join  ** Total: %fms, Req: %fms, Res: %fms, Han: %fms%n", joinTotalAvg, joinRequestAvg, joinResponseAvg, joinHandleResponseAvg);
        System.out.printf("** Earn  ** Total: %fms, Req: %fms, Res: %fms, Han: %fms%n", earnTotalAvg, earnRequestAvg, earnResponseAvg, earnHandleResponseAvg);
        System.out.printf("** Spend ** Total: %fms, Req: %fms, Res: %fms, Han: %fms%n", spendTotalAvg, spendRequestAvg, spendResponseAvg, spendHandleResponseAvg);
        System.out.println("****************************************************************************************************");
    }

    private double computeAverage(long[] joinHandleResponseTime) {
        return LongStream.of(joinHandleResponseTime).average().getAsDouble() / 1000000;
    }
}
