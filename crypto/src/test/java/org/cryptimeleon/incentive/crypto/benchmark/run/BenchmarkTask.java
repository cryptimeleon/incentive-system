package org.cryptimeleon.incentive.crypto.benchmark.run;

import org.cryptimeleon.incentive.crypto.Setup;
import org.cryptimeleon.incentive.crypto.benchmark.Benchmark;
import org.cryptimeleon.incentive.crypto.benchmark.BenchmarkConfig;
import org.cryptimeleon.incentive.crypto.benchmark.BenchmarkResult;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

/**
 * Run a full benchmark an print the results to stdout.
 * This test can be executed via the `benchmark` gradle task.
 */
public class BenchmarkTask {
    Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    @Test
    void runBenchmark() {
        BenchmarkConfig benchmarkConfig = new BenchmarkConfig(50, 128, Setup.BilinearGroupChoice.Herumi_MCL);
        BenchmarkResult benchmarkResult = Benchmark.runBenchmark(benchmarkConfig);
        benchmarkResult.printReport();
    }
}
