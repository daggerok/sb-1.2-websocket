/*
 * Copyright 2002-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.krm1312.test;

import com.google.common.base.Stopwatch;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.io.net.ChannelStream;
import reactor.io.net.NetStreams;
import reactor.io.net.NetStreams.TcpClientFactory;
import reactor.io.net.impl.netty.tcp.NettyTcpClient;
import reactor.io.net.tcp.TcpClient;
import reactor.rx.Promise;
import reactor.rx.Promises;
import reactor.rx.Streams;

import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.Reactor2StompCodec;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;

public class ClientApp {

	private static Logger logger = LoggerFactory.getLogger(ClientApp.class);


	public static void main(String[] args) throws Exception {

		TcpClientFactory<Message<byte[]>, Message<byte[]>> tcpClientFactory =
				spec -> spec.codec(new Reactor2StompCodec()).connect("127.0.0.1", 61613);

		TcpClient<Message<byte[]>, Message<byte[]>> tcpClient =
				NetStreams.tcpClient(NettyTcpClient.class, tcpClientFactory);

		AtomicReference<ChannelStream<Message<byte[]>, Message<byte[]>>> reference = new AtomicReference<>();
		CountDownLatch latch = new CountDownLatch(1);

		tcpClient.start(channelStream -> {

			reference.set(channelStream);

			channelStream.consume(message -> {
				StompHeaderAccessor inAccessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
				if (StompCommand.CONNECTED.equals(inAccessor.getCommand())) {
					latch.countDown();
				}
			});

			StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
			accessor.setAcceptVersion("1.1,1.2");
			accessor.setLogin("guest");
			accessor.setPasscode("guest");
			Message<byte[]> connectMessage = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
			channelStream.writeWith(Streams.just(connectMessage)).subscribe(Promises.prepare());

			return Promises.prepare();
		});

		latch.await(5000, TimeUnit.MILLISECONDS);
		ChannelStream<Message<byte[]>, Message<byte[]>> channelStream = reference.get();

		int cnt = 1000;
		Stopwatch sw = Stopwatch.createStarted();
		for (int i=0; i < cnt; i++) {
			logger.debug("Sending message: " + i);
			StompHeaderAccessor outAccessor = StompHeaderAccessor.create(StompCommand.SEND);
			outAccessor.setDestination("/topic/foo");
			Message<byte[]> outMessage = MessageBuilder.createMessage(new byte[0], outAccessor.getMessageHeaders());
			Promise<Void> promise = Promises.prepare();
			channelStream.writeWith(Streams.just(outMessage)).subscribe(promise);
			promise.await();
		}
		logger.info("Sent {} in {}", cnt, sw);

		System.in.read();

	}

}
