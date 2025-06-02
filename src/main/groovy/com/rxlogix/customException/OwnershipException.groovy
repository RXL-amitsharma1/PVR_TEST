package com.rxlogix.customException

import grails.validation.ValidationException
import org.springframework.validation.Errors


class OwnershipException extends ValidationException {

    String className
    String name

    public OwnershipException (String className, String name, String msg, Errors e) {
        super(msg,e)
        this.className = className
        this.name = name
    }

}
