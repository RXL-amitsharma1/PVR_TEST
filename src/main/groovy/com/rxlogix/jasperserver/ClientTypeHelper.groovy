package com.rxlogix.jasperserver

import com.jaspersoft.jasperserver.dto.resources.ResourceMediaType
import com.rxlogix.jasperserver.converters.ToClientConverter

import javax.ws.rs.core.MediaType
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType
import java.util.regex.Matcher
import java.util.regex.Pattern;

@XmlAccessorType(XmlAccessType.NONE)
public class ClientTypeHelper<T> {

    private final Class<? extends ToClientConverter<?, T, ?>> converterClass;

    public ClientTypeHelper(Class<? extends ToClientConverter> converterClass){
        this.converterClass = (Class)converterClass;
    }

    public ClientTypeHelper(ToClientConverter<?, T, ?> converter){
        this.converterClass = (Class)converter.getClass();
    }

    private Class<?> clientClass;
    private String clientResourceType;

    public Class<T> getClientClass(){
        if(clientClass == null){
            clientClass = (Class) GenericParametersHelper.getGenericTypeArgument(converterClass, ToClientConverter.class, 1);
            if (clientClass == null) {
                throw new IllegalStateException("Unable to identify clientTypeClass. It can happen because " +
                        converterClass.getName() + " is raw implementation of " + ToClientConverter.class.getName());
            }
        }
        return (Class<T>) clientClass;
    }

    public T getNewClientObjectInstance() {
        try {
            return (T) getClientClass().newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Couldn't instantiate client object", e);
        }
    }

    public String getClientResourceType() {
        if (clientResourceType == null) {
            clientResourceType = ClientTypeHelper.extractClientType(getClientClass());
        }
        return clientResourceType;
    }

    public static String extractClientType(Class<?> clientObjectClass) {
        String clientResourceType = null;
        final XmlRootElement xmlRootElement = clientObjectClass.getAnnotation(XmlRootElement.class);
        if (xmlRootElement != null && !"##default".equals(xmlRootElement.name())) {
            clientResourceType = xmlRootElement.name();
        } else {
            final XmlType xmlType = clientObjectClass.getAnnotation(XmlType.class);
            if (xmlType != null && !"##default".equals(xmlType.name())) {
                clientResourceType = xmlType.name();
            }
        }
        if (clientResourceType == null) {
            final String classSimpleName = clientObjectClass.getSimpleName();
            clientResourceType = classSimpleName.replaceFirst("^.", classSimpleName.substring(0, 1).toLowerCase());
        }
        return clientResourceType;
    }

    public static String extractClientType(MediaType mediaType){
        return mediaType == null ? null : extractClientType(mediaType.toString());
    }

    public static String extractClientType(String mediaType){
        String clientResourceType = null;
        Matcher matcher = Pattern.compile(ResourceMediaType.RESOURCE_MEDIA_TYPE_PREFIX + "([^+]+)").matcher(mediaType != null ? mediaType : "");
        if(matcher.find()){
            clientResourceType = matcher.group(1);
        }
        return clientResourceType;
    }
}
