package org.krm1312.test;

import com.google.common.base.Stopwatch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.broker.BrokerAvailabilityEvent;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

/**
 * Created by kevinm on 9/21/15.
 */
@Service
public class Sender implements ApplicationListener {

    @Autowired
    private SimpMessagingTemplate template;


    @Override
    public void onApplicationEvent(ApplicationEvent event) {

        if (event instanceof BrokerAvailabilityEvent && ((BrokerAvailabilityEvent) event).isBrokerAvailable()) {

            ExecutorService service = Executors.newSingleThreadExecutor();

            service.submit(() -> {

                int cnt = 1000;
                Stopwatch sw = Stopwatch.createStarted();
                int sent = 0;
                for (int i = 0; i < cnt; i++) {
                    System.out.println("Sending msg: " + i);
                    template.convertAndSend("/topic/1", "Message: " + i);
                    sent = i;
                    if (sw.elapsed(TimeUnit.SECONDS) > 10) {
                        System.out.println("Giving up after only sending " + i + " messages");
                        break;
                    }
                }

                System.out.println("Sent " + sent + " messages in " + sw);
                System.exit(0);
            });
        }
    }

}
