package com.consol.citrus.samples.flightbooking;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class FlightBookingDemo {
    
    public static void main(String[] args) {
        new ClassPathXmlApplicationContext("flightBooking.xml", FlightBookingDemo.class);
    }
}
