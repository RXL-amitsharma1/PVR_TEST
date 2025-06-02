package com.rxlogix.jasperserver.exception

import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor

public class ResourceAlreadyExistsException extends RemoteException{
    public ResourceAlreadyExistsException(String resourceName){
        this("Resource " + (resourceName != null ? "'" + resourceName + "'" : "") +" already exists", resourceName)
    }
    
    public ResourceAlreadyExistsException(String message, String ... parameters){
        super(new ErrorDescriptor().setMessage(message).setErrorCode("resource.already.exists").setParameters(parameters))
    }

    public ResourceAlreadyExistsException(ErrorDescriptor errorDescriptor){
            super(errorDescriptor)
    }
}
