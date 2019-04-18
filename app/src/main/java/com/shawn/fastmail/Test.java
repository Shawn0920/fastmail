package com.shawn.fastmail;

public class Test {
    public static void main(String[] args) {
        int result = 600;
        int r = 0;
        for (int i = 0; result >= 0; i++) {
            r += result-30;
            result = result-30;

            System.out.println(i);
        }
        System.out.println(r);
    }
}
