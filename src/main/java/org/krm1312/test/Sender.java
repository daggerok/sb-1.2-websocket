package org.krm1312.test;

import com.google.common.base.Stopwatch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

/**
 * Created by kevinm on 9/21/15.
 */
@Service
public class Sender {

    @Autowired
    private SimpMessagingTemplate template;


    @PostConstruct
    public void sendSomeMessages() {

        ExecutorService service = Executors.newSingleThreadExecutor();


        service.submit(() -> {

            try {
                // give a bit to startup
                Thread.sleep(2000);
            } catch (Exception e) {}

            int cnt = 1000;

            Stopwatch sw = Stopwatch.createStarted();
            for (int i = 0; i < cnt && sw.elapsed(TimeUnit.SECONDS) < 10; i++) {
                System.out.println("Sending msg: " + i);
                template.convertAndSend("/topic/1", "Message: " + i);
            }

            System.out.println("Sent " + cnt + " messages in " + sw);

            System.exit(0);
        });

    }

}
