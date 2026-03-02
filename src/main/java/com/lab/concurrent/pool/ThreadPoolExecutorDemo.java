package com.lab.concurrent.pool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ThreadPoolExecutorDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("=== 实验一：验证执行顺序与临时线程 ===");
        testExecutionOrder();

        Thread.sleep(10000); // 间隔一下

        System.out.println("\n=== 实验二：验证拒绝策略与线程回收 ===");
        testRejectAndRecycle();

        Thread.sleep(10000); // 间隔一下

        System.out.println("\n=== 实验三：验证 Future 的使用 ===");
        testSubmitWithResult();
    }

    /**
     * 场景一：验证 "核心 -> 队列 -> 最大" 顺序
     * 预期：Task 5,6 比 Task 3,4 先开始执行
     */
    public static void testExecutionOrder() {
        // 1. 创建线程池 (2核心, 4最大, 2队列)
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                2, 4, 60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(2),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy()
        );

        // 2. 提交 6 个任务
        for (int i = 1; i <= 6; i++) {
            final int taskId = i;
            try {
                executor.execute(() -> {
                    String threadName = Thread.currentThread().getName();
                    System.out.println("✅ [任务" + taskId + "] 开始执行 - 线程：" + threadName + " | 时间：" + System.currentTimeMillis());
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {

                    }
                    System.out.println("❌ [任务" + taskId + "] 执行结束 - 线程：" + threadName);
                });
            } catch (Exception e) {
                System.out.println("🚨 [任务" + taskId + "] 被拒绝! " + e.getMessage());
            }
        }

        // 3. 观察日志后关闭 (实际开发中不要马上关，这里为了演示)
        // executor.shutdown();
        // 注意：为了看实验二，这里先不 shutdown，或者重新创建一个新的 pool
    }

    /**
     * 场景二：验证拒绝策略 和 临时线程回收
     */
    public static void testRejectAndRecycle() throws InterruptedException {
        // 1. 创建线程池 (设置较短的 keepAliveTime 以便观察回收)
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                2, 4, 5L, TimeUnit.SECONDS, // 5秒后回收临时线程
                new ArrayBlockingQueue<>(2),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy() // 尝试修改这个策略看看效果
        );

        // 2. 提交 8 个任务 (超出容量 2+2+4=8? 不，是 2核心+2队列+2临时=6，第7个就会触发策略)
        // 容量计算：Core(2) + Queue(2) + Extra(2) = 6.
        // 第 7, 8 个任务会触发拒绝策略
        for (int i = 1; i <= 8; i++) {
            final int taskId = i;
            try {
                executor.execute(() -> {
                    System.out.println("🏃 [任务" + taskId + "] 正在运行 - " + Thread.currentThread().getName());
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                    }
                });
            } catch (Exception e) {
                System.out.println("🚨 [任务" + taskId + "] 触发拒绝策略! 策略行为: " + e.getClass().getSimpleName());
            }
        }

        // 3. 观察当前线程数 (应该是 4，满负荷)
        System.out.println("\n📊 当前活跃线程数: " + executor.getActiveCount());
        System.out.println("📊 当前池大小: " + executor.getPoolSize());

        // 4. 等待所有任务执行完毕 + 额外等待 6 秒 (超过 keepAliveTime 5秒)
        Thread.sleep(2000);

        // 5. 再次观察线程数 (应该回缩到 2)
        System.out.println("\n⏳ 等待 6 秒后...");
        System.out.println("📊 当前池大小: " + executor.getPoolSize() + " (预期回缩到 2)");

        executor.shutdown();
    }

    public static void testSubmitWithResult() throws Exception {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                2, 4, 60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(2),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy()
        );

        List<Future<String>> futures = new ArrayList<>();

        System.out.println("=== 提交任务 ===");
        for (int i = 1; i <= 3; i++) {
            final int taskId = i;
            // 使用 submit，并指定返回类型 String
            Future<String> future = executor.submit(() -> {
                System.out.println("🏃 任务 [" + taskId + "] 开始执行 - " + Thread.currentThread().getName());
                Thread.sleep(1000); // 模拟耗时
                return "✅ 任务 [" + taskId + "] 的结果是: " + (taskId * 100); // 返回结果
            });
            futures.add(future);
            System.out.println("📝 任务 [" + taskId + "] 已提交，拿到 Future 对象");
        }

        System.out.println("\n=== 获取结果 ===");
        for (int i = 0; i < futures.size(); i++) {
            Future<String> f = futures.get(i);
            // 🛑 这里会阻塞！如果任务没做完，程序停在这里等
            System.out.println("⏳ 等待任务 [" + (i+1) + "] 的结果...");
            String result = f.get();
            System.out.println("🎉 获取到结果: " + result);
        }

        executor.shutdown();
    }

}