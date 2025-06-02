package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.enums.PeriodicReportTypeEnum
import com.rxlogix.enums.ReportExecutionStatusEnum
import com.rxlogix.enums.*
import com.rxlogix.user.User

class ExecutorDTO {

    //Common parameter in both Case series and Report Configuration artifacts
    SuperQuery globalQuery
    boolean includeLockedVersion = true
    boolean includeAllStudyDrugsCases = false
    boolean excludeFollowUp = false
    EvaluateCaseDateEnum evaluateDateAs
    Date asOfVersionDate
    String productSelection
    String productGroupSelection
    String studySelection
    boolean excludeNonValidCases = true
    boolean excludeDeletedCases = true
    boolean suspectProduct = false
    String usedEventSelection
    String usedEventGroupSelection
    User owner
    boolean limitPrimaryPath = false
    Date nextRunDate = new Date()

    // For case series integration
    String caseSeriesOwner = Constants.PVR_CASE_SERIES_OWNER

    // Parameter specific to Report Configuration Artifact
    ReportResultDTO reportResultDTO = new ReportResultDTO()
    Integer numOfExecutions = 0

    String name
    DateRangeEnum dateRange
    DateRangeType dateRangeType
    SourceProfile sourceProfile
    PeriodicReportTypeEnum periodicReportTypeEnum
    Date startDate = null
    Date endDate = null
    Class aClass
    List<QueryValueList> globalQueryValueLists = []
    Set<ParameterValue> poiInputsParameterValues
    Locale locale

    Boolean isCumulative
    Long reportId
    Long caseSeriesId
    String reportName
    String primaryDestination
    boolean includePreviousMissingCases = false
    boolean includeOpenCasesInDraft = false
    boolean includeNonSignificantFollowUp = false
    Long tenantId
    boolean isMultiIngredient = false
    boolean includeWHODrugs = false

    public void commonProperties(def object) {
        this.includeLockedVersion = object.includeLockedVersion
        this.includeAllStudyDrugsCases = object.includeAllStudyDrugsCases
        this.excludeFollowUp = object.excludeFollowUp
        this.productSelection = object.productSelection
        this.studySelection = object.studySelection
        this.excludeNonValidCases = object.excludeNonValidCases
        this.excludeDeletedCases = object.excludeDeletedCases
        this.suspectProduct = object.suspectProduct
        this.owner = object.owner
        this.evaluateDateAs = object.evaluateDateAs
        this.aClass = object.class
        this.tenantId = object.tenantId
        this.isMultiIngredient = object.isMultiIngredient
        this.includeWHODrugs = object.includeWHODrugs
    }

    public void baseConfigurationProperties(BaseConfiguration baseConfiguration) {
        this.commonProperties(baseConfiguration)
        this.reportId = baseConfiguration.id
        this.nextRunDate = baseConfiguration.nextRunDate
        this.dateRangeType = baseConfiguration.dateRangeType
        this.sourceProfile = baseConfiguration.sourceProfile
        this.limitPrimaryPath = baseConfiguration.limitPrimaryPath
        this.asOfVersionDate = baseConfiguration.getAsOfVersionDate()
        this.name = baseConfiguration.reportName
        this.usedEventSelection = baseConfiguration.usedEventSelection
        this.productGroupSelection = baseConfiguration.productGroupSelection
        this.usedEventGroupSelection = baseConfiguration.usedEventGroupSelection
        this.includeNonSignificantFollowUp = baseConfiguration.includeNonSignificantFollowUp
        if (baseConfiguration instanceof ReportConfiguration) {
            this.poiInputsParameterValues = baseConfiguration.poiInputsParameterValues
            this.globalQueryValueLists = baseConfiguration.globalQueryValueLists
            this.globalQuery = baseConfiguration.globalQuery
            this.locale = baseConfiguration.owner?.preference?.locale
            if (baseConfiguration instanceof PeriodicReportConfiguration) {
                this.includePreviousMissingCases = baseConfiguration.includePreviousMissingCases
                this.periodicReportTypeEnum = baseConfiguration?.periodicReportType
                this.primaryDestination = baseConfiguration?.primaryReportingDestination
                this.includeOpenCasesInDraft = baseConfiguration.includeOpenCasesInDraft && baseConfiguration.generateCaseSeries
                if (baseConfiguration.globalDateRangeInformation) {
                    List dateRanges = baseConfiguration.globalDateRangeInformation.getReportStartAndEndDate()
                    this.startDate = dateRanges[0]
                    this.endDate = dateRanges[1]
                    this.dateRange = baseConfiguration?.globalDateRangeInformation?.dateRangeEnum
                }
            }
        } else if (baseConfiguration instanceof ExecutedReportConfiguration) {
            this.numOfExecutions = baseConfiguration.numOfExecutions
            this.globalQuery = baseConfiguration.executedGlobalQuery
            this.locale = baseConfiguration.locale
            if (baseConfiguration instanceof ExecutedPeriodicReportConfiguration) {
                this.includePreviousMissingCases = baseConfiguration.includePreviousMissingCases
                this.periodicReportTypeEnum = baseConfiguration?.periodicReportType
                this.primaryDestination = baseConfiguration?.primaryReportingDestination
                this.includeOpenCasesInDraft = baseConfiguration.includeOpenCasesInDraft && baseConfiguration.hasGeneratedCasesData && (baseConfiguration.status in [ReportExecutionStatusEnum.GENERATING_DRAFT, ReportExecutionStatusEnum.GENERATING_NEW_SECTION])
                if (baseConfiguration.executedGlobalDateRangeInformation) {
                    List dateRanges = baseConfiguration.executedGlobalDateRangeInformation.getReportStartAndEndDate()
                    this.startDate = dateRanges[0]
                    this.endDate = dateRanges[1]
                    this.dateRange = baseConfiguration?.executedGlobalDateRangeInformation?.dateRangeEnum
                }
            }
        }
    }

    ExecutorDTO() {}

    public ExecutorDTO(BaseCaseSeries caseSeries) {
        this.commonProperties(caseSeries)
        this.caseSeriesId = caseSeries.id
        ExecutedReportConfiguration executedReportConfiguration
        //for case series asOfVersionDate would be new date1
        this.asOfVersionDate = caseSeries.asOfVersionDate ?: new Date()
        if(caseSeries instanceof ExecutedCaseSeries) {
            this.globalQuery = caseSeries.executedGlobalQuery
            this.dateRange = caseSeries.executedCaseSeriesDateRangeInformation.dateRangeEnum
            List dateRanges = caseSeries.executedCaseSeriesDateRangeInformation.getReportStartAndEndDate()
            this.startDate = dateRanges[0]
            this.endDate = dateRanges[1]
            this.globalQueryValueLists = caseSeries.executedGlobalQueryValueLists
            executedReportConfiguration = caseSeries.findAssociatedConfiguration()
            this.caseSeriesOwner = caseSeries.caseSeriesOwner
        }
        else if(caseSeries instanceof CaseSeries){
            this.dateRange = caseSeries.caseSeriesDateRangeInformation.dateRangeEnum
            this.startDate = caseSeries.caseSeriesDateRangeInformation.dateRangeStartAbsolute
            this.endDate = caseSeries.caseSeriesDateRangeInformation.dateRangeEndAbsolute
        }
        this.name = caseSeries.seriesName
        this.usedEventSelection = caseSeries.eventSelection
        this.dateRangeType = caseSeries.dateRangeType
        this.locale = caseSeries.locale
        this.numOfExecutions = caseSeries.numExecutions
        this.productGroupSelection = caseSeries.productGroupSelection
        this.usedEventGroupSelection = caseSeries.eventGroupSelection
        if (executedReportConfiguration && executedReportConfiguration instanceof ExecutedPeriodicReportConfiguration) {
            this.includePreviousMissingCases = executedReportConfiguration.includePreviousMissingCases
            this.primaryDestination = executedReportConfiguration?.primaryReportingDestination
            this.periodicReportTypeEnum = executedReportConfiguration.periodicReportType
            this.isCumulative = false
            this.reportId = executedReportConfiguration.id
            this.reportName = executedReportConfiguration.reportName
            this.includeOpenCasesInDraft = executedReportConfiguration.includeOpenCasesInDraft && executedReportConfiguration.hasGeneratedCasesData
            if(executedReportConfiguration.cumulativeCaseSeriesId == caseSeries.id){
                this.isCumulative = true
            }
        }
    }

    public ExecutorDTO(ReportConfiguration reportConfiguration, ExecutedReportConfiguration executedReportConfiguration, boolean isExtraCumulative) {
        this.baseConfigurationProperties(reportConfiguration)
        this.numOfExecutions = executedReportConfiguration.numOfExecutions
        updateExtraCumulative(reportConfiguration, executedReportConfiguration, isExtraCumulative)
    }

    //used for generateCasesResult method with isExtraCumulative parameter check
    public void updateExtraCumulative(ReportConfiguration reportConfiguration, ExecutedReportConfiguration executedReportConfiguration, boolean isExtraCumulative) {
        List dateRanges = DateRangeEnum.cumulativeDateRange
        this.startDate = dateRanges[0]
        this.endDate = dateRanges[1]
        if (!isExtraCumulative && executedReportConfiguration.instanceOf(ExecutedPeriodicReportConfiguration) && reportConfiguration.globalDateRangeInformation) {
            dateRanges = reportConfiguration.globalDateRangeInformation.getReportStartAndEndDate()
            this.startDate = dateRanges[0]
            this.endDate = dateRanges[1]
        }
    }

    //Static methods
    public static ExecutorDTO create(BaseConfiguration baseConfiguration) {
        ExecutorDTO executorDTO = new ExecutorDTO()
        executorDTO.baseConfigurationProperties(baseConfiguration)
        executorDTO
    }

    //Static methods
    public static ExecutorDTO create(BaseCaseSeries caseSeries) {
        new ExecutorDTO(caseSeries)
    }

    public
    static ExecutorDTO create(ReportConfiguration reportConfiguration, ExecutedReportConfiguration executedReportConfiguration, boolean isExtraCumulative) {
        new ExecutorDTO(reportConfiguration, executedReportConfiguration, isExtraCumulative)
    }

    public String getValidProductGroupSelection() {
        if (productGroupSelection && productGroupSelection != "[]") {
            return productGroupSelection
        }
        return null
    }

    public String getUsedValidEventGroupSelection(){
        if (usedEventGroupSelection && usedEventGroupSelection != "[]") {
            return usedEventGroupSelection
        }
        return null
    }
}
