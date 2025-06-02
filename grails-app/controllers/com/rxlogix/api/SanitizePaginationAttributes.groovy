package com.rxlogix.api

trait SanitizePaginationAttributes {

    // This method's test case is written in ReportFieldRestControllerSpec
    void sanitize(params) {
        params.max = params.int('length') ?: params.max
        params.max = params.max ?: 50

        params.offset = params.int('start') ?: params.offset
        params.offset = params.offset ?: 0

        if(params.sort instanceof Object[] ) params.sort = params.sort[0]
        params.sort = params.sort ? (params.sort != 'createdBy') ? params.sort : "owner.fullName" : 'dateCreated'

        if(params.direction instanceof Object[] ) params.direction = params.direction[0]
        params.order = params.direction ?: 'desc'
        params.searchString = params.searchString?.trim()
    }

    void forSelectBox(params) {
        params.max = params.int('max') ?: 30
        params.page = params.int('page') ?: 1
        params.offset = Math.max(params.page - 1, 0) * params.max
        params.term = params.term?.trim() ?: ""
    }
}