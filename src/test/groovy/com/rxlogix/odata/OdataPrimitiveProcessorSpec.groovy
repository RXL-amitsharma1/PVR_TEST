package com.rxlogix.odata

import com.rxlogix.OdataService
import grails.converters.JSON
import org.apache.olingo.commons.api.format.ContentType
import org.apache.olingo.server.api.OData
import org.apache.olingo.server.api.ODataRequest
import org.apache.olingo.server.api.ODataResponse
import org.apache.olingo.server.api.ServiceMetadata
import org.apache.olingo.commons.api.edmx.EdmxReference
import org.apache.olingo.server.core.uri.UriInfoImpl
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([OdataService])
class OdataPrimitiveProcessorSpec extends Specification {

    def createOdataPrimitiveProcessor() {
        OData odata = OData.newInstance();
        ServiceMetadata edm = odata.createServiceMetadata(new OdataEdmProvider(), new ArrayList<EdmxReference>());
        OdataPrimitiveProcessor odataPrimitiveProcessor = new OdataPrimitiveProcessor();
        odataPrimitiveProcessor.init(odata, edm)

        OdataService odataService = new OdataService()
        OdataSettingsCache.metaClass.static.getEntityMap = { String dsName, String entityName -> return OdataTestUtil.entityMap["CaseSeries"] }
        OdataSettingsCache.metaClass.static.getEntityFields = { String dsName, String entityName -> return OdataTestUtil.entityFields["CaseSeries"] }
        odataService.metaClass.getEntity = { String dsName, String tableName, String configLimitQuery, allowedFields, id -> return OdataTestUtil.entity }
        OdataSettingsCache.metaClass.static.getOdataService= { ->
            return odataService
        }
        odataPrimitiveProcessor.metaClass.getOdataService= { ->
            return odataService
        }
        return odataPrimitiveProcessor
    }


    void "test readPrimitive method"() {

        when:
        OdataPrimitiveProcessor odataPrimitiveProcessor = createOdataPrimitiveProcessor()
        ODataResponse response = new ODataResponse()
        UriInfoImpl uriInfo = OdataTestUtil.createUri("CaseSeries", "1", "stringField")
        odataPrimitiveProcessor.readPrimitive(new ODataRequest(), response, uriInfo, ContentType.APPLICATION_JSON)
        then:
        JSON.parse(response.getContent().text).value == "test"

    }
}
