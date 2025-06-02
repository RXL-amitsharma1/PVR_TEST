package com.rxlogix.jasperserver.converters

import com.jaspersoft.jasperserver.dto.resources.AbstractClientDataSourceHolder
import com.jaspersoft.jasperserver.dto.resources.ClientReferenceableDataSource
import com.rxlogix.jasperserver.Resource
import com.rxlogix.jasperserver.ResourceReference
import com.rxlogix.jasperserver.exception.IllegalParameterValueException
import com.rxlogix.jasperserver.exception.MandatoryParameterNotFoundException

public abstract class DataSourceHolderResourceConverter<ResourceType extends Resource, ClientType extends AbstractClientDataSourceHolder<ClientType>>
        extends ResourceConverterImpl<ResourceType, ClientType> {
    def resourceReferenceConverterProvider

    protected abstract void setDataSourceToResource(ResourceReference dataSourceReference, ResourceType resource)

    protected abstract ResourceReference getDataSourceFromResource(ResourceType resource)

    @Override
    protected ResourceType genericFieldsToServer(ClientType clientObject, ResourceType resultToUpdate, ToServerConversionOptions options)
            throws IllegalParameterValueException, MandatoryParameterNotFoundException {
        resultToUpdate = super.genericFieldsToServer(clientObject, resultToUpdate, options)
        ResourceReference dataSourceReference = resourceReferenceConverterProvider
                .getConverterForType(ClientReferenceableDataSource.class)
                .toServer(clientObject.getDataSource(), getDataSourceFromResource(resultToUpdate), options)
        setDataSourceToResource(dataSourceReference, resultToUpdate)
        return resultToUpdate
    }

    @Override
    protected ClientType genericFieldsToClient(ClientType client, ResourceType serverObject, ToClientConversionOptions options) {
        final ResourceReference dataSource = getDataSourceFromResource(serverObject)
        client.setDataSource(resourceReferenceConverterProvider.getConverterForType(ClientReferenceableDataSource.class)
                .toClient(dataSource, options))
        return super.genericFieldsToClient(client, serverObject, options)
    }
}
