package org.cryptimeleon.incentivesystem.cryptoprotocol;

import org.cryptimeleon.incentivesystem.cryptoprotocol.benchmark.Benchmark;
import org.cryptimeleon.incentivesystem.cryptoprotocol.benchmark.BenchmarkConfig;
import org.cryptimeleon.incentivesystem.cryptoprotocol.benchmark.BenchmarkResult;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

public class BenchmarkTest {
    Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    @Test
    void runBenchmark() {
        BenchmarkConfig benchmarkConfig = new BenchmarkConfig(10, 128, Setup.BilinearGroupChoice.Herumi_MCL);
        BenchmarkResult benchmarkResult = Benchmark.runBenchmark(benchmarkConfig, (benchmarkState, integer) -> {
            if (integer % 100 == 0) {
                logger.info(String.format("%s, round %d", benchmarkState.name(), integer));
            }
        });
        benchmarkResult.printReport(logger);
    }
}
