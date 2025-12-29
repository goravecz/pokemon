package com.accenture.pokemon.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Component
public class PokeApiLoggingInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(PokeApiLoggingInterceptor.class);

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        String path = request.getURI().getPath();
        String identifier = path.substring(path.lastIndexOf('/') + 1);
        LOG.info("Fetching Pokemon with identifier: {}", identifier);

        ClientHttpResponse response = execution.execute(request, body);
        if (LOG.isDebugEnabled()) {
            InputStreamReader inputStreamReader = new InputStreamReader(
                    response.getBody(), StandardCharsets.UTF_8);
            String bufferedBody = new BufferedReader(inputStreamReader).lines()
                    .collect(Collectors.joining("\n"));
            LOG.debug("Response body: {}", bufferedBody);
        }

        return response;
    }
}
