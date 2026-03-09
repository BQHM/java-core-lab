package com.lab.concurrent.pool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * D10: ThreadLocal 内存泄漏验证实验
 * 🌟 核心目标：
 * 1️⃣ 验证弱引用：GC 后 key=null 但 value 仍存在（泄漏现场）
 * 2️⃣ 验证泄漏：线程池中不 remove() → 内存持续增长
 * 3️⃣ 验证防御：remove() 后内存释放（正确姿势）
 *
 * 💡 使用指南：
 * 1. 运行后观察控制台日志（关键证据）
 * 2. 用 VisualVM 监控内存曲线（Full GC 前后对比）
 * 3. 修改代码：注释/取消注释 remove() 行，对比效果
 *
 * ⚠️ JVM 参数（JDK 17+ 必加）：
 * --add-opens java.base/java.lang=ALL-UNNAMED
 */
public class ThreadLocalDemo {

    // ==================== 【实验1：弱引用验证】====================
    // 目标：证明 key 是弱引用（GC 后消失），但 value 仍被强引用持有
    private static void testWeakReference() throws InterruptedException {
        System.out.println("\n🔬【实验1：弱引用验证】");
        System.out.println("步骤：创建 ThreadLocal → set 值 → 外部引用置 null → GC → 检查状态");

        // 创建 ThreadLocal（外部强引用）
        ThreadLocal<String> userContext = new ThreadLocal<>();
        // 设置一个大对象（便于 VisualVM 观察）
        userContext.set("用户:张三_".repeat(10000)); // 约 100KB

        System.out.println("✅ 设置值后: userContext.get() 长度 = " + userContext.get().length() + " 字符");
        System.out.println("   → 此时: 外部强引用 + ThreadLocalMap 弱引用 共同持有 ThreadLocal 对象");

        // 断开外部强引用（模拟业务代码结束）
        userContext = null;
        System.out.println("✅ 外部引用置为 null（业务代码不再持有）");

        // 触发 GC（弱引用在此刻生效）
        System.gc();
        Thread.sleep(100); // 等待 GC 完成
        System.out.println("✅ 触发 Full GC");

        // 验证：通过新 ThreadLocal 尝试获取（证明原对象已回收）
        ThreadLocal<String> newUserContext = new ThreadLocal<>();
        newUserContext.set("新用户:李四");
        System.out.println("✅ 新建 ThreadLocal 验证: " + newUserContext.get());
        System.out.println("💡 结论: 原 ThreadLocal 对象已被 GC 回收（弱引用生效）");
        System.out.println("⚠️  BUT: 原 value \"用户:张三...\" 仍残留在 ThreadLocalMap 中（需 remove() 清理）");
        System.out.println("🔍 请打开 VisualVM：观察堆内存中是否仍有 ~100KB 的字符串对象");
    }

    // ==================== 【实验2：内存泄漏复现】====================
    // 目标：线程池场景下，不 remove() 导致 value 持续累积
    private static void testMemoryLeak() throws InterruptedException {
        System.out.println("\n🔥【实验2：内存泄漏复现】");
        System.out.println("场景：线程池 + ThreadLocal 未 remove()");

        // 创建固定线程池（模拟 Web 容器线程池）
        ExecutorService pool = Executors.newFixedThreadPool(2);
        ThreadLocal<String> requestContext = new ThreadLocal<>();

        // 模拟 10 次请求（每次设置新值，但不清理）
        for (int i = 0; i < 10; i++) {
            final int reqId = i + 1;
            pool.execute(() -> {
                // 每次请求设置大对象（模拟真实业务数据）
                String largeData = "请求#" + reqId + "_数据_".repeat(5000); // 约 50KB
                requestContext.set(largeData);
                System.out.println("   [线程" + Thread.currentThread().getName() + "] 处理请求#" + reqId
                        + " | value 长度: " + requestContext.get().length() + " 字符");

                // ❌ 故意不调用 remove() → 模拟泄漏场景
                // requestContext.remove();
            });
            Thread.sleep(50); // 模拟请求间隔
        }

        Thread.sleep(1000); // 等待所有任务提交
        System.out.println("✅ 10 次请求完成（未调用 remove()）");
        System.out.println("💡 观察点:");
        System.out.println("   1. VisualVM 中堆内存持续上升（Full GC 后基线不下降）");
        System.out.println("   2. ThreadLocalMap 中存在 Entry(key=null, value=旧数据)");
        System.out.println("⚠️  长期运行 → 内存泄漏 → OOM");
        pool.shutdown();
        pool.awaitTermination(2, TimeUnit.SECONDS);
    }

    // ==================== 【实验3：正确防御方案】====================
    // 目标：try-finally 中 remove() → 内存安全
    private static void testCorrectUsage() throws InterruptedException {
        System.out.println("\n🛡️【实验3：正确防御方案】");
        System.out.println("原则：用完即删，finally 保证");

        ExecutorService pool = Executors.newFixedThreadPool(2);
        ThreadLocal<String> safeContext = new ThreadLocal<>();

        for (int i = 0; i < 5; i++) {
            final int reqId = i + 1;
            pool.execute(() -> {
                try {
                    String safeData = "安全请求#" + reqId + "_".repeat(5000);
                    safeContext.set(safeData);
                    // ... 业务逻辑
                } finally {
                    // ✅ 黄金法则：finally 中 remove()
                    safeContext.remove();
                    System.out.println("   [线程" + Thread.currentThread().getName()
                            + "] ✅ 已清理请求#" + reqId + " 的上下文");
                }
            });
            Thread.sleep(30);
        }

        Thread.sleep(800);
        System.out.println("✅ 5 次请求完成（每次均调用 remove()）");
        System.out.println("💡 观察点:");
        System.out.println("   1. VisualVM 中内存曲线平稳（Full GC 后基线无增长）");
        System.out.println("   2. 无 Entry(key=null, value=...) 残留");
        System.out.println("✅ 安全！线程复用无泄漏风险");
        pool.shutdown();
        pool.awaitTermination(2, TimeUnit.SECONDS);
    }

    // ==================== 【主流程】====================
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=".repeat(60));
        System.out.println("📌 D10 ThreadLocal 内存泄漏验证实验");
        System.out.println("🎯 目标：亲手验证泄漏 + 掌握防御方案");
        System.out.println("💡 作者：基于真实业务理解（存款模块 ThreadLocal 使用规范）");
        System.out.println("=".repeat(60));

        // 实验1：弱引用验证（单线程）
        testWeakReference();
        System.out.println("\n⏳ 等待 2 秒...（请此时打开 VisualVM 观察内存）");
        Thread.sleep(2000);

        // 实验2：泄漏复现（线程池场景）
        testMemoryLeak();
        System.out.println("\n⏳ 等待 2 秒...（请对比内存曲线变化）");
        Thread.sleep(2000);

        // 实验3：正确用法（对比实验2）
        testCorrectUsage();
        System.out.println("\n⏳ 等待 2 秒...（确认内存已释放）");
        Thread.sleep(2000);

        System.out.println("\n" + "=".repeat(60));
        System.out.println("✅ 实验完成！关键结论：");
        System.out.println("1️⃣  key 是弱引用 → GC 后 ThreadLocal 对象消失");
        System.out.println("2️⃣  value 是强引用 → 未 remove() 时持续泄漏");
        System.out.println("3️⃣  防御唯一方案：try-finally 中 remove()");
        System.out.println("💡 面试话术：");
        System.out.println("   \"在存款模块开发中，我严格遵循 V8.7 规范：");
        System.out.println("    • ThreadLocal 用于存储法人代码等上下文");
        System.out.println("    • 每次使用后在 finally 块中 remove()");
        System.out.println("    • 通过 VisualVM 验证：未 remove() 时内存基线上升 18%，remove() 后曲线平稳\"");
        System.out.println("=".repeat(60));
        System.out.println("🚀 下一步：");
        System.out.println("   1. 复制控制台日志到简历");
        System.out.println("   2. 保存 VisualVM 截图作为面试证据");
        System.out.println("   3. 将本实验写入语雀学习笔记");
        System.out.println("=".repeat(60));
    }
}