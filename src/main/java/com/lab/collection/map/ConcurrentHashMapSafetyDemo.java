package com.lab.collection.map;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Demo: 验证 HashMap 的线程不安全性 vs ConcurrentHashMap 的线程安全性
 * <p>
 * 核心验证点：
 * 1. 多线程并发 put 时，HashMap 会导致数据丢失或报错。
 * 2. ConcurrentHashMap 在高并发下依然保证数据完整性和安全性。
 * 3. 理解 JDK 1.8 CHM 的 CAS + synchronized 机制优势。
 *
 * @author BQHM
 * @date 2026-02-27
 */
public class ConcurrentHashMapSafetyDemo {

    // 配置参数
    private static final int THREAD_COUNT = 10;
    private static final int PUT_COUNT_PER_THREAD = 200;
    private static final int EXPECTED_TOTAL = THREAD_COUNT * PUT_COUNT_PER_THREAD; // 2000

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 开始验证并发安全性 ===");
        System.out.println("配置: " + THREAD_COUNT + " 个线程，每个线程 put " + PUT_COUNT_PER_THREAD + " 次");
        System.out.println("期望总数据量: " + EXPECTED_TOTAL + "\n");

        // 【实验 1】测试 HashMap (不安全)
        testUnsafeMap();

        // 【实验 2】测试 ConcurrentHashMap (安全)
        testSafeMap();

        System.out.println("\n✅ 结论：生产环境严禁在多线程中使用 HashMap，必须使用 ConcurrentHashMap！");
    }

    /**
     * 测试 HashMap 的线程不安全性
     */
    private static void testUnsafeMap() throws InterruptedException {
        System.out.println("1. 🚨 测试 HashMap (无锁):");
        Map<String, Integer> map = new HashMap<>();

        long startTime = System.currentTimeMillis();
        runThreads(map);
        long endTime = System.currentTimeMillis();

        int size = map.size();
        String status = (size == EXPECTED_TOTAL) ? "✅ 意外成功 (概率极低)" : "❌ 数据丢失 (Size: " + size + ")";

        System.out.println("   -> 结果: " + status);
        System.out.println("   -> 耗时: " + (endTime - startTime) + " ms");
        System.out.println("   -> 分析: HashMap 没有同步机制，多线程 put 会导致覆盖写入或链表成环。");
    }

    /**
     * 测试 ConcurrentHashMap 的线程安全性
     */
    private static void testSafeMap() throws InterruptedException {
        System.out.println("\n2. 🛡️ 测试 ConcurrentHashMap (CAS + synchronized):");
        Map<String, Integer> map = new ConcurrentHashMap<>();

        long startTime = System.currentTimeMillis();
        runThreads(map);
        long endTime = System.currentTimeMillis();

        int size = map.size();
        String status = (size == EXPECTED_TOTAL) ? "✅ 数据完整 (Size: " + size + ")" : "❌ 异常丢失";

        System.out.println("   -> 结果: " + status);
        System.out.println("   -> 耗时: " + (endTime - startTime) + " ms");
        System.out.println("   -> 分析: CHM 通过细粒度锁保证线程安全，虽略有性能开销但数据绝对可靠。");
    }

    /**
     * 通用方法：启动多个线程并发写入
     */
    private static void runThreads(Map<String, Integer> map) throws InterruptedException {
        Thread[] threads = new Thread[THREAD_COUNT];

        for (int i = 0; i < THREAD_COUNT; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < PUT_COUNT_PER_THREAD; j++) {
                    // Key 设计为唯一，确保理论上应该有 2000 个不同的 Entry
                    map.put("key-" + threadId + "-" + j, j);
                }
            });
            threads[i].start();
        }

        // 等待所有线程执行完毕
        for (Thread t : threads) {
            t.join();
        }
    }
}