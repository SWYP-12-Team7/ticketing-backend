package com.example.ticketing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example")
public class TicketingApplication {

  public static void main(String[] args) {
    SpringApplication.run(TicketingApplication.class, args);
  }

}
