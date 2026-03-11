package com.lab.concurrent.debug;


/**
 * 🌟 测试jstck工具
 */
public class DeadLockDemo {

    private static final Object lock1 = new Object();
    private static final Object lock2 = new Object();

    public static void main(String[] args) {

        new Thread(()->{
            synchronized (lock1){
                System.out.println("线程1已经获得A锁，现在准备拿B锁");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                synchronized (lock2){
                    System.out.println("线程1获得已经获得"+lock2+"现在准备执行");
                }
            }
        },"Thread1").start();

        new Thread(()->{
           synchronized (lock2){
               System.out.println("线程2已经获得B锁，现在准备拿A锁");
               try {
                   Thread.sleep(1000);
               } catch (InterruptedException e) {
                   throw new RuntimeException(e);
               }

               synchronized (lock1){
                   System.out.println("线程2获得已经获得"+lock1+"现在准备执行");
               }
           }
        },"Thread2").start();
    }
}
