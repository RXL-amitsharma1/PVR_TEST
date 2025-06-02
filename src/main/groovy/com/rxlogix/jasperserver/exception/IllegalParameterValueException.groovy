package com.rxlogix.jasperserver.exception

import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor

public class IllegalParameterValueException extends RemoteException {
    public static final String ERROR_CODE  = "illegal.parameter.value.error"

    public IllegalParameterValueException(String parameterName, String parameterValue) {
        this("Value of parameter " + (parameterName != null ? "'" + parameterName + "'" : "") + " invalid", parameterName, parameterValue)
    }

    public IllegalParameterValueException(String message, String... parameters) {
        super(message)
        setErrorDescriptor(new ErrorDescriptor().setMessage(message).setErrorCode(ERROR_CODE).setParameters(parameters))
    }

    public IllegalParameterValueException(ErrorDescriptor errorDescriptor){
        super(errorDescriptor)
    }
}
