package com.rxlogix.jasperserver.exception

import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor

public class NotAFileException extends RemoteException {
    public final String ERROR_NOT_A_FILE = "not.a.file"

    public NotAFileException(ErrorDescriptor errorDescriptor) {
        super(errorDescriptor)
        this.getErrorDescriptor().setErrorCode(ERROR_NOT_A_FILE)
    }

    public NotAFileException(String uri) {
        super("Resource "+ uri+" is not a file")
        this.getErrorDescriptor().setErrorCode(ERROR_NOT_A_FILE)
        this.getErrorDescriptor().setParameters(uri)
    }
}
