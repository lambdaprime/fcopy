/**
 * Copyright 2020 lambdaprime
 * 
 * Email: id.blackmesa@gmail.com 
 * Website: https://github.com/lambdaprime
 * 
 */
package id.fcopy;

import static id.xfunction.function.Unchecked.wrapAccept;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import id.xfunction.SmartArgs;

public class FCopyApp {

    private static int BLOCK_SIZE = 256_000;
    private static int THREADS = ForkJoinPool.getCommonPoolParallelism();
    private static int QUEUE_SIZE = THREADS * THREADS;
    private static boolean SILENT_MODE;
    
    private static ExecutorService executor;
    private static MemoryBlockAllocator allocator;
    
    static void copy(Path src, Path dst) throws Exception {
        if (!SILENT_MODE)
            System.out.format("Copying %s to %s\n", src, dst);
        File srcFile = src.toFile();
        File dstFile = dst.toFile();
        try (var raf = new RandomAccessFile(dstFile, "rwd")) {
            raf.setLength(srcFile.length());
        }
        for (int i = 0; i < srcFile.length(); i += BLOCK_SIZE) {
            int n = i;
            executor.submit(() -> {
                copy(srcFile, dstFile, n);
            });
        }
        
    }

    static void copy(File src, File dst, long offset) {
        byte[] buf = allocator.allocate();
        try (var in = new RandomAccessFile(src, "r");
            var out = new RandomAccessFile(dst, "rwd")) {
            in.seek(offset);
            out.seek(offset);
            int d = (int) (src.length() - offset);
            int s = d < buf.length? d: buf.length;
//            System.out.println(s + " " + offset);
            in.read(buf, 0, s);
            out.write(buf, 0, s);
        } catch (Exception e) {
            e.printStackTrace();
        }
        allocator.deallocate(buf);
    }
    
    public static void main(String[] args) throws Exception {
        Map<String, Consumer<String>> handlers = Map.of(
            "-bs", v -> BLOCK_SIZE = Integer.parseInt(v),
            "-t", v -> THREADS = Integer.parseInt(v),
            "-c", v -> QUEUE_SIZE = Integer.parseInt(v)
        );
        LinkedList<String> positionalArgs = new LinkedList<>();
        Function<String, Boolean> defaultHandler = arg -> {
            switch (arg) {
            case "-s":
                SILENT_MODE = true;
                return true;
            default:
                positionalArgs.add(arg);
                return true;
            }
        };
        new SmartArgs(handlers, defaultHandler)
            .parse(args);
        executor = new BlockingExecutorService(THREADS, QUEUE_SIZE);
        allocator = new MemoryBlockAllocator(BLOCK_SIZE * THREADS, BLOCK_SIZE);
        if (positionalArgs.size() < 2) {
            System.out.println("Usage");
            System.exit(1);
        }
        if (!SILENT_MODE) {
            System.out.println("Configuration:");
            System.out.format("Threads: %d\n", THREADS);
            System.out.format("Queue size: %d\n", QUEUE_SIZE);
            System.out.format("Block size: %d\n", BLOCK_SIZE);
            System.out.println();
        }
        File src = new File(positionalArgs.get(0));
        File dst = new File(positionalArgs.get(1));
        Path srcPath = src.toPath();
        Path dstPath = dst.toPath();
        if (src.isFile() && dst.isDirectory()) {
            dst = new File(dst, src.getName());
        }
        var visitor = new RecursiveCopyVisitor(srcPath, dstPath, wrapAccept(FCopyApp::copy));
        Files.walkFileTree(srcPath, visitor);
        executor.shutdown();
        executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
    }

}
