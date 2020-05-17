/**
 * Copyright 2020 lambdaprime
 * 
 * Email: id.blackmesa@gmail.com 
 * Website: https://github.com/lambdaprime
 * 
 */
package id.fcopy.tests;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static java.util.stream.IntStream.range;

import id.fcopy.BlockingExecutorService;

public class BlockingExecutorServiceTest {

    @ParameterizedTest
    @CsvSource({
        "1, 100, 100000",
        "100, 1, 100000",
        "100, 100, 1",
        "100, 10, 100000",
        "1, 1, 100000",
        "11, 1, 100000",
        "11, 7, 100000",
        "200, 7, 100000",
    })
    public void test_different_params(int numOfThreads, int capacity, int total) throws Exception {
        var c = new AtomicInteger();
        var executor = new BlockingExecutorService(numOfThreads, capacity);
        range(0, total).forEach(i ->
            executor.submit(() -> c.incrementAndGet()));
        executor.shutdown();
        executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
        Assertions.assertEquals(total, c.get());
    }

}
