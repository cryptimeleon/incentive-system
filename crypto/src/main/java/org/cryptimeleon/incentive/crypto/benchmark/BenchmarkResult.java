package org.cryptimeleon.incentive.crypto.benchmark;

import java.io.Serializable;
import java.util.stream.LongStream;

/**
 * Class holding results of a benchmark and precomputed analyzed results.
 */
public class BenchmarkResult implements Serializable {
    long[] joinRequestTime;
    long[] joinResponseTime;
    long[] joinHandleResponseTime;
    long[] earnStoreRequestTime;
    long[] earnStoreResponseTime;
    long[] earnProviderRequestTime;
    long[] earnProviderResponseTime;
    long[] earnHandleResponseTime;
    long[] spendStoreRequestTime;
    long[] spendStoreResponseTime;
    long[] spendProviderRequestTime;
    long[] spendProviderResponseTime;
    long[] spendHandleResponseTime;
    public double joinStoreRequestAvg = 0;
    public double joinStoreResponseAvg = 0;
    public double joinProviderRequestAvg;
    public double joinProviderResponseAvg;
    public double joinHandleResponseAvg;

    public double earnStoreRequestAvg;
    public double earnStoreResponseAvg;
    public double earnProviderRequestAvg;
    public double earnProviderResponseAvg;
    public double earnHandleResponseAvg;

    public double spendStoreRequestAvg;
    public double spendStoreResponseAvg;
    public double spendProviderRequestAvg;
    public double spendProviderResponseAvg;
    public double spendHandleResponseAvg;

    public double joinTotalAvg;
    public double earnTotalAvg;
    public double spendTotalAvg;
    public double totalAvg;

    /**
     * Constructor that takes benchmark timing data as arrays containing the time for a step in nanoseconds.
     * Analyzes the data to offer results.
     */
    public BenchmarkResult(long[] joinRequest,
                           long[] joinResponse,
                           long[] joinHandleResponse,
                           long[] earnStoreRequest,
                           long[] earnStoreResponse,
                           long[] earnProviderRequest,
                           long[] earnProviderResponse,
                           long[] earnHandleResponse,
                           long[] spendStoreRequest,
                           long[] spendStoreResponse,
                           long[] spendProviderRequest,
                           long[] spendProviderResponse,
                           long[] spendHandleResponse
    ) {

    this.joinRequestTime=joinRequest;
    this.joinResponseTime=joinResponse;
    this.joinHandleResponseTime=joinHandleResponse;
    this.earnStoreRequestTime=earnStoreRequest;
    this.earnStoreResponseTime=earnStoreResponse;
    this.earnProviderRequestTime=earnProviderRequest;
    this.earnProviderResponseTime=earnProviderResponse;
    this.earnHandleResponseTime=earnHandleResponse;
    this.spendStoreRequestTime=spendStoreRequest;
    this.spendStoreResponseTime=spendStoreResponse;
    this.spendProviderRequestTime=spendProviderRequest;
    this.spendProviderResponseTime=spendProviderResponse;
    this.spendHandleResponseTime=spendHandleResponse;

        analyzeData();
    }


    /**
     * Precomputed average times in ms and aggregate results.
     * Should be called from the constructor to ensure the results are present.
     */
    private void analyzeData() {
        joinProviderRequestAvg = computeAverage(joinRequestTime);
        joinProviderResponseAvg = computeAverage(joinResponseTime);
        joinHandleResponseAvg = computeAverage(joinHandleResponseTime);

        earnStoreRequestAvg= computeAverage(earnStoreRequestTime);
        earnStoreResponseAvg = computeAverage(earnStoreResponseTime);
        earnProviderRequestAvg= computeAverage(earnProviderRequestTime);
        earnProviderResponseAvg = computeAverage(earnProviderResponseTime);
        earnHandleResponseAvg = computeAverage(earnHandleResponseTime);

        spendStoreRequestAvg= computeAverage(spendStoreRequestTime);
        spendStoreResponseAvg = computeAverage(spendStoreResponseTime);
        spendProviderRequestAvg= computeAverage(spendProviderRequestTime);
        spendProviderResponseAvg = computeAverage(spendProviderResponseTime);
        spendHandleResponseAvg = computeAverage(spendHandleResponseTime);

        joinTotalAvg = joinStoreRequestAvg + joinStoreResponseAvg + joinProviderRequestAvg + joinProviderResponseAvg + joinHandleResponseAvg;
        earnTotalAvg = earnStoreRequestAvg + earnStoreResponseAvg + earnProviderRequestAvg + earnProviderResponseAvg + earnHandleResponseAvg;
        spendTotalAvg = spendStoreRequestAvg + spendStoreResponseAvg + spendProviderRequestAvg + spendProviderResponseAvg + spendHandleResponseAvg;
        totalAvg = joinTotalAvg + earnTotalAvg + spendTotalAvg;
    }

    /**
     * Print the aggregated results to stdout.
     */
    public void printReport() {
        System.out.println("****************************************************************************************************");
        System.out.printf("** Total ** %.3fms%n", totalAvg);
        System.out.printf("** Join  ** Total: %.3fms, A1: %.3fms, S: %.3fms, A2: %.3fms, P: %.3fms, A3: %.3fms%n", joinTotalAvg, joinStoreRequestAvg, joinStoreResponseAvg, joinProviderRequestAvg, joinProviderResponseAvg, joinHandleResponseAvg);
        System.out.printf("** Earn  ** Total: %.3fms, A1: %.3fms, S: %.3fms, A2: %.3fms, P: %.3fms, A3: %.3fms%n", earnTotalAvg, earnStoreRequestAvg, earnStoreResponseAvg, earnProviderRequestAvg, earnProviderResponseAvg, earnHandleResponseAvg);
        System.out.printf("** Spend ** Total: %.3fms, A1: %.3fms, S: %.3fms, A2: %.3fms, P: %.3fms, A3: %.3fms%n", spendTotalAvg, spendStoreRequestAvg, spendStoreResponseAvg, spendProviderRequestAvg, spendProviderResponseAvg, spendHandleResponseAvg);
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
