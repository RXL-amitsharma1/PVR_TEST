package com.rxlogix.odata

import com.rxlogix.OdataService
import grails.util.Holders
import org.apache.olingo.commons.api.data.*
import org.apache.olingo.commons.api.edm.EdmEntitySet
import org.apache.olingo.commons.api.edm.EdmEntityType
import org.apache.olingo.commons.api.edm.EdmProperty
import org.apache.olingo.commons.api.format.ContentType
import org.apache.olingo.commons.api.http.HttpHeader
import org.apache.olingo.commons.api.http.HttpStatusCode
import org.apache.olingo.server.api.*
import org.apache.olingo.server.api.processor.EntityCollectionProcessor
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions
import org.apache.olingo.server.api.serializer.ODataSerializer
import org.apache.olingo.server.api.serializer.SerializerException
import org.apache.olingo.server.api.serializer.SerializerResult
import org.apache.olingo.server.api.uri.*
import org.apache.olingo.server.api.uri.queryoption.*
import org.apache.olingo.server.api.uri.queryoption.expression.Expression
import org.apache.olingo.server.api.uri.queryoption.expression.Member
import org.springframework.web.util.UriComponentsBuilder

public class OdataEntityCollectionProcessor implements EntityCollectionProcessor {
    private OData odata
    private ServiceMetadata serviceMetadata
    OdataService odataService

    OdataService getOdataService() {
        return Holders.applicationContext.getBean("odataService")
    }

    public void init(OData odata, ServiceMetadata serviceMetadata) {
        this.odata = odata
        this.serviceMetadata = serviceMetadata
    }

    public void readEntityCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat) throws ODataApplicationException, SerializerException {
        try {
            List<UriResource> resourcePaths = uriInfo.getUriResourceParts()
            UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0)
            EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet()

            def params = [:]
            handlePagination(params, uriInfo)
            handleOrderBy(params, uriInfo)
            SelectOption selectOption = uriInfo.getSelectOption()
            EntityCollection entitySet = null
            if (edmEntitySet.getName()) {
                String entityName = edmEntitySet.getName()
                handleFilter(params, uriInfo, entityName)
                entitySet = getData(entityName, params, request)
            }
            ODataSerializer serializer = odata.createSerializer(responseFormat)

            EdmEntityType edmEntityType = edmEntitySet.getEntityType();
            String selectList = odata.createUriHelper().buildContextURLSelectList(edmEntityType, null, selectOption)
            ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).selectList(selectList).build();

            final String id = request.getRawBaseUri() + "/" + edmEntitySet.getName()
            EntityCollectionSerializerOptions opts = EntityCollectionSerializerOptions.with()
                    .contextURL(contextUrl).id(id).count(uriInfo.getCountOption()).select(selectOption).build()

            SerializerResult serializedContent = serializer.entityCollection(serviceMetadata, edmEntityType, entitySet, opts)

            response.setContent(serializedContent.getContent())
            response.setStatusCode(HttpStatusCode.OK.getStatusCode())
            response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString())
        } catch (Exception e) {
            e.printStackTrace();
            throw e
        }
    }

    private static void handleFilter(Map params, UriInfo uriInfo, String entityName) {
        String dsName = OdataUtils.getDsName()
        FilterOption filterOption = uriInfo.getFilterOption()
        if (filterOption != null) {
            Expression filterExpression = filterOption.getExpression()
            FilterExpressionVisitor expressionVisitor = new FilterExpressionVisitor(OdataSettingsCache.getEntityFields(dsName, entityName).fields)
            String filterSql = filterExpression.accept(expressionVisitor)
            if (filterSql) {
                params << [filter: filterSql]
                params << [paramValues: expressionVisitor.values]
            }
        }
    }

    private static void handleOrderBy(Map params, UriInfo uriInfo) {
        OrderByOption orderByOption = uriInfo.getOrderByOption()
        String orderby = ""
        orderByOption?.orders?.each {
            if (it.expression instanceof Member) {

                UriInfoResource resourcePath = ((Member) it.expression).getResourcePath();
                UriResource uriResource = resourcePath.getUriResourceParts().get(0);
                if (uriResource instanceof UriResourcePrimitiveProperty) {
                    EdmProperty edmProperty = ((UriResourcePrimitiveProperty) uriResource).getProperty()
                    if (orderby) orderby += ","
                    orderby += edmProperty.getName() + (it.descending ? " desc" : " asc")
                }
            }
        }
        if (orderby)
            params << [orderby: orderby]

    }

    private static void handlePagination(Map params, UriInfo uriInfo) {
        CountOption countOption = uriInfo.getCountOption()
        if (countOption) {
            params << [count: countOption.getValue()]
        }

        SkipOption skipOption = uriInfo.getSkipOption()
        if (skipOption != null) {
            int skipNumber = skipOption.getValue()
            if (skipNumber >= 0) {
                params << [skip: skipNumber]
            } else {
                throw new ODataApplicationException("Invalid value for skip", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ROOT);
            }
        }

        TopOption topOption = uriInfo.getTopOption()
        if (topOption != null) {
            int topNumber = topOption.getValue()
            if (topNumber >= 0) {
                params << [top: topNumber]
            } else {
                throw new ODataApplicationException("Invalid value for top", HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ROOT);
            }
        }
    }

    private EntityCollection getData(String entityName, Map params, ODataRequest request) {
        String dsName = OdataUtils.getDsName()
        EntityCollection productsCollection = new EntityCollection()
        def allowedFields = OdataSettingsCache.getEntityFields(dsName, entityName).fields
        def entity = OdataSettingsCache.getEntityMap(dsName, entityName)
        List<Entity> productList = productsCollection.getEntities()
        Map data = getOdataService().getDataForEntity(dsName, entity.tableName, entity.limitQuery, allowedFields, params)
        data.data.each { row ->
            Entity e = new Entity()
            boolean hasId = false
            row.each {
                e.addProperty(new Property(null, it.label, ValueType.PRIMITIVE, OdataUtils.toType(it.value, it.dataType)))
                if (it.label == "ID") {
                    hasId = true
                    e.setId(OdataUtils.createId(entityName, OdataUtils.toType(it.value, it.dataType)))
                }
            }
            if (!hasId) {
                e.setId(new URI(entityName + "()"))
            }
            productList.add(e)
        }
        if (params.count) {
            productsCollection.setCount(data.meta.count)
        }
        if (data.meta.hasNext) {
            productsCollection.setNext(createNextUrl(params, request))
        }

        return productsCollection
    }

    private URI createNextUrl(Map params, ODataRequest request) {
        Integer skip = (params.skip ?: 0 as Integer) + (params.top ?: 20 as Integer)
        String nextUrl
        String requestURL = request.getRawRequestUri()

        if (requestURL.indexOf("\$skip") > -1) {
            UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromUriString(requestURL)
            urlBuilder.replaceQueryParam("\$skip", skip)

            nextUrl = urlBuilder.build().toUriString()
        } else {
            nextUrl = requestURL + ((requestURL.indexOf("?") > -1) ? "&" : "?") + "\$skip=" + skip

        }
        return new URI(nextUrl)
    }


}
