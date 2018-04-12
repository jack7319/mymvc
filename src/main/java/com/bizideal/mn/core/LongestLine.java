package com.bizideal.mn.core;

/**
 * @author : liulq
 * @date: 创建时间: 2018/4/12 14:49
 * @version: 1.0
 * @Description: 最长子序列
 */
public class LongestLine {

    // dp[i] = max{dp[j]+1}, 1<=j<i,a[j]<a[i].
    // 状态的定义：给定一个数组，fi表示前i个元素的最长子序列长度
    // 状态转换方程：fi = max{fi, (fj+1 && 1<j<i && s[j]<s[i]) | (s[j] && s[j]>s[i])}
    public static void main(String[] args) {
        int[] s = {9, 4, 6, 3, 7, 8};
        int n = s.length;
        int[] arr = new int[n + 1];
        // i是个数
        // j是1-i
        arr[1] = 1;
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= i; j++) {
                if (s[j - 1] < s[i - 1]) {
                    arr[i] = Math.max(arr[i], arr[j] + 1);
                } else {
                    arr[i] = arr[j];
                }
            }
            for (int i1 : arr) {
                System.out.print(i1 + " ");
            }
            System.out.println();
        }
        System.out.println(arr[n]);

        s();
        ss();
    }

    // 最大连续子序列和
    public static void s() {
        int[] s = {1, 2, -1, -4, 1, 1};
        int begin1 = 0;
        int begin2 = 0;
        int end = 0;
        int tempSum = 0, sum = 0;

        for (int i = 0; i < s.length; i++) {
            if (tempSum > 0) {
                tempSum += s[i];
            } else {
                tempSum = s[i];
                begin1 = i;
            }
            if (tempSum > sum) {
                sum = tempSum;
                end = i;
                begin2 = begin1;
            }
        }
        System.out.println(begin2);
        System.out.println(end);
        System.out.println(sum);
    }

    // 最长公众子串
    public static void ss() {
        String str1 = "12d";
        String str2 = "12d";
        String min = str1.length() <= str2.length() ? str1 : str2;
        String max = str1.length() > str2.length() ? str1 : str2;
        String target = "";
        for (int i = min.length(); i > 0; i--) {
            for (int j = 0; j <= min.length() - 1 && j < i; j++) {
                String str = min.substring(j, i);
                if (max.contains(str) && str.length() > target.length()) {
                    target = str;
                }
            }
        }
        System.out.println(target);
    }

}
