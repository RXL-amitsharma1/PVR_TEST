package com.rxlogix.odata

import com.rxlogix.OdataService
import grails.util.Holders
import org.apache.olingo.commons.api.data.ContextURL
import org.apache.olingo.commons.api.data.Entity
import org.apache.olingo.commons.api.data.Property
import org.apache.olingo.commons.api.data.ValueType
import org.apache.olingo.commons.api.edm.EdmEntitySet
import org.apache.olingo.commons.api.edm.EdmPrimitiveType
import org.apache.olingo.commons.api.edm.EdmProperty
import org.apache.olingo.commons.api.format.ContentType
import org.apache.olingo.commons.api.http.HttpHeader
import org.apache.olingo.commons.api.http.HttpStatusCode
import org.apache.olingo.server.api.*
import org.apache.olingo.server.api.deserializer.DeserializerException
import org.apache.olingo.server.api.processor.PrimitiveProcessor
import org.apache.olingo.server.api.serializer.ODataSerializer
import org.apache.olingo.server.api.serializer.PrimitiveSerializerOptions
import org.apache.olingo.server.api.serializer.SerializerException
import org.apache.olingo.server.api.uri.*

class OdataPrimitiveProcessor implements PrimitiveProcessor {

    private OData odata;
    private ServiceMetadata serviceMetadata;

    OdataService getOdataService() {
        return Holders.applicationContext.getBean("odataService")
    }

    void init(OData odata, ServiceMetadata serviceMetadata) {
        this.odata = odata
        this.serviceMetadata = serviceMetadata
    }

    void readPrimitive(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, SerializerException {
        try {
            List<UriResource> resourceParts = uriInfo.getUriResourceParts()
            UriResourceEntitySet uriEntitySet = (UriResourceEntitySet) resourceParts.get(0)
            EdmEntitySet edmEntitySet = uriEntitySet.getEntitySet()
            List<UriParameter> keyPredicates = uriEntitySet.getKeyPredicates()
            UriResourceProperty uriProperty = (UriResourceProperty) resourceParts.get(resourceParts.size() - 1) // the last segment is the Property
            EdmProperty edmProperty = uriProperty.getProperty()
            String edmPropertyName = edmProperty.getName()
            EdmPrimitiveType edmPropertyType = (EdmPrimitiveType) edmProperty.getType()

            Property property = getPropertie(edmEntitySet, keyPredicates, edmPropertyName)
            if (property.getValue() != null) {
                ODataSerializer serializer = odata.createSerializer(responseFormat)

                ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).navOrPropertyPath(edmPropertyName).build()
                PrimitiveSerializerOptions options = PrimitiveSerializerOptions.with().contextURL(contextUrl).build()

                InputStream propertyStream = serializer.primitive(serviceMetadata, edmPropertyType, property, options).getContent()

                response.setContent(propertyStream)
                response.setStatusCode(HttpStatusCode.OK.getStatusCode())
                response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString())
            } else {
                response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode())
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e
        }
    }

    Property getPropertie(EdmEntitySet edmEntitySet, List<UriParameter> keyPredicates, String edmPropertyName) {
        Entity entity = getData(edmEntitySet, keyPredicates)

        if (entity == null)
            throw new ODataApplicationException("Entity not found", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH)

        Property property = entity.getProperty(edmPropertyName)
        if (property == null)
            throw new ODataApplicationException("Property not found", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH)

        return property
    }

    public void updatePrimitive(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat)
            throws ODataApplicationException, DeserializerException, SerializerException {
        throw new ODataApplicationException("Not supported.", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT)
    }

    public void deletePrimitive(ODataRequest request, ODataResponse response, UriInfo uriInfo) throws ODataApplicationException {
        throw new ODataApplicationException("Not supported.", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT)
    }

    private Entity getData(EdmEntitySet edmEntitySet, List<UriParameter> keyPredicates) {
        String dsName = OdataUtils.getDsName()
        if (edmEntitySet.getName()) {
            String entityName = edmEntitySet.getName()
            def allowedFields = OdataSettingsCache.getEntityFields(dsName, entityName).fields
            def entity = OdataSettingsCache.getEntityMap(dsName, entityName)

            List data = getOdataService().getEntity(dsName, entity.tableName, entity.limitQuery, allowedFields, keyPredicates?.get(0)?.getText())

            Entity e = new Entity()
            data.each {
                e.addProperty(new Property(null, it.label, ValueType.PRIMITIVE, OdataUtils.toType(it.value, it.dataType)))
                if (it.label == "ID")
                    e.setId(OdataUtils.createId(entityName, OdataUtils.toType(it.value, it.dataType)))
            }
            return e
        }
        return null;
    }
}
