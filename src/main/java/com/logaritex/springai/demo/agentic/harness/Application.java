package com.logaritex.springai.demo.agentic.harness;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	CommandLineRunner commandLineRunner(ChatClient.Builder chatClientBuilder) {
		return args -> {

			ChatClient chatClient = chatClientBuilder
				.defaultAdvisors(BestAgenticHarnessAdvisor.builder()
					.chatClientBuilder(chatClientBuilder.clone())
					.maxRepeatAttempts(3)
					.successRating(4)
					.order(0)
					.build())
				.build();

			var answer = chatClient.prompt("What is current weather in Paris?").call().content();

			System.out.println(answer);
		};
	}

}
