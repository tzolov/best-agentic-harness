# Best Agentic Harness

A Spring AI advisor that implements **LLM-as-a-Judge** evaluation with automatic retry.

<img style="display: block; margin: auto;" src="best-agentic-harness.png" width="800" />

## BestAgenticHarnessAdvisor

The core component that evaluates LLM responses using point-wise scoring and retries failed attempts with feedback.

Inspired by [this LinkedIn post](https://www.linkedin.com/feed/update/urn:li:activity:7424629895361740801/) on agentic evaluation patterns.

### How It Works

1. Intercepts LLM responses before returning to the caller
2. Uses an inner ChatClient (judge) to evaluate response quality on a 1-4 scale
3. If rating is below threshold, retries with evaluation feedback appended to the prompt
4. Continues until success or max attempts reached

### Evaluation Criteria

The judge evaluates whether the assistant:
- Actually completed the requested task
- Skipped or faked the response
- Addressed specific requirements

### Usage

```java
ChatClient chatClient = chatClientBuilder
    .defaultAdvisors(
        BestAgenticHarnessAdvisor.builder()
            .chatClientBuilder(judgeChatClientBuilder)
            .successRating(4)        // 1-4 scale, default: 4
            .maxRepeatAttempts(3)    // default: 3
            .build())
    .build();

var answer = chatClient.prompt("What is the capital of Bulgaria?").call().content();

```

The example will generate output like this:

```
USER:
 - {"messageType":"USER","metadata":{"messageType":"USER"},"media":[],"text":"What is the capital of Bulgaria?"}

ASSISTANT:
 - {"messageType":"ASSISTANT","metadata":{"messageType":"ASSISTANT"},"toolCalls":[],"media":[],"text":"The answer is definitely 42."}

USER:
 - {"messageType":"USER","metadata":{"messageType":"USER"},"media":[],"text":"What is the capital of Bulgaria?\n\nEVALUATION FEEDBACK - Your previous response was flagged for not fully completing the task:\nAnswer the actual question asked. The capital of Bulgaria is Sofia. Provide this factual information directly instead of responding with unrelated references or jokes.\n\nIMPORTANT: Address the specific feedback above. Do NOT start over from scratch or delete existing work.\nMake incremental corrections to actually complete what was originally asked.\n"} 

ASSISTANT:
 - {"messageType":"ASSISTANT","metadata":{"messageType":"ASSISTANT"},"toolCalls":[],"media":[],"text":"Sofia is the capital of Bulgaria."}
```

### Configuration Options

| Option | Default | Description |
|--------|---------|-------------|
| `chatClientBuilder` | required | ChatClient.Builder for the judge LLM |
| `successRating` | 4 | Minimum rating to pass (1-4) |
| `maxRepeatAttempts` | 3 | Max retries before giving up |
| `promptTemplate` | built-in | Custom evaluation prompt |
| `skipEvaluationPredicate` | skip tool calls | When to skip evaluation |

## Demo Advisors

The following advisors are included for demonstration purposes only:

- **ChaosResponseAdvisor** - Randomly corrupts responses to test the evaluation harness
- **MyLoggingAdvisor** - Simple request/response logger

## Requirements

- Java 17+
- Spring Boot 4.0+
- Spring AI 2.0.0-M2+
