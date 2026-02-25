# ☕ Java Core Lab (Java 核心原理实验室)

> 📅 创建时间：2026-02-25
> 👤 作者：[BQHM](https://github.com/BQHM)
> 📧 联系：2531127623zh@gmail.com
> 🎯 目标：通过代码实验深入理解 Java 集合、并发、JVM 等底层原理，为面试和技术进阶做准备。

## 📖 项目简介
本项目包含一系列独立的 Java Demo，用于验证和演示 Java 核心技术点的底层机制。
每个 Demo 对应一个具体的知识点，通过 Debug 调试和单元测试，直观展示源码执行流程。
旨在将“背诵八股文”转变为“代码实证”，打造面试时的核心竞争力。

## 🚀 核心模块 (持续更新中)

### 1. 集合框架 (Collection Framework)
| Demo 类名 | 验证目标 | 关键结论 | 状态 |
| :--- | :--- | :--- | :---:|
| `HashMapStructureDemo` | 懒加载、初始容量对齐 | 首次 put 才初始化数组；容量自动转为 2 的幂 | ✅ 已完成 |
| `HashMapResizeDemo` | 扩容机制 (Resize) | size > threshold (0.75) 时扩容，容量翻倍 | 🚧 进行中 |
| `HashMapCollisionDemo` | 哈希冲突、尾插法 | 自定义 hashCode 制造冲突；JDK 1.8 采用尾插法 | ⬜ 待开始 |
| `HashMapRedBlackTreeDemo` | 链表转红黑树 | 链表长度>8 且 数组容量>64 时转树 | ⬜ 待开始 |

### 2. 多线程 (Concurrency)
- [ ] 待更新：线程池参数调优实验
- [ ] 待更新：锁升级过程验证

### 3. JVM
- [ ] 待更新：OOM 故障模拟
- [ ] 待更新：GC 日志分析

## 🛠️ 如何运行
本项目基于 **Maven** 构建，使用 **JDK 17**。

1. 克隆项目：
   ```bash
   git clone https://github.com/BQHM/java-core-lab.git
   cd java-core-lab