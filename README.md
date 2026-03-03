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
| Demo 类名 | 验证目标 | 关键结论 | 状态 |
| :--- | :--- | :--- | :---:|
| `HashMapStructureDemo` | 懒加载、初始容量对齐 | 首次 put 才初始化；容量自动转为 2 的幂 | ✅ 已完成 |
| `HashMapResizeDemo` | 扩容机制 (Resize) | size > threshold (0.75) 时扩容，容量翻倍 | ✅ 已完成 |
| `HashMapCollisionDemo` | 哈希冲突、尾插法 | 自定义 hashCode 制造冲突；JDK 1.8 采用尾插法 | ✅ 已完成 |
| `HashMapRedBlackTreeDemo` | 红黑树转化条件 | **实测：需同时满足 长度>8 且 容量>64 (八大六十四)** | ✅ 已完成 |
| `ConcurrentHashMapSafetyDemo` | 线程安全对比 | **实测：HashMap 并发丢数据；CHM (CAS+synchronized) 安全** | ✅ 已完成 |

#### 📌 D1-D3 实验亮点
- **红黑树双阈值**：通过控制变量法（容量 16 vs 128），证实了转树不仅需要链表长，还需要数组容量足够大。
- **并发安全压测**：模拟 10 线程并发写入 2000 次数据，HashMap 出现数据丢失，而 ConcurrentHashMap 始终保持数据完整。

### 2. 多线程 (Concurrency)
| Demo 类名 | 验证目标 | 关键结论 | 状态 |
| :--- | :--- | :--- | :---:|
| `ThreadLifecycleDemo` | **线程 6 种状态流转** | **实测验证 NEW/RUNNABLE/BLOCKED/WAITING/TIMED_WAITING/TERMINATED** | ✅ 已完成 |
| `ThreadPoolExecutorDemo` | **线程池 7 大参数与拒绝策略** | **实测验证“先核心->再队列->后最大”顺序；发现“后来者 (临时线程任务) 先执行”现象；验证 4 种拒绝策略效果** | ✅ 已完成 |
| `SynchronizedLockUpgradeDemo` | 锁升级过程验证 | 实测：无锁 -> 偏向锁 (JDK17 显示为 thin) -> 轻量级锁 -> 重量级锁 (fat lock) | ✅ 已完成  |
| `ThreadLocalDemo` | 内存泄漏模拟 | 待更新：验证弱引用与 remove() 的重要性 | ⬜ 待开始 |

#### 📌 D5-D6 实验亮点
- **状态精准捕获**：通过精确控制 `sleep` 时序，成功捕获了稍纵即逝的 `BLOCKED` 状态。
- **线程池反直觉现象**：实测发现，当队列满时，新提交的任务会触发创建临时线程**立即执行**，而早先提交但在队列中的任务反而要**等待**。证明了**执行顺序 ≠ 提交顺序**。
- **拒绝策略实测**：对比了 `AbortPolicy` (抛异常), `CallerRunsPolicy` (调用者运行), `DiscardPolicy` (丢弃) 的实际行为，验证了 `CallerRuns` 的背压效应。
- **线程回收验证**：观察到临时线程在空闲超过 `keepAliveTime` 后，线程池大小自动回缩至核心线程数。

### 3. JVM
- [ ] 待更新：OOM 故障模拟 (Heap & Stack)
- [ ] 待更新：GC 日志分析
- [ ] 待更新：类加载机制验证 (双亲委派)

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