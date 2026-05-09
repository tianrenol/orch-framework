package com.tianrenservice.orch_framework.core.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 懒加载 FutureTask - 调用 get() 时才触发 run()
 */
public class LazyFutureTask<V> extends FutureTask<V> {

    private final AtomicBoolean started = new AtomicBoolean(false);

    public LazyFutureTask(Callable<V> callable) {
        super(callable);
    }

    public void start() {
        if (started.compareAndSet(false, true)) {
            super.run();
        }
    }

    @Override
    public V get() throws ExecutionException, InterruptedException {
        start();
        return super.get();
    }
}
