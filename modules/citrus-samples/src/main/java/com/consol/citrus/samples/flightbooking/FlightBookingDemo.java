package com.consol.citrus.samples.flightbooking;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.consol.citrus.activemq.ActiveMQServer;

public class FlightBookingDemo {
    
    public static void main(String[] args) {
        ActiveMQServer activemqServer = new ActiveMQServer();
        activemqServer.startup();
        
        new ClassPathXmlApplicationContext("flightBooking.xml", FlightBookingDemo.class);
    }
}
