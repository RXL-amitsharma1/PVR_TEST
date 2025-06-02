package com.rxlogix.customException;

public class ParameterMissingException extends IllegalArgumentException{

    ParameterMissingException(String message){
        super(message)
    }

}
