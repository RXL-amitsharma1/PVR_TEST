package com.rxlogix.jasperserver.converters

import com.jaspersoft.jasperserver.dto.resources.ClientResource
import com.rxlogix.jasperserver.ClientTypeHelper
import com.rxlogix.jasperserver.Resource
import com.rxlogix.jasperserver.exception.IllegalParameterValueException
import com.rxlogix.jasperserver.exception.MandatoryParameterNotFoundException
import com.rxlogix.util.DateUtil
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder

import java.text.ParseException

public abstract class ResourceConverterImpl<ResourceType extends Resource, ClientType extends ClientResource<ClientType>>
        implements ResourceConverter<ResourceType, ClientType> {
    //protected ResourceFactory objectFactory

    //private PermissionsService permissionsService

    //private GenericTypeProcessorRegistry genericTypeProcessorRegistry

    private ServerResourceTypeExtractor serverResourceTypeExtractor

    private ClientTypeHelper<ClientType> clientTypeHelper

    //private ResourceValidator defaultValidator

    public ResourceConverterImpl() {
        clientTypeHelper = new ClientTypeHelper(this.getClass())
        serverResourceTypeExtractor = new ServerResourceTypeExtractor(this.getClass())
    }

    // object factory returns correct type of resource. So, cast below is safe
    @SuppressWarnings("unchecked")
    protected ResourceType getNewResourceInstance() {
        return Class.forName(getServerResourceType()).newInstance()
        //return (ResourceType) objectFactory.newResource(null, getServerResourceType())
    }

    public String getServerResourceType() {
        return serverResourceTypeExtractor.getServerResourceType()
    }

    public ResourceType toServer(ClientType clientObject, ToServerConversionOptions options) throws IllegalParameterValueException, MandatoryParameterNotFoundException {
        return toServer(clientObject, null, options)
    }

    public ResourceType toServer(ClientType clientObject, ResourceType resultToUpdate, ToServerConversionOptions options) throws IllegalParameterValueException, MandatoryParameterNotFoundException {
        ResourceType resource = genericFieldsToServer(clientObject, resultToUpdate, options)
        resource = resourceSpecificFieldsToServer(clientObject, resource, options)
        /*
        if (options != null && options.getAttachments() != null) {
            // impossible to build
            @SuppressWarnings("unchecked")
            AttachmentsProcessor<ResourceType> attachmentsProcessor = genericTypeProcessorRegistry.getTypeProcessor(getServerResourceType(), AttachmentsProcessor.class, false)
            if (attachmentsProcessor != null) {
                resource = attachmentsProcessor.processAttachments(resource, options.getAttachments())
            }
        }
        */
        if(options == null || !options.isSuppressValidation()){
            validateResource(resource)
        }
        return resource
    }

    protected void validateResource(ResourceType resource) {
        //ResourceValidator<ResourceType> validator = genericTypeProcessorRegistry.getTypeProcessor(resource.getResourceType(), ResourceValidator.class, false)
        //(validator != null ? validator : defaultValidator).validate(resource)
    }

    protected ResourceType genericFieldsToServer(ClientType clientObject, ResourceType resultToUpdate, ToServerConversionOptions options) throws IllegalParameterValueException, MandatoryParameterNotFoundException {
        if (resultToUpdate == null) {
            resultToUpdate = getNewResourceInstance()
            resultToUpdate.setVersion(Resource.VERSION_NEW)
        } else {
            resultToUpdate.setVersion(clientObject.getVersion() == null || (options != null && options.isResetVersion()) ?
                    Resource.VERSION_NEW : clientObject.getVersion())
        }
        resultToUpdate.setURIString(clientObject.getUri())
        if (clientObject.getCreationDate() != null) {
            try {
                resultToUpdate.setCreationDate(DateUtil.StringToDate(clientObject.getCreationDate(), DateUtil.ISO_DATE_TIME_FORMAT))
            } catch (ParseException ex) {
                throw new IllegalParameterValueException("creationDate", clientObject.getCreationDate())
            }
        }
        if (clientObject.getUpdateDate() != null) {
            try {
                resultToUpdate.setUpdateDate(DateUtil.StringToDate(clientObject.getUpdateDate(), DateUtil.ISO_DATE_TIME_FORMAT))
            } catch (ParseException ex) {
                throw new IllegalParameterValueException("updateDate", clientObject.getUpdateDate())
            }
        }
        resultToUpdate.setDescription(clientObject.getDescription())
        resultToUpdate.setLabel(clientObject.getLabel())
        return resultToUpdate
    }

    protected abstract ResourceType resourceSpecificFieldsToServer(ClientType clientObject, ResourceType resultToUpdate, ToServerConversionOptions options) throws IllegalParameterValueException, MandatoryParameterNotFoundException

    public ClientType toClient(ResourceType serverObject, ToClientConversionOptions options = null) {
        final ClientType client = genericFieldsToClient(getNewClientObjectInstance(), serverObject, options)
        return resourceSpecificFieldsToClient(client, serverObject, options)
    }

    protected ClientType genericFieldsToClient(ClientType client, ResourceType serverObject, ToClientConversionOptions options) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication()
        if (serverObject.getCreationDate() != null) {
            client.setCreationDate(DateUtil.toDateString(serverObject.getCreationDate(), DateUtil.ISO_DATE_TIME_FORMAT))
        }
        client.setDescription(serverObject.getDescription())
        client.setLabel(serverObject.getLabel())
        if (serverObject.getUpdateDate() != null) {
            client.setUpdateDate(DateUtil.toDateString(serverObject.getUpdateDate(), DateUtil.ISO_DATE_TIME_FORMAT))
        }
        client.setUri(serverObject.getURIString())
        client.setVersion(serverObject.getVersion())
        //client.setPermissionMask(permissionsService.getEffectivePermission(serverObject, authentication).getPermissionMask())
        return client
    }

    protected abstract ClientType resourceSpecificFieldsToClient(ClientType client, ResourceType serverObject, ToClientConversionOptions options)

    protected ClientType getNewClientObjectInstance() {
        return clientTypeHelper.getNewClientObjectInstance()
    }

    // Client object class is extracted from real implementation class by reflection. So, cast is safe.
    @SuppressWarnings("unchecked")
    public Class<ClientType> getClientTypeClass() {
        return clientTypeHelper.getClientClass()
    }
/*
    protected final ResourceReference findReference(List<ResourceReference> references, String uri) {
        ResourceReference result = null
        if (references != null) {
            for (ResourceReference reference : references) {
                if (reference.getTargetURI().equals(uri)) {
                    result = reference
                }
            }
        }
        return result
    }
*/
    public String getClientResourceType() {
        return clientTypeHelper.getClientResourceType()
    }
}
