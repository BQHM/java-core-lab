package com.lab.collection.map;

import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.Field;

/**
 * Demo: 验证 HashMap 基础结构 (懒加载 + 容量对齐)
 *
 * 核心验证点：
 * 1. 懒加载：new 之后 table 为 null，首次 put 才初始化。
 * 2. 容量对齐：传入容量会自动调整为最近的 2 的幂。
 *
 * @author BQHM
 * @date 2026-02-25
 */
public class HashMapStructureDemo {

    public static void main(String[] args) throws Exception {
        testLazyLoadAndAlignment();
    }

    public static void testLazyLoadAndAlignment() throws Exception {
        System.out.println("=== 开始验证基础结构 (懒加载 + 容量对齐) ===");

        // 【实验 1】验证懒加载
        // 传入初始容量 2 (它是 2 的幂，所以实际容量就是 2)
        System.out.println("\n1. 执行 new HashMap<>(2)...");
        Map<String, String> map = new HashMap<>(2);

        System.out.println("   -> 此时检查 Table:");
        printTableStatus(map); // 预期：null

        // 【实验 2】验证首次 Put 触发初始化
        System.out.println("\n2. 执行 map.put(\"key1\", \"value1\")...");
        map.put("key1", "value1");

        System.out.println("   -> 此时检查 Table:");
        int currentLen = printTableStatus(map); // 预期：2

        // 【实验 3】验证阈值
        System.out.println("\n3. 检查阈值 (Threshold):");
        printThreshold(map);
        System.out.println("   -> 解释：容量 2 * 0.75 = 1.5，向下取整为 1。");
        System.out.println("   -> 意味着：再放 1 个元素 (总共 2 个) 就会触发扩容！");

        // 【实验 4】验证容量非 2 的幂时的自动对齐 (可选)
        System.out.println("\n4. 额外测试：new HashMap<>(3)...");
        Map<String, String> map2 = new HashMap<>(3);
        map2.put("x", "y"); // 触发初始化
        System.out.print("   -> 传入 3，实际初始化为：");
        printTableLength(map2);
        System.out.println("   -> 结论：自动对齐到最近的 2 的幂 (4)。");

        System.out.println("\n=== 基础结构验证结束 ===");
    }

    /**
     * 🔮 魔法方法：打印 Table 数组状态 (长度 或 null)
     */
    private static int printTableStatus(Map<?, ?> map) throws Exception {
        Class<?> clazz = map.getClass();
        Field tableField = clazz.getDeclaredField("table");
        tableField.setAccessible(true);
        Object table = tableField.get(map);

        if (table == null) {
            System.out.println("      📏 Table 状态：null (尚未初始化，验证了【懒加载】)");
            return 0;
        } else {
            int len = java.lang.reflect.Array.getLength(table);
            System.out.println("      📏 Table 状态：Node[" + len + "] (已初始化)");
            return len;
        }
    }

    /**
     * 🔮 辅助方法：只打印长度
     */
    private static void printTableLength(Map<?, ?> map) throws Exception {
        Class<?> clazz = map.getClass();
        Field tableField = clazz.getDeclaredField("table");
        tableField.setAccessible(true);
        Object table = tableField.get(map);

        if (table != null) {
            int len = java.lang.reflect.Array.getLength(table);
            System.out.println(len);
        }
    }

    /**
     * 🔮 辅助方法：打印阈值
     */
    private static void printThreshold(Map<?, ?> map) throws Exception {
        Class<?> clazz = map.getClass();
        Field thresholdField = clazz.getDeclaredField("threshold");
        thresholdField.setAccessible(true);
        int threshold = thresholdField.getInt(map);
        System.out.println("      🚩 当前 Threshold: " + threshold);
    }
}