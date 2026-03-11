package com.lab.concurrent.debug;

public class HighCpuDemo {

    private static boolean flag = true;

    public static void main(String[] args) throws InterruptedException {

        // 对应你的实际线程CPU，电脑多少CPU，就开几个线程
        for (int i = 0; i < 8; i++) {
            new Thread(() -> {
                while (flag) {
                    // 纯密集计算，无IO、无分支判断，持续消耗CPU
                    double result = 0;
                    for (long j = 0; j < 100000; j++) {
                        result += Math.sqrt(j) * Math.log(j + 1);
                    }
                }
            }, "Thread" + i).start();
        }

        // 主线程睡眠60秒，给你足够的时间做排查操作
        Thread.sleep(60000);
        // 60秒后自动停止所有线程
        flag = false;
        System.out.println("程序已停止");
    }
}