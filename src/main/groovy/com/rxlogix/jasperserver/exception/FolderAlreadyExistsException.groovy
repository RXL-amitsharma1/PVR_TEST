package com.rxlogix.jasperserver.exception

import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor

public class FolderAlreadyExistsException extends ResourceAlreadyExistsException {
    public final String FOLDER_ALREADY_EXISTS = "folder.already.exits"

    public FolderAlreadyExistsException(String newUri, String existingUri){
        super(new ErrorDescriptor()
                .setMessage(String.format("The folder %s cannot be created, because folder %s already exists", newUri, existingUri))
                .setParameters(newUri, existingUri))
        this.getErrorDescriptor().setErrorCode(FOLDER_ALREADY_EXISTS)
    }
}
