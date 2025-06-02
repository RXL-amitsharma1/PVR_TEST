package com.rxlogix.odata

import com.rxlogix.OdataService
import com.rxlogix.config.OdataSettings
import grails.converters.JSON
import org.apache.olingo.commons.api.format.ContentType
import org.apache.olingo.commons.api.http.HttpStatusCode
import org.apache.olingo.server.api.OData
import org.apache.olingo.server.api.ODataRequest
import org.apache.olingo.server.api.ODataResponse
import org.apache.olingo.server.api.ServiceMetadata
import org.apache.olingo.server.core.uri.UriInfoImpl
import spock.lang.Shared
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

import java.nio.charset.StandardCharsets

@ConfineMetaClassChanges([OdataService])
class OdataEntityProcessorSpec extends Specification {
    @Shared
            odataEntityProcessor = createOdataEntityProcessor()

    def createOdataEntityProcessor() {
        OData odata = OData.newInstance();
        ServiceMetadata edm = odata.createServiceMetadata(new OdataEdmProvider(), new ArrayList<>());
        OdataEntityProcessor odataEntityProcessor = new OdataEntityProcessor();
        odataEntityProcessor.init(odata, edm)

        OdataService odataService = new OdataService()
        OdataSettingsCache.metaClass.static.getEntityMap = { String dsName, String entityName -> return OdataTestUtil.entityMap["CaseSeries"] }
        OdataSettingsCache.metaClass.static.getEntityFields = { String dsName, String entityName -> return OdataTestUtil.entityFields["CaseSeries"] }
        odataService.metaClass.getEntity = { String dsName, String tableName, String configLimitQuery, allowedFields, id -> return OdataTestUtil.entity }
        odataService.metaClass.createEntity = { String dsName, String tableName, List columns, List values -> return null }
        odataService.metaClass.updateEntity = { String dsName, String tableName, List columns, List values, id, String limitQuery = "" -> return 1 }
        odataService.metaClass.deleteEntity = { String dsName, String tableName, id, String limitQuery = "" -> return 1 }
        OdataSettingsCache.metaClass.static.getOdataService= { ->
            return odataService
        }
        odataEntityProcessor.metaClass.getOdataService= { ->
            return odataService
        }
        return odataEntityProcessor
    }


    void "test readEntity method"() {
        OdataSettingsCache.metaClass.static.getEntitiesMap = { String dsName -> return OdataTestUtil.entityMap["CaseSeries"] }
        OdataSettingsCache.metaClass.static.getEntityFields = { String dsName, String entityName -> return OdataTestUtil.entityFields["CaseSeries"] }
        when:
        ODataResponse response = new ODataResponse()
        UriInfoImpl uriInfo = OdataTestUtil.createUri("CaseSeries", "1", null)
        odataEntityProcessor.readEntity(new ODataRequest(), response, uriInfo, ContentType.APPLICATION_JSON)
        def res = JSON.parse(response.getContent().text)

        then:
        res.numberField == 2
        res.ID == 1
        res.stringField == "test"
    }

    void "test createEntity method"() {

        when:
        ODataResponse response = new ODataResponse()
        ODataRequest request = new ODataRequest()
        request.setBody(new ByteArrayInputStream("{\"ID\":100,\"numberField\":200,\"stringField\":\"str\"}".getBytes(StandardCharsets.UTF_8)))
        UriInfoImpl uriInfo = OdataTestUtil.createUri("CaseSeries", null, null)

        odataEntityProcessor.createEntity(request, response, uriInfo, ContentType.APPLICATION_JSON, ContentType.APPLICATION_JSON)
        def res = JSON.parse(response.getContent().text)

        then:
        res.numberField == 200
        res.ID == 100
        res.stringField == "str"
    }

    void "test updateEntity method"() {

        when:
        ODataResponse response = new ODataResponse()
        ODataRequest request = new ODataRequest()
        request.setBody(new ByteArrayInputStream("{\"ID\":100,\"numberField\":200,\"stringField\":\"str\"}".getBytes(StandardCharsets.UTF_8)))
        UriInfoImpl uriInfo = OdataTestUtil.createUri("CaseSeries", "2", null)
        odataEntityProcessor.updateEntity(request, response, uriInfo, ContentType.APPLICATION_JSON, ContentType.APPLICATION_JSON)

        then:
        response.getStatusCode() == HttpStatusCode.NO_CONTENT.getStatusCode()

    }

    void "test deleteEntity method"() {

        when:
        ODataResponse response = new ODataResponse()
        ODataRequest request = new ODataRequest()
        request.setBody(new ByteArrayInputStream("{\"ID\":100,\"numberField\":200,\"stringField\":\"str\"}".getBytes(StandardCharsets.UTF_8)))
        UriInfoImpl uriInfo = OdataTestUtil.createUri("CaseSeries", "2", null)
        odataEntityProcessor.deleteEntity(request, response, uriInfo)

        then:
        response.getStatusCode() == HttpStatusCode.NO_CONTENT.getStatusCode()

    }
}
