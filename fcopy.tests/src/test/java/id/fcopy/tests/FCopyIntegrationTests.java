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
import java.util.Map;

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
    private static final Path TESTFILES_PATH = Paths.get("")
            .toAbsolutePath()
            .resolve("testfiles");
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
        test_copy_file();
        test_copy_dir();
        test_copy_file_single_thread();
        test_copy_file_relative();
    }

    private void verifyMd5(String...files) throws Exception {
        Map<String, String> md5sums = Map.of(
            "1.bmp", "b4ac928984ac0435c8b3b3661d979b88",
            "2.bmp", "0f8d3023f9aa5beb20738c2b0749eaa6",
            "3.bmp", "23bd54947300af686c8d5326120156af",
            "4.bmp", "9f5d44346f945c3c2b73aec6b5893651");
        for (var file: files) {
            Assertions.assertEquals(md5sums.get(file),
                XUtils.md5Sum(tmpDir.resolve(file).toFile()));
        }
    }
    
    private void test_copy_file() throws Exception {
        var out = runOk("%s %s", TESTFILES_PATH.resolve("1.bmp"), tmpDir);
        Assertions.assertTrue(new TemplateMatcher(readResource(
                getClass(), "test_copy_file")).matches(out));
        verifyMd5("1.bmp");
    }

    private void test_copy_file_single_thread() throws Exception {
        var out = runOk("-t 1 %s %s", TESTFILES_PATH.resolve("1.bmp"), tmpDir);
        Assertions.assertTrue(new TemplateMatcher(readResource(
                getClass(), "test_copy_file_single_thread")).matches(out));
        Assertions.assertEquals("b4ac928984ac0435c8b3b3661d979b88",
            XUtils.md5Sum(tmpDir.resolve("1.bmp").toFile()));
    }

    private void test_copy_file_relative() throws Exception {
        var out = runOk("testfiles/1.bmp %s", tmpDir);
        Assertions.assertTrue(new TemplateMatcher(readResource(
                getClass(), "test_copy_file")).matches(out));
        Assertions.assertEquals("b4ac928984ac0435c8b3b3661d979b88",
            XUtils.md5Sum(tmpDir.resolve("1.bmp").toFile()));
    }

    private void test_copy_dir() throws Exception {
        var out = runOk("%s %s", TESTFILES_PATH, tmpDir);
        Assertions.assertTrue(new TemplateMatcher(readResource(
                getClass(), "test_copy_dir")).matches(out));
        verifyMd5("1.bmp", "2.bmp", "3.bmp", "4.bmp");
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
        var out = proc.stdoutAsString() + "\n" + proc.stderrAsString() + "\n";
        System.out.print(out);
        Assertions.assertEquals(expectedCode, code);
        return out;
    }
    
}
