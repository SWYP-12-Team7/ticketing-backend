package com.jpa;

import org.springframework.boot.SpringApplication;

public class TestJpaApplication {

  public static void main(String[] args) {
    SpringApplication.from(JpaApplication::main).with(TestcontainersConfiguration.class).run(args);
  }

}
