package com.consol.citrus.ws;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class JettyServerDemo {

    public static void main(String[] args) {
        new ClassPathXmlApplicationContext("jetty-server-demo.xml", JettyServerDemo.class);
    }
}
