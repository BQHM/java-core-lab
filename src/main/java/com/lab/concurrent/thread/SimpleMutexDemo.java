package com.lab.concurrent.thread;

import com.lab.concurrent.thread.SimpleMutex;

/**
 * D8 验证实录：亲眼见证 AQS 如何管理线程排队
 * 🎯 预期现象：
 *   [T1] 拿到锁 → [T2] 尝试抢锁（阻塞）→ [T1] 释放 → [T2] 自动唤醒拿到锁
 * 💡 面试铁证：控制台日志即“CLH 队列阻塞/唤醒”实证
 */
public class SimpleMutexDemo {
    public static void main(String[] args) throws InterruptedException {
        SimpleMutex lock = new SimpleMutex();

        // 线程 T1：持锁 3 秒（模拟业务处理）
        Thread t1 = new Thread(() -> {
            System.out.println("[T1] 🔑 尝试获取锁...");
            lock.lock();
            System.out.println("[T1] ✅ 拿到锁！休眠 3 秒（模拟业务）");
            try { Thread.sleep(3000); } catch (InterruptedException e) {}
            lock.unlock();
            System.out.println("[T1] 🔓 释放锁");
        }, "T1");

        // 线程 T2：延迟 100ms 启动（确保 T1 先抢到锁）
        Thread t2 = new Thread(() -> {
            System.out.println("[T2] 🔑 尝试获取锁...（此时 T1 持有，应阻塞）");
            lock.lock(); // ⚠️ 此处会阻塞！直到 T1 释放
            System.out.println("[T2] ✅ 拿到锁！（被 AQS 自动唤醒）");
            lock.unlock();
            System.out.println("[T2] 🔓 释放锁");
        }, "T2");

        // 启动验证（关键：T2 延迟启动确保竞争条件）
        t1.start();
        Thread.sleep(100); // 确保 T1 先执行到 lock()
        t2.start();

        t1.join(); t2.join();
        System.out.println("\n🎯 D8 验证成功！AQS 自动完成：阻塞 → 唤醒 → 获取");
        System.out.println("💡 核心结论：你只需定义'抢锁规则'(tryAcquire)，排队逻辑 AQS 全包！");
    }
}