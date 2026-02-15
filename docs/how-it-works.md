# How It Works

Understand the LLM-as-a-Judge evaluation and retry flow.

## Overview

The `BestAgenticHarnessAdvisor` acts as a Spring AI advisor that sits between your application and the LLM. It intercepts responses before they reach the caller and evaluates their quality using a separate judge LLM.

## Evaluation Flow

1. **Intercept the response** — The advisor intercepts the LLM response before it is returned to the caller.
2. **Evaluate with the judge** — A separate inner `ChatClient` (the judge) evaluates the response quality on a **1–4 scale**.
3. **Check the rating** — If the rating meets or exceeds the configured `successRating` threshold, the response is returned as-is.
4. **Retry with feedback** — If the rating is below threshold, the evaluation feedback is appended to the original prompt and the request is retried.
5. **Repeat or return** — The process continues until the response passes evaluation or `maxRepeatAttempts` is reached.

## Evaluation Criteria

The judge evaluates whether the assistant:

- **Actually completed the requested task** — Did it answer what was asked?
- **Skipped or faked the response** — Did it give a placeholder or irrelevant answer?
- **Addressed specific requirements** — Did it cover all parts of the request?

## Rating Scale

| Rating | Meaning |
|--------|---------|
| 1 | Response is completely wrong or irrelevant |
| 2 | Response partially addresses the question |
| 3 | Response is mostly correct but incomplete |
| 4 | Response fully and correctly addresses the question |

!!! note

    By default, the advisor requires a rating of **4** to pass. You can lower this threshold via the `successRating` configuration option.
