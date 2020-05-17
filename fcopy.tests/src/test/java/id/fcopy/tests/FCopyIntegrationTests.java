/**
 * Copyright 2020 lambdaprime
 * 
 * Email: id.blackmesa@gmail.com 
 * Website: https://github.com/lambdaprime
 * 
 */
package id.fcopy.tests;

import static id.xfunction.XUtils.readResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import id.xfunction.XExec;
import id.xfunction.XUtils;

public class FCopyIntegrationTests {

    private static final String FCOPY_PATH = Paths.get("")
            .toAbsolutePath()
            .resolve("build/fcopy/fcopy")
            .toString();
    private Path tmpDir;

    @BeforeEach
    void setup() throws IOException {
        tmpDir = Files.createTempDirectory("gggg");
    }
    
    @AfterEach
    void cleanup() throws IOException {
        XUtils.deleteDir(tmpDir);
    }
    
    @Test
    public void test() throws Exception {
        test_no_args();
    }

    private void test_no_args() throws Exception {
        Assertions.assertEquals(readResource("README.md"),
                new XExec(FCOPY_PATH).run().stdoutAsString());
    }
    
}
