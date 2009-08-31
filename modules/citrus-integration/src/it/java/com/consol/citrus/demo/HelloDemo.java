package com.consol.citrus.demo;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class HelloDemo {
    public static void main(String[] args) {
        new ClassPathXmlApplicationContext("hello-demo-ctx.xml", HelloDemo.class);
    }
}
