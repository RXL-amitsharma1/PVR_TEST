package com.rxlogix

class ReportResultDTO {

    Long versionRows = 0L
    Long versionTime = 0L
    Long versionRowsFilter = 0L
    Long filterVersionTime = 0L
    Long reAssessTime =0L
    Long queryRows = 0L
    Long queryTime = 0L

    public String toString() {
        return "versionRows: ${versionRows},versionTime: ${versionTime},versionRowsFilter: ${versionRowsFilter},filterVersionTime: ${filterVersionTime}," +
                "reAssessTime: ${reAssessTime},queryRows: ${queryRows},queryTime: ${queryTime} "
    }
}
