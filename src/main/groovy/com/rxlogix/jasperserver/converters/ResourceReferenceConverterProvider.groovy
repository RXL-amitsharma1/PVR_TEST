package com.rxlogix.jasperserver.converters

import com.jaspersoft.jasperserver.dto.resources.ClientReferenceable

public class ResourceReferenceConverterProvider {
    def resourceConverterProvider
    def repositoryService

    public <T extends ClientReferenceable> ResourceReferenceConverter<T> getConverterForType(Class<T> referenceableClass) {
        return new ResourceReferenceConverter(resourceConverterProvider, repositoryService)
    }
}
