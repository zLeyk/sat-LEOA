package com.sat.utils;

public class Randomget {


       public static String getRandom1(int len) {
           int rs = (int) ((Math.random() * 9 + 1) * Math.pow(10, len - 1));
           return String.valueOf(rs);
       }


}
