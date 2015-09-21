package org.krm1312.test;

import com.google.common.base.Stopwatch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import javax.annotation.PostConstruct;

/**
 * Created by kevinm on 9/21/15.
 */
@SpringBootApplication
@ComponentScan
public class App {

    public static void main(String args[]) {

        new SpringApplicationBuilder()
                .sources(App.class)
                .headless(true)
                .run(args);

    }

}
