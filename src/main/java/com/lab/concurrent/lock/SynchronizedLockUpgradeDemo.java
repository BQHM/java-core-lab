package com.lab.concurrent.lock;

import org.openjdk.jol.info.ClassLayout;

public class SynchronizedLockUpgradeDemo {

    static final Object lockObj = new Object();

    public static void main(String[] args) throws InterruptedException {
        System.out.println("========== 开始验证 synchronized 锁升级 ==========\n");

        System.out.println("【阶段 1】程序刚启动，还没人抢锁 (无锁/偏向锁)");
        printLockStatus("初始状态");

        System.out.println("\n【阶段 1】主线程进入同步块 (应该变为偏向锁)");
        synchronized (lockObj) {
            printLockStatus("主线程持有锁 (偏向锁)");
        }
        System.out.println("\n【阶段 2】启动两个线程交替抢锁 (模拟轻微竞争 -> 轻量级锁)");

        Thread t1 = new Thread(() -> {
            synchronized (lockObj) {
                printLockStatus("线程 1 持有锁 (轻量级锁)");
                try {
                    Thread.sleep(1000);
                }catch (InterruptedException e){

                }
            }
        }, "线程 1");

        Thread t2 = new Thread(() -> {
            synchronized (lockObj) {
                printLockStatus("线程 2 持有锁 (轻量级锁)");
            }
        }, "线程 2");

        // 启动并等待 T1 执行完
        t1.start();
        t1.join(); // 让主线程等 T1 做完，确保 T1 释放锁后 T2 再抢，形成交替

        // 启动并等待 T2 执行完
        t2.start();
        t2.join();

        System.out.println("\n【阶段 3】启动 10 个线程同时疯狂抢锁 (激烈竞争 -> 重量级锁)");

        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            threads[i] = new Thread(() -> {
                synchronized (lockObj) {
                    // 故意持锁时间长一点，让其他线程必须排队阻塞
                    try { Thread.sleep(1000); } catch (InterruptedException e) {}
                }
            });
            threads[i].start();
        }

        Thread.sleep(200);

        // 此时锁肯定已经升级为重量级了。
        // 我们尝试在主线程再抢一次锁（此时主线程也会阻塞，直到前面有人释放）
        // 为了演示，我们在外面打印一下状态（注意：此时可能抓不到正在锁内的瞬间，但标志位已变）
        System.out.println("大量线程竞争中... 锁已升级为重量级锁 (标志位应为 10)");
        // 由于竞争激烈，我们直接强制打印一次看看最终状态
        // 注意：一旦升级成重量级，就算没人抢了，它也不会降级回轻量级！
        printLockStatus("激烈竞争后 (永久重量级)");

        // 等待所有线程结束，程序退出
        for (Thread t : threads) {
            t.join();
        }

        System.out.println("\n========== 实验结束 ==========");
    }

    private static void printLockStatus(String label){
        System.out.println("--- " + label + " ---");
        System.out.println(ClassLayout.parseInstance(lockObj).toPrintable());
        System.out.println();
    }

}
