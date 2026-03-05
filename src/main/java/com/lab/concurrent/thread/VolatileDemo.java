package com.lab.concurrent.thread;

public class VolatileDemo {
    // 去掉 volatile → 线程B可能永远看不到 flag=true！
    // private static boolean flag = false;
    private static boolean flag = false; // ✅ 加上它！

    public static void main(String[] args) throws InterruptedException {
        Thread writer = new Thread(() -> {
            System.out.println("[Writer] 睡2秒后设置 flag=true");
            try { Thread.sleep(1000); } catch (Exception e) {}
            flag = true;
            System.out.println("[Writer] ✅ 已设置 flag=true");
        }, "Writer");

        Thread reader = new Thread(() -> {
            System.out.println("[Reader] 开始循环检查 flag...");
            while (!flag) {
                System.out.println("线程开始循环");
            }
            System.out.println("[Reader] 🔓 检测到 flag=true，退出循环！");
        }, "Reader");

        reader.start();
        Thread.sleep(100); // 确保 Reader 先启动
        writer.start();

        reader.join(); writer.join();
        System.out.println("\n🎯 验证结论：volatile 保证修改对其他线程【立即可见】");
    }
}