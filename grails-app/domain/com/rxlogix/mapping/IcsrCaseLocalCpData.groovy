package com.rxlogix.mapping

import com.rxlogix.Constants
/**
 * Stores local CP data for a case and profile
 */
class IcsrCaseLocalCpData implements Serializable {

    Long intakeCaseId
    Long profileId
    String profileName
    Date dateCreated
    Date lastUpdated
    boolean isDeleted = false

    public IcsrCaseLocalCpData() {}

    public IcsrCaseLocalCpData(Long intakeCaseId, Long profileId, String profileName, Date dateCreated, Date lastUpdated, boolean isDeleted) {
        this.intakeCaseId = intakeCaseId
        this.profileId = profileId
        this.profileName = profileName
        this.dateCreated = dateCreated
        this.lastUpdated = lastUpdated
        this.isDeleted = isDeleted
    }

    static constraints = {
        intakeCaseId(nullable: false)
        profileId(nullable: false)
        profileName(nullable: false)
        dateCreated(nullable: false)
        lastUpdated(nullable: false)
        isDeleted(nullable: false)
    }

    static mapping = {
        datasource Constants.PVCM
        table "ICSR_LOCAL_CP_DATA"
        id column: 'ID', generator: "sequence", params: [sequence: "SEQ_APP_MESSAGE_QUEUE"]
        version false
        intakeCaseId column: 'INTAKE_CASE_ID'
        profileId column: 'PROFILE_ID'
        profileName column: 'PROFILE_NAME'
        dateCreated column: 'CREATED_DATE'
        lastUpdated column: 'UPDATED_DATE'
        isDeleted column: 'IS_DELETED'
    }

}
