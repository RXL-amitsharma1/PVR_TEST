package com.rxlogix.api

import com.rxlogix.user.FieldProfile
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.rest.RestfulController

@Secured('permitAll')
class FieldProfileRestController extends RestfulController implements SanitizePaginationAttributes {

    FieldProfileRestController() {
        super(FieldProfile)
    }

    def index() {
        sanitize(params)
        def fieldProfileNameQuery = FieldProfile.fetchAllFieldProfileBySearchString(params.searchString)
        params.sort == "owner.fullName" ? params.sort = "createdBy" : params.sort
        List<FieldProfile> fieldProfileList = fieldProfileNameQuery.list([max: params.max, offset: params.offset, sort: params.sort, order: params.order])
        List<Map> fieldProfiles = fieldProfileList.collect{
            [id:it.id,name:it.name,description:it.description ,createdBy:it.createdBy, lastUpdated:it.lastUpdated, dateCreated:it.dateCreated]
        }
        render([aaData: fieldProfiles, recordsTotal: FieldProfile.fetchAllFieldProfileBySearchString(null).count(), recordsFiltered: fieldProfileNameQuery.count()] as JSON)
    }
}
