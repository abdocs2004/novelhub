package com.novelhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point. @SpringBootApplication = @Configuration + @EnableAutoConfiguration + @ComponentScan.
 * Spring scans this package (com.novelhub) and all sub-packages for @Component, @Service,
 * @Repository, @RestController ... and creates the objects ("beans") for us.
 */
@SpringBootApplication
public class NovelHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(NovelHubApplication.class, args);
    }
}
