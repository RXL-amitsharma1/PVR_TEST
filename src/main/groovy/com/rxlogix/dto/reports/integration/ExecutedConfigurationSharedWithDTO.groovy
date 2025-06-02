package com.rxlogix.dto.reports.integration

import grails.validation.Validateable

class ExecutedConfigurationSharedWithDTO implements Validateable{

    Long exConfigId
    List<String> sharedWithUsers = []
    List<String> sharedWithGroups = []

}