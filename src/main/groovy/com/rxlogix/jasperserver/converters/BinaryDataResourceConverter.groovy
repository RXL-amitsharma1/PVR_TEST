package com.rxlogix.jasperserver.converters

import com.jaspersoft.jasperserver.dto.resources.ClientFile
import com.rxlogix.jasperserver.ClientTypeHelper
import com.rxlogix.jasperserver.FileResource
import com.rxlogix.jasperserver.Resource
import com.rxlogix.jasperserver.exception.IllegalParameterValueException
import com.rxlogix.jasperserver.exception.MandatoryParameterNotFoundException
import org.apache.velocity.runtime.resource.ContentResource

public class BinaryDataResourceConverter implements ResourceConverter<Resource, ClientFile> {
    def fileResourceTypes = [
            "font",
            "jrxml",
            "jar",
            "prop",
            "jrtx",
            "xml",
            "json",
            "css",
            "accessGrantSchema",
            "olapMondrianSchema",
            "dashboardComponent",
            "img"
    ]
    ResourceConverter<Resource, ClientFile> fileResourceConverter
    ResourceConverter<Resource, ClientFile> contentResourceConverter

    public Class<ClientFile> getClientTypeClass() {
        return ClientFile.class
    }

    public ClientFile toClient(Resource serverObject, ToClientConversionOptions options) {
        if (serverObject instanceof FileResource) {
            return fileResourceConverter.toClient(serverObject, options)
        } else if (serverObject instanceof ContentResource) {
            return contentResourceConverter.toClient(serverObject, options)
        } else {
            throw new IllegalStateException(getClass().getName() + " can't process server object of type " + serverObject.getResourceType())
        }
    }

    public String getClientResourceType() {
        return ClientTypeHelper.extractClientType(getClientTypeClass())
    }

    public Resource toServer(ClientFile clientObject, ToServerConversionOptions options) throws IllegalParameterValueException, MandatoryParameterNotFoundException {
        return toServer(clientObject, fileResourceTypes.contains(clientObject.getType().toString()) ? new FileResource() : new ContentResource(), options)
    }

    public Resource toServer(ClientFile clientObject, Resource resultToUpdate, ToServerConversionOptions options) throws IllegalParameterValueException, MandatoryParameterNotFoundException {
        if (resultToUpdate instanceof FileResource || (resultToUpdate == null && fileResourceTypes.contains(clientObject.getType().name()))) {
            return fileResourceConverter.toServer(clientObject, resultToUpdate, options)
        } else {
            return contentResourceConverter.toServer(clientObject, resultToUpdate, options)
        }
    }

    public String getServerResourceType() {
        return null
    }
}
