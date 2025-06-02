package com.rxlogix.api

import com.rxlogix.config.Notification
import com.rxlogix.user.User
import grails.plugin.springsecurity.annotation.Secured
import grails.rest.RestfulController

@Secured('permitAll')
class NotificationRestController extends RestfulController {
    def messageSource
    def springSecurityService

    NotificationRestController() {
        super(Notification)
    }

    /**
     * Get notifications for a given User
     * @param User id
     * @return list of notifications for a given user
     */
    def forUser(User userObj) {
        if (userObj && userObj.id == springSecurityService?.principal?.id) {
            if(grailsApplication.config.pvreports.notification.max.records == -1) {
                respond Notification.where {
                    user.id == userObj.id
                }.list().collect {
                    return it.toMap(userObj)
                }, [formats: ['json']]
            }else{
                respond Notification.where {
                    user.id == userObj.id
                }.list(max: grailsApplication.config.pvreports.notification.max.records, sort: "dateCreated", order: "desc").collect {
                    return it.toMap(userObj)
                }, [formats: ['json']]
            }
        }
    }

    /**
     * Delete notification by id
     * @param Notification id
     * @return boolean whether or not deletion was successful
     */
    def deleteNotificationById(Long id) {
        if (id) {
            try {
                Notification.executeUpdate("delete from Notification where id =:id", [id: id])
                render true
            } catch (Exception e) {
                log.error("Could not delete notification! $e.localizedMessage")
                render false
            }
        }
    }

    /**
     * Delete all notifications for user by id
     * @param User id
     * @return boolean whether or not deletion was successful
     */
    def deleteNotificationsForUserId(Long id) {
        if (id) {
            try {
                Notification.executeUpdate("delete from Notification where user.id =:userId", [userId: id])
                render true
            } catch (Exception e) {
                log.error("Could not delete notifications for user $id! $e.localizedMessage")
                render false
            }
        }
    }
}
