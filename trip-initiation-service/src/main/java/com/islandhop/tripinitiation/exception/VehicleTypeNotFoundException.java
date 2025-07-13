package com.islandhop.tripinitiation.exception;

public class VehicleTypeNotFoundException extends RuntimeException {
    public VehicleTypeNotFoundException(String message) {
        super(message);
    }
}