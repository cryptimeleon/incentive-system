package org.cryptimeleon.incentive.crypto.benchmark.run;

import org.cryptimeleon.incentive.crypto.BilinearGroupChoice;
import org.cryptimeleon.incentive.crypto.benchmark.Benchmark;
import org.cryptimeleon.incentive.crypto.benchmark.BenchmarkConfig;
import org.cryptimeleon.incentive.crypto.benchmark.BenchmarkResult;
import org.junit.jupiter.api.Test;

/**
 * Run a full benchmark an print the results to stdout.
 * This test can be executed via the `benchmark` gradle task.
 */
public class BenchmarkTask {
    @Test
    void runBenchmark() {
        BenchmarkConfig benchmarkConfig = new BenchmarkConfig(50, 128, BilinearGroupChoice.Herumi_MCL);
        System.out.println("****************************************************************************************************");
        System.out.println("Running Benchmark with MCL");
        BenchmarkResult benchmarkResult = Benchmark.runBenchmark(benchmarkConfig);
        benchmarkResult.printReport();
    }
}
