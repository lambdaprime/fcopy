/**
 * Copyright 2020 lambdaprime
 * 
 * Email: id.blackmesa@gmail.com 
 * Website: https://github.com/lambdaprime
 * 
 */
package id.fcopy;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class FCopyApp {

    private static final int BLOCK_SIZE = 256_000;
    private static final int THREADS = 8;
    private static final int TOTAL_SIZE = BLOCK_SIZE * THREADS;
    
    private static ExecutorService executor;
    private static MemoryBlockAllocator allocator;
    
    static void copy(File src, File dst) throws Exception {
        try (var raf = new RandomAccessFile(dst, "rwd")) {
            raf.setLength(src.length());
        }
        for (int i = 0; i < src.length(); i += BLOCK_SIZE) {
            int n = i;
            executor.submit(() -> {
                copy(src, dst, n);
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
        executor = new BlockingExecutorService(THREADS, 2);
        allocator = new MemoryBlockAllocator(TOTAL_SIZE, BLOCK_SIZE);
        copy(new File("/media/d/Downloads/ubuntu/ubuntu-18.04.1-desktop-amd64.iso"),
            new File("/tmp/l"));
//        copy(new File("/home/lynx/tmp/heels.org"),
//            new File("/tmp/l"));
        executor.shutdown();
        
        executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
    }

}
