package com.rxlogix.jasperserver.converters

import com.jaspersoft.jasperserver.dto.resources.ClientFolder
import com.rxlogix.jasperserver.Folder
import com.rxlogix.jasperserver.exception.IllegalParameterValueException

public class FolderResourceConverter extends ResourceConverterImpl<Folder, ClientFolder> {


    @Override
    protected Folder resourceSpecificFieldsToServer(ClientFolder clientObject, Folder resultToUpdate, ToServerConversionOptions options) throws IllegalParameterValueException {
        // currently no folder specific conversion
        return resultToUpdate
    }

    @Override
    protected ClientFolder resourceSpecificFieldsToClient(ClientFolder client, Folder serverObject, ToClientConversionOptions options) {
        // currently no folder specific conversion
        return client
    }
}
