package com.bizideal.mn.core;

/**
 * @author : liulq
 * @date: 创建时间: 2018/4/12 13:07
 * @version: 1.0
 * @Description:
 */
public class Package {

    public static void main(String[] args) {
        int[] w = {2, 3, 2, 5, 4};
        int[] v = {3, 5, 3, 8, 6};
        int n = w.length;
        int c = 12;

        // 01背包 一维数组
        int[] array = new int[c + 1];
        for (int i = 1; i <= n; i++) {
            for (int j = c; j >= w[i - 1]; j--) {
                array[j] = Math.max(array[j], array[j - w[i - 1]] + v[i - 1]);
            }
        }
        System.out.println(array[c]);

        // 完全背包
        int[] arr = new int[c + 1]; // arr[j] 代表容量为j时的最优解
        for (int i = 1; i <= n; i++) {
            for (int j = w[i - 1]; j <= c; j++) {
                // arr[j - w[i - 1]] + v[i - 1]表示在此前基础上再放一件的最大值，可能此前已经放了一件第i件产品，也可能是0件
                // 如果arr[j]比较大，表示物品数量为i，容量为j的情况下，只放前i-1件物品比较优
                arr[j] = Math.max(arr[j], arr[j - w[i - 1]] + v[i - 1]);
            }
            System.out.println();
        }
        System.out.println(arr[c]);

        // 01背包 二维数据
        int[][] m = new int[n + 1][c + 1];
        int[][] p = new int[n + 1][c + 1];
        for (int i = 1; i < n + 1; i++) {
            for (int j = 1; j < c + 1; j++) {
                if (j >= w[i - 1]) {
                    int a = m[i - 1][j];
                    int b = m[i - 1][j - w[i - 1]] + v[i - 1];
                    m[i][j] = Math.max(a, b);
                    p[i][j] = b >= a ? 1 : 0;
                }
            }
        }
        System.out.println(m[n][c]);
        int i = n;
        int j = c;
        while (i > 0) {
            if (p[i][j] == 1) {
                System.out.print(i + "  ");
                j = j - w[i - 1];
            }
            i--;
        }

    }
}
