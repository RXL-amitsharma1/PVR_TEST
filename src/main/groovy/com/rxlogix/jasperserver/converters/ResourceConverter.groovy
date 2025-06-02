package com.rxlogix.jasperserver.converters
        
import com.jaspersoft.jasperserver.dto.resources.ClientResource
import com.rxlogix.jasperserver.Resource;

public interface ResourceConverter <ResourceType extends Resource, ClientType extends ClientResource<ClientType>>
        extends ToClientConverter<ResourceType, ClientType, ToClientConversionOptions>,
                ToServerConverter<ClientType, ResourceType, ToServerConversionOptions> {
    Class<ClientType> getClientTypeClass();
}
