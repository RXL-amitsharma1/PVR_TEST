package com.rxlogix.jasperserver.converters

import com.jaspersoft.jasperserver.dto.resources.ClientResource
import com.rxlogix.jasperserver.ClientTypeHelper
import com.rxlogix.jasperserver.Resource
import com.rxlogix.jasperserver.ResourceLookup
import com.rxlogix.jasperserver.exception.IllegalParameterValueException
import grails.util.Holders

public class ResourceConverterProvider {
    BinaryDataResourceConverter binaryDataResourceConverter
    //@javax.annotation.Resource
    //private List<Class<?>> disabledResourceTypes
    private List<String> disabledResourceClientTypes = new ArrayList<String>()
    private Map<String, ToClientConverter<? super Resource, ? extends ClientResource, ToClientConversionOptions>> toClientConverters
    private Map<String, ToServerConverter<? super ClientResource, ? extends Resource, ToServerConversionOptions>> toServerConverters
    private Map<String, ResourceConverter<? extends Resource, ? extends ClientResource>> resourceConverters
    private volatile boolean initialized = false

    public ToClientConverter<? super Resource, ? extends ClientResource, ToClientConversionOptions> getToClientConverter(String serverType) throws IllegalParameterValueException {
        prepareConverters()
        final ToClientConverter<? super Resource, ? extends ClientResource, ToClientConversionOptions> toClientConverter = toClientConverters.get(serverType)
        if(toClientConverter == null){
            throw new IllegalParameterValueException("type", serverType)
        }
        return toClientConverter
    }

    public ToClientConverter<? super Resource, ? extends ClientResource, ToClientConversionOptions> getToClientConverter(String serverType, String clientType){
        prepareConverters()
        return (ToClientConverter<? super Resource, ? extends ClientResource, ToClientConversionOptions>) resourceConverters.get(getCombinedConverterKey(serverType, clientType))
    }

    public ToServerConverter<? super ClientResource, ? extends Resource, ToServerConversionOptions> getToServerConverter(String serverType, String clientType){
        prepareConverters()
        return (ToServerConverter<? super ClientResource, ? extends Resource, ToServerConversionOptions>) resourceConverters.get(getCombinedConverterKey(serverType, clientType))
    }

    public ToClientConverter<? super Resource, ? extends ClientResource, ToClientConversionOptions> getToClientConverter(Resource serverObject) {
        try {
            return getToClientConverter(serverObject instanceof ResourceLookup ? ResourceLookup.class.getName() : serverObject.getResourceType())
        } catch (IllegalParameterValueException e) {
            throw new IllegalStateException("Couldn't find converter for " + serverObject.getResourceType())
        }
    }

    public ToServerConverter<? super ClientResource, ? extends Resource, ToServerConversionOptions> getToServerConverter(ClientResource clientObject) throws IllegalParameterValueException {
        return getToServerConverter(ClientTypeHelper.extractClientType(clientObject.getClass()))
    }

    public ToServerConverter<? super ClientResource, ? extends Resource, ToServerConversionOptions> getToServerConverter(String clientType) throws IllegalParameterValueException {
        prepareConverters()
        final ToServerConverter<? super ClientResource, ? extends Resource, ToServerConversionOptions> toServerConverter = toServerConverters.get(clientType != null ? clientType.toLowerCase() : null)
        if(toServerConverter == null){
            throw new IllegalParameterValueException("type", clientType)
        }
        return toServerConverter
    }

    public Class<? extends ClientResource> getClientTypeClass(String clientType) throws IllegalParameterValueException {
        final ResourceConverter<? extends Resource, ? extends ClientResource> resourceConverter =
                (ResourceConverter<? extends Resource, ? extends ClientResource>) getToServerConverter(clientType)
        return resourceConverter.getClientTypeClass()
    }

    // corresponding resourceType of converter assures type safety in further usage
    @SuppressWarnings("unchecked")
    protected void prepareConverters() {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    toClientConverters = new HashMap<String, ToClientConverter<? super Resource, ? extends ClientResource, ToClientConversionOptions>>()
                    toServerConverters = new HashMap<String, ToServerConverter<? super ClientResource,
                            ? extends Resource, ToServerConversionOptions>>()
                    resourceConverters = new HashMap<String, ResourceConverter<? extends Resource, ? extends ClientResource>>()
                    final List<ResourceConverter<? super Resource, ? extends ClientResource>> converters = getConverters()
                    if (getConverters() != null) {
                        for (ResourceConverter currentConverter : converters) {
                            final String serverResourceType = currentConverter.getServerResourceType()
                            final String clientResourceType = currentConverter.getClientResourceType().toLowerCase()
                            if(!disabledResourceClientTypes.contains(clientResourceType)) {
                                toClientConverters.put(serverResourceType, currentConverter)
                                toServerConverters.put(clientResourceType, currentConverter)
                                resourceConverters.put(getCombinedConverterKey(serverResourceType, clientResourceType), currentConverter)
                            }
                        }
                    }
                    toServerConverters.put(binaryDataResourceConverter.getClientResourceType(), (ToServerConverter)binaryDataResourceConverter)
                    initialized = true
                }
            }
        }
    }

    protected String getCombinedConverterKey(String serverResourceType, String clientResourceType){
        return serverResourceType + "<=>" + clientResourceType.toLowerCase()
    }

    // cast is safe, spring application context assure safety
    @SuppressWarnings("unchecked")
    protected List<ResourceConverter<? super Resource, ? extends ClientResource>> getConverters() {
        final Map<String, ResourceConverter> convertersMap = Holders.applicationContext.getBeansOfType(ResourceConverter.class)
        return (List) new ArrayList<ResourceConverter>(convertersMap.values())
    }
/*
    @PostConstruct
    public void initialize(){
        for (Class<?> disabledResourceType : disabledResourceTypes) {
            disabledResourceClientTypes.add(ClientTypeHelper.extractClientType(disabledResourceType).toLowerCase())
        }
    }
*/
}
