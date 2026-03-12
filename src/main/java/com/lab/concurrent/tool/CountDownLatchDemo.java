package com.lab.concurrent.tool;

import java.util.concurrent.CountDownLatch;

public class CountDownLatchDemo {

    public static void main(String[] args) {

        int workerNum = 6;
        CountDownLatch countDownLatch = new CountDownLatch(workerNum);

        for (int i = 1; i <= workerNum; i++) {
            final int workerId = i;
            new Thread(() -> {
                try {
                    // 模拟工人买食材耗时（0-1秒，随机时间，体现线程执行顺序随机）
                    Thread.sleep((long) (Math.random() * 1000));
                    System.out.println("工人" + workerId + "：食材买完了！");

                    countDownLatch.countDown();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }).start();
        }

        System.out.println("包工头：等" + workerNum + "个工人买完食材...");

        try {
            // 阻塞主线程，直到计数器变为0
            countDownLatch.await();
            // 计数器归0后，才执行这行
            System.out.println("包工头：所有人都买完了，开饭！");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
