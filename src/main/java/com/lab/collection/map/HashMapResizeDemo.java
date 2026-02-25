package com.lab.collection.map;

import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.Field;

/**
 * Demo: 验证 HashMap 扩容机制 (自动打印数组长度版)
 */
public class HashMapResizeDemo {

    public static void main(String[] args) throws Exception {
        testResizeWithPrint();
    }

    public static void testResizeWithPrint() throws Exception {
        System.out.println("=== 开始验证扩容机制 (自动打印版) ===");

        // 初始容量 4，阈值 = 4 * 0.75 = 3
        Map<Integer, String> map = new HashMap<>(4);

        System.out.println("初始状态 -> 容量: 4, 阈值: 3");
        printTableLength(map); // 打印初始长度 (应该是 null 或 0)

        for (int i = 0; i < 6; i++) {
            map.put(i, "Value-" + i);

            System.out.println("--------------------------------");
            System.out.println("操作：put(" + i + ")");
            System.out.println("当前 Size: " + map.size());

            // 🔥 核心：每次 put 后，自动打印底层数组长度
            int length = printTableLength(map);

            if (i == 2) {
                System.out.println("⚠️ 注意：当前 size=3，达到阈值，下一个元素将触发扩容！");
            }
            if (i == 3) {
                System.out.println("🚀 爆发！刚刚触发了扩容，长度从 4 变成了 " + length + "！");
            }
        }
        System.out.println("=== 验证结束 ===");
    }

    /**
     * 🔮 魔法方法：利用反射获取 private 的 table 数组长度
     */
    private static int printTableLength(Map<?, ?> map) throws Exception {
        // 1. 获取 HashMap 类的 Class 对象
        Class<?> clazz = map.getClass();

        // 2. 获取名为 "table" 的私有字段
        Field tableField = clazz.getDeclaredField("table");

        // 3. 暴力破解：允许访问私有字段
        tableField.setAccessible(true);

        // 4. 获取当前 map 对象中的 table 值
        Object table = tableField.get(map);

        if (table == null) {
            System.out.println("📏 当前 Table 长度：null (尚未初始化/懒加载)");
            return 0;
        } else {
            // 5. 获取数组长度
            int length = java.lang.reflect.Array.getLength(table);
            System.out.println("📏 当前 Table 长度：" + length);
            return length;
        }
    }
}