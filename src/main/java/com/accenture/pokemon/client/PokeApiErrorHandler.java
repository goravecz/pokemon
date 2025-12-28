package com.accenture.pokemon.client;

import com.accenture.pokemon.exception.RetryablePokeApiException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;
import java.net.URI;

@Component
public class PokeApiErrorHandler implements ResponseErrorHandler {

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().isError();    }

    @Override
    public void handleError(URI url, HttpMethod method, ClientHttpResponse response) throws IOException {
        HttpStatusCode status = response.getStatusCode();

        if (status.is4xxClientError()) {
            throw new HttpClientErrorException(status);
        }

        if (status.is5xxServerError()) {
            throw new RetryablePokeApiException(
                    new HttpServerErrorException(status)
            );
        }
    }
}
