package com.mesh_suite.exception;

public class MissingFormIdException extends IllegalArgumentException{
    public MissingFormIdException(String msg) {
        super(msg);
    }
}
