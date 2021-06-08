package org.cryptimeleon.incentivesystem.cryptoprotocol;

import org.cryptimeleon.incentivesystem.cryptoprotocol.benchmark.Benchmark;
import org.cryptimeleon.incentivesystem.cryptoprotocol.benchmark.BenchmarkConfig;
import org.cryptimeleon.incentivesystem.cryptoprotocol.benchmark.BenchmarkResult;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

/**
 * Test the benchmark with a simple debug group setup.
 * Don't execute an expensive benchmark here, use the benchmark package for that.
 */
public class BenchmarkTest {
    Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    @Test
    void runBenchmark() {
        BenchmarkConfig benchmarkConfig = new BenchmarkConfig(10, 128, Setup.BilinearGroupChoice.Debug);
        BenchmarkResult benchmarkResult = Benchmark.runBenchmark(benchmarkConfig, (benchmarkState, integer) -> {
            if (integer % 10 == 0) {
                logger.info(String.format("%s, round %d", benchmarkState.name(), integer));
            }
        });
        benchmarkResult.printReport();
    }
}
