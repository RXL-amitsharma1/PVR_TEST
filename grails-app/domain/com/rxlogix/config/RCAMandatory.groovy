package com.rxlogix.config

import com.rxlogix.enums.AuditLogCategoryEnum
import com.rxlogix.enums.ReasonOfDelayAppEnum
import com.rxlogix.enums.ReasonOfDelayFieldEnum
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import org.hibernate.criterion.CriteriaSpecification
@CollectionSnapshotAudit
class RCAMandatory implements Serializable {

    ReasonOfDelayAppEnum ownerApp
    ReasonOfDelayFieldEnum field
    List <WorkflowState> mandatoryInStates = []
    List <WorkflowState> editableInStates = []
    List <User> editableByUsers = []
    List <User> editableByGroups = []

    static auditable =  true

    static hasMany = [mandatoryInStates: WorkflowState, editableInStates: WorkflowState,
                      editableByUsers: User, editableByGroups: UserGroup]

    static constraints = {
    }

    static mapping = {
        table("RCA_MANDATORY_FIELDS")
        ownerApp column: "OWNER_APP"
        field column: "FIELD"
        mandatoryInStates joinTable: [name: "RCA_MANDATORY_WFS", column: "WFS_ID", key: "RCA_MANDATORY_ID"], indexColumn: [name: "RCA_WFS_IDX"]
        editableInStates joinTable: [name: "RCA_EDITABLE_WFS", column: "WFS_ID", key: "RCA_MANDATORY_ID"], indexColumn: [name: "RCA_WFS_IDX"]
        editableByUsers joinTable: [name: "RCA_EDIT_USERS", column: "USER_ID", key: "RCA_MANDATORY_ID"], indexColumn: [name: "RCA_USER_IDX"]
        editableByGroups joinTable: [name: "RCA_EDIT_USRGRPS", column: "USER_GRP_ID", key: "RCA_MANDATORY_ID"], indexColumn: [name: "RCA_GRP_IDX"]
        version false
    }

    String getInstanceIdentifierForAuditLog() {
        return "$ownerApp - $field"
    }

    static namedQueries = {
        getMandatoryRCAFields { ReasonOfDelayAppEnum app, WorkflowState wfs ->
            projections {
                property("field")
            }
            createAlias('mandatoryInStates', 'mnd', CriteriaSpecification.LEFT_JOIN)
            and {
                eq("ownerApp", app)
                or {
                    eq("mnd.id", wfs.id)
                }
            }
        }

        getMandatoryRCAFieldsForPVQ { List<Long> wfsIds ->
            projections {
                property("field")
                property("mnd.id", "wfsId")
            }
            createAlias('mandatoryInStates', 'mnd', CriteriaSpecification.LEFT_JOIN)
            and {
                eq("ownerApp", ReasonOfDelayAppEnum.PVQ)
                or {
                    'in'("mnd.id", wfsIds)
                }
            }
        }

        getEditableRCAFields { ReasonOfDelayAppEnum app, WorkflowState wfs, User currentUser ->
            projections {
                property("field")
            }
            createAlias('editableByUsers', 'users', CriteriaSpecification.LEFT_JOIN)
            createAlias('editableByGroups', 'groups', CriteriaSpecification.LEFT_JOIN)
            createAlias('editableInStates', 'edit', CriteriaSpecification.LEFT_JOIN)
            and {
                eq("ownerApp", app)
                or {
                    eq("users.id", currentUser.id)
                    if (UserGroup.countAllUserGroupByUser(currentUser)) {
                        'in'('groups.id', UserGroup.fetchAllUserGroupByUser(currentUser).id)
                    }
                    and {
                        isNull("users.id")
                        isNull("groups.id")
                    }
                }
                or {
                    eq("edit.id", wfs.id)
                    isNull("edit.id")
                }
            }
        }
    }

    String toString() {
        return "$ownerApp - $field"
    }

}