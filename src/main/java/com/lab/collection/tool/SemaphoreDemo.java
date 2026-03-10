package com.lab.collection.tool;

import java.util.concurrent.Semaphore;

public class SemaphoreDemo {

    public static void main(String[] args) {
        Semaphore  semaphore = new Semaphore(3);

        for(int i =1 ;i<=3 ;i++){
            final int carId = i;
            new Thread(() -> {
                try {
                    semaphore.acquire();
                    System.out.println("车" + carId + " → 抢到车位，停进去了");
                    // 模拟停车耗时
                    Thread.sleep(1000);
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }finally {
                    // 释放许可（开车走）
                    semaphore.release();
                    System.out.println("车" + carId + " → 离开停车场，空出车位");
                }
            }).start();
        }
    }
}
