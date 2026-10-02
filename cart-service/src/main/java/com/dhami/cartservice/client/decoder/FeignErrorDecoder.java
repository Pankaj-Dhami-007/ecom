package com.dhami.cartservice.client.decoder;

import com.dhami.cartservice.cart.CartServiceException;
import com.dhami.cartservice.common.exception.AccessDeniedException;
import com.dhami.cartservice.common.exception.ResourceNotFoundException;
import com.dhami.cartservice.common.exception.UnauthenticatedException;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;

public class FeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        HttpStatus status = HttpStatus.valueOf(response.status());

        String body = extractBody(response);

        if (status == HttpStatus.NOT_FOUND) {
            return new ResourceNotFoundException(body != null ? body : "Requested resource not found");
        }

        if (status == HttpStatus.UNAUTHORIZED) {
            return new UnauthenticatedException("Downstream service rejected the request");
        }

        if (status == HttpStatus.FORBIDDEN) {
            return new AccessDeniedException(body != null ? body : "Downstream service denied the request");
        }

        if (status == HttpStatus.BAD_REQUEST) {
            return new CartServiceException(body != null ? body : "Downstream service: Bad request");
        }

        if (status.is5xxServerError()) {
            return new CartServiceException(body != null ? body : "Downstream service is currently unavailable");
        }

        return defaultDecoder.decode(methodKey, response);
    }

    private String extractBody(Response response) {
        try {
            if (response.body() != null) {
                byte[] bodyBytes = Util.toByteArray(response.body().asInputStream());
                return new String(bodyBytes, StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }
}