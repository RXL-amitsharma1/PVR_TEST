package reports

import com.rxlogix.UserService
import com.rxlogix.config.*
import com.rxlogix.enums.TemplateTypeEnum
import com.rxlogix.user.User
import grails.converters.JSON
import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import org.grails.web.json.JSONElement
import spock.lang.Specification

@Integration
@Rollback
class ImportServiceSpec extends Specification {

    def importService
    def userService = Mock(UserService)

    def setup() {
        userService.metaClass.getUser = { User.findByUsername("admin") }
    }

    def cleanup() {
        revokeMetaClassChanges(UserService, userService)
    }

    public static void revokeMetaClassChanges(Class type, def instance = null)  {
        GroovySystem.metaClassRegistry.removeMetaClass(type)
        if (instance != null)  {
            instance.metaClass = null
        }
    }


    void "Test Load Case Line Listing Template" () {
        given:
        def json = getCaseLineListingTemplateJSON()
        def listOfTemplatesJSON = "[${json}]"
        JSONElement listOfTemplates = JSON.parse(listOfTemplatesJSON)

        when:
        List<ReportTemplate> templates = importService.importTemplates(listOfTemplates)

        then:
        noExceptionThrown()
        CaseLineListingTemplate template = templates.get(0).first()
        template.name == "CIOMS II Line Listing - unique"
        template.category.name == "Case Listing"
        template.templateType == TemplateTypeEnum.CASE_LINE
        template.columnList.reportFieldInfoList.size() == 15
    }

    void "Test Load Data Tabulation Template" () {
        given:
        def json = getDataTabulationTemplateJSON()
        def listOfTemplatesJSON = "[${json}]"
        JSONElement listOfTemplates = JSON.parse(listOfTemplatesJSON)

        when:
        List<ReportTemplate> templates = importService.importTemplates(listOfTemplates)

        then:
        noExceptionThrown()
        DataTabulationTemplate template = templates.get(0).first()
        template.name == "Data Tabulation Template - unique"
        template.category.name == "Submissions"
        template.templateType == TemplateTypeEnum.DATA_TAB
        template.columnMeasureList.size() == 1
        template.rowList.reportFieldInfoList.size() == 1

    }

    void "Test Load Custom SQL Template" () {
        given:
        def json = getCustomSQLTemplateJSON()
        def listOfTemplatesJSON = "[${json}]"
        JSONElement listOfTemplates = JSON.parse(listOfTemplatesJSON)

        when:
        List<ReportTemplate> templates = importService.importTemplates(listOfTemplates)

        then:
        noExceptionThrown()
        CustomSQLTemplate template = templates.get(0).first()
        template.name == "Custom SQL Template - unique"
        template.templateType == TemplateTypeEnum.CUSTOM_SQL
        template.category.name == "Regulatory"
        template.customSQLTemplateSelectFrom == "select case_num \"Case Number\" from V_C_IDENTIFICATION cm"
        template.customSQLTemplateWhere == null
        template.columnNamesList == "[Case Number]"
    }

    void "Test Load Non Case Template" () {
        given:
        def json = getNonCaseTemplateJSON()
        def listOfTemplatesJSON = "[${json}]"
        JSONElement listOfTemplates = JSON.parse(listOfTemplatesJSON)

        when:
        List<ReportTemplate> templates = importService.importTemplates(listOfTemplates)

        then:
        noExceptionThrown()
        NonCaseSQLTemplate template = templates.get(0).first()
        template.name == "Non Case Template - unique"
        template.templateType == TemplateTypeEnum.NON_CASE
        template.customSQLValues.first().key == ":compound_id"
        //examining the sql itself is too tedious to perform, but could be done if necessary
    }

    private String getCaseLineListingTemplateJSON() {
        return """
        {
        	"nameWithDescription": "CIOMS II Line Listing (Standard Template for CIOMS II Line Listing) - Owner: pvr admin",
        	"renamedRowCols": null,
        	"ownerId": 3375,
        	"modifiedBy": "pvr_admin",
        	"pageBreakByGroup": false,
        	"allSelectedFieldsInfo": [{
        		"class": "com.rxlogix.config.ReportFieldInfo",
        		"id": 1342967,
        		"argusName": "cm.CASE_NUM",
        		"blindedValue": false,
        		"commaSeparatedValue": false,
        		"customExpression": null,
        		"datasheet": null,
        		"renameValue": null,
        		"reportField": {
        			"class": "com.rxlogix.config.ReportField",
        			"id": 3523
        		}

        		,
        		"reportFieldInfoList": {
        			"class": "com.rxlogix.config.ReportFieldInfoList",
        			"id": 1342966
        		}

        		,
        		"sortEnumValue":"asc"
        		,
        		"sortLevel": 1,
        		"stackId": -1,
        		"suppressRepeatingValues": true
        	}, {
        		"class": "com.rxlogix.config.ReportFieldInfo",
        		"id": 1342968,
        		"argusName": "cm.OCCURED_COUNTRY_ID",
        		"blindedValue": false,
        		"commaSeparatedValue": false,
        		"customExpression": null,
        		"datasheet": null,
        		"renameValue": "Country",
        		"reportField": {
        			"class": "com.rxlogix.config.ReportField",
        			"id": 3546
        		}

        		,
        		"reportFieldInfoList": {
        			"class": "com.rxlogix.config.ReportFieldInfoList",
        			"id": 1342966
        		}

        		,
        		"sort": null,
        		"sortLevel": -1,
        		"stackId": 1,
        		"suppressRepeatingValues": true
        	}, {
        		"class": "com.rxlogix.config.ReportFieldInfo",
        		"id": 1342969,
        		"argusName": "cm.REPORT_TYPE_ID",
        		"blindedValue": false,
        		"commaSeparatedValue": false,
        		"customExpression": null,
        		"datasheet": null,
        		"renameValue": "Report Type",
        		"reportField": {
        			"class": "com.rxlogix.config.ReportField",
        			"id": 3547
        		}

        		,
        		"reportFieldInfoList": {
        			"class": "com.rxlogix.config.ReportFieldInfoList",
        			"id": 1342966
        		}

        		,
        		"sort": null,
        		"sortLevel": -1,
        		"stackId": 1,
        		"suppressRepeatingValues": true
        	}, {
        		"class": "com.rxlogix.config.ReportFieldInfo",
        		"id": 1342970,
        		"argusName": "cm.PAT_AGE_UNIT_GROUP",
        		"blindedValue": false,
        		"commaSeparatedValue": false,
        		"customExpression": null,
        		"datasheet": null,
        		"renameValue": "Age",
        		"reportField": {
        			"class": "com.rxlogix.config.ReportField",
        			"id": 3826
        		}

        		,
        		"reportFieldInfoList": {
        			"class": "com.rxlogix.config.ReportFieldInfoList",
        			"id": 1342966
        		}

        		,
        		"sort": null,
        		"sortLevel": -1,
        		"stackId": 2,
        		"suppressRepeatingValues": true
        	}, {
        		"class": "com.rxlogix.config.ReportFieldInfo",
        		"id": 1342971,
        		"argusName": "cm.PATIENT_GENDER_ID",
        		"blindedValue": false,
        		"commaSeparatedValue": false,
        		"customExpression": null,
        		"datasheet": null,
        		"renameValue": "Gender",
        		"reportField": {
        			"class": "com.rxlogix.config.ReportField",
        			"id": 3807
        		}

        		,
        		"reportFieldInfoList": {
        			"class": "com.rxlogix.config.ReportFieldInfoList",
        			"id": 1342966
        		}

        		,
        		"sort": null,
        		"sortLevel": -1,
        		"stackId": 2,
        		"suppressRepeatingValues": true
        	}, {
        		"class": "com.rxlogix.config.ReportFieldInfo",
        		"id": 1342972,
        		"argusName": "csda.SUSP_PROD_INFO",
        		"blindedValue": false,
        		"commaSeparatedValue": false,
        		"customExpression": null,
        		"datasheet": null,
        		"renameValue": "Product Name/ Form/ Daily Dose Dose Frequency",
        		"reportField": {
        			"class": "com.rxlogix.config.ReportField",
        			"id": 4379
        		}

        		,
        		"reportFieldInfoList": {
        			"class": "com.rxlogix.config.ReportFieldInfoList",
        			"id": 1342966
        		}

        		,
        		"sort": null,
        		"sortLevel": -1,
        		"stackId": -1,
        		"suppressRepeatingValues": true
        	}, {
        		"class": "com.rxlogix.config.ReportFieldInfo",
        		"id": 1342973,
        		"argusName": "dcpai.ROUTE_OF_ADMINISTRATION",
        		"blindedValue": false,
        		"commaSeparatedValue": false,
        		"customExpression": null,
        		"datasheet": null,
        		"renameValue": "Route",
        		"reportField": {
        			"class": "com.rxlogix.config.ReportField",
        			"id": 3877
        		}

        		,
        		"reportFieldInfoList": {
        			"class": "com.rxlogix.config.ReportFieldInfoList",
        			"id": 1342966
        		}

        		,
        		"sort": null,
        		"sortLevel": -1,
        		"stackId": -1,
        		"suppressRepeatingValues": true
        	}, {
        		"class": "com.rxlogix.config.ReportFieldInfo",
        		"id": 1342974,
        		"argusName": "csdda.SUSP_PROD_DOSE_DATES",
        		"blindedValue": false,
        		"commaSeparatedValue": false,
        		"customExpression": null,
        		"datasheet": null,
        		"renameValue": "Treatment Dates Duration",
        		"reportField": {
        			"class": "com.rxlogix.config.ReportField",
        			"id": 4386
        		}

        		,
        		"reportFieldInfoList": {
        			"class": "com.rxlogix.config.ReportFieldInfoList",
        			"id": 1342966
        		}

        		,
        		"sort": null,
        		"sortLevel": -1,
        		"stackId": -1,
        		"suppressRepeatingValues": true
        	}, {
        		"class": "com.rxlogix.config.ReportFieldInfo",
        		"id": 1342975,
        		"argusName": "dci.EVT_VERBATIM_PREF_ALL",
        		"blindedValue": false,
        		"commaSeparatedValue": false,
        		"customExpression": null,
        		"datasheet": null,
        		"renameValue": "Event Verbatim (Preferred Term)",
        		"reportField": {
        			"class": "com.rxlogix.config.ReportField",
        			"id": 3572
        		}

        		,
        		"reportFieldInfoList": {
        			"class": "com.rxlogix.config.ReportFieldInfoList",
        			"id": 1342966
        		}

        		,
        		"sort": null,
        		"sortLevel": -1,
        		"stackId": -1,
        		"suppressRepeatingValues": true
        	}, {
        		"class": "com.rxlogix.config.ReportFieldInfo",
        		"id": 1342976,
        		"argusName": "dci.ONSET_LATENCY_ALL",
        		"blindedValue": false,
        		"commaSeparatedValue": false,
        		"customExpression": null,
        		"datasheet": null,
        		"renameValue": "Event Onset Date/Time to Onset",
        		"reportField": {
        			"class": "com.rxlogix.config.ReportField",
        			"id": 3571
        		}

        		,
        		"reportFieldInfoList": {
        			"class": "com.rxlogix.config.ReportFieldInfoList",
        			"id": 1342966
        		}

        		,
        		"sort": null,
        		"sortLevel": -1,
        		"stackId": -1,
        		"suppressRepeatingValues": true
        	}, {
        		"class": "com.rxlogix.config.ReportFieldInfo",
        		"id": 1342977,
        		"argusName": "dci.EVENT_SERIOUSNESS_ALL",
        		"blindedValue": false,
        		"commaSeparatedValue": false,
        		"customExpression": null,
        		"datasheet": null,
        		"renameValue": "Event Seriousness",
        		"reportField": {
        			"class": "com.rxlogix.config.ReportField",
        			"id": 3570
        		}

        		,
        		"reportFieldInfoList": {
        			"class": "com.rxlogix.config.ReportFieldInfoList",
        			"id": 1342966
        		}

        		,
        		"sort": null,
        		"sortLevel": -1,
        		"stackId": -1,
        		"suppressRepeatingValues": true
        	}, {
        		"class": "com.rxlogix.config.ReportFieldInfo",
        		"id": 1342978,
        		"argusName": "ce.CONSER_CORE_LISTEDNESS",
        		"blindedValue": false,
        		"commaSeparatedValue": false,
        		"customExpression": null,
        		"datasheet": null,
        		"renameValue": "Event Listedness",
        		"reportField": {
        			"class": "com.rxlogix.config.ReportField",
        			"id": 232909
        		}

        		,
        		"reportFieldInfoList": {
        			"class": "com.rxlogix.config.ReportFieldInfoList",
        			"id": 1342966
        		}

        		,
        		"sort": null,
        		"sortLevel": -1,
        		"stackId": -1,
        		"suppressRepeatingValues": true
        	}, {
        		"class": "com.rxlogix.config.ReportFieldInfo",
        		"id": 1342979,
        		"argusName": "ce.CONSER_CORE_CAUSALITY",
        		"blindedValue": false,
        		"commaSeparatedValue": false,
        		"customExpression": null,
        		"datasheet": null,
        		"renameValue": "Event Reportability",
        		"reportField": {
        			"class": "com.rxlogix.config.ReportField",
        			"id": 232910
        		}

        		,
        		"reportFieldInfoList": {
        			"class": "com.rxlogix.config.ReportFieldInfoList",
        			"id": 1342966
        		}

        		,
        		"sort": null,
        		"sortLevel": -1,
        		"stackId": -1,
        		"suppressRepeatingValues": true
        	}, {
        		"class": "com.rxlogix.config.ReportFieldInfo",
        		"id": 1342980,
        		"argusName": "cm.OUTCOME",
        		"blindedValue": false,
        		"commaSeparatedValue": false,
        		"customExpression": null,
        		"datasheet": null,
        		"renameValue": "Patient Outcome",
        		"reportField": {
        			"class": "com.rxlogix.config.ReportField",
        			"id": 3591
        		}

        		,
        		"reportFieldInfoList": {
        			"class": "com.rxlogix.config.ReportFieldInfoList",
        			"id": 1342966
        		}

        		,
        		"sort": null,
        		"sortLevel": -1,
        		"stackId": -1,
        		"suppressRepeatingValues": true
        	}, {
        		"class": "com.rxlogix.config.ReportFieldInfo",
        		"id": 1342981,
        		"argusName": "cc.COMMENT_TXT",
        		"blindedValue": false,
        		"commaSeparatedValue": false,
        		"customExpression": null,
        		"datasheet": null,
        		"renameValue": "Company Comments",
        		"reportField": {
        			"class": "com.rxlogix.config.ReportField",
        			"id": 4005
        		}

        		,
        		"reportFieldInfoList": {
        			"class": "com.rxlogix.config.ReportFieldInfoList",
        			"id": 1342966
        		}

        		,
        		"sort": null,
        		"sortLevel": -1,
        		"stackId": -1,
        		"suppressRepeatingValues": true
        	}],
        	"instanceIdentifierForAuditLog": "CIOMS II Line Listing",
        	"factoryDefault": false,
        	"rowColumnListId": null,
        	"templateType": {
        		"enumType": "com.rxlogix.enums.TemplateTypeEnum",
        		"name": "CASE_LINE"
        	}

        	,
        	"renamedGrouping": null,
        	"description": "Standard Template for CIOMS II Line Listing",
        	"name": "CIOMS II Line Listing - unique",
        	"groupingListId": null,
        	"columnListId": 1342966,
        	"tags": [],
        	"categoryId": 4467,
        	"lastUpdated": "2016-06-10T08:42:04Z",
        	"qualityChecked": true,
        	"isDeleted": false,
        	"reassessListedness": null,
        	"columnList": [{
        		"reportFieldName": "masterCaseNum",
        		"stackId": -1,
        		"reportField": {
        			"id": 3523,
        			"name": "masterCaseNum",
        			"description": null,
        			"dataType": "java.lang.String",
        			"fieldGroup": "CaseInformation",
        			"listDomainClass": null,
        			"transform": "masterCaseNum",
        			"isText": false,
        			"isAutocomplete": false
        		}

        		,
        		"suppressRepeatingValues": true,
        		"argusName": "cm.CASE_NUM",
        		"blindedValue": false,
        		"reportFieldId": 3523,
        		"sortEnumValue": "asc",
        		"renameValue": null,
        		"reportFieldInfoListId": 1342966,
        		"sortLevel": 1,
        		"commaSeparatedValue": false,
        		"datasheet": null,
        		"customExpression": null
        	}, {
        		"reportFieldName": "masterCountryId",
        		"stackId": 1,
        		"reportField": {
        			"id": 3546,
        			"name": "masterCountryId",
        			"description": null,
        			"dataType": "java.lang.String",
        			"fieldGroup": "CaseInformation",
        			"listDomainClass": "com.rxlogix.TableColumnSelectableList",
        			"transform": "masterCountryId",
        			"isText": false,
        			"isAutocomplete": false
        		}

        		,
        		"suppressRepeatingValues": true,
        		"argusName": "cm.OCCURED_COUNTRY_ID",
        		"blindedValue": false,
        		"reportFieldId": 3546,
        		"sortEnumValue": null,
        		"renameValue": "Country",
        		"reportFieldInfoListId": 1342966,
        		"sortLevel": -1,
        		"commaSeparatedValue": false,
        		"datasheet": null,
        		"customExpression": null
        	}, {
        		"reportFieldName": "masterRptTypeId",
        		"stackId": 1,
        		"reportField": {
        			"id": 3547,
        			"name": "masterRptTypeId",
        			"description": null,
        			"dataType": "java.lang.String",
        			"fieldGroup": "CaseInformation",
        			"listDomainClass": "com.rxlogix.TableColumnSelectableList",
        			"transform": "masterRptTypeId",
        			"isText": false,
        			"isAutocomplete": false
        		}

        		,
        		"suppressRepeatingValues": true,
        		"argusName": "cm.REPORT_TYPE_ID",
        		"blindedValue": false,
        		"reportFieldId": 3547,
        		"sortEnumValue": null,
        		"renameValue": "Report Type",
        		"reportFieldInfoListId": 1342966,
        		"sortLevel": -1,
        		"commaSeparatedValue": false,
        		"datasheet": null,
        		"customExpression": null
        	}, {
        		"reportFieldName": "patInfoPatientAgeUnit",
        		"stackId": 2,
        		"reportField": {
        			"id": 3826,
        			"name": "patInfoPatientAgeUnit",
        			"description": null,
        			"dataType": "java.lang.String",
        			"fieldGroup": "PatientInformation",
        			"listDomainClass": null,
        			"transform": "patInfoPatientAgeUnit",
        			"isText": false,
        			"isAutocomplete": false
        		}

        		,
        		"suppressRepeatingValues": true,
        		"argusName": "cm.PAT_AGE_UNIT_GROUP",
        		"blindedValue": false,
        		"reportFieldId": 3826,
        		"sortEnumValue": null,
        		"renameValue": "Age",
        		"reportFieldInfoListId": 1342966,
        		"sortLevel": -1,
        		"commaSeparatedValue": false,
        		"datasheet": null,
        		"customExpression": null
        	}, {
        		"reportFieldName": "patInfoGenderId",
        		"stackId": 2,
        		"reportField": {
        			"id": 3807,
        			"name": "patInfoGenderId",
        			"description": null,
        			"dataType": "java.lang.String",
        			"fieldGroup": "PatientInformation",
        			"listDomainClass": "com.rxlogix.TableColumnSelectableList",
        			"transform": "patInfoGenderId",
        			"isText": false,
        			"isAutocomplete": false
        		}

        		,
        		"suppressRepeatingValues": true,
        		"argusName": "cm.PATIENT_GENDER_ID",
        		"blindedValue": false,
        		"reportFieldId": 3807,
        		"sortEnumValue": null,
        		"renameValue": "Gender",
        		"reportFieldInfoListId": 1342966,
        		"sortLevel": -1,
        		"commaSeparatedValue": false,
        		"datasheet": null,
        		"customExpression": null
        	}, {
        		"reportFieldName": "suspProdInfo",
        		"stackId": -1,
        		"reportField": {
        			"id": 4379,
        			"name": "suspProdInfo",
        			"description": null,
        			"dataType": "java.lang.String",
        			"fieldGroup": "ProductInformation",
        			"listDomainClass": null,
        			"transform": "suspProdInfo",
        			"isText": false,
        			"isAutocomplete": false
        		}

        		,
        		"suppressRepeatingValues": true,
        		"argusName": "csda.SUSP_PROD_INFO",
        		"blindedValue": false,
        		"reportFieldId": 4379,
        		"sortEnumValue": null,
        		"renameValue": "Product Name/ Form/ Daily Dose Dose Frequency",
        		"reportFieldInfoListId": 1342966,
        		"sortLevel": -1,
        		"commaSeparatedValue": false,
        		"datasheet": null,
        		"customExpression": null
        	}, {
        		"reportFieldName": "productRouteOfAdmin",
        		"stackId": -1,
        		"reportField": {
        			"id": 3877,
        			"name": "productRouteOfAdmin",
        			"description": null,
        			"dataType": "java.lang.String",
        			"fieldGroup": "TherapyInformation",
        			"listDomainClass": null,
        			"transform": "productRouteOfAdmin",
        			"isText": false,
        			"isAutocomplete": false
        		}

        		,
        		"suppressRepeatingValues": true,
        		"argusName": "dcpai.ROUTE_OF_ADMINISTRATION",
        		"blindedValue": false,
        		"reportFieldId": 3877,
        		"sortEnumValue": null,
        		"renameValue": "Route",
        		"reportFieldInfoListId": 1342966,
        		"sortLevel": -1,
        		"commaSeparatedValue": false,
        		"datasheet": null,
        		"customExpression": null
        	}, {
        		"reportFieldName": "prodSuspDoseDateAgg",
        		"stackId": -1,
        		"reportField": {
        			"id": 4386,
        			"name": "prodSuspDoseDateAgg",
        			"description": null,
        			"dataType": "java.lang.String",
        			"fieldGroup": "DosageRegimenInformation",
        			"listDomainClass": null,
        			"transform": "prodSuspDoseDateAgg",
        			"isText": false,
        			"isAutocomplete": false
        		}

        		,
        		"suppressRepeatingValues": true,
        		"argusName": "csdda.SUSP_PROD_DOSE_DATES",
        		"blindedValue": false,
        		"reportFieldId": 4386,
        		"sortEnumValue": null,
        		"renameValue": "Treatment Dates Duration",
        		"reportFieldInfoListId": 1342966,
        		"sortLevel": -1,
        		"commaSeparatedValue": false,
        		"datasheet": null,
        		"customExpression": null
        	}, {
        		"reportFieldName": "masterEvtVerbatimPrefAll",
        		"stackId": -1,
        		"reportField": {
        			"id": 3572,
        			"name": "masterEvtVerbatimPrefAll",
        			"description": null,
        			"dataType": "java.lang.String",
        			"fieldGroup": "EventInformation",
        			"listDomainClass": null,
        			"transform": "masterEvtVerbatimPrefAll",
        			"isText": false,
        			"isAutocomplete": false
        		}

        		,
        		"suppressRepeatingValues": true,
        		"argusName": "dci.EVT_VERBATIM_PREF_ALL",
        		"blindedValue": false,
        		"reportFieldId": 3572,
        		"sortEnumValue": null,
        		"renameValue": "Event Verbatim (Preferred Term)",
        		"reportFieldInfoListId": 1342966,
        		"sortLevel": -1,
        		"commaSeparatedValue": false,
        		"datasheet": null,
        		"customExpression": null
        	}, {
        		"reportFieldName": "masterOnsetLatencyAll",
        		"stackId": -1,
        		"reportField": {
        			"id": 3571,
        			"name": "masterOnsetLatencyAll",
        			"description": null,
        			"dataType": "java.lang.String",
        			"fieldGroup": "EventInformation",
        			"listDomainClass": null,
        			"transform": "masterOnsetLatencyAll",
        			"isText": false,
        			"isAutocomplete": false
        		}

        		,
        		"suppressRepeatingValues": true,
        		"argusName": "dci.ONSET_LATENCY_ALL",
        		"reportFieldId": 3571,
        		"blindedValue": false,
        		"sortEnumValue": null,
        		"renameValue": "Event Onset Date/Time to Onset",
        		"reportFieldInfoListId": 1342966,
        		"sortLevel": -1,
        		"commaSeparatedValue": false,
        		"datasheet": null,
        		"customExpression": null
        	}, {
        		"reportFieldName": "masterSeriousnessAll",
        		"stackId": -1,
        		"reportField": {
        			"id": 3570,
        			"name": "masterSeriousnessAll",
        			"description": null,
        			"dataType": "java.lang.String",
        			"fieldGroup": "AssessmentInformation",
        			"listDomainClass": null,
        			"transform": "masterSeriousnessAll",
        			"isText": false,
        			"isAutocomplete": false
        		}

        		,
        		"suppressRepeatingValues": true,
        		"argusName": "dci.EVENT_SERIOUSNESS_ALL",
        		"reportFieldId": 3570,
        		"blindedValue": false,
        		"sortEnumValue": null,
        		"renameValue": "Event Seriousness",
        		"reportFieldInfoListId": 1342966,
        		"sortLevel": -1,
        		"commaSeparatedValue": false,
        		"datasheet": null,
        		"customExpression": null
        	}, {
        		"reportFieldName": "eventConserCoreListedness",
        		"stackId": -1,
        		"reportField": {
        			"id": 232909,
        			"name": "eventConserCoreListedness",
        			"description": null,
        			"dataType": "java.lang.String",
        			"fieldGroup": "EventInformation",
        			"listDomainClass": "com.rxlogix.TableColumnSelectableList",
        			"transform": "eventConserCoreListedness",
        			"isText": false,
        			"isAutocomplete": false
        		}

        		,
        		"suppressRepeatingValues": true,
        		"argusName": "ce.CONSER_CORE_LISTEDNESS",
        		"reportFieldId": 232909,
        		"blindedValue": false,
        		"sortEnumValue": null,
        		"renameValue": "Event Listedness",
        		"reportFieldInfoListId": 1342966,
        		"sortLevel": -1,
        		"commaSeparatedValue": false,
        		"datasheet": null,
        		"customExpression": null
        	}, {
        		"reportFieldName": "eventConserCoreCausality",
        		"stackId": -1,
        		"reportField": {
        			"id": 232910,
        			"name": "eventConserCoreCausality",
        			"description": null,
        			"dataType": "java.lang.String",
        			"fieldGroup": "EventInformation",
        			"listDomainClass": null,
        			"transform": "eventConserCoreCausality",
        			"isText": false,
        			"isAutocomplete": false
        		}

        		,
        		"suppressRepeatingValues": true,
        		"argusName": "ce.CONSER_CORE_CAUSALITY",
        		"blindedValue": false,
        		"reportFieldId": 232910,
        		"sortEnumValue": null,
        		"renameValue": "Event Reportability",
        		"reportFieldInfoListId": 1342966,
        		"sortLevel": -1,
        		"commaSeparatedValue": false,
        		"datasheet": null,
        		"customExpression": null
        	}, {
        		"reportFieldName": "assessOutcome",
        		"stackId": -1,
        		"reportField": {
        			"id": 3591,
        			"name": "assessOutcome",
        			"description": null,
        			"dataType": "java.lang.String",
        			"fieldGroup": "CaseInformation",
        			"listDomainClass": "com.rxlogix.TableColumnSelectableList",
        			"transform": "assessOutcome",
        			"isText": false,
        			"isAutocomplete": false
        		}

        		,
        		"suppressRepeatingValues": true,
        		"argusName": "cm.OUTCOME",
        		"blindedValue": false,
        		"reportFieldId": 3591,
        		"sortEnumValue": null,
        		"renameValue": "Patient Outcome",
        		"reportFieldInfoListId": 1342966,
        		"sortLevel": -1,
        		"commaSeparatedValue": false,
        		"datasheet": null,
        		"customExpression": null
        	}, {
        		"reportFieldName": "companyCmtsCommentTxt",
        		"stackId": -1,
        		"reportField": {
        			"id": 4005,
        			"name": "companyCmtsCommentTxt",
        			"description": null,
        			"dataType": "java.lang.String",
        			"fieldGroup": "CaseInformation",
        			"listDomainClass": null,
        			"transform": "companyCmtsCommentTxt",
        			"isText": false,
        			"isAutocomplete": false
        		}

        		,
        		"suppressRepeatingValues": true,
        		"argusName": "cc.COMMENT_TXT",
        		"blindedValue": false,
        		"reportFieldId": 4005,
        		"sortEnumValue": null,
        		"renameValue": "Company Comments",
        		"reportFieldInfoListId": 1342966,
        		"sortLevel": -1,
        		"commaSeparatedValue": false,
        		"datasheet": null,
        		"customExpression": null
        	}],
        	"originalTemplateId": 0,
        	"editable": true,
        	"hasBlanks": false,
        	"fieldNameWithIndex": ["masterCaseNum_0", "masterCountryId_1", "masterRptTypeId_2", "patInfoPatientAgeUnit_3", "patInfoGenderId_4", "suspProdInfo_5", "productRouteOfAdmin_6", "prodSuspDoseDateAgg_7", "masterEvtVerbatimPrefAll_8", "masterOnsetLatencyAll_9", "masterSeriousnessAll_10", "eventConserCoreListedness_11", "eventConserCoreCausality_12", "assessOutcome_13", "companyCmtsCommentTxt_14"],
        	"category": {
        		"class": "com.rxlogix.config.Category",
        		"id": 4467,
        		"defaultName": true,
        		"name": "Case Listing"
        	}

        	,
        	"createdBy": "pvr_admin",
        	"columnShowTotal": false,
        	"columnShowSubTotal": false,
        	"columnShowDistinct": false,
        	"ciomsI": false,
        	"suppressRepeatingValuesColumnList": null,
        	"owner": {
        		"id": 3375,
        		"username": "pvr_admin",
        		"fullName": "pvr admin"
        	}

        	,
        	"dateCreated": "2016-06-10T07:39:17Z",
        	"rowColumnList": null,
        	"groupingList": null
        }
        """.stripIndent()
    }

    private String getDataTabulationTemplateJSON() {
        return """
        {
            "nameWithDescription": "Data Tabulation Template - unique  - Owner: Dev User",
            "rowListId": 6448,
            "ownerId": 15,
            "modifiedBy": "dev",
            "allSelectedFieldsInfo": [{
                "class": "com.rxlogix.config.ReportFieldInfo",
                "id": 6451,
                "argusName": "cm.REPORT_TYPE_ID",
                "blindedValue": false,
                "commaSeparatedValue": false,
                "customExpression": null,
                "datasheet": null,
                "renameValue": null,
                "reportField": {
                    "class": "com.rxlogix.config.ReportField",
                    "id": 622
                },
                "reportFieldInfoList": {
                    "class": "com.rxlogix.config.ReportFieldInfoList",
                    "id": 6450
                },
                "sort": null,
                "sortLevel": -1,
                "stackId": -1,
                "suppressRepeatingValues": false
            }, {
                "class": "com.rxlogix.config.ReportFieldInfo",
                "id": 6449,
                "argusName": "cf.SERIOUSNESS_FLAG",
                "blindedValue": false,
                "commaSeparatedValue": false,
                "customExpression": null,
                "datasheet": null,
                "renameValue": null,
                "reportField": {
                    "class": "com.rxlogix.config.ReportField",
                    "id": 348
                },
                "reportFieldInfoList": {
                    "class": "com.rxlogix.config.ReportFieldInfoList",
                    "id": 6448
                },
                "sort": null,
                "sortLevel": -1,
                "stackId": -1,
                "suppressRepeatingValues": false
            }],
            "showChartSheet": false,
            "instanceIdentifierForAuditLog": "Data Tabulation Template - unique",
            "columnMeasureList": [{
                "showTotalIntervalCases": false,
                "showTotalCumulativeCases": false,
                "measures": [{
                    "class": "com.rxlogix.config.DataTabulationMeasure",
                    "id": 6452,
                    "customPeriodFrom": null,
                    "customPeriodTo": null,
                    "dateRangeCount": {
                        "enumType": "com.rxlogix.reportTemplate.CountTypeEnum",
                        "name": "PERIOD_COUNT"
                    },
                    "name": "Case Count",
                    "percentageOption": {
                        "enumType": "com.rxlogix.reportTemplate.PercentageOptionEnum",
                        "name": "NO_PERCENTAGE"
                    },
                    "showSubtotalRowAfterGroups": false,
                    "showTotal": false,
                    "showTotalAsColumn": false,
                    "showTotalRowOnly": false,
                    "type": {
                        "enumType": "com.rxlogix.reportTemplate.MeasureTypeEnum",
                        "name": "CASE_COUNT"
                    }
                }],
                "columnListId": 6450,
                "columnList": [{
                    "reportFieldName": "masterRptTypeId",
                    "stackId": -1,
                    "reportField": {
                        "id": 622,
                        "name": "masterRptTypeId",
                        "description": null,
                        "dataType": "java.lang.String",
                        "fieldGroup": "CaseInformation",
                        "listDomainClass": "com.rxlogix.TableColumnSelectableList",
                        "transform": "masterRptTypeId",
                        "isText": false,
                        "isAutocomplete": false
                    },
                    "suppressRepeatingValues": false,
                    "argusName": "cm.REPORT_TYPE_ID",
                    "blindedValue": false,
                    "reportFieldId": 622,
                    "sortEnumValue": null,
                    "renameValue": null,
                    "reportFieldInfoListId": 6450,
                    "sortLevel": -1,
                    "commaSeparatedValue": false,
                    "datasheet": null,
                    "customExpression": null
                }]
            }],
            "factoryDefault": false,
            "templateType": {
                "enumType": "com.rxlogix.enums.TemplateTypeEnum",
                "name": "DATA_TAB"
            },
            "rowList": [{
                "reportFieldName": "assessSeriousness",
                "stackId": -1,
                "reportField": {
                    "id": 348,
                    "name": "assessSeriousness",
                    "description": null,
                    "dataType": "java.lang.String",
                    "fieldGroup": "CaseInformation",
                    "listDomainClass": "com.rxlogix.TableColumnSelectableList",
                    "transform": "assessSeriousness",
                    "isText": false,
                    "isAutocomplete": false
                },
                "suppressRepeatingValues": false,
                "argusName": "cf.SERIOUSNESS_FLAG",
                "blindedValue": false,
                "reportFieldId": 348,
                "sortEnumValue": null,
                "renameValue": null,
                "reportFieldInfoListId": 6448,
                "sortLevel": -1,
                "commaSeparatedValue": false,
                "datasheet": null,
                "customExpression": null
            }],
            "description": null,
            "name": "Data Tabulation Template - unique",
            "tags": [{
                "id": 1184,
                "name": "Site Productivity"
            }],
            "categoryId": 1171,
            "lastUpdated": "2016-06-14T22:15:21Z",
            "JSONStringMeasures": "[[{\'customPeriodTo\':null,\'showTotal\':false,\'count\':\'PERIOD_COUNT\',\'percentage\':\'NO_PERCENTAGE\',\'name\':\'Case Count\',\'customPeriodFrom\':null,\'type\':\'CASE_COUNT\'}]]",
            "qualityChecked": true,
            "isDeleted": false,
            "reassessListedness": null,
            "originalTemplateId": 0,
            "editable": true,
            "hasBlanks": false,
            "category": {
                "class": "com.rxlogix.config.Category",
                "id": 1171,
                "defaultName": true,
                "name": "Submissions"
            },
            "createdBy": "dev",
            "ciomsI": false,
            "owner": {
                "id": 15,
                "username": "dev",
                "fullName": "Dev User"
            },
            "dateCreated": "2016-06-14T22:15:21Z"
        }
        """.stripIndent()
    }

    private String getCustomSQLTemplateJSON() {
        return """
        {
            "nameWithDescription": "Custom SQL Template (With Category and Tag) - Owner: Dev User",
            "ownerId": 15,
            "modifiedBy": "dev",
            "allSelectedFieldsInfo": null,
            "columnNamesList": "[Case Number]",
            "instanceIdentifierForAuditLog": "Custom SQL Template",
            "factoryDefault": false,
            "customSQLTemplateSelectFrom": 'select case_num \"Case Number\" from V_C_IDENTIFICATION cm',
            "templateType": {
                "enumType": "com.rxlogix.enums.TemplateTypeEnum",
                "name": "CUSTOM_SQL"
            },
            "customSQLTemplateWhere": null,
            "description": "With Category and Tag",
            "customSQLValues": [],
            "name": "Custom SQL Template - unique",
            "tags": [{
                "id": 1180,
                "name": "Data entry"
            }],
            "categoryId": 1174,
            "lastUpdated": "2016-06-14T21:49:54Z",
            "qualityChecked": true,
            "isDeleted": false,
            "reassessListedness": null,
            "originalTemplateId": 0,
            "editable": true,
            "hasBlanks": false,
            "category": {
                "class": "com.rxlogix.config.Category",
                "id": 1174,
                "defaultName": true,
                "name": "Regulatory"
            },
            "createdBy": "dev",
            "ciomsI": false,
            "owner": {
                "id": 15,
                "username": "dev",
                "fullName": "Dev User"
            },
            "dateCreated": "2016-06-14T21:49:54Z"
        }
        """.stripIndent()
    }

    private String getNonCaseTemplateJSON() {
        return """
        {
            "nameWithDescription": "Non Case Template  - Owner: Dev User",
            "ownerId": 15,
            "modifiedBy": "dev",
            "allSelectedFieldsInfo": null,
            "columnNamesList": "[]",
            "instanceIdentifierForAuditLog": "Non Case Template",
            "factoryDefault": false,
            "templateType": {
                "enumType": "com.rxlogix.enums.TemplateTypeEnum",
                "name": "NON_CASE"
            },
            "nonCaseSql": "SELECT DISTINCT (SELECT li.ingredient FROM VW_FAMILY_NAME_DSP lpf,  vw_lpi_family_id lpi, VW_INGREDIENT_DSP li  WHERE lp.prod_family_id = lpf.prod_family_id  AND lpi.prod_family_id = lpf.prod_family_id   AND lpi.ingredient_id = li.ingredient_id  AND ROWNUM = 1) ingredient, lp.product_name  || ' ['|| (SELECT lf.formulation FROM vw_lfor_formulation lf  WHERE lf.formulation_id = lp.formulation_id) || ']' productname,  NVL((SELECT ll.trade_name || ' [' || (SELECT ld.sheet_name  FROM VW_DATASHEET_NAME ld   WHERE ld.datasheet_id = ll.datasheet_id AND ROWNUM = 1 )  || ']'  FROM VW_PROD_LICENSE_LINK_DSP llp, VW_TRADE_NAME_DSP ll WHERE llp.product_id = lp.product_id  AND llp.license_id = ll.license_id  AND ll.withdraw_date IS NULL AND ROWNUM = 1), 'WITHDRAWN'  ) \\"PRODUCT LICENSE [DATASHEET]\\"  FROM c_prod_identification cp, VW_PRODUCT_DSP lp, c_study_identification cs WHERE cs.case_id = cp.case_id  AND NVL (cp.product_id, cp.FLAG_STUDY_DRUG) = lp.product_id  AND cp.PROD_DRUG_CODE = 'Study Drug' AND cs.study_number LIKE :compound_id || '-%'  ORDER BY ingredient ASC",
            "description": null,
            "customSQLValues": [{
                "class": "com.rxlogix.config.CustomSQLValue",
                "id": 6421,
                "isFromCopyPaste": false,
                "key": ":compound_id",
                "value": null
            }],
            "name": "Non Case Template - unique",
            "tags": [],
            "categoryId": null,
            "lastUpdated": "2016-06-14T21:35:20Z",
            "qualityChecked": true,
            "isDeleted": false,
            "reassessListedness": null,
            "originalTemplateId": 0,
            "editable": true,
            "usePvrDB": false,
            "hasBlanks": true,
            "category": null,
            "createdBy": "dev",
            "ciomsI": false,
            "owner": {
                "id": 15,
                "username": "dev",
                "fullName": "Dev User"
            },
            "dateCreated": "2016-06-14T21:35:20Z"
        }
       """.stripIndent()
    }
}
