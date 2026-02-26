package com.lab.collection.map;

import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.Field;

/**
 * Demo: 验证 HashMap 链表转红黑树的“双阈值”条件
 *
 * 核心验证点：
 * 1. 转树条件：链表长度 > 8 且 数组容量 > 64。
 * 2. 如果容量 <= 64，即使冲突严重 (长度>8)，也会优先扩容而不是转树。
 *
 * @author BQHM
 * @date 2026-02-27
 */
public class HashMapRedBlackTreeDemo {

    // ⚠️ 坏钥匙：强制所有实例返回相同的 hashCode，制造极端冲突
    static class BadKey {
        int id;
        public BadKey(int id) { this.id = id; }

        @Override
        public int hashCode() {
            return 1; // 强制所有 Key 映射到同一个桶
        }

        @Override
        public boolean equals(Object o) {
            return this.id == ((BadKey)o).id;
        }

        @Override
        public String toString() {
            return "K" + id;
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== 开始验证红黑树转化 (双阈值规则) ===\n");

        // 【场景 1】容量小 (16)，冲突多 (10 个)
        // 预期：因为容量 16 <= 64，即使长度 10 > 8，也不会转树，而是扩容。
        System.out.println("1. 【容量小】new HashMap<>(16), 放入 10 个冲突 Key:");
        Map<BadKey, String> smallMap = new HashMap<>(16);
        for (int i = 0; i < 10; i++) {
            smallMap.put(new BadKey(i), "v" + i);
        }
        checkTreeStatus(smallMap, "小容量场景 (Capacity=16)");

        // 【场景 2】容量大 (128)，冲突多 (10 个)
        // 预期：容量 128 > 64 且 长度 10 > 8，满足双条件，应该转树！
        System.out.println("\n2. 【容量大】new HashMap<>(128), 放入 10 个冲突 Key:");
        Map<BadKey, String> largeMap = new HashMap<>(128);
        for (int i = 0; i < 10; i++) {
            largeMap.put(new BadKey(i), "v" + i);
        }
        checkTreeStatus(largeMap, "大容量场景 (Capacity=128)");

        System.out.println("\n✅ 结论：只有 [容量>64] 且 [链表长度>8] 时，才会转为红黑树。");
        System.out.println("   口诀：八大六十四，缺一不可！");
    }

    /**
     * 🔮 魔法方法：检查指定冲突位置的节点类型 (Node vs TreeNode)
     */
    private static void checkTreeStatus(Map<?, ?> map, String label) throws Exception {
        Class<?> clazz = map.getClass();
        Field tableField = clazz.getDeclaredField("table");
        tableField.setAccessible(true);
        Object table = tableField.get(map);

        if (table == null) {
            System.out.println("   ❌ Table 为空");
            return;
        }

        int len = java.lang.reflect.Array.getLength(table);
        System.out.println("   📏 [" + label + "] 当前数组容量: " + len);

        // 遍历数组，找到第一个非空桶 (即我们的冲突点)
        for (int i = 0; i < len; i++) {
            Object node = java.lang.reflect.Array.get(table, i);
            if (node != null) {
                System.out.println("   🔍 发现冲突链表/树 at Index[" + i + "]");

                // 1. 检查节点类名
                String className = node.getClass().getSimpleName();
                System.out.println("   🧱 节点类型: " + className);

                if ("TreeNode".equals(className)) {
                    System.out.println("   🎉 成功转化为【红黑树】!");
                } else {
                    System.out.println("   ⚠️ 仍是【普通链表】(Node)");

                    // 2. 计算链表长度
                    int count = 0;
                    Field nextField = node.getClass().getDeclaredField("next");
                    nextField.setAccessible(true);
                    while (node != null) {
                        count++;
                        node = nextField.get(node);
                    }
                    System.out.println("   📏 链表实际长度: " + count);

                    if (count > 8) {
                        System.out.println("   💡 分析：长度已超 8 但未转树，原因是容量 (" + len + ") <= 64，触发了扩容而非转树。");
                    }
                }
                break; // 只需要看第一个冲突点
            }
        }
    }
}