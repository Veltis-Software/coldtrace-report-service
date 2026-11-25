package com.acme.coldtrace.reports;

import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages={"com.acme.coldtrace.reports","com.acme.coldtrace.platform.reports"})
@EnableScheduling
public class ReportApplication {
  public static void main(String[] args) {
    SpringApplication.run(ReportApplication.class, args);
  }
}
