package com.rxlogix
import com.rxlogix.user.Role
import grails.plugin.springsecurity.annotation.Secured

import static org.springframework.http.HttpStatus.NOT_FOUND

@Secured(['ROLE_USER_MANAGER'])

class RoleController {

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def CRUDService
    def searchService

    def index() {
        params.max = Constants.Search.MAX_SEARCH_RESULTS
        render view: "index", model: [roleInstanceList:  Role.list(params), roleInstanceTotal: Role.count()]
    }

    def show(Role roleInstance) {
        if (!roleInstance) {
            notFound()
            return
        }

        render view: "show", model: [roleInstance: roleInstance]
    }

    protected void notFound() {
        request.withFormat {
            form {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'role.label'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }
}
