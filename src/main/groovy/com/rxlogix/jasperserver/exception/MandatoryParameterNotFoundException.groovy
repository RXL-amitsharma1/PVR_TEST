package com.rxlogix.jasperserver.exception

import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor

public class MandatoryParameterNotFoundException extends RemoteException {
    public final static String MANDATORY_PARAMETER_ERROR = "mandatory.parameter.error"

    public MandatoryParameterNotFoundException(String parameterName){
        this("mandatory parameter " + (parameterName != null ? "'" + parameterName + "'" : "") +" not found", parameterName)
    }

    public MandatoryParameterNotFoundException(String message, String... parameters){
        super()
        setErrorDescriptor(new ErrorDescriptor().setMessage(message).setErrorCode(MANDATORY_PARAMETER_ERROR).setParameters(parameters))
    }
}
