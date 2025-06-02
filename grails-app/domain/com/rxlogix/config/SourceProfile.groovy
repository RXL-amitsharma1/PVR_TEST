package com.rxlogix.config

import com.rxlogix.Constants
import com.rxlogix.enums.SourceProfileTypeEnum
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.user.UserGroupUser
import grails.plugins.orm.auditable.CollectionSnapshotAudit

@CollectionSnapshotAudit
class SourceProfile {
    static auditable = true

    int sourceId
    String sourceName
    String sourceAbbrev
    Boolean isCentral = false
    Boolean isDeleted = false
    SourceProfileTypeEnum sourceProfileTypeEnum = SourceProfileTypeEnum.SINGLE
    String caseNumberFieldName = "masterCaseNum"
    Set<DateRangeType> dateRangeTypes = []
    boolean includeLatestVersionOnly = false

    static hasMany = [dateRangeTypes:DateRangeType]

    static constraints = {
        sourceId unique: true
        sourceName unique: true
        sourceAbbrev unique: true, maxSize: 5, minSize: 3
    }

    static mapping = {
        table name: "SOURCE_PROFILE"
        sourceId column: "SOURCE_ID"
        sourceName column: "SOURCE_NAME"
        sourceAbbrev column: "SOURCE_ABBREVIATION"
        isCentral column: "IS_CENTRAL"
        isDeleted column: "IS_DELETED"
        sourceProfileTypeEnum column: "SOURCE_TYPE"
        caseNumberFieldName column: "CASE_NUMBER_FIELD_NAME"
        dateRangeTypes joinTable: [name: "SRC_PROFILE_DATE_RANGE_MAP", column: "DATE_RANGE_ID", key: "SRC_PROFILE_ID"]
        includeLatestVersionOnly column: "INCLU_LATEST_VER_ONLY"

    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        SourceProfile that = (SourceProfile) o
        if(sourceName != that.sourceName) return false
        if(sourceAbbrev != that.sourceAbbrev) return false
        if (sourceId != that.sourceId) return false
        if (isDeleted != that.isDeleted) return false
        return true
    }

    int hashCode() {
        int result
        result = (sourceId != null ? sourceId.hashCode() : 0)
        result = 31 * result + (isDeleted ? 1 : 0)
        return result
    }

    static SourceProfile getCentral() {
        findByIsCentralAndIsDeleted(true, false)
    }

    static Set<SourceProfile> sourceProfilesForUser(User user) {
        Set<SourceProfile> sourceProfiles = []
        if (UserGroupUser.countByUser(user)) {
            List<UserGroup> userGroups = UserGroup.fetchAllUserGroupByUser(user)
            sourceProfiles = userGroups.collect { it.sourceProfiles }?.flatten()?.sort {
                it.sourceId
            } as Set<SourceProfile>
        } else {
            sourceProfiles = sortedSourceProfiles() as Set<SourceProfile>
        }
        sourceProfiles = sourceProfiles.size() == 1 ? sourceProfiles : sourceProfiles << fetchAllDataSource()
        return sourceProfiles
    }

    static fetchAllDataSource(){
        SourceProfile.findBySourceProfileTypeEnum(SourceProfileTypeEnum.ALL)
    }

    static List<SourceProfile> sortedSourceProfiles() {
        SourceProfile.findAllByIsDeletedAndSourceProfileTypeEnum(false, SourceProfileTypeEnum.SINGLE)?.sort {
            it.sourceId
        }
    }

    static List<SourceProfile> sortedCentralAndAffliateSourceProfiles() {
        List<SourceProfile> sourceProfiles = []
        sourceProfiles.add(getCentral())
        sourceProfiles.addAll(SourceProfile.findAllByIsDeletedAndSourceAbbrev(false, Constants.AFFLIATE_DATA_SOURCE))
        sourceProfiles?.sort {
            it.sourceId
        }
    }

    static Integer countSourceProfiles() {
        SourceProfile.countByIsDeletedAndSourceAbbrevNotEqualAndSourceProfileTypeEnum(false, Constants.EVDAS_DATA_SOURCE, SourceProfileTypeEnum.SINGLE) {
            it
        }
    }

    String getInstanceIdentifierForAuditLog() {
        return "${sourceName} (${(sourceAbbrev)})"
    }

    public static List<String> fetchAllCaseNumberFieldNames(){
        List caseNumberFieldNames = createCriteria().list {
            projections {
                distinct('caseNumberFieldName')
            }
        }

        //To handle CaseNumber(J)
        caseNumberFieldNames.add("masterCaseNumJ")

        return caseNumberFieldNames
    }

    @Override
    public String toString() {
        return sourceName
    }
}