package com.islandhop.tripinit.exception;

public class VehicleTypeNotFoundException extends RuntimeException {
    public VehicleTypeNotFoundException(String message) {
        super(message);
    }
}