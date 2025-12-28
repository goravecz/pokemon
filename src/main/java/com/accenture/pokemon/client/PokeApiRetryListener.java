package com.accenture.pokemon.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.stereotype.Component;

@Component
public class PokeApiRetryListener implements RetryListener {

    private static final Logger LOG = LoggerFactory.getLogger(PokeApiRetryListener.class);

    @Override
    public <T, E extends Throwable> boolean open(
            RetryContext context,
            RetryCallback<T, E> callback
    ) {
        return true;
    }

    @Override
    public <T, E extends Throwable> void onError(
            RetryContext context,
            RetryCallback<T, E> callback,
            Throwable throwable
    ) {
        LOG.warn(
                "Retry attempt {} failed: {}",
                context.getRetryCount(),
                throwable.getMessage()
        );
    }

    @Override
    public <T, E extends Throwable> void close(
            RetryContext context,
            RetryCallback<T, E> callback,
            Throwable throwable
    ) {
        if (throwable != null) {
            LOG.error(
                    "Retries exhausted after {} attempts",
                    context.getRetryCount()
            );
        }
    }
}
