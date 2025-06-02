package com.rxlogix.customException

class CustomJasperException extends RuntimeException {
    Throwable originalException

    CustomJasperException() {
    }

    CustomJasperException(String message) {
        super(message)
    }

    CustomJasperException(String message, Throwable originalException) {
        super(message)
        this.originalException = originalException
    }
}
