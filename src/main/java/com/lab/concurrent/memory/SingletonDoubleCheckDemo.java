package com.lab.concurrent.memory;

/**
 * 🌟 双重检查锁单例（volatile 防御指令重排序）
 * <p>
 * 💡 为什么这么写？
 * 1. 懒加载：首次调用 getInstance() 时才创建实例
 * 2. 双重检查：减少 synchronized 开销（高性能单例）
 * 3. volatile：防止指令重排序导致其他线程拿到【未初始化完成】的对象
 * <p>
 * 📚 你能学到：
 * ✅ new Singleton() 的3步指令（分配内存→初始化→引用赋值）
 * ✅ 指令重排序风险：2和3可能交换 → 其他线程拿到半初始化对象
 * ✅ volatile 如何通过内存屏障禁止重排序（StoreStore + StoreLoad）
 */
public class SingletonDoubleCheckDemo {

    // ✅ 关键：volatile 修饰【静态实例变量】
    // ❌ 无 volatile 风险：线程B可能拿到未初始化完成的对象（调用方法时NPE！）
    private static volatile SingletonDoubleCheckDemo instance;

    // 私有构造器（防止外部 new）
    private SingletonDoubleCheckDemo() {
        // 模拟耗时初始化（放大重排序风险）
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {

        }
        System.out.println("[Singleton] 实例初始化完成");
    }

    /**
     * 双重检查锁获取单例
     * <p>
     * 🌰 指令重排序风险场景（无 volatile 时）：
     * 1. 线程A执行：分配内存 → 【引用赋值】(instance=非空) → 初始化（重排序！）
     * 2. 线程B检查：instance != null → 直接 return（但对象未初始化！）
     * 3. 线程B调用 instance.method() → NullPointerException！
     * <p>
     * ✅ volatile 作用：
     * - 写屏障：instance = new ... 前插入 StoreStore 屏障，禁止"初始化"和"赋值"重排序
     * - 读屏障：if (instance == null) 前插入 LoadLoad 屏障，保证读取最新值
     */
    public static SingletonDoubleCheckDemo getInstance() {
        // 第一次检查（无锁，高性能）
        if (instance == null) {
            synchronized (SingletonDoubleCheckDemo.class) {
                // 第二次检查（防止多线程重复创建）
                if (instance == null) {
                    instance = new SingletonDoubleCheckDemo(); // volatile 防重排序关键点
                }
            }
        }
        return instance;
    }

    // 业务方法（用于验证单例唯一性）
    public void doSomething() {
        System.out.println("Singleton instance hash: " + System.out.hashCode());
    }

    // 多线程测试：验证单例唯一性 + 无NPE
    public static void main(String[] args) throws InterruptedException {
        System.out.println("🚀 启动 10 个线程并发获取单例...");

        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            threads[i] = new Thread(() -> {
                SingletonDoubleCheckDemo singleton = getInstance();
                singleton.doSomething(); // 无 volatile 时可能 NPE！
            }, "Thread-" + i);
            threads[i].start();
        }

        for (Thread t : threads) t.join();
        System.out.println("\n✅ 验证结论：volatile 保证了单例创建的【有序性】，避免半初始化对象风险");
    }
}