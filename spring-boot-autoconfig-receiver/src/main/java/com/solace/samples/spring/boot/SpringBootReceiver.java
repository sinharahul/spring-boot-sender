package com.solace.samples.spring.boot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executor;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@SpringBootApplication
public class SpringBootReceiver {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootReceiver.class, args);
	}

	@Bean(destroyMethod = "shutdown")
	public ThreadPoolTaskExecutor topicExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setAllowCoreThreadTimeOut(true);
		executor.setKeepAliveSeconds(300);
		executor.setCorePoolSize(4);
		executor.setQueueCapacity(0);
		executor.setThreadNamePrefix("TOPIC-");
		return executor;
	}
	@Bean
	JmsListenerContainerFactory<?> topicListenerFactory(ConnectionFactory connectionFactory, DefaultJmsListenerContainerFactoryConfigurer configurer, @Qualifier("topicExecutor") Executor topicExecutor) {
		DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
		configurer.configure(factory, connectionFactory);
		//factory.setPubSubDomain(true);
		factory.setSessionTransacted(false);
		factory.setSubscriptionDurable(false);
		factory.setTaskExecutor(topicExecutor);
		return factory;
	}
	@JmsListener(destination = "first",concurrency = "10",containerFactory = "topicListenerFactory")
	public void handle(Message message) {

		Date receiveTime = new Date();

		if (message instanceof TextMessage) {
			TextMessage tm = (TextMessage) message;
			try {
				System.out.println(
						""+Thread.currentThread().getName()+" Message Received at " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(receiveTime)
								+ " with message content of: " + tm.getText());
			} catch (JMSException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println(message.toString());
		}
	}

}
