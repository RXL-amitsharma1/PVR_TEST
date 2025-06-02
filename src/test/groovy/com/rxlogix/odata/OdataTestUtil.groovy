package com.rxlogix.odata

import org.apache.olingo.commons.api.edm.FullQualifiedName
import org.apache.olingo.commons.api.edm.provider.CsdlEdmProvider
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType
import org.apache.olingo.commons.api.edm.provider.CsdlProperty
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef
import org.apache.olingo.commons.core.edm.EdmEntitySetImpl
import org.apache.olingo.commons.core.edm.EdmPropertyImpl
import org.apache.olingo.commons.core.edm.EdmProviderImpl
import org.apache.olingo.server.api.uri.UriResourceProperty
import org.apache.olingo.server.core.uri.UriInfoImpl
import org.apache.olingo.server.core.uri.UriParameterImpl
import org.apache.olingo.server.core.uri.UriResourceEntitySetImpl
import org.apache.olingo.server.core.uri.UriResourceImpl
import org.apache.olingo.server.core.uri.UriResourcePrimitivePropertyImpl

import java.sql.Types

class OdataTestUtil {
    static UriInfoImpl createUri(String entityName,String id, String primitive){
        UriInfoImpl uriInfo = new UriInfoImpl()
        CsdlEdmProvider odataEdmProvider =new OdataEdmProvider()
        EdmProviderImpl edmProvider = new EdmProviderImpl(odataEdmProvider)
        CsdlEntitySet entitySet = new CsdlEntitySet();
        entitySet.setName(entityName);
        entitySet.setType( new FullQualifiedName("NAMESPACE", "CaseSeries"));
        EdmEntitySetImpl edmEntitySet=new EdmEntitySetImpl(edmProvider, null,entitySet )
        UriResourceImpl uriResource =new UriResourceEntitySetImpl(edmEntitySet)
        if(id) {
            UriParameterImpl uriParameter = new UriParameterImpl()
            uriParameter.setText("1")
            uriResource.setKeyPredicates([uriParameter])

        }
        uriInfo.addResourcePart(uriResource)
        if(primitive) {
            CsdlProperty csdlProperty = new CsdlProperty()
            csdlProperty.setName(primitive)
            csdlProperty.setType("String")
            EdmPropertyImpl edmProperty = new EdmPropertyImpl(edmProvider, csdlProperty)
            UriResourceProperty uriResourceProperty = new UriResourcePrimitivePropertyImpl(edmProperty)
            uriInfo.addResourcePart(uriResourceProperty)
        }
        return uriInfo
    }
    static Map entityMap = [
            CaseSeries: [tableName  : "C_IDENTIFICATION",
                         description: "some description",
                         limitQuery : "CASE_ID>100015",
                         create     : true,
                         update     : true,
                         delete     : true,
                         fields     : ["ID"         : "ID",
                                       "numberField": "NUMBER_FIELD",
                                       "stringField": "STRING_FIELD",
                                       "dateField": "DATE_FIELD"
                         ]]
    ]

    static CsdlEntityType getType() {
        CsdlEntityType entityType = new CsdlEntityType();
        entityType.setName("CaseSeries");
        List<CsdlProperty> properties = []
        properties << new CsdlProperty().setName("ID").setType(OdataUtils.getEdmPrimitiveTypeKindName(Types.DECIMAL));
        properties << new CsdlProperty().setName("numberField").setType(OdataUtils.getEdmPrimitiveTypeKindName(Types.DECIMAL));
        properties << new CsdlProperty().setName("stringField").setType(OdataUtils.getEdmPrimitiveTypeKindName(Types.VARCHAR));
        properties << new CsdlProperty().setName("dateField").setType(OdataUtils.getEdmPrimitiveTypeKindName(Types.DATE));
        entityType.setProperties(properties);
        CsdlPropertyRef propertyRef = new CsdlPropertyRef();
        propertyRef.setName("ID");
        entityType.setKey(Collections.singletonList(propertyRef));
        return entityType
    }
    static Map entityFields = [CaseSeries: [fields:[
            [columnName: "ID", label: "ID", dataType: OdataUtils.getEdmPrimitiveTypeKindName(Types.DECIMAL)],
            [columnName: "NUMBER_FIELD", label: "numberField", dataType:  OdataUtils.getEdmPrimitiveTypeKindName(Types.DECIMAL)],
            [columnName: "STRING_FIELD", label: "stringField", dataType:  OdataUtils.getEdmPrimitiveTypeKindName(Types.VARCHAR)],
            [columnName: "DATE_FIELD", label: "dateField", dataType:  OdataUtils.getEdmPrimitiveTypeKindName(Types.DATE)]
            ],
            type:getType()
    ]]

    static List entity = [
            [value: 1, label: "ID", dataType: Types.DECIMAL],
            [value: 2, label: "numberField", dataType: Types.DECIMAL],
            [value: "test", label: "stringField", dataType: Types.VARCHAR],
            [value: new Date(2018,1,1), label: "dateField", dataType: Types.DATE]
    ]

    static def value
}
