package com.aldisued.iot.monitoring.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class SensorAlreadyExistsException extends RuntimeException {
    public SensorAlreadyExistsException(String name) {
        super("A sensor with the same name '" + name + "' already exists.");
    }
}
