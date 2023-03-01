package org.cryptimeleon.incentive.crypto.benchmark;

import org.cryptimeleon.incentive.crypto.BilinearGroupChoice;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

/**
 * Test the benchmark with a simple debug group setup.
 * Don't execute an expensive benchmark here, use the benchmark package for that.
 */
public class BenchmarkTest {
    final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    @Test
    void runBenchmark() {
        BenchmarkConfig benchmarkConfig = new BenchmarkConfig(10, 128, BilinearGroupChoice.Debug);
        BenchmarkResult benchmarkResult = Benchmark.runBenchmark(benchmarkConfig, (benchmarkState, integer) -> {
            if (integer % 10 == 0) {
                logger.info(String.format("%s, round %d", benchmarkState.name(), integer));
            }
        });
        benchmarkResult.printReport();
    }
}
