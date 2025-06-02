package com.rxlogix.config


import com.rxlogix.user.User
import grails.util.Holders

class ExecutedPublisherSource extends BasicPublisherSource {

    static belongsTo = [configuration: ExecutedReportConfiguration]

    static mapping = {
        tablePerHierarchy false
        table name: "EX_CONFIGURATION_ATTACH"
    }

    static constraints = {
        configuration nullable: true
    }

    boolean isVisible(User user = null) {
        User currentUser = user ?: Holders.applicationContext.getBean("userService").currentUser
        if (currentUser.isAdmin()) return true
        if (userGroup) {
            return userGroup?.users?.find { it.id == currentUser.id }
        } else {
            if (configuration)
                return configuration.isViewableBy(currentUser)
            else
                return true
        }
    }

    public String toString() {
        return name
    }


    @Override
    Map toMap() {
        Map result = super.toMap()
        result.configuration = this.configuration?.reportName
        return result
    }
}
