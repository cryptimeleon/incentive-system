package org.cryptimeleon.incentivesystem.cryptoprotocol.benchmark;

import java.io.Serializable;
import java.util.stream.LongStream;

/**
 * Class holding results of a benchmark and precomputed analyzed results.
 */
public class BenchmarkResult implements Serializable {
    public long[] joinRequestTime;
    public long[] joinResponseTime;
    public long[] joinHandleResponseTime;
    public long[] earnRequestTime;
    public long[] earnResponseTime;
    public long[] earnHandleResponseTime;
    public long[] spendRequestTime;
    public long[] spendResponseTime;
    public long[] spendHandleResponseTime;

    public double joinRequestAvg;
    public double joinResponseAvg;
    public double joinHandleResponseAvg;

    public double earnRequestAvg;
    public double earnResponseAvg;
    public double earnHandleResponseAvg;

    public double spendRequestAvg;
    public double spendResponseAvg;
    public double spendHandleResponseAvg;

    public double joinTotalAvg;
    public double earnTotalAvg;
    public double spendTotalAvg;
    public double totalAvg;

    /**
     * Constructor that takes benchmark timing data as arrays containing the time for a step in nanoseconds.
     * Analyzes the data to offer results.
     */
    public BenchmarkResult(long[] joinRequestTime, long[] joinResponseTime, long[] joinHandleResponseTime, long[] earnRequestTime, long[] earnResponseTime, long[] earnHandleResponseTime, long[] spendRequestTime, long[] spendResponseTime, long[] spendHandleResponseTime) {
        this.joinRequestTime = joinRequestTime;
        this.joinResponseTime = joinResponseTime;
        this.joinHandleResponseTime = joinHandleResponseTime;
        this.earnRequestTime = earnRequestTime;
        this.earnResponseTime = earnResponseTime;
        this.earnHandleResponseTime = earnHandleResponseTime;
        this.spendRequestTime = spendRequestTime;
        this.spendResponseTime = spendResponseTime;
        this.spendHandleResponseTime = spendHandleResponseTime;

        analyzeData();
    }


    /**
     * Precomputed average times in ms and aggregate results.
     * Should be called from the constructor to ensure the results are present.
     */
    private void analyzeData() {
        joinRequestAvg = computeAverage(joinRequestTime);
        joinResponseAvg = computeAverage(joinResponseTime);
        joinHandleResponseAvg = computeAverage(joinHandleResponseTime);

        earnRequestAvg = computeAverage(earnRequestTime);
        earnResponseAvg = computeAverage(earnResponseTime);
        earnHandleResponseAvg = computeAverage(earnHandleResponseTime);

        spendRequestAvg = computeAverage(spendRequestTime);
        spendResponseAvg = computeAverage(spendResponseTime);
        spendHandleResponseAvg = computeAverage(spendHandleResponseTime);

        joinTotalAvg = joinRequestAvg + joinResponseAvg + joinHandleResponseAvg;
        earnTotalAvg = earnRequestAvg + earnResponseAvg + earnHandleResponseAvg;
        spendTotalAvg = spendRequestAvg + spendResponseAvg + spendHandleResponseAvg;
        totalAvg = joinTotalAvg + earnTotalAvg + spendTotalAvg;
    }

    /**
     * Print the aggregated results to stdout.
     */
    public void printReport() {
        System.out.println("****************************************************************************************************");
        System.out.printf("** Total ** %fms%n", totalAvg);
        System.out.printf("** Join  ** Total: %fms, Req: %fms, Res: %fms, Han: %fms%n", joinTotalAvg, joinRequestAvg, joinResponseAvg, joinHandleResponseAvg);
        System.out.printf("** Earn  ** Total: %fms, Req: %fms, Res: %fms, Han: %fms%n", earnTotalAvg, earnRequestAvg, earnResponseAvg, earnHandleResponseAvg);
        System.out.printf("** Spend ** Total: %fms, Req: %fms, Res: %fms, Han: %fms%n", spendTotalAvg, spendRequestAvg, spendResponseAvg, spendHandleResponseAvg);
        System.out.println("****************************************************************************************************");
    }

    /**
     * Utility function for computing the average time of an array of times and converts it from nanoseconds to milliseconds.
     *
     * @param times array containing the times to compute the avg of
     * @return average time in ms
     */
    private double computeAverage(long[] times) {
        return LongStream.of(times).average().getAsDouble() / 1000000;
    }
}
