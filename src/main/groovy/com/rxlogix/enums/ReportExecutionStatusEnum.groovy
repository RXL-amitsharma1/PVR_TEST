package com.rxlogix.enums

public enum ReportExecutionStatusEnum {
    SCHEDULED("Scheduled"),
    GENERATING("Generating"),
    DELIVERING("Delivering"),
    COMPLETED("Completed"),
    ERROR("Error"),
    WARN("Warn"),
    BACKLOG("Backlog"),
//    Statuses for ExecutingConfiguration
    GENERATING_NEW_SECTION("Generating New Section"),
    GENERATED_CASES("Generated Cases"),
    GENERATING_DRAFT("Generating Draft"),
    GENERATING_FINAL_DRAFT("Generating Final Draft"),
    GENERATED_DRAFT("Generated Draft"),
    GENERATED_FINAL_DRAFT("Generated Final Draft"),
    SUBMITTED("Submitted"),
    GENERATED_NEW_SECTION("Generated New Section"),
    GENERATING_ON_DEMAND_SECTION("Generating On Demand Section"),
    GENERATED_ON_DEMAND_SECTION("Generated On Demand Section")


    private final String val

    ReportExecutionStatusEnum(String val) {
        this.val = val
    }

    String value() { return val }

    public getI18nKey() {
        return "app.executionStatus.${this.name()}"
    }

    public getI18nValueForExecutionStatusDropDown(){
        return "app.executionStatus.dropDown.${this.name()}"
    }

    public getI18nValueForAggregateReportStatus() {
        return "app.executionStatus.aggregateReportStatus.${this.name()}"
    }

    public static List<ReportExecutionStatusEnum> getReportsListingStatuses() {
        return [COMPLETED, GENERATED_CASES, GENERATING_DRAFT, GENERATED_DRAFT, GENERATING_FINAL_DRAFT, GENERATED_FINAL_DRAFT, SUBMITTED, GENERATING_NEW_SECTION, GENERATING_ON_DEMAND_SECTION]
    }

    public static List<ReportExecutionStatusEnum> getReportsListingStatusesForPublisher() {
        return [COMPLETED, GENERATED_CASES, GENERATED_DRAFT, GENERATED_FINAL_DRAFT, SUBMITTED]
    }

    static List<ReportExecutionStatusEnum> getCompletedStatusesList() {
        return [COMPLETED, WARN]
    }

    static List<ReportExecutionStatusEnum> getInProgressStatusesList() {
        return [GENERATING, DELIVERING, GENERATING_DRAFT, GENERATING_FINAL_DRAFT,GENERATING_NEW_SECTION, GENERATING_ON_DEMAND_SECTION]
    }

    static List<ReportExecutionStatusEnum> getAllList() {
        return [SCHEDULED, GENERATING, DELIVERING, COMPLETED, ERROR, WARN, BACKLOG, GENERATED_CASES, GENERATING_NEW_SECTION, GENERATING_ON_DEMAND_SECTION, GENERATING_DRAFT, GENERATING_FINAL_DRAFT, GENERATED_DRAFT, GENERATED_FINAL_DRAFT, SUBMITTED]
    }

    static List<ReportExecutionStatusEnum> getExeuctionStatusList(){
        return [GENERATING, BACKLOG, SCHEDULED , COMPLETED, ERROR]
    }

    static List<ReportExecutionStatusEnum> getExecutionStatusForICList(){
        return [GENERATING, COMPLETED, ERROR]
    }

    String getKey(){
        name()
    }
}
