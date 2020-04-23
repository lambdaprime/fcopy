/**
 * Copyright 2020 lambdaprime
 * 
 * Email: id.blackmesa@gmail.com 
 * Website: https://github.com/lambdaprime
 * 
 */
package id.fcopy;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.IntStream;

import id.xfunction.XAsserts;
import id.xfunction.function.Unchecked;

public class MemoryBlockAllocator {

    private ArrayBlockingQueue<byte[]> queue;
    
    public MemoryBlockAllocator(int totalSize, int blockSize) {
        XAsserts.assertTrue(totalSize % blockSize == 0, "totalSize/blockSize");
        var size = totalSize / blockSize;
        queue = new ArrayBlockingQueue<>(size);
        IntStream.range(0, size).forEach(i -> {
            queue.add(new byte[blockSize]);
        });
    }
    
    public byte[] allocate() {
        return Unchecked.get(() -> queue.take());
    }

    public void deallocate(byte[] block) {
        Unchecked.run(() -> queue.put(block));
    }

}
