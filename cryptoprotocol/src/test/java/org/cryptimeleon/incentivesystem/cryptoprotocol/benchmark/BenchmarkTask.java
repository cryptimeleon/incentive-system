package org.cryptimeleon.incentivesystem.cryptoprotocol.benchmark;

import org.cryptimeleon.incentivesystem.cryptoprotocol.Setup;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

public class BenchmarkTask {
    Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    @Test
    void runBenchmark() {
        BenchmarkConfig benchmarkConfig = new BenchmarkConfig(100, 128, Setup.BilinearGroupChoice.Herumi_MCL);
        BenchmarkResult benchmarkResult = Benchmark.runBenchmark(benchmarkConfig);
        benchmarkResult.printReport();
    }
}
