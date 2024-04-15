package org.codequistify.master.global.lock;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class LockManager {
    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    public ReentrantLock getLock(Object... keys) {
        String key = generateKey(keys);
        return locks.computeIfAbsent(key, k -> new ReentrantLock());
    }

    public void unlock(Object... keys) {
        String key = generateKey(keys);
        ReentrantLock lock = locks.get(key);
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
            if (!lock.hasQueuedThreads()) {
                locks.remove(key, lock);
            }
        }
    }

    private String generateKey(Object... keys) {
        StringBuilder sb = new StringBuilder();
        for (Object key : keys) {
            sb.append(key.toString());
            sb.append("-");
        }
        return sb.toString();
    }
}
