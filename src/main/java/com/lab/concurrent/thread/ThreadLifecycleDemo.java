package com.lab.concurrent.thread;

/**
 * Demo: 验证 线程生命周期
 * @author BQHM
 * @date 2026-02-28
 */
public class ThreadLifecycleDemo {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 开始验证线程 6 种状态 ===\n");

        threadNew();        // 改名更准确 (NEW)
        threadRunnable();   // 改名更准确
        threadBlocked();    // 规范命名 (大驼峰)
        threadWaiting();
        threadTimedWaiting(); // 改名更准确 (TIMED_WAITING)

        System.out.println("\n=== 所有验证结束，程序即将退出 ===");
    }

    // 1. NEW 状态
    public static void threadNew() {
        Thread t = new Thread(() -> System.out.println("线程 [NEW] 创建成功"));
        System.out.println("🔍 [NEW]          : " + t.getState());
    }

    // 2. RUNNABLE 状态
    public static void threadRunnable() throws InterruptedException {
        Thread t = new Thread(() -> { while (true); }); // 空转
        t.start();
        Thread.sleep(100); // 等它启动
        System.out.println("🔍 [RUNNABLE]     : " + t.getState());
        t.interrupt(); // 【优化】强行中断，防止死循环卡住程序
    }

    // 3. BLOCKED 状态
    public static void threadBlocked() throws InterruptedException {
        Object lock = new Object();
        Thread t1 = new Thread(() -> { synchronized (lock) { while (true); } });
        Thread t2 = new Thread(() -> { synchronized (lock) { System.out.println("进不来"); } });

        t1.start();
        Thread.sleep(100);
        t2.start();
        Thread.sleep(200); // 等 t2 撞墙
        System.out.println("🔍 [BLOCKED]      : " + t2.getState());

        t1.interrupt(); // 【优化】清理现场
        t2.interrupt();
    }

    // 4. WAITING 状态
    public static void threadWaiting() throws InterruptedException {
        Object lock = new Object();
        Thread t = new Thread(() -> {
            synchronized (lock) {
                try { lock.wait(); } catch (InterruptedException e) {}
            }
        });
        t.start();
        Thread.sleep(200);
        System.out.println("🔍 [WAITING]      : " + t.getState());
        // 可选：t.interrupt() 唤醒它
    }

    // 5. TIMED_WAITING 状态
    public static void threadTimedWaiting() throws InterruptedException {
        Thread t = new Thread(() -> {
            try { Thread.sleep(10000); } catch (InterruptedException e) {}
        });
        t.start();
        Thread.sleep(200);
        System.out.println("🔍 [TIMED_WAITING]: " + t.getState());
        t.interrupt(); // 【优化】提前唤醒，不用等 10 秒
    }
}