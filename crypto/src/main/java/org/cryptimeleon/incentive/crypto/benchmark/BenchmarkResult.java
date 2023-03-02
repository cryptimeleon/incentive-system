package org.cryptimeleon.incentive.crypto.benchmark;

import java.io.Serializable;
import java.util.Locale;
import java.util.stream.LongStream;

/**
 * Class holding results of a benchmark and precomputed analyzed results.
 */
public class BenchmarkResult implements Serializable {
    public double registrationStoreRequestAvg;
    public double registrationStoreResponseAvg;
    public double registrationProviderRequestAvg;
    public double registrationProviderResponseAvg;
    public double registrationHandleResponseAvg;
    public double joinStoreRequestAvg;
    public double joinStoreResponseAvg;
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
    public double registrationTotalAvg;
    public double joinTotalAvg;
    public double earnTotalAvg;
    public double spendTotalAvg;
    public double registrationAppAvg;
    public double joinAppAvg;
    public double earnAppAvg;
    public double spendAppAvg;
    final long[] registrationStoreRequestTime;
    final long[] registrationStoreResponseTime;
    final long[] joinHandleResponseTime;
    final long[] earnStoreRequestTime;
    final long[] earnStoreResponseTime;
    final long[] earnProviderRequestTime;
    final long[] earnProviderResponseTime;
    final long[] earnHandleResponseTime;
    final long[] spendStoreRequestTime;
    final long[] spendStoreResponseTime;
    final long[] spendProviderRequestTime;
    final long[] spendProviderResponseTime;
    final long[] spendHandleResponseTime;
    final long[] registrationProviderRequestTime;
    final long[] registrationProviderResponseTime;
    final long[] registrationHandleResponseTime;
    final long[] joinStoreRequestTime;
    final long[] joinStoreResponseTime;
    final long[] joinProviderRequestTime;
    final long[] joinProviderResponseTime;

    /**
     * Constructor that takes benchmark timing data as arrays containing the time for a step in nanoseconds.
     * Analyzes the data to offer results.
     */
    public BenchmarkResult(
            long[] registrationStoreRequest,
            long[] registrationStoreResponse,
            long[] registrationProviderRequest,
            long[] registrationProviderResponse,
            long[] registrationHandleResponse,
            long[] joinStoreRequest,
            long[] joinStoreResponse,
            long[] joinProviderRequest,
            long[] joinProviderResponse,
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
        this.registrationStoreRequestTime = registrationStoreRequest;
        this.registrationStoreResponseTime = registrationStoreResponse;
        this.registrationProviderRequestTime = registrationProviderRequest;
        this.registrationProviderResponseTime = registrationProviderResponse;
        this.registrationHandleResponseTime = registrationHandleResponse;

        this.joinStoreRequestTime = joinStoreRequest;
        this.joinStoreResponseTime = joinStoreResponse;
        this.joinProviderRequestTime = joinProviderRequest;
        this.joinProviderResponseTime = joinProviderResponse;
        this.joinHandleResponseTime = joinHandleResponse;

        this.earnStoreRequestTime = earnStoreRequest;
        this.earnStoreResponseTime = earnStoreResponse;
        this.earnProviderRequestTime = earnProviderRequest;
        this.earnProviderResponseTime = earnProviderResponse;
        this.earnHandleResponseTime = earnHandleResponse;

        this.spendStoreRequestTime = spendStoreRequest;
        this.spendStoreResponseTime = spendStoreResponse;
        this.spendProviderRequestTime = spendProviderRequest;
        this.spendProviderResponseTime = spendProviderResponse;
        this.spendHandleResponseTime = spendHandleResponse;

        analyzeData();
    }


    /**
     * Precomputed average times in ms and aggregate results.
     * Should be called from the constructor to ensure the results are present.
     */
    private void analyzeData() {
        registrationStoreRequestAvg = computeAverage(registrationStoreRequestTime);
        registrationStoreResponseAvg = computeAverage(registrationStoreResponseTime);
        registrationProviderRequestAvg = computeAverage(registrationProviderRequestTime);
        registrationProviderResponseAvg = computeAverage(registrationProviderResponseTime);
        registrationHandleResponseAvg = computeAverage(registrationHandleResponseTime);

        joinStoreRequestAvg = computeAverage(joinStoreRequestTime);
        joinStoreResponseAvg = computeAverage(joinStoreResponseTime);
        joinProviderRequestAvg = computeAverage(joinProviderRequestTime);
        joinProviderResponseAvg = computeAverage(joinProviderResponseTime);
        joinHandleResponseAvg = computeAverage(joinHandleResponseTime);

        earnStoreRequestAvg = computeAverage(earnStoreRequestTime);
        earnStoreResponseAvg = computeAverage(earnStoreResponseTime);
        earnProviderRequestAvg = computeAverage(earnProviderRequestTime);
        earnProviderResponseAvg = computeAverage(earnProviderResponseTime);
        earnHandleResponseAvg = computeAverage(earnHandleResponseTime);

        spendStoreRequestAvg = computeAverage(spendStoreRequestTime);
        spendStoreResponseAvg = computeAverage(spendStoreResponseTime);
        spendProviderRequestAvg = computeAverage(spendProviderRequestTime);
        spendProviderResponseAvg = computeAverage(spendProviderResponseTime);
        spendHandleResponseAvg = computeAverage(spendHandleResponseTime);

        registrationTotalAvg = registrationStoreRequestAvg + registrationStoreResponseAvg + registrationProviderRequestAvg + registrationProviderResponseAvg + registrationHandleResponseAvg;
        joinTotalAvg = joinStoreRequestAvg + joinStoreResponseAvg + joinProviderRequestAvg + joinProviderResponseAvg + joinHandleResponseAvg;
        earnTotalAvg = earnStoreRequestAvg + earnStoreResponseAvg + earnProviderRequestAvg + earnProviderResponseAvg + earnHandleResponseAvg;
        spendTotalAvg = spendStoreRequestAvg + spendStoreResponseAvg + spendProviderRequestAvg + spendProviderResponseAvg + spendHandleResponseAvg;

        registrationAppAvg = registrationStoreRequestAvg + registrationProviderRequestAvg + registrationHandleResponseAvg;
        joinAppAvg = joinStoreRequestAvg + joinProviderRequestAvg + joinHandleResponseAvg;
        earnAppAvg = earnStoreRequestAvg + earnProviderRequestAvg + earnHandleResponseAvg;
        spendAppAvg = spendStoreRequestAvg + spendProviderRequestAvg + spendHandleResponseAvg;
    }

    /**
     * Print the aggregated results to stdout.
     */
    public void printReport() {
        System.out.println("****************************************************************************************************");
        System.out.printf(Locale.ENGLISH, "** Registration ** Total: %.3fms, A1: %.3fms, S: %.3fms, A2: %.3fms, P: %.3fms, A3: %.3fms%n", registrationTotalAvg, registrationStoreRequestAvg, registrationStoreResponseAvg, registrationProviderRequestAvg, registrationProviderResponseAvg, registrationHandleResponseAvg);
        System.out.printf(Locale.ENGLISH, "** Join         ** Total: %.3fms, A1: %.3fms, S: %.3fms, A2: %.3fms, P: %.3fms, A3: %.3fms%n", joinTotalAvg, joinStoreRequestAvg, joinStoreResponseAvg, joinProviderRequestAvg, joinProviderResponseAvg, joinHandleResponseAvg);
        System.out.printf(Locale.ENGLISH, "** Earn         ** Total: %.3fms, A1: %.3fms, S: %.3fms, A2: %.3fms, P: %.3fms, A3: %.3fms%n", earnTotalAvg, earnStoreRequestAvg, earnStoreResponseAvg, earnProviderRequestAvg, earnProviderResponseAvg, earnHandleResponseAvg);
        System.out.printf(Locale.ENGLISH, "** Spend        ** Total: %.3fms, A1: %.3fms, S: %.3fms, A2: %.3fms, P: %.3fms, A3: %.3fms%n", spendTotalAvg, spendStoreRequestAvg, spendStoreResponseAvg, spendProviderRequestAvg, spendProviderResponseAvg, spendHandleResponseAvg);
        System.out.println("****************************************************************************************************");
    }

    public void printCSV() {
        System.out.println("****************************************************************************************************");
        System.out.printf(Locale.ENGLISH, "%.4f, %.4f, %.4f, %.4f, %.4f%n", registrationStoreRequestAvg, registrationStoreResponseAvg, registrationProviderRequestAvg, registrationProviderResponseAvg, registrationHandleResponseAvg);
        System.out.printf(Locale.ENGLISH, "%.4f, %.4f, %.4f, %.4f, %.4f%n", joinStoreRequestAvg, joinStoreResponseAvg, joinProviderRequestAvg, joinProviderResponseAvg, joinHandleResponseAvg);
        System.out.printf(Locale.ENGLISH, "%.4f, %.4f, %.4f, %.4f, %.4f%n", earnStoreRequestAvg, earnStoreResponseAvg, earnProviderRequestAvg, earnProviderResponseAvg, earnHandleResponseAvg);
        System.out.printf(Locale.ENGLISH, "%.4f, %.4f, %.4f, %.4f, %.4f%n", spendStoreRequestAvg, spendStoreResponseAvg, spendProviderRequestAvg, spendProviderResponseAvg, spendHandleResponseAvg);
        System.out.println("****************************************************************************************************");
    }

    /**
     * Utility function for computing the average time of an array of times and converts it from nanoseconds to milliseconds.
     *
     * @param times array containing the times to compute the avg of
     * @return average time in ms
     */
    private double computeAverage(long[] times) {
        return LongStream.of(times).average().getAsDouble() / 1_000_000;
    }
}
