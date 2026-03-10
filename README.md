# ☕ Java Core Lab (Java 核心原理实验室)

> 📅 创建时间：2026-02-25
> 
> 👤 作者：[BQHM](https://github.com/BQHM)
> 
> 📧 联系：2531127623@qq.com
> 
> 🎯 目标：通过代码实验深入理解 Java 集合、并发、JVM 等底层原理，为面试和技术进阶做准备。

## 📖 项目简介
本项目包含一系列独立的 Java Demo，用于验证和演示 Java 核心技术点的底层机制。
每个 Demo 对应一个具体的知识点，通过 Debug 调试和单元测试，直观展示源码执行流程。
旨在将“背诵八股文”转变为“代码实证”，打造面试时的核心竞争力。

## 🚀 核心模块 (持续更新中)

### 1. 集合框架 (Collection Framework)
| Demo 类名                       | 验证目标          | 关键结论                                       |  状态   |
|:------------------------------|:--------------|:-------------------------------------------|:-----:|
| `HashMapStructureDemo`        | 懒加载、初始容量对齐    | 首次 put 才初始化；容量自动转为 2 的幂                    | ✅ 已完成 |
| `HashMapResizeDemo`           | 扩容机制 (Resize) | size > threshold (0.75) 时扩容，容量翻倍           | ✅ 已完成 |
| `HashMapCollisionDemo`        | 哈希冲突、尾插法      | 自定义 hashCode 制造冲突；JDK 1.8 采用尾插法            | ✅ 已完成 |
| `HashMapRedBlackTreeDemo`     | 红黑树转化条件       | 实测：需同时满足 长度>8 且 容量>64 (八大六十四)              | ✅ 已完成 |
| `ConcurrentHashMapSafetyDemo` | 线程安全对比        | 实测：HashMap 并发丢数据；CHM (CAS+synchronized) 安全 | ✅ 已完成 |

#### 📌 D1-D3 实验亮点
- **红黑树双阈值**：通过控制变量法（容量 16 vs 128），证实了转树不仅需要链表长，还需要数组容量足够大。
- **并发安全压测**：模拟 10 线程并发写入 2000 次数据，HashMap 出现数据丢失，而 ConcurrentHashMap 始终保持数据完整。

### 2. 多线程 (Concurrency)
| Demo 类名 | 验证目标 | 关键结论 | 状态 |
|----------|----------|----------|------|
| `ThreadLifecycleDemo` | 线程 6 种状态流转 | 实测验证 NEW/RUNNABLE/BLOCKED/WAITING/TIMED_WAITING/TERMINATED | ✅ 已完成 |
| `ThreadPoolExecutorDemo` | 线程池 7 大参数与拒绝策略 | 实测验证"先核心→再队列→后最大"顺序；发现"后来者（临时任务）先执行"现象；验证 4 种拒绝策略效果 | ✅ 已完成 |
| `SynchronizedLockUpgradeDemo` | 锁升级过程验证 | 实测：无锁 → 偏向锁 (JDK17 显示为 thin) → 轻量级锁 → 重量级锁 (fat lock) | ✅ 已完成 |
| `SimpleMutexDemo` | AQS 线程排队验证 | 实测：T1 掌握 → T2 阻塞入队 → T1 释放 → T2 唤醒；控制日志打印 CLH 队列阻塞/唤醒 实证 | ✅ 已完成 |
| `VolatileVisibilityDemo` | volatile 三维度验证 | 可见性 (无 volatile 时 reader 死循环；有 volatile 立即感知)；有序性 (双重检查锁单例初始化对象，NPE 验证)；原子性 (volatile i++ 非原子) | ✅ 已完成 |
| `SingletonDoubleCheckDemo` | 双重检查锁单例模式 | 实测：volatile 防止指令重排序导致半初始化对象；10 线程并发验证单例唯一性；未加 volatile 时偶发 NPE 风险 | ✅ 已完成 |
| `ThreadLocalDemo` | 内存泄漏复现 | 实测：验证弱引用与 `remove()` 的重要性 | ✅ 已完成 |
| `CountDownLatchMealDemo` | 主线程等待多线程完成 | 实测：验证"1 方等 N 方"核心逻辑；5 个工人线程随机完成，主线程阻塞等待计数器归 0 | ✅ 已完成 |
| `SemaphoreParkingDemo` | 并发限流控制 | 实测：验证"最多 N 个线程同时执行"核心逻辑；10 个线程抢 3 个许可，可视化限流效果；`finally` 保证许可释放 | ✅ 已完成 |
| `CyclicBarrierGameDemo` | 多线程互相等待同步 | 实测：验证"N 方互相等"核心逻辑；3 个玩家线程到齐后执行屏障动作；验证"计数器可重置"特性，可复用屏障对象 | ✅ 已完成 |

#### 📌 D5-D6 实验亮点
- **状态精准捕获**：通过精确控制 `sleep` 时序，成功捕获了稍纵即逝的 `BLOCKED` 状态。
- **线程池反直觉现象**：实测发现，当队列满时，新提交的任务会触发创建临时线程**立即执行**，而早先提交但在队列中的任务反而要**等待**。证明了**执行顺序 ≠ 提交顺序**。
- **拒绝策略实测**：对比了 `AbortPolicy` (抛异常), `CallerRunsPolicy` (调用者运行), `DiscardPolicy` (丢弃) 的实际行为，验证了 `CallerRuns` 的背压效应。
- **线程回收验证**：观察到临时线程在空闲超过 `keepAliveTime` 后，线程池大小自动回缩至核心线程数。

### D11 实验亮点
- **工具类场景化验证**：
   - `CountDownLatchDemo`：验证"主线程等子线程"，5 个工人随机完成任务，"开饭"日志始终最后输出，证明阻塞逻辑生效；
   - `SemaphoreDemo`：验证"限流"，10 个线程抢 3 个许可，控制台始终最多 3 个"抢到车位"日志，可视化限流效果；
   - `CyclicBarrierGameDemo`：验证"线程互相等"，3 个玩家随机到达副本门口，全部到齐后才执行"队长喊冲"，再同步进入副本，证明屏障同步生效；
- **生产级实践**：
   - `Semaphore` 中 `release()` 放在 `finally` 块，保证许可一定释放，避免资源泄漏；
   - `CyclicBarrier` 中使用 `await(long, TimeUnit)` 超时版本，避免永久阻塞风险；
- **核心区别对比**：
   - CountDownLatch：1 方等 N 方，计数器不可逆；
   - Semaphore：限制并发数，许可可重复获取/释放；
   - CyclicBarrier：N 方互相等，计数器可重置。

## 3. JVM
| 待更新 | 内容 | 状态 |
|--------|------|------|
| [待更新] | OOM 故障模拟 (Heap & Stack) | ⏳ 待开始 |
| [待更新] | GC 日志分析 | ⏳ 待开始 |
| [待更新] | 类加载机制验证 (双亲委派) | ⏳ 待开始 |

## 🛠️ 如何运行
本项目基于 **Maven** 构建，使用 **JDK 17**。

1. 克隆项目：
   ```bash
   git clone https://github.com/BQHM/java-core-lab.git
   cd java-core-lab
   
2. 重要提示：关于反射实验配置
部分 Demo (如 `HashMap*Demo`) 使用了反射访问 JDK 私有字段。在 JDK 17 下运行需添加 VM 参数：
   ```bash
   --add-opens java.base/java.util=ALL-UNNAMED