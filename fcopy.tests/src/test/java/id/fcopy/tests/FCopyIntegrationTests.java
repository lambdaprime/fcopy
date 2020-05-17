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

import id.xfunction.TemplateMatcher;
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
        test_copy_non_existing_file();
    }

    private void test_no_args() throws Exception {
        Assertions.assertEquals(readResource("README.md"),
                new XExec(FCOPY_PATH).run().stdoutAsString());
    }
    
    private void test_copy_non_existing_file() throws Exception {
        var out = runFail("fffff %s", tmpDir);
        Assertions.assertTrue(new TemplateMatcher(readResource(
                getClass(), "test_copy_non_existing_file")).matches(out));
    }

    private String runFail(String fmt, Object...args) {
        return run(1, fmt, args);
    }

    private String runOk(String fmt, Object...args) {
        return run(0, fmt, args);
    }
    
    private String run(int expectedCode, String fmt, Object...args) {
        var proc = new XExec(FCOPY_PATH + " " + String.format(fmt, args))
                .run();
        var code = proc.getCode();
        proc.flushStdout();
        proc.flushStderr();
        var out = proc.stdoutAsString() + "\n" + proc.stderrAsString();
        System.out.print(out);
        Assertions.assertEquals(expectedCode, code);
        return out;
    }
    
}
