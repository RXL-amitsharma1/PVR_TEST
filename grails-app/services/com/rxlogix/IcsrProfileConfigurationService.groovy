package com.rxlogix

import com.rxlogix.config.ExecutionStatus
import com.rxlogix.config.IcsrProfileConfiguration
import com.rxlogix.enums.ExecutingEntityTypeEnum
import com.rxlogix.mapping.AuthorizationType
import com.rxlogix.user.User
import grails.gorm.transactions.Transactional

@Transactional
class IcsrProfileConfigurationService {

    def CRUDService
    def userService
    def sqlGenerationService

    void saveUpdate(IcsrProfileConfiguration icsrProfileConfiguration) {
        if (!icsrProfileConfiguration.id) {
            CRUDService.save(icsrProfileConfiguration)
        } else {
            if (icsrProfileConfiguration.e2bDistributionChannel && (!icsrProfileConfiguration.e2bDistributionChannel.id || icsrProfileConfiguration.e2bDistributionChannel.isDirty())) {
                CRUDService.saveOrUpdate(icsrProfileConfiguration.e2bDistributionChannel)
            }
            CRUDService.updateWithMandatoryAuditlog(icsrProfileConfiguration)
        }
    }

    List getExecutedProfilesIdsForUser(User currentUser) {
        List<Long> icsrProfileConfigurationIds = IcsrProfileConfiguration.ownedByAndSharedWithUser(currentUser, currentUser?.isAdmin(), false).list()*.id
        List<Long> executedIcsrProfileIdsForUser = []

        if (icsrProfileConfigurationIds && icsrProfileConfigurationIds.size() > 0) {
            executedIcsrProfileIdsForUser = ExecutionStatus.getExecutionStatusByEntity(icsrProfileConfigurationIds, ExecutingEntityTypeEnum.ICSR_PROFILE_CONFIGURATION).list()
        }

        return executedIcsrProfileIdsForUser
    }

    boolean hasIssuesInAuthorizationType(Set<Long> authIds) {
        List<AuthorizationType> authorizationTypeList
        Integer langId = sqlGenerationService. getPVALanguageId(userService.currentUser?.preference?.locale?.toString() ?: 'en')
        AuthorizationType.withNewSession {
            authorizationTypeList = AuthorizationType.findAllByIdInListAndLangId(authIds, langId)
        }

        // Identify the categories of the selected authorization types
        def categories = authorizationTypeList.collect { type ->
            Constants.validAuthCategories.find { category, values -> type.name in values }?.key
        }

        // Ensure both objects belong to the same category
        if (categories.unique().size() != 1) {
            return true
        }
        return false
    }
}
