package com.serviceonwheels.auth_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ServiceRequestNotFoundException extends RuntimeException {

    public ServiceRequestNotFoundException(String message) {
        super(message);
    }
}
