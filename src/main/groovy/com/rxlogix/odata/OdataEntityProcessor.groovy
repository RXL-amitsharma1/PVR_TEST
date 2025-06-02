package com.rxlogix.odata

import com.rxlogix.OdataService
import grails.util.Holders
import org.apache.olingo.commons.api.data.ContextURL
import org.apache.olingo.commons.api.data.Entity
import org.apache.olingo.commons.api.data.Property
import org.apache.olingo.commons.api.data.ValueType
import org.apache.olingo.commons.api.edm.EdmEntitySet
import org.apache.olingo.commons.api.edm.EdmEntityType
import org.apache.olingo.commons.api.format.ContentType
import org.apache.olingo.commons.api.http.HttpHeader
import org.apache.olingo.commons.api.http.HttpStatusCode
import org.apache.olingo.server.api.*
import org.apache.olingo.server.api.deserializer.DeserializerException
import org.apache.olingo.server.api.processor.EntityProcessor
import org.apache.olingo.server.api.serializer.EntitySerializerOptions
import org.apache.olingo.server.api.serializer.ODataSerializer
import org.apache.olingo.server.api.serializer.SerializerException
import org.apache.olingo.server.api.serializer.SerializerResult
import org.apache.olingo.server.api.uri.UriInfo
import org.apache.olingo.server.api.uri.UriParameter
import org.apache.olingo.server.api.uri.UriResource
import org.apache.olingo.server.api.uri.UriResourceEntitySet

class OdataEntityProcessor implements EntityProcessor {

    private OData odata
    private ServiceMetadata serviceMetadata

    OdataService getOdataService() {
        return Holders.applicationContext.getBean("odataService")
    }

    void init(OData odata, ServiceMetadata serviceMetadata) {
        this.odata = odata
        this.serviceMetadata = serviceMetadata
    }

    void readEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, SerializerException {
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts()
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0)
        EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet()

        List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates()
        Entity entity = getData(edmEntitySet, keyPredicates)

        EdmEntityType entityType = edmEntitySet.getEntityType()

        ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).suffix(ContextURL.Suffix.ENTITY).build()
        EntitySerializerOptions options = EntitySerializerOptions.with().contextURL(contextUrl).build()

        ODataSerializer serializer = this.odata.createSerializer(responseFormat)
        SerializerResult serializerResult = serializer.entity(serviceMetadata, entityType, entity, options)
        InputStream entityStream = serializerResult.getContent()
        response.setContent(entityStream)
        response.setStatusCode(HttpStatusCode.OK.getStatusCode())
        response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString())
    }

    private Entity getData(EdmEntitySet edmEntitySet, List<UriParameter> keyPredicates) {
        String dsName = OdataUtils.getDsName()
        if (edmEntitySet.getName()) {
            String entityName = edmEntitySet.getName()
            def allowedFields = OdataSettingsCache.getEntityFields(dsName, entityName).fields
            def entity = OdataSettingsCache.getEntityMap(dsName, entityName)

            List data = getOdataService().getEntity(dsName, entity.tableName, entity.limitQuery, allowedFields, keyPredicates?.get(0)?.getText())
            if (!data)
                throw new ODataApplicationException("Entity not found", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH)
            Entity e = new Entity()
            data.each {
                e.addProperty(new Property(null, it.label, ValueType.PRIMITIVE, OdataUtils.toType(it.value, it.dataType)))
                if (it.label == "ID")
                    e.setId(OdataUtils.createId(entityName, OdataUtils.toType(it.value, it.dataType)))
            }
            return e
        }
        return null
    }

    public void createEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, DeserializerException, SerializerException {
        try {
            String dsName = OdataUtils.getDsName()
            EdmEntitySet edmEntitySet = ((UriResourceEntitySet) uriInfo.getUriResourceParts().get(0)).getEntitySet()
            EdmEntityType edmEntityType = edmEntitySet.getEntityType()
            Entity requestEntity = this.odata.createDeserializer(requestFormat).entity(request.getBody(), edmEntityType).getEntity()

            String entityName = edmEntitySet.getName()
            if (!OdataSettingsCache.getEntityMap(dsName, entityName).create)
                throw new ODataApplicationException("Operation create is not allowed for this entity.", HttpStatusCode.FORBIDDEN.getStatusCode(), Locale.ROOT);
            Map entityConfig = OdataSettingsCache.getEntityMap(dsName, entityName)
            String tableName = entityConfig.tableName
            def (columns, values) = createColumnValues(requestEntity, entityConfig)
            getOdataService().createEntity(dsName, tableName, columns, values)
            Entity createdEntity = requestEntity //retrieve from DB if needed

            ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).build()
            EntitySerializerOptions options = EntitySerializerOptions.with().contextURL(contextUrl).build()
            SerializerResult serializedResponse = odata.createSerializer(responseFormat).entity(serviceMetadata, edmEntityType, createdEntity, options)
            response.setContent(serializedResponse.getContent())
            response.setStatusCode(HttpStatusCode.CREATED.getStatusCode())
            response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString())
        } catch (Exception e) {
            e.printStackTrace();
            throw e
        }
    }


    public void updateEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, DeserializerException, SerializerException {
        try {
            String dsName = OdataUtils.getDsName()
            UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) uriInfo.getUriResourceParts().get(0)
            EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet()
            EdmEntityType edmEntityType = edmEntitySet.getEntityType()
            Entity requestEntity = this.odata.createDeserializer(requestFormat).entity(request.getBody(), edmEntityType).getEntity()

            List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates()
            String entityName = edmEntitySet.getName()
            if (!OdataSettingsCache.getEntityMap(dsName, entityName).update)
                throw new ODataApplicationException("Operation update is not allowed for this entity.", HttpStatusCode.FORBIDDEN.getStatusCode(), Locale.ROOT);

            Map entityConfig = OdataSettingsCache.getEntityMap(dsName, entityName)
            String tableName = entityConfig.tableName
            String limitQuery = entityConfig.limitQuery
            def (columns, values) = createColumnValues(requestEntity, entityConfig)
            int updated = getOdataService().updateEntity(dsName, tableName, columns, values, keyPredicates?.get(0)?.getText(), limitQuery)
            if (updated == 0)
                throw new ODataApplicationException("Entity not found", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH)

            response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode())
        } catch (Exception e) {
            e.printStackTrace();
            throw e
        }
    }

    private createColumnValues(Entity requestEntity, Map entityConfig) {
        List columns = []
        List values = []
        requestEntity.getProperties().each { prop ->
            String fieldName = prop.getName()
            String columnName = entityConfig.fields[fieldName]
            columns << columnName
            values << prop.getValue()
        }
        [columns, values]
    }

    public void deleteEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo) throws ODataApplicationException {
        try {
            String dsName = OdataUtils.getDsName()
            List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
            UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0)
            EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet()
            List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates()

            String entityName = edmEntitySet.getName()
            String limitQuery = OdataSettingsCache.getEntityMap(dsName, entityName).limitQuery
            if (!OdataSettingsCache.getEntityMap(dsName, entityName).delete)
                throw new ODataApplicationException("Operation delete is not allowed for this entity.", HttpStatusCode.FORBIDDEN.getStatusCode(), Locale.ROOT);
            int updated = getOdataService().deleteEntity(dsName, entityName, keyPredicates?.get(0)?.getText(), limitQuery)
            if (updated == 0)
                throw new ODataApplicationException("Entity not found", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH)

            response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode())
        } catch (Exception e) {
            e.printStackTrace();
            throw e
        }
    }
}
