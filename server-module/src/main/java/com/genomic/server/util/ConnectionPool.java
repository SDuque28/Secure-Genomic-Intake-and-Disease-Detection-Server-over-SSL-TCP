package com.genomic.server.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ConnectionPool<T> {
    private final BlockingQueue<T> pool;
    private final int maxSize;

    public ConnectionPool(int maxSize) {
        this.maxSize = maxSize;
        this.pool = new LinkedBlockingQueue<>(maxSize);
    }

    public T borrowObject() throws InterruptedException {
        return pool.poll(5, TimeUnit.SECONDS); // Wait up to 5 seconds
    }

    public void returnObject(T object) {
        if (object != null) {
            pool.offer(object);
        }
    }

    public int getSize() {
        return pool.size();
    }
}