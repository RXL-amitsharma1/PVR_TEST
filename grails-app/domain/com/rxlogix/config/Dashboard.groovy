package com.rxlogix.config

import com.rxlogix.enums.DashboardEnum
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.util.ViewHelper
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.sql.JoinType

/**
 * Created by gologuzov on 22.11.16.
 */
@CollectionSnapshotAudit
class Dashboard {
    static auditable = true

    User owner
    String label
    List<User> sharedWith = []
    List<UserGroup> sharedWithGroup = []
    DashboardEnum dashboardType
    List<ReportWidget> widgets = []
    boolean isDeleted = false
    String parentId
    //Standard fields
    Date dateCreated = new Date()
    Date lastUpdated = new Date()
    String createdBy
    String modifiedBy
    String icon

    static hasMany = [widgets: ReportWidget, sharedWith: User, sharedWithGroup: UserGroup]

    static mapping = {
        table "DASHBOARD"
        owner column: "PVUSER_ID"
        parentId column: "PARENT_ID"
        sharedWith joinTable: [name: "DASHBOARD_SHARED_WITHS", column: "SHARED_WITH_ID", key: "DASHBOARD_ID"], indexColumn: [name: "SHARED_WITH_IDX"]
        sharedWithGroup joinTable: [name: "DASHBOARD_SHARED_W_GRPS", column: "SHARED_WITH_GROUP_ID", key: "DASHBOARD_ID"], indexColumn: [name: "SHARED_WITH_GROUP_IDX"]
        dashboardType column: "DASHBOARD_TYPE"
        label column: "LABEL"
        isDeleted column: 'IS_DELETED'
        widgets joinTable: [name: "DASHBOARD_RWIDGET", column: "REPORT_WIDGET_ID", key: "DASHBOARD_WIDGETS_ID"], indexColumn: [name: "WIDGETS_IDX"]
        icon column: "ICON"
    }

    static constraints = {
        label(nullable: true, blank: true, maxSize: 255, validator: { val, obj ->
            if (val?.size() < 5) {
                return ['com.rxlogix.config.Dashboard.label.minSize.notmet']
            }
            return true
        })
        owner(nullable: true, blank: true)
        parentId(nullable: true, blank: true)
        widgets(nullable: true, blank: true)
        widgets cascade: "all-delete-orphan"
        icon nullable: true
    }
    static namedQueries = {

        selectPublicByGroupId { Long groupId ->
            resultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY)
            createAlias('sharedWithGroup', 'group', JoinType.INNER_JOIN)
            'in'('dashboardType', [DashboardEnum.PVQ_PUBLIC, DashboardEnum.PVR_PUBLIC, DashboardEnum.PVC_PUBLIC])
            eq('group.id', groupId)
            eq('isDeleted', false)
        }

        selectByUserForPvr { User user ->
            Map<String, DashboardEnum> dashboardEnumMap = [main: DashboardEnum.PVR_MAIN, pub: DashboardEnum.PVR_PUBLIC, user: DashboardEnum.PVR_USER]
            selectByUserForTypes(user, dashboardEnumMap)
        }

        selectByUserForPvq { User user ->
            Map<String, DashboardEnum> dashboardEnumMap = [main: DashboardEnum.PVQ_MAIN, pub: DashboardEnum.PVQ_PUBLIC, user: DashboardEnum.PVQ_USER]
            selectByUserForTypes(user, dashboardEnumMap)
        }

        selectByUserForPvc { User user ->
            Map<String, DashboardEnum> dashboardEnumMap = [main: DashboardEnum.PVC_MAIN, pub: DashboardEnum.PVC_PUBLIC, user: DashboardEnum.PVC_USER]
            selectByUserForTypesForChildDashboard(0,user, dashboardEnumMap)
        }

        selectByUserForPvcChild { Long parentId,User user ->
            Map<String, DashboardEnum> dashboardEnumMap = [main: DashboardEnum.PVC_MAIN, pub: DashboardEnum.PVC_PUBLIC, user: DashboardEnum.PVC_USER]
            selectByUserForTypesForChildDashboard(parentId,user, dashboardEnumMap)
        }
        selectByUserForPvcShared { Long parentId, User user ->
            resultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY)
            createAlias('sharedWithGroup', 'group', JoinType.LEFT_OUTER_JOIN)
            createAlias('sharedWith', 'sw', JoinType.LEFT_OUTER_JOIN)
            and {
                'eq'('dashboardType', DashboardEnum.PVC_PUBLIC)
                or {
                    and {
                        isNull("sw.id")
                        isNull("group.id")
                    }
                    eq('sw.id', user.id)
                    'in'('group.id', UserGroup.fetchAllUserGroupByUser(user)?.collect { it.id } ?: [0L])
                }
            }
            not {
                eq('owner.id', user.id)
            }
            or {
                Dashboard.findAllByDashboardTypeAndIsDeleted(DashboardEnum.PVC_MAIN, false)?.collate(999)?.each { dList -> 'in'('parentId', dList.collect { it.id.toString() }) }
            }
            eq('isDeleted', false)
        }
        selectByUserForTypes { User user, Map<String, DashboardEnum> dashboardEnumMap ->
            resultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY)
            createAlias('sharedWithGroup', 'group', JoinType.LEFT_OUTER_JOIN)
            createAlias('sharedWith', 'sw', JoinType.LEFT_OUTER_JOIN)
            or {
                and {
                    'eq'('dashboardType', dashboardEnumMap.pub)
                    or {
                        and{
                            isNull("sw.id")
                            isNull("group.id")
                        }
                        eq('sw.id', user.id)
                        eq('owner.id', user.id)
                        'in'('group.id', UserGroup.fetchAllUserGroupByUser(user)?.collect { it.id }?:[0L])
                    }
                }
                and {
                    eq('dashboardType', dashboardEnumMap.user)
                    eq('owner.id', user.id)
                }
            }
            eq('isDeleted', false)
        }

        selectByUserForTypesForChildDashboard { Long parentId,User user, Map<String, DashboardEnum> dashboardEnumMap ->
            resultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY)
            createAlias('sharedWithGroup', 'group', JoinType.LEFT_OUTER_JOIN)
            createAlias('sharedWith', 'sw', JoinType.LEFT_OUTER_JOIN)
            or {
                and{
                    eq('dashboardType', dashboardEnumMap.user)
                    eq('owner.id', user.id)
                }
                and {
                    eq('dashboardType', dashboardEnumMap.pub)
                    or {
                        eq('sw.id', user.id)
                        eq('owner.id', user.id)
                        'in'('group.id', UserGroup.fetchAllUserGroupByUser(user)?.collect { it.id }?:[0L])
                    }
                }
            }
            if(parentId) {
                eq('parentId', parentId.toString())
            }else{
                or{
                    isNull('parentId')
                    eq('parentId', "0")
                }
            }
            eq('isDeleted', false)
        }
    }

    boolean isEditableBy(User currentUser) {
        return (currentUser?.isAdmin() || (owner?.id == currentUser?.id) || (isSharedWith(currentUser))
        )
    }

    boolean isSharedWith(User currentUser) {
        if (this.sharedWith?.any { it.id == currentUser.id }) {
            return true;
        }
        List<UserGroup> groups = UserGroup.fetchAllUserGroupByUser(currentUser).flatten()
        return this.sharedWithGroup?.any { it.id in groups*.id }
    }

    Map toMap() {
        [
                id             : id,
                label          : label,
                owner          : owner?.fullName,
                dashboardType  : ViewHelper.getMessage(dashboardType.getI18nKey()),
                sharedWith     : sharedWith?.collect { it.fullName }?.join(", "),
                sharedWithGroup: sharedWithGroup?.collect { it.name }?.join(", "),
        ]
    }

    String getInstanceIdentifierForAuditLog() {
        return label ?: "-"
    }

    Map appendAuditLogCustomProperties(Map newValues, Map oldValues) {
        if(newValues && oldValues) {
            List newWidgets = (newValues.widgets) ? newValues.widgets as List : []
            List oldWidgets = (oldValues.widgets) ? oldValues.widgets as List : []
            if (newWidgets.size() < oldWidgets.size())
                newValues.put("removedWidget", ViewHelper.getWidgetNames((oldWidgets - newWidgets).toString()))
        }
        return [newValues: newValues, oldValues: oldValues]
    }

}
