package com.rakutenmobile.userapi.user.adapter.in.restful.reactive.exception;

import com.rakutenmobile.openapi.models.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
public class DefaultGlobalExceptionHandler extends AbstractErrorWebExceptionHandler {
    private final Map<Class<? extends Exception>, HttpStatus> exceptionToStatusCode;
    private final HttpStatus defaultStatus;

    public DefaultGlobalExceptionHandler(ErrorAttributes errorAttributes,
                                         WebProperties.Resources resources,
                                         ApplicationContext applicationContext,
                                         Map<Class<? extends Exception>, HttpStatus> exceptionToStatusCode,
                                         HttpStatus defaultStatus
                                         ) {
        super(errorAttributes, resources, applicationContext);
        this.exceptionToStatusCode = exceptionToStatusCode;
        this.defaultStatus = defaultStatus;
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }
    private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        Throwable error = getError(request);
        log.error("An error has been occurred", error.getMessage());
        HttpStatus httpStatus;
        if (error instanceof Exception exception) {
            httpStatus = exceptionToStatusCode.getOrDefault(exception.getClass(), defaultStatus);
        }
        else {
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        ErrorResponse dto = new ErrorResponse();
        dto.setErrorCode(httpStatus.value());
        if (httpStatus.value() == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            dto.setErrorMessage("something bad happen. please contact developers");
        }else {
            dto.setErrorMessage(error.getMessage());
        }

        return ServerResponse
                .status(httpStatus)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(dto));
    }
}
