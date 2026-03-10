package com.lab.concurrent.jmh;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.*;

// 1. 测试模式：吞吐量（每秒执行多少次）+ 平均时间
@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
// 2. 输出时间单位：毫秒（适合线程池压测）
@OutputTimeUnit(TimeUnit.MILLISECONDS)
// 3. 预热：3轮，每轮1秒（等JIT优化）
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
// 4. 实测：5轮，每轮2秒（取平均值更准）
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
// 5. 进程隔离：1个进程（避免干扰）
@Fork(1)
// 6. 线程数：测试时用10个线程提交任务（模拟并发）
@Threads(50)
// 7. 状态：每个测试线程一个实例（避免线程安全问题）
@State(Scope.Thread)
public class ThreadPoolJmhBenchmark {

    // 定义3个线程池（你已写的部分）
    private ThreadPoolExecutor executor1;
    private ThreadPoolExecutor executor2;
    private ThreadPoolExecutor executor3;

    // ========== 新增：初始化线程池（每个测试前执行一次） ==========
    @Setup
    public void init() {
        // 把你原来的线程池创建逻辑移到这里（@Setup保证测试前初始化）
        executor1 = new ThreadPoolExecutor(
                5, 20, 60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(100),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        executor2 = new ThreadPoolExecutor(
                20, 80, 60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(100),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.DiscardOldestPolicy()
        );

        executor3 = new ThreadPoolExecutor(
                50, 200, 60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(100),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.DiscardOldestPolicy()
        );
    }

    // ========== 新增：销毁线程池（测试后执行） ==========
    @TearDown
    public void destroy() {
        executor1.shutdown();
        executor2.shutdown();
        executor3.shutdown();
    }

    @Benchmark
    public void testExecutor1() throws ExecutionException, InterruptedException {
        executor1.submit(()->{
            try{
                Thread.sleep(10);
                long sum = 0;
                for (int i = 0; i < 100; i++){
                    sum += i;
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).get();
    }

    @Benchmark
    public void testExecutor2() throws ExecutionException, InterruptedException {
        executor1.submit(()->{
            try{
                Thread.sleep(10);
                long sum = 0;
                for (int i = 0; i < 100; i++){
                    sum += i;
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).get();
    }

    @Benchmark
    public void testExecutor3() throws ExecutionException, InterruptedException {
        executor1.submit(()->{
            try{
                Thread.sleep(10);
                long sum = 0;
                for (int i = 0; i < 100; i++){
                    sum += i;
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).get();
    }

    // ========== JMH启动入口（替换原来的main方法） ==========
    public static void main(String[] args) throws RunnerException {
        // 构建JMH运行参数
        Options options = new OptionsBuilder()
                // 指定要测试的类
                .include(ThreadPoolJmhBenchmark.class.getSimpleName())
                // 关闭日志（避免干扰）
                .shouldFailOnError(true)
                .build();

        // 启动基准测试
        new Runner(options).run();
    }
}
