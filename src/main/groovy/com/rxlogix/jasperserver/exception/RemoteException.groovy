package com.rxlogix.jasperserver.exception

//import com.jaspersoft.jasperserver.api.common.error.handling.SecureExceptionHandler
import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor

public class RemoteException extends RuntimeException {

    private ErrorDescriptor errorDescriptor

    public RemoteException() {
        super()
        this.errorDescriptor = new ErrorDescriptor()
    }

    public RemoteException(String message) {
        super(message)
        this.errorDescriptor = new ErrorDescriptor().setMessage(message)
    }

    public RemoteException(String message, Throwable cause) {
        super(message, cause)
        this.errorDescriptor = new ErrorDescriptor().setMessage(message)
    }
/*
    public RemoteException(Throwable cause, SecureExceptionHandler exceptionHandler) {
        super(cause)
        this.errorDescriptor = exceptionHandler.handleException(cause)
    }
*/
    public RemoteException(ErrorDescriptor errorDescriptor){
        super(errorDescriptor.getMessage())
        this.errorDescriptor = errorDescriptor
    }

    public RemoteException(ErrorDescriptor errorDescriptor, Throwable e){
        super(errorDescriptor.getMessage(), e)
        this.errorDescriptor = errorDescriptor
    }

    public ErrorDescriptor getErrorDescriptor() {
        return errorDescriptor
    }

    public void setErrorDescriptor(ErrorDescriptor errorDescriptor) {
        this.errorDescriptor = errorDescriptor
    }

    public Boolean isUnexpected(){
        return getErrorDescriptor() != null && ErrorDescriptor.ERROR_CODE_UNEXPECTED_ERROR.equals(getErrorDescriptor().getErrorCode())
    }
}
