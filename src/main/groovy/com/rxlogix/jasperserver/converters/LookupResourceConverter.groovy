package com.rxlogix.jasperserver.converters

import com.jaspersoft.jasperserver.dto.resources.ClientResourceLookup
import com.rxlogix.jasperserver.ResourceLookup
import com.rxlogix.jasperserver.exception.IllegalParameterValueException

public class LookupResourceConverter extends ResourceConverterImpl<ResourceLookup, ClientResourceLookup> {
    ResourceConverterProvider resourceConverterProvider

/*
    @Override
    public ResourceDetails toServer(ClientResourceLookup clientObject, ResourceLookup resultToUpdate, ToServerConversionOptions options) {
        throw new IllegalStateException("ResourceLookup is read only object. ToServer conversion isn't supported")
    }
*/
    @Override
    protected ResourceLookup resourceSpecificFieldsToServer(ClientResourceLookup clientObject, ResourceLookup resultToUpdate, ToServerConversionOptions options) throws IllegalParameterValueException {
        throw new IllegalStateException("ResourceLookup is read only object. ToServer conversion isn't supported")
    }

    @Override
    public ResourceLookup toServer(ClientResourceLookup clientObject, ToServerConversionOptions options) {
        throw new IllegalStateException("ResourceLookup is read only object. ToServer conversion isn't supported")
    }

    @Override
    protected ClientResourceLookup resourceSpecificFieldsToClient(ClientResourceLookup client, ResourceLookup serverObject, ToClientConversionOptions options) {
        client.setResourceType(toClientResourceType(serverObject.getResourceType()))
        return client
    }

    protected String toClientResourceType(String serverResourceType){
        String clientType
        try {
            clientType = resourceConverterProvider.getToClientConverter(serverResourceType).getClientResourceType()
        } catch (IllegalParameterValueException e){
            // no converter for this serverResourceType
            clientType = "unknown"
        }
        return clientType
    }
}
