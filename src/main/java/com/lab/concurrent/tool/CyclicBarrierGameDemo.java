package com.lab.concurrent.tool;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

public class CyclicBarrierGameDemo {

    public static void main(String[] args) throws InterruptedException {
        int count = 3;

        CyclicBarrier cyclicBarrier = new CyclicBarrier(count,() -> {
            System.out.println("队长：所有人到齐了，冲！");
        });

        for (int i = 1; i <= count; i++) {
            final int playerId = i;
            new Thread(() -> {
                try {
                    // 第一步：模拟玩家赶路到副本门口（耗时随机，体现执行顺序随机）
                    long walkTime = (long) (Math.random() * 1500); // 0-1.5秒
                    Thread.sleep(walkTime);
                    System.out.println("玩家" + playerId + "：到副本门口了，等队友！");

                    // 关键：到达屏障，等待其他玩家（核心方法）
                    cyclicBarrier.await(1,TimeUnit.SECONDS);

                    // 第二步：所有玩家到齐后，才执行这行（进副本打怪）
                    System.out.println("玩家" + playerId + "：进入副本打怪！");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, "Player " + i).start();
        }

        Thread.sleep(5000);

        for (int i = 1; i <= count; i++){
            final int playerId = i;
            new Thread(() -> {
                try{
                    Thread.sleep((long) (Math.random() * 1000));
                    System.out.println("第二轮玩家" + playerId + "：到副本门口了，等队友！");
                    cyclicBarrier.await();
                    System.out.println("第二轮玩家" + playerId + "：进入副本打怪！");
                }catch (Exception e){
                    e.printStackTrace();
                }
            }, "Player " + i).start();
        }

    }

}
