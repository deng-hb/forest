package com.denghb.utils;

import java.util.Random;

public class RandomUtils {

    private static Random _random = new Random();

    public static int random(int min, int max) {
        if (min > max) {
            int tmp = max;
            max = min;
            min = tmp;
        }
        return _random.nextInt(max - min + 1) + min;
    }

    private static char[] _source = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k',
            'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w',
            'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

    public static String random(int length) {

        StringBuilder str = new StringBuilder("");
        for (int i = 0; i < length; i++) {
            int j = random(0, _source.length - 1);
            str.append(_source[j]);
        }

        return str.toString();
    }


    public static void main(String[] args) {

        for (int i = 0; i < 20; i++) {
            System.out.println(random(10, -11));
        }
        for (int i = 0; i < 100; i++) {
            System.out.println(random(5));
        }
    }
}
