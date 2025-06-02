package com.rxlogix.jasperserver.converters

import com.jaspersoft.jasperserver.dto.resources.ClientReference
import com.jaspersoft.jasperserver.dto.resources.ClientReferenceable
import com.jaspersoft.jasperserver.dto.resources.ClientResource
import com.jaspersoft.jasperserver.dto.resources.ClientUriHolder
import com.rxlogix.jasperserver.*
import com.rxlogix.jasperserver.exception.IllegalParameterValueException
import com.rxlogix.jasperserver.exception.MandatoryParameterNotFoundException
import org.springframework.security.access.AccessDeniedException

public class ResourceReferenceConverter<T extends ClientReferenceable> {
    protected final ResourceConverterProvider resourceConverterProvider
    protected final RepositoryService repositoryService
    //protected final List<ClientReferenceRestriction> restrictions = new ArrayList<ClientReferenceRestriction>()
    //protected ConfigurationBean configurationBean
    //protected PermissionsService permissionsService

    public ResourceReferenceConverter(ResourceConverterProvider resourceConverterProvider,
            RepositoryService repositoryService
                                      //, PermissionsService permissionsService, ConfigurationBean configurationBean, ClientReferenceRestriction... restriction
    ) {
        this.repositoryService = repositoryService
        //this.permissionsService = permissionsService
        this.resourceConverterProvider = resourceConverterProvider
        //if (restriction != null) {
        //    restrictions.addAll(Arrays.asList(restriction))
        //}
        //this.configurationBean = configurationBean
    }
/*
    public ResourceReferenceConverter<T> addReferenceRestriction(ClientReferenceRestriction restriction) {
        restrictions.add(restriction)
        return this
    }
*/
    /**
     * Converts ResoruceReferrence to client type object.
     *
     *
     * @param serverObject ResourceReference instance
     * @param options - to client conversion options
     * @return client referenceable object
     * @throws ClassCastException if server side local resource of given ResourceReference is of wrong type.
     */
    @SuppressWarnings("unchecked")
    public T toClient(ResourceReference serverObject, ToClientConversionOptions options) throws ClassCastException {
        ClientUriHolder result = null
        if (serverObject != null) {
            if ((options != null && options.isExpanded())) {
                try {
                    Resource localResource

                    if (serverObject.isLocal()) {
                        localResource = serverObject.getLocalResource()
                    } else {
                        localResource = repositoryService.getResource(serverObject.getReferenceURI())
                    }

                    result = resourceConverterProvider.getToClientConverter(localResource).toClient(localResource, options)
                } catch (AccessDeniedException e) {
                    result = new ClientReference(serverObject.getTargetURI())
                }
            } else {
                result = new ClientReference(serverObject.getTargetURI())
            }
        }
        return (T) result
    }

    public ResourceReference toServer(T clientObject,ToServerConversionOptions options) throws IllegalParameterValueException, MandatoryParameterNotFoundException {
        return toServer(clientObject, null, options)
    }

    public ResourceReference toServer(T clientObject, ResourceReference resultToUpdate, ToServerConversionOptions options)
            throws IllegalParameterValueException, MandatoryParameterNotFoundException {
        final ResourceReference resourceReference
        if (clientObject == null) {
            resourceReference = null
        } else if (clientObject.getClass() == ClientReference.class) {
            resourceReference = toServerReference(clientObject, resultToUpdate, options)
        } else if (clientObject instanceof ClientResource) {
            if (options == null || !options.isAllowReferencesOnly()) {
                /*
                if (restrictions != null){
                    for (ClientReferenceRestriction restriction : restrictions) {
                        restriction.validateReference((ClientResource)clientObject)
                    }
                }
                */
                resourceReference = toServerLocalResource((ClientResource) clientObject, resultToUpdate, options)
            } else {
                throw new IllegalParameterValueException("reference", clientObject.toString())
            }
        } else {
            // shouldn't happen
            throw new IllegalParameterValueException("References of type "
                    + ClientTypeHelper.extractClientType(clientObject.getClass()) + " are not supported")
        }
        return resourceReference
    }

    protected ResourceReference toServerReference(T clientObject, ResourceReference resultToUpdate, ToServerConversionOptions options) throws IllegalParameterValueException {
        ResourceReference result
        final String uriFromClient = clientObject.getUri()
        if (resultToUpdate != null && resultToUpdate.getTargetURI().equals(uriFromClient)) {
            // no update is needed. Existing ResourceReference points to the same URI
            result = resultToUpdate
        } else {
            // if it is local resource, reference with local resource must be returned or update will fail
            // NOTE: it is always preferable to pass resultToUpdate to get actual resource from there to avoid rereading and possible errors.
            String ownersUri = options == null ? null : options.getOwnersUri()
            Resource referencedResource = validateAndGetReference(uriFromClient, ownersUri)
            final boolean local = ownersUri != null && uriFromClient.startsWith(ownersUri + "_files" + Folder.SEPARATOR)
            result = resultToUpdate == null ? new ResourceReference(referencedResource) : resultToUpdate
            if (!local) {
                result.setReference(uriFromClient)
            }
        }
        return result
    }

    protected Resource validateAndGetReference(String referenceUri, String ownersUri) throws IllegalParameterValueException {
        if (referenceUri == null) {
            throw new IllegalParameterValueException("resourceReference.uri", "null")
        }

        if (!isAssignable(ownersUri, referenceUri)){
            throw new IllegalParameterValueException("resourceReference.uri", referenceUri)
        }
        // we need to update reference
        final Resource resource = repositoryService.getResource(referenceUri)
        if (resource == null) {
            // resource with such URI doesn't exist
            throw new IllegalParameterValueException("Referenced resource doesn't exist", "resourceReference.uri", referenceUri)
        } /*else if (!restrictions.isEmpty()) {
            final ClientResource clientTargetObject = resourceConverterProvider.getToClientConverter(resource).toClient(resource, null)
            for (ClientReferenceRestriction restriction : restrictions) {
                restriction.validateReference(clientTargetObject)
            }
        }*/
        return resource
    }

    protected ResourceReference toServerLocalResource(ClientResource clientObject, ResourceReference resultToUpdate, ToServerConversionOptions options) throws IllegalParameterValueException, MandatoryParameterNotFoundException {
        ResourceReference result
        Resource localResource = resourceConverterProvider.getToServerConverter(clientObject).toServer(clientObject, options)
        if (localResource.getName() == null) {
            //localResource.setName(clientObject.getLabel().replaceAll(configurationBean.getResourceIdNotSupportedSymbols(), "_"))
        }
        if (options != null && options.isResetVersion()){
            localResource.setVersion(Resource.VERSION_NEW)
        }
        if (resultToUpdate != null) {
            result = resultToUpdate
            result.setLocalResource(localResource)
        } else {
            result = new ResourceReference(localResource)
        }
        return result
    }

    protected final boolean isAssignable(String parent, String child){
        boolean res = true
        if (parent != null){
            int suffixIndex = child.lastIndexOf("_files")
            if (suffixIndex > 0){
                String possibleParentUri = child.substring(0, suffixIndex)
                if (!possibleParentUri.equals(parent)){
                    res = repositoryService.getResource(possibleParentUri) == null
                }
            }
        }
        return res
    }
/*
    public static class ReferenceClassRestriction implements ClientReferenceRestriction {
        protected final Class<? extends ClientReferenceable> targetClientClass

        public ReferenceClassRestriction(Class<? extends ClientReferenceable> targetClientClass) {
            this.targetClientClass = targetClientClass
        }

        @Override
        public void validateReference(ClientResource clientResource) throws IllegalParameterValueException {
            if (!targetClientClass.isAssignableFrom(clientResource.getClass())) {
                throw new IllegalParameterValueException("Reference target is of wrong type",
                        "resourceReference.uri",
                        clientResource.getUri(),
                        ClientTypeHelper.extractClientType(clientResource.getClass()))
            }
        }
    }

    public static class FileTypeRestriction implements ClientReferenceRestriction{
        private final ClientFile.FileType fileType
        private final String fieldName

        public FileTypeRestriction(ClientFile.FileType fileType){
            this(fileType, null)
        }

        public FileTypeRestriction(ClientFile.FileType fileType, String fieldName){
            this.fileType = fileType
            this.fieldName = fieldName
        }

        @Override
        public void validateReference(ClientResource clientResource) throws IllegalParameterValueException {
            if(clientResource instanceof ClientFile && fileType != ((ClientFile)clientResource).getType()){
                throw new IllegalParameterValueException("Reference target is of wrong type",
                        fieldName != null ? fieldName : "file.type",
                        clientResource.getUri(),
                        ((ClientFile)clientResource).getType().name())
            }
        }
    }
    */
}
