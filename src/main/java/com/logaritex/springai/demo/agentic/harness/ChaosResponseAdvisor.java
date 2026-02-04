package com.logaritex.springai.demo.agentic.harness;

import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;

/**
 * Advisor that randomly corrupts AI responses with nonsensical text.
 * Useful for testing system resilience and error handling.
 *
 * @author Christian Tzolov
 */
public class ChaosResponseAdvisor implements BaseAdvisor {

	private static final Logger logger = LoggerFactory.getLogger(ChaosResponseAdvisor.class);

	private static final String[] RANDOM_RESPONSES = {
		"The answer is definitely 42.",
		"I'm sorry, I was distracted by a butterfly.",
		"Have you tried turning it off and on again?",
		"The quick brown fox jumps over the lazy dog.",
		"According to my calculations... beep boop... error.",
		"I think the answer you're looking for is: banana.",
		"Let me consult my crystal ball... unclear, ask again later.",
		"The mitochondria is the powerhouse of the cell."
	};

	private final int order;

	private final double corruptionProbability;

	private final Random random;

	/**
	 * Creates advisor with 50% corruption probability.
	 * @param order the advisor order
	 */
	public ChaosResponseAdvisor(int order) {
		this(order, 0.5);
	}

	/**
	 * Creates advisor with specified corruption probability.
	 * @param order the advisor order
	 * @param corruptionProbability probability (0.0-1.0) of corrupting a response
	 */
	public ChaosResponseAdvisor(int order, double corruptionProbability) {
		this.order = order;
		this.corruptionProbability = corruptionProbability;
		this.random = new Random();
	}

	@Override
	public int getOrder() {
		return this.order;
	}

	@Override
	public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
		return chatClientRequest;
	}

	@Override
	public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
		if (this.random.nextDouble() < this.corruptionProbability) {
			logger.info("Corrupting response with random text");
			return corruptResponse(chatClientResponse);
		}
		return chatClientResponse;
	}

	private ChatClientResponse corruptResponse(ChatClientResponse chatClientResponse) {
		ChatResponse originalChatResponse = chatClientResponse.chatResponse();
		if (originalChatResponse == null || originalChatResponse.getResult() == null) {
			return chatClientResponse;
		}

		String randomText = RANDOM_RESPONSES[this.random.nextInt(RANDOM_RESPONSES.length)];

		AssistantMessage corruptedMessage = new AssistantMessage(randomText);
		Generation corruptedGeneration = new Generation(corruptedMessage,
				originalChatResponse.getResult().getMetadata());

		ChatResponse corruptedChatResponse = ChatResponse.builder()
			.from(originalChatResponse)
			.generations(List.of(corruptedGeneration))
			.build();

		return chatClientResponse.mutate()
			.chatResponse(corruptedChatResponse)
			.build();
	}

}