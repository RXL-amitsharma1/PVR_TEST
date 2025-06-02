package com.rxlogix.api

import com.rxlogix.user.UserGroup
import com.rxlogix.util.DateUtil
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.rest.RestfulController

@Secured('permitAll')
class UserGroupRestController extends RestfulController implements SanitizePaginationAttributes {

    UserGroupRestController() {
        super(UserGroup)
    }

    def index() {
        sanitize(params)
        def userGroupNameQuery = UserGroup.findUserGroupBySearchString(params.searchString, params.sort, params.order)
        params.sort == "owner.fullName" ? params.sort = "createdBy" : params.sort
        List<UserGroup> userGroupList = userGroupNameQuery.list([max: params.max, offset: params.offset])
        List<Map> userGroups = userGroupList.collect{
            [id:it.id,name:it.name,description:it.description,fieldProfile: it.fieldProfile?.name ,createdBy:it.createdBy, lastUpdated:it.lastUpdated, dateCreated:it.dateCreated]
        }
        render([aaData: userGroups, recordsTotal: UserGroup.findUserGroupBySearchString(null, null).count(), recordsFiltered: userGroupNameQuery.count()] as JSON)
    }
}
