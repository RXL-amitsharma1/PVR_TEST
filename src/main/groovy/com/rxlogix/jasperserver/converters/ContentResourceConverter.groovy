package com.rxlogix.jasperserver.converters

import com.jaspersoft.jasperserver.dto.resources.ClientFile
import com.rxlogix.jasperserver.ContentResource
import com.rxlogix.jasperserver.exception.IllegalParameterValueException

import javax.xml.bind.DatatypeConverter;

public class ContentResourceConverter extends ResourceConverterImpl<ContentResource, ClientFile>{

    @Override
    protected ContentResource resourceSpecificFieldsToServer(ClientFile clientObject, ContentResource resultToUpdate, ToServerConversionOptions options) throws IllegalParameterValueException {
        if(resultToUpdate.getFileType() != null &&
                (!resultToUpdate.getFileType().equals(clientObject.getType().name()) &&
                        !(ContentResource.TYPE_UNSPECIFIED.equals(resultToUpdate.getFileType()) && clientObject.getType().equals(ClientFile.FileType.unspecified)))){
            // it's not allowed to change resource type
            throw new IllegalParameterValueException("type", clientObject.getType().name());
        }
        if (ClientFile.FileType.unspecified.equals(clientObject.getType())){
            resultToUpdate.setFileType(ContentResource.TYPE_UNSPECIFIED);
        } else {
            resultToUpdate.setFileType(clientObject.getType().name());
        }

        if (clientObject.getContent() != null && !"".equals(clientObject.getContent())) {
            try {
                resultToUpdate.setData(DatatypeConverter.parseBase64Binary(clientObject.getContent()));
            } catch (IllegalArgumentException e) {
                throw new IllegalParameterValueException("content", "");
            }
        }

        return resultToUpdate;
    }

    @Override
    protected ClientFile resourceSpecificFieldsToClient(ClientFile client, ContentResource serverObject, ToClientConversionOptions options) {
        if (ContentResource.TYPE_UNSPECIFIED.equals(serverObject.getFileType())){
            client.setType(ClientFile.FileType.unspecified);
        } else {
            client.setType(ClientFile.FileType.valueOf(serverObject.getFileType()));
        }
        return client;
    }
}
