package com.rxlogix.jasperserver.exception

//import com.jaspersoft.jasperserver.api.common.error.handling.SecureExceptionHandler
import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor

public class ResourceNotFoundException extends RemoteException {
    public static final String ERROR_CODE_RESOURCE_NOT_FOUND = "resource.not.found"
    public ResourceNotFoundException() {
        super()
        getErrorDescriptor().setErrorCode(ERROR_CODE_RESOURCE_NOT_FOUND)
    }

    public ResourceNotFoundException(String message) {
        super("Resource " + message + " not found")
        ErrorDescriptor errorDescriptor = getErrorDescriptor()
        errorDescriptor.setErrorCode(ERROR_CODE_RESOURCE_NOT_FOUND)
        errorDescriptor.setParameters(message)
    }

    public ResourceNotFoundException(String message, String id) {
        super(message)
        ErrorDescriptor errorDescriptor = getErrorDescriptor()
        errorDescriptor.setErrorCode(ERROR_CODE_RESOURCE_NOT_FOUND)
        errorDescriptor.setParameters(id)
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super("Resource " + message + " not found", cause)
        getErrorDescriptor().setErrorCode(ERROR_CODE_RESOURCE_NOT_FOUND)
        ErrorDescriptor errorDescriptor = getErrorDescriptor()
        errorDescriptor.setParameters(message)
    }
/*
    public ResourceNotFoundException(Throwable cause, SecureExceptionHandler exceptionHandler) {
        super(cause, exceptionHandler)
        getErrorDescriptor().setErrorCode(ERROR_CODE_RESOURCE_NOT_FOUND)
    }
*/
    public ResourceNotFoundException(ErrorDescriptor errorDescriptor){
        super(errorDescriptor)
    }
}
