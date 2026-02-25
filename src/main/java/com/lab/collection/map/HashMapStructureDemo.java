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
        testLazyLoad();
        testResize();
        testCollision();
    }


    public static void testLazyLoad() {
        System.out.println("=== 实验一：验证懒加载 ===");
        // 1. 创建 Map，指定初始容量为 2 (方便观察)
        // 注意：HashMap 实际容量会自动扩容为最近的 2 的幂，所以 2->2, 3->4
        Map<String, String> map = new HashMap<>(2);

        System.out.println("1. 创建后，未 put 任何数据。此时 table 数组应该为 null。");
        // TODO: 在这里打断点，运行后查看 'map' 对象的 'table' 字段是否为 null

        // 2. 放入第一个数据
        map.put("key1", "value1");
        System.out.println("2. 放入第一个数据后，触发初始化。");
        // TODO: 再次打断点，查看 'table' 字段，长度应该是 2 或 4
    }

    public static void testResize() {
        System.out.println("\n=== 实验二：验证扩容机制 ===");
        // 1. 创建容量为 4 的 Map，阈值 = 4 * 0.75 = 3
        Map<Integer, String> map = new HashMap<>(4);

        System.out.println("开始放入数据...");
        for (int i = 0; i < 6; i++) {
            map.put(i, "Value" + i);
            System.out.println("放入 key=" + i + ", 当前 size=" + map.size());
            // TODO: 每次循环都打断点，观察 table 数组长度的变化
        }
    }

    // 内部静态类，专门用来制造冲突
    static class BadKey {
        int id;
        public BadKey(int id) { this.id = id; }

        // 作弊！让所有对象的 hash 值都一样
        @Override
        public int hashCode() {
            return 1;
        }

        @Override
        public boolean equals(Object o) {
            return this.id == ((BadKey)o).id;
        }

        @Override
        public String toString() {
            return "Key-" + id;
        }
    }

    public static void testCollision() {
        System.out.println("\n=== 实验三：验证哈希冲突与链表 ===");
        Map<BadKey, String> map = new HashMap<>(4);

        // 放入 5 个 key，它们的 hashCode 都是 1，必然冲突！
        for (int i = 0; i < 5; i++) {
            map.put(new BadKey(i), "Value" + i);
        }

        System.out.println("放入 5 个冲突 Key 完成。");
        // TODO: 打断点，观察 table[1] 位置，是否变成了一个链表结构 (Node -> Node -> Node...)
    }
}