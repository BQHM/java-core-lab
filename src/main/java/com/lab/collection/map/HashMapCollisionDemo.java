package com.lab.collection.map;

import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.Field;
import java.lang.reflect.Array;

/**
 * Demo: 验证哈希冲突与尾插法 (自动打印链表版)
 */
public class HashMapCollisionDemo {

    static class BadKey {
        int id;
        public BadKey(int id) { this.id = id; }
        @Override
        public int hashCode() { return 1; } // 强制冲突
        @Override
        public boolean equals(Object o) { return this.id == ((BadKey)o).id; }
        @Override
        public String toString() { return "Key-" + id; }
    }

    public static void main(String[] args) throws Exception {
        testTailInsertionWithPrint();
    }

    public static void testTailInsertionWithPrint() throws Exception {
        System.out.println("=== 开始验证尾插法 (自动打印版) ===");

        Map<BadKey, String> map = new HashMap<>(4);

        map.put(new BadKey(1), "First");
        System.out.println("放入 Key-1 后：");
        printChainStructure(map);

        map.put(new BadKey(2), "Second");
        System.out.println("\n放入 Key-2 后：");
        printChainStructure(map);

        map.put(new BadKey(3), "Third");
        System.out.println("\n放入 Key-3 后：");
        printChainStructure(map);

        System.out.println("\n✅ 结论：链表顺序为 1 -> 2 -> 3，证明是【尾插法】！");
    }

    /**
     * 🔮 魔法方法：打印冲突链表的完整结构
     */
    private static void printChainStructure(Map<?, ?> map) throws Exception {
        Class<?> clazz = map.getClass();
        Field tableField = clazz.getDeclaredField("table");
        tableField.setAccessible(true);
        Object table = tableField.get(map);

        if (table == null) {
            System.out.println("  Table 为空");
            return;
        }

        int len = Array.getLength(table);
        for (int i = 0; i < len; i++) {
            Object node = Array.get(table, i);
            if (node != null) {
                System.out.print("  Index [" + i + "]: ");

                // 遍历链表
                int count = 0;
                while (node != null) {
                    // 获取 node 的 key 字段
                    Field keyField = node.getClass().getDeclaredField("key");
                    keyField.setAccessible(true);
                    Object key = keyField.get(node);

                    System.out.print(key + " -> ");

                    // 获取 next 字段
                    Field nextField = node.getClass().getDeclaredField("next");
                    nextField.setAccessible(true);
                    node = nextField.get(node);
                    count++;
                }
                System.out.println("null (链表长度：" + count + ")");
            }
        }
    }
}