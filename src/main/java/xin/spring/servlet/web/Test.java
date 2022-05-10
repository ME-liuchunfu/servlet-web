package xin.spring.servlet.web;

public class Test {

    public static void main(String[] args) {
        int i = 94065900;
        long sub = (long) 10e5;
        System.out.println((i / sub) + "." + (i % sub));
        System.out.println(sub);
    }
}
