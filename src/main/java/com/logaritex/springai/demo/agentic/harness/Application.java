package com.logaritex.springai.demo.agentic.harness;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
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

			// Reuse the main LLM model as a judge too
			var judgeChatClientBuilder = chatClientBuilder.clone()
				.defaultAdvisors(new MyLoggingAdvisor(0, "[EVALUATOR]"));

			ChatClient chatClient = chatClientBuilder // @formatter:off
				.defaultAdvisors(
					BestAgenticHarnessAdvisor.builder()
						.chatClientBuilder(judgeChatClientBuilder)
						.order(BaseAdvisor.HIGHEST_PRECEDENCE + 1000)
						.build(),
					new MyLoggingAdvisor(BaseAdvisor.LOWEST_PRECEDENCE + 2000, "[MAIN]"),
					new ChaosResponseAdvisor(BaseAdvisor.LOWEST_PRECEDENCE + 3000, 0.8))
				.build(); // @formatter:on

			var answer = chatClient.prompt("What is the capital of Bulgaria?").call().content();

			System.out.println(answer);
		};
	}

}
