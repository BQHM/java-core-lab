package com.lab.collection.map;

import java.util.HashMap;
import java.util.Map;

/**
 * HashMap 结构验证 Demo
 * 用于面试演示：数组 + 链表 + 红黑树 的基本概念
 *
 * @author YourName
 * @date 2026-02-25
 */
public class HashMapStructureDemo {

    public static void main(String[] args) {
        System.out.println("=== Java Core Lab: HashMap Structure Test ===");

        // 1. 创建一个默认容量的 HashMap (容量 16, 负载因子 0.75)
        // 想象这是银行的 16 个柜台窗口
        Map<String, String> map = new HashMap<>();

        // 2. 放入几个数据，模拟银行账号
        // 叫号机 (Hash 算法) 会把它们分配到不同的窗口
        map.put("ACCT_001", "张三 - 存款账户");
        map.put("ACCT_002", "李四 - 理财账户");
        map.put("ACCT_003", "王五 - 贷款账户");

        // 3. 获取数据 (O(1) 速度)
        String accountInfo = map.get("ACCT_001");

        System.out.println("查询结果：" + accountInfo);
        System.out.println("当前 Map 大小：" + map.size());

        // 4. 准备下一步：后续我们将在这里演示扩容和冲突
        System.out.println("=== 基础验证通过，准备开始深度测试 ===");
    }
}