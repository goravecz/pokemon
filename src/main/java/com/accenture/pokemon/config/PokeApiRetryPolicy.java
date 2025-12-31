package com.accenture.pokemon.config;

import org.springframework.retry.RetryContext;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

/**
 * Custom retry policy for PokeAPI calls.
 * Retries on:
 * - 5xx server errors
 * - 429 (rate limit)
 * - 408 (timeout)
 * - Network/connection errors
 */
public class PokeApiRetryPolicy extends SimpleRetryPolicy {

    public PokeApiRetryPolicy(int maxAttempts) {
        super(maxAttempts);
    }

    @Override
    public boolean canRetry(RetryContext context) {
        Throwable lastThrowable = context.getLastThrowable();
        
        if (lastThrowable == null) {
            return super.canRetry(context);
        }

        if (lastThrowable instanceof ResourceAccessException) {
            return super.canRetry(context);
        }

        if (lastThrowable instanceof HttpServerErrorException) {
            return super.canRetry(context);
        }

        if (lastThrowable instanceof HttpClientErrorException clientError) {
            int statusCode = clientError.getStatusCode().value();
            if (statusCode == 429 || statusCode == 408) {
                return super.canRetry(context);
            }
        }

        return false;
    }
}
