/**
 * Copyright 2020 lambdaprime
 * 
 * Email: id.blackmesa@gmail.com 
 * Website: https://github.com/lambdaprime
 * 
 */
package id.fcopy.tests;

import org.junit.jupiter.api.Test;

import id.fcopy.MemoryBlockAllocator;

public class MemoryBlockAllocatorTest {

    @Test
    public void test() {
        var mba = new MemoryBlockAllocator(32, 8);
        mba.allocate();
        mba.allocate();
        mba.allocate();
        mba.allocate();
    }

}
