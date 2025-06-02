package com.rxlogix.jasperserver.converters

import com.jaspersoft.jasperserver.dto.resources.ClientFile
import com.rxlogix.jasperserver.FileResource
import com.rxlogix.jasperserver.exception.IllegalParameterValueException

import javax.xml.bind.DatatypeConverter

class FileResourceConverter extends ResourceConverterImpl<FileResource, ClientFile> {
    def repositoryService

    @Override
    protected FileResource resourceSpecificFieldsToServer(ClientFile clientObject, FileResource resultToUpdate, ToServerConversionOptions options) throws IllegalParameterValueException {
        if(resultToUpdate.getFileType() != null || resultToUpdate.isReference()){
            String type
            if (resultToUpdate.isReference()){
                FileResource referenced = (FileResource)repositoryService.getResource(null, resultToUpdate.getReferenceURI())
                type = referenced.getFileType()
            } else {
                type = resultToUpdate.getFileType()
            }

            if (!type.equals(clientObject.getType().name())){
                // it's not allowed to change resource type
                throw new IllegalParameterValueException("type", clientObject.getType().name())
            }
        }

        if (!resultToUpdate.isReference()){
            resultToUpdate.setFileType(clientObject.getType().name())
        }

        if (clientObject.getContent() != null && !"".equals(clientObject.getContent())) {
            try {
                resultToUpdate.setData(DatatypeConverter.parseBase64Binary(clientObject.getContent()))
            } catch (IllegalArgumentException e) {
                throw new IllegalParameterValueException("content", "")
            }
            resultToUpdate.setReferenceURI(null)
            resultToUpdate.setFileType(clientObject.getType().name())
        }
        return resultToUpdate
    }

    @Override
    protected ClientFile resourceSpecificFieldsToClient(ClientFile client, FileResource serverObject, ToClientConversionOptions options) {
        String type
        if (serverObject.isReference()){
            FileResource referenced = (FileResource)repositoryService.getResource(null, serverObject.getReferenceURI())
            type = referenced.getFileType()
        } else {
            type = serverObject.getFileType()
        }
        ClientFile.FileType clientFileType
        try{
            clientFileType = ClientFile.FileType.valueOf(type)
        }catch (Exception e){
            // if no appropriate client type in an enum, then let it be unspecified
            clientFileType  = ClientFile.FileType.unspecified
        }
        client.setType(clientFileType)
        return client
    }
}
