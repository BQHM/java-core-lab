package com.lab.concurrent.thread;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * D8 核心验证：继承 AQS 实现可重入互斥锁
 * ✅ 最终版：解决 @Override 报红问题
 * 💡 面试话术锚点：
 *    • state=0 空闲 / >0 持有计数（可重入）
 *    • tryAcquire 失败 → AQS 自动入队阻塞
 *    • tryRelease 归零 → AQS 自动唤醒队首
 */
public class SimpleMutex implements Lock {

    // 【核心】AQS 子类：定义“抢锁规则”，排队逻辑 AQS 全包
    private static class Sync extends AbstractQueuedSynchronizer {

        // 🌟 你定义的规则：state=0 时 CAS 抢锁；当前线程重入则 state+1
        @Override
        protected boolean tryAcquire(int acquires) {
            Thread current = Thread.currentThread();
            int state = getState();

            if (state == 0) { // 空闲 → 尝试抢锁
                if (compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current); // 标记持有者
                    return true;
                }
            } else if (current == getExclusiveOwnerThread()) { // 重入！
                setState(state + acquires);
                return true;
            }
            return false; // 失败 → AQS 自动入队阻塞
        }

        // 🌟 你定义的规则：state 减 1，归零时清除持有者
        @Override
        protected boolean tryRelease(int releases) {
            if (Thread.currentThread() != getExclusiveOwnerThread())
                throw new IllegalMonitorStateException();

            int next = getState() - releases;
            boolean free = (next == 0);
            if (free) setExclusiveOwnerThread(null); // 归零才真正释放
            setState(next);
            return free; // 返回 true → AQS 唤醒队首线程
        }

        @Override
        protected boolean isHeldExclusively() {
            return getState() > 0 && getExclusiveOwnerThread() == Thread.currentThread();
        }

        // ✅ 【终极修复】移除 @Override，直接实现 newCondition()
        public Condition newCondition() {
            return new ConditionObject(); // 使用 Sync 类自己的 ConditionObject
        }

        // 【关键】内部类：继承 AQS 的 ConditionObject
        private class ConditionObject extends AbstractQueuedSynchronizer.ConditionObject {
            // 空实现即可，AQS 已提供完整等待/唤醒逻辑
        }
    }

    private final Sync sync = new Sync();

    // 【关键】调用 AQS 模板方法：acquire/release 自动处理排队/唤醒
    @Override public void lock() { sync.acquire(1); }
    @Override public void unlock() { sync.release(1); }

    // Lock 接口必需实现（测试未使用，保持简洁）
    @Override public void lockInterruptibly() throws InterruptedException {
        sync.acquireInterruptibly(1);
    }

    @Override public boolean tryLock() {
        return sync.tryAcquire(1);
    }

    @Override public boolean tryLock(long time, java.util.concurrent.TimeUnit unit) throws InterruptedException {
        return sync.tryAcquireNanos(1, unit.toNanos(time));
    }

    @Override public Condition newCondition() {
        return sync.newCondition(); // ← 现在不再报红！
    }
}