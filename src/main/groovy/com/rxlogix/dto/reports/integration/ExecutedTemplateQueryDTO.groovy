package com.rxlogix.dto.reports.integration


import com.rxlogix.dto.caseSeries.integration.ExecutedDateRangeInfoDTO
import com.rxlogix.dto.caseSeries.integration.QueryValueListDTO
import com.rxlogix.enums.GranularityEnum
import com.rxlogix.enums.QueryLevelEnum

class ExecutedTemplateQueryDTO {
    Long templateId
    Long queryId

    ExecutedDateRangeInfoDTO executedTemplateQueryDateRangeInfoDTO

    List templateValueLists
    List<QueryValueListDTO> executedQueryValueListDTOList

    String header
    String title
    String footer

    boolean headerProductSelection = false
    boolean headerDateRange = false
    boolean blindProtected = false // Used for CIOMS I Template.
    boolean privacyProtected = false // Used for CIOMS I Template.
    GranularityEnum granularity
    Date reassessListednessDate
    Date templtReassessDate

    QueryLevelEnum queryLevel = QueryLevelEnum.CASE
}
