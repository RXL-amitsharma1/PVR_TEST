package com.rxlogix

import grails.plugin.springsecurity.annotation.Secured


@Secured('permitAll')
class ErrorsController {

    def forbidden() {
        flash.error = message(code: "error.403.message")
        String refererUrl = request.getHeader("Referer")
        if (refererUrl?.trim()) {
            redirect(uri: refererUrl)
        } else {
            redirect(controller: "dashboard", action: "index")
        }
    }

    def notFound = {
        render view: '/errors/error404'
    }

    def notAllowed = {
        render view: '/errors/error405'
    }

    def serverError = {
        render view: '/errors/error500'
    }
}
