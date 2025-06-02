package com.rxlogix.jasperserver.exception

import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor

public class AccessDeniedException extends RemoteException {

    public static final String ERROR_CODE_ACCESS_DENIED = "access.denied"

    public AccessDeniedException(ErrorDescriptor errorDescriptor){
        super(errorDescriptor)
        this.getErrorDescriptor().setErrorCode(ERROR_CODE_ACCESS_DENIED)
    }

    public AccessDeniedException(String message) {
        super(message)
        this.getErrorDescriptor().setErrorCode(ERROR_CODE_ACCESS_DENIED)
    }

    public AccessDeniedException(String message, String... parameters) {
        super(message)
        setErrorDescriptor(new ErrorDescriptor().setMessage(message).setErrorCode(ERROR_CODE_ACCESS_DENIED).setParameters(parameters))
    }
}