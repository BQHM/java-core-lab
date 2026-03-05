package com.lab.concurrent.memory;

/**
 * 🌟 volatile 可见性验证 Demo
 * <p>
 * 💡 为什么这么写？
 * 1. 用两个线程模拟"生产者-消费者"场景（最典型共享变量场景）
 * 2. 无 volatile 时：消费者线程可能永远看不到 ready 变化（死循环）
 * 3. 有 volatile 时：消费者立即感知变化并退出循环
 * <p>
 * 📚 你能学到：
 * ✅ volatile 如何解决"工作内存→主存"同步问题
 * ✅ 为什么局部变量不能加 volatile（编译报错验证）
 * ✅ 面试高频陷阱：volatile 不保证原子性（i++ 问题）
 */
public class VolatileVisibilityDemo {

    // ✅ 关键1：volatile 修饰【实例变量】（属于对象，多线程共享时需可见性）
    // ❌ 错误示范：若去掉 volatile，reader 线程可能永远看不到 ready=true（死循环！）
    private volatile boolean ready = false;

    // ✅ 关键2：普通变量（无 volatile）→ 用于对比验证
    private int number = 0;

    // ❌ 编译错误验证：局部变量不能加 volatile（取消注释会报错）
    // private void testLocal() {
    //     volatile int localVar = 0; // 编译错误：modifier volatile not allowed here
    // }

    public void start() throws InterruptedException {
        // 🧪 场景：writer 线程修改共享变量，reader 线程等待该变量变化
        Thread writer = new Thread(() -> {
            System.out.println("[Writer] 准备设置 number=42 和 ready=true");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }

            number = 42;      // 1. 先写数据
            ready = true;     // 2. 再设标志位（volatile 保证此操作对 reader 立即可见）
            System.out.println("[Writer] 已设置 ready=true");
        }, "Writer-Thread");

        Thread reader = new Thread(() -> {
            System.out.println("[Reader] 开始等待 ready 变为 true...");
            // 🔁 循环等待：无 volatile 时可能永远看不到 ready 变化！
            while (!ready) {
                // Thread.yield(); // 让出CPU，加速验证（可选）
            }
            // 💡 关键：若 number 无 happens-before 保证，可能读到 0（指令重排序风险）
            System.out.println("[Reader] 检测到 ready=true, number=" + number);
        }, "Reader-Thread");

        reader.start();
        writer.start();
        reader.join();
        writer.join();
        System.out.println("\n✅ 验证结论：volatile 保证了 ready 变量的【可见性】");
    }

    // 🌰 原子性陷阱演示（面试必问！）
    private volatile int counter = 0;

    public void atomicityTrap() {
        System.out.println("\n⚠️ 陷阱演示：volatile 不保证原子性");
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 10000; i++) counter++; // 非原子操作！
        });
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 10000; i++) counter++; // 非原子操作！
        });
        t1.start();
        t2.start();
        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {

        }
        System.out.println("预期值: 20000, 实际值: " + counter +
                " → 证明 volatile 不能替代 AtomicInteger");
    }

    public static void main(String[] args) throws InterruptedException {
        VolatileVisibilityDemo demo = new VolatileVisibilityDemo();
        System.out.println("实验1==============================================");
        demo.start();          // 验证可见性
        System.out.println("实验2==============================================");
        demo.atomicityTrap();  // 验证原子性陷阱
    }
}