package com.rxlogix.central

import com.rxlogix.api.SanitizePaginationAttributes
import com.rxlogix.config.ActionItem
import com.rxlogix.config.Capa8D
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException
import grails.web.mapping.LinkGenerator
import com.rxlogix.enums.ReasonOfDelayAppEnum


@Secured(["isAuthenticated()"])
class PvcIssueController implements SanitizePaginationAttributes {
    def userService
    LinkGenerator grailsLinkGenerator

    @Secured(['ROLE_PVC_EDIT'])
    def create() {
        forward(controller: "issue", action: 'create', params: [type: ReasonOfDelayAppEnum.PVC.name()])
    }

    @Secured(['ROLE_PVC_VIEW'])
    def index() {
        render view: "index", model:[type: ReasonOfDelayAppEnum.PVC]
    }

    @Secured(['ROLE_PVC_VIEW'])
    def view() {
        forward(controller: "issue", action: 'view', params: [id: params.id, type: ReasonOfDelayAppEnum.PVC])
    }

    @Secured(['ROLE_PVC_EDIT'])
    def edit() {
        forward(controller: "issue", action: 'edit', params: [id: params.id, type: ReasonOfDelayAppEnum.PVC])
    }

    @Secured(['ROLE_PVC_EDIT'])
    def save() {
        forward(controller: "issue", action: 'save')
    }

    @Secured(['ROLE_PVC_EDIT'])
    def validateAndCreate() {
        forward(controller: "issue", action: 'validateAndCreate')
    }

    @Secured(['ROLE_PVC_EDIT'])
    def update() {
        forward(controller: "issue", action: 'update')

    }

    @Secured(['ROLE_PVC_EDIT'])
    def share() {
        forward(controller: "issue", action: 'share', params: [id:params.id, type: ReasonOfDelayAppEnum.PVC])
    }

    @Secured(['ROLE_PVC_EDIT'])
    def delete(Capa8D capa) {
        forward(controller: "issue", action: 'delete', params: [id:params.id, type: ReasonOfDelayAppEnum.PVC])
    }

    @Secured(["ROLE_CONFIGURATION_VIEW"])
    def email() {
        forward(controller: "issue", action: 'email', params: [id:params.id, type: ReasonOfDelayAppEnum.PVC])
    }

}