package com.rxlogix.config

import com.rxlogix.enums.ReportFormatEnum
import com.rxlogix.hibernate.EscapedILikeExpression
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import grails.plugins.orm.auditable.AuditEntityIdentifier
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import org.hibernate.sql.JoinType

@CollectionSnapshotAudit
class Capa8D {
    static auditable = true
    static final int REMARKS_MAX_LENGTH = 255
    static final int DESCRIPTION_MAX_BYTES = 32000
    Long id
    @AuditEntityIdentifier
    String issueNumber

    String issueType

    User teamLead

    Date lastStatusChanged

    User initiator
    User approvedBy

    String description

    String rootCause
    String verificationResults
    String comments
    String category

    List<User> sharedWith = []
    List<UserGroup> sharedWithGroup = []
    List<String> emailToUsers = []
    List<ReportFormatEnum> attachmentFormats = []
    EmailConfiguration emailConfiguration

    //Standard fields
    Date dateCreated = new Date()
    Date lastUpdated = new Date()
    String createdBy
    String modifiedBy

    ExecutedReportConfiguration configuration
    ReportSubmission submission
    boolean attachmentChecked = false
    boolean isDeleted = false
    String ownerType

    public void setDescription(String d) {
        description = d?.replaceAll("\r", "")
    }

    public String getDescription() {
        return description
    }

    static hasMany = [
            teamMembers      : User,
            correctiveActions: ActionItem,
            preventiveActions: ActionItem,
            sharedWith       : User,
            sharedWithGroup  : UserGroup,
            emailToUsers     : String,
            attachmentFormats: ReportFormatEnum,
            attachments      : Capa8DAttachment
    ]

    static belongsTo = [QualityCaseData, QualitySampling, QualitySubmission]

    String[] remarks

    static mapping = {
        table('CAPA_8D')
        emailConfiguration column: "EMAIL_CONFIGURATION_ID"
        version false
        attachmentChecked column: "ATTACHMENT_CHECKED"
        ownerType column: "OWNER_TYPE"
        correctiveActions joinTable: [name: "CAPA8D_CORRECTIVE", column: "CORRECTIVE_ACTION_ID", key: "CAPA8D_ID"]
        preventiveActions joinTable: [name: "CAPA8D_PREVENTIVE", column: "PREVENTIVE_ACTION_ID", key: "CAPA8D_ID"]
    }

    static constraints = {
        issueNumber nullable: false, unique: true, maxSize: 200
        issueType nullable: true, maxSize: 255
        teamLead nullable: true

        lastStatusChanged nullable: true

        remarks nullable: true, validator: { val, obj ->
            if (obj.remarks && obj.remarks.size() > 0) {
                for (int i = 0; i < obj.remarks.size(); i++) {
                    if (obj.remarks[i].length() > REMARKS_MAX_LENGTH) {
                        return "com.rxlogix.config.Capa8D.remarks.maxSize.exceeded"
                    }
                }
            }
        }
        category nullable: true, maxSize: 255
        initiator nullable: true
        approvedBy nullable: true
        description nullable: true, maxSize: 32000
        rootCause nullable: true, maxSize: 2000
        verificationResults nullable: true, maxSize: 2000
        comments nullable: true, maxSize: 2000
        configuration nullable: true
        submission nullable: true
        sharedWith joinTable: [name: "CAPA8D_SHARED_WITHS", column: "SHARED_WITH_ID", key: "CAPA_8D_ID"], indexColumn: [name: "SHARED_WITH_IDX"]
        sharedWithGroup joinTable: [name: "CAPA8D_SHARED_W_GRPS", column: "SHARED_WITH_GROUP_ID", key: "CAPA_8D_ID"], indexColumn: [name: "SHARED_WITH_GROUP_IDX"]
        emailToUsers joinTable: [name: "CAPA8D_EMAIL_USERS", column: "EMAIL_USER", key: "CAPA_8D_ID"], indexColumn: [name: "EMAIL_USER_IDX"]
        attachmentFormats joinTable: [name: "CAPA8D_RPT_FORMATS", column: "RPT_FORMAT", key: "CAPA_8D_ID"], indexColumn: [name: "RPT_FORMAT_IDX"]
        emailConfiguration nullable: true
        ownerType nullable: false
    }

    def toMap() {
        [
                id                 : id,
                issueNumber        : issueNumber,
                issueType          : issueType,
                category           : category,
                approvedBy         : approvedBy?.getFullName(),
                initiator          : initiator?.getFullName(),
                teamLead           : teamLead?.getFullName(),
                description        : description,
                rootCause          : rootCause,
                verificationResults: verificationResults,
                comments           : comments,
                dateCreated        : dateCreated,
                lastUpdated        : lastUpdated,
                createdBy          : createdBy,
                approvedId         : approvedBy?.id,
                initiatorId        : initiator?.id,
                teamLeadId         : teamLead?.id,
                remarks            : remarks,
                teamMembers        : teamMembers,
                attachments        : attachments,
                attachmentChecked  : attachmentChecked
        ]
    }

    String toString() {
        return issueNumber
    }

    static namedQueries = {
        capasByActionItem { actionItemId ->
            projections {
                distinct('id')
            }
            createAlias('correctiveActions', 'correctiveActions', JoinType.LEFT_OUTER_JOIN)
            createAlias('preventiveActions', 'preventiveActions', JoinType.LEFT_OUTER_JOIN)
            or {
                eq 'correctiveActions.id', actionItemId
                eq 'preventiveActions.id', actionItemId
            }
        }

        capasBySearchString { String ownerType, String search, List<Closure> advancedFilterCriteria ->
            eq('isDeleted', false)
            eq('ownerType', ownerType)
            if (search) {
                createAlias('teamLead', 'teamLead', JoinType.LEFT_OUTER_JOIN)
                createAlias('initiator', 'initiator', JoinType.LEFT_OUTER_JOIN)
                createAlias('approvedBy', 'approvedBy', JoinType.LEFT_OUTER_JOIN)
                or {
                    iLikeWithEscape('issueNumber', "%${EscapedILikeExpression.escapeString(search)}%")
                    iLikeWithEscape('issueType', "%${EscapedILikeExpression.escapeString(search)}%")
                    iLikeWithEscape('category', "%${EscapedILikeExpression.escapeString(search)}%")
                    iLikeWithEscape('teamLead.fullName', "%${EscapedILikeExpression.escapeString(search)}%")
                    iLikeWithEscape('initiator.fullName', "%${EscapedILikeExpression.escapeString(search)}%")
                    iLikeWithEscape('approvedBy.fullName', "%${EscapedILikeExpression.escapeString(search)}%")
                    iLikeWithEscape('description', "%${EscapedILikeExpression.escapeString(search)}%")
                    iLikeWithEscape('rootCause', "%${EscapedILikeExpression.escapeString(search)}%")
                    iLikeWithEscape('verificationResults', "%${EscapedILikeExpression.escapeString(search)}%")
                    iLikeWithEscape('comments', "%${EscapedILikeExpression.escapeString(search)}%")
                }
            }
            if (advancedFilterCriteria) {
                advancedFilterCriteria.each { cl ->
                    cl.delegate = delegate
                    cl.call()
                }
            }
        }
    }

}
