package com.rxlogix.jasperserver.converters

import com.rxlogix.jasperserver.GenericParametersHelper

public class ServerResourceTypeExtractor {
    private String serverResourceType

    private final Class<? extends ToServerConverter> converterClass

    public ServerResourceTypeExtractor(Class<? extends ToServerConverter> converterClass){
        this.converterClass = converterClass
    }

    public String getServerResourceType() {
        if (serverResourceType == null) {
            final Class<?> serverResourceTypeClass = GenericParametersHelper.getGenericTypeArgument(converterClass,
                    ToServerConverter.class, 1)
            if (serverResourceTypeClass != null) {
                serverResourceType = serverResourceTypeClass.getName()
            } else {
                throw new IllegalStateException("Unable to identify serverResourceType. It can happen because " +
                        converterClass.getName() + " is raw implementation of " + ToServerConverter.class.getName())
            }
        }
        return serverResourceType
    }
}
