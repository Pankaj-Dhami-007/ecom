package com.dhami.ecom.order_service.client.decoder;

import com.dhami.ecom.order_service.order.OrderServiceException;
import com.dhami.ecom.order_service.common.exception.ResourceNotFoundException;
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
            return new ResourceNotFoundException(body != null ? body : "Requested product not found");
        }

        if (status == HttpStatus.BAD_REQUEST) {
            return new OrderServiceException(body != null ? body : "Product service: Bad request");
        }

        if (status.is5xxServerError()) {
            return new OrderServiceException(body != null ? body : "Product service is currently unavailable");
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
