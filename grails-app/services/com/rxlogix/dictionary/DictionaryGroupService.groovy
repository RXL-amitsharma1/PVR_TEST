package com.rxlogix.dictionary

import com.hazelcast.internal.json.Json
import com.rxlogix.Constants
import com.rxlogix.pvdictionary.DictionaryGroupCmd
import com.rxlogix.pvdictionary.DictionaryGroupServiceAPI
import com.rxlogix.pvdictionary.config.PVDictionaryConfig
import com.rxlogix.pvdictionary.exception.DictionaryGroupException
import com.rxlogix.user.Preference
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.util.DateUtil
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.gorm.multitenancy.Tenants
import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional
import grails.util.Holders
import grails.validation.ValidationException
import org.grails.web.json.JSONObject
import org.springframework.web.context.request.RequestContextHolder

@ReadOnly
@Transactional(readOnly = true)
class DictionaryGroupService implements DictionaryGroupServiceAPI {

    def userService
    def dictionaryGroupDaoService

    @Override
    Map fetchUserDetail(String userName,String userToken = Constants.USER_TOKEN) {
        if(userToken==Constants.USER_TOKEN) {
            User user = User.findByUsernameIlike(userName)
            if (!user) {
                log.error("User not found for userName: ${userName} for fetchUserDetail")
                throw new DictionaryGroupException('pv.dictionary.group.error.user.not.found', 'User not found')
            }
            return [id: Constants.USER_TOKEN + user.id, text: user.fullName ?: user.username, blinded: user.isBlinded]
        }
        else{
            UserGroup userGroup = UserGroup.findByName(userName)
            if(!userGroup) {
                log.error("User Groups not found for userGroupName: ${userName} for fetchUserDetail")
                throw new DictionaryGroupException('pv.dictionary.group.error.not.found', 'User group not found')
            } else {
                return [id: Constants.USER_GROUP_TOKEN + userGroup.id, text: userGroup.name, blinded: userGroup.isBlinded]
            }
        }
        return [id: Constants.USER_TOKEN + user.id, text: user.fullName ?: user.username, blinded: user.isBlinded]
    }


    @Override
    Map fetchUserShareList(String term, Integer page, Integer max, String userName) {
        int offset = Math.max(page - 1, 0) * max
        User user = User.findByUsernameIlike(userName)
        if (!user) {
            log.error("User not found for userName: ${userName} for fetchUserShareList")
            throw new DictionaryGroupException('pv.dictionary.group.error.user.not.found', 'User not found')
        }
        List<String> authorities = user.authorities.collect { it.authority }
        Set<User> activeUsers = getAllowedSharedWithUsersForCurrentUser(user, authorities, term)
        Set<UserGroup> activeGroups = getAllowedSharedWithGroupsForCurrentUser(user, authorities, term)
        List userList = activeUsers.unique { it.id }.collect {
            [id: Constants.USER_TOKEN + it.id, text: it.fullName ?: it.username, blinded: it.isBlinded]
        }
        List groupList = activeGroups.unique { it.id }.collect {
            [id: Constants.USER_GROUP_TOKEN + it.id, text: it.name, blinded: it.isBlinded]
        }
        def items = []
        splitResult(items, offset, max, groupList, userList)
        return [items: items, total_count: userList.size() + groupList.size()]
    }


    @Override
    Map fetchList(Integer dictionaryType, String term, String dataSource, Integer page, Integer max, String userName, Boolean exactSearch = false, Boolean isMultiIngredient = false) {
        Integer tenantId = Tenants.currentId()
        User user = User.findByUsernameIlike(userName)
        if (!user) {
            log.error("User not found for userName: ${userName} for fetchList")
            throw new DictionaryGroupException('pv.dictionary.group.error.user.not.found', 'User not found')
        }
        boolean userIsAdmin = user.isAnyAdmin()
        List<Map> items = DictionaryGroup.getAllIdsBySearch(dictionaryType, term, dataSource, user, tenantId, exactSearch, isMultiIngredient).list(max: max, offset: Math.max(page - 1, 0) * max).collect { id ->
            def dg = DictionaryGroup.read(id)
            return getMapData(dg, (userIsAdmin || dg.ownerId == user.id))
        }

        return [items: items, total_count: DictionaryGroup.countRecordsBySearch(dictionaryType, term, dataSource, user, tenantId, exactSearch, isMultiIngredient).get()]
    }

    @Override
    Map save(DictionaryGroupCmd dgc,boolean flag = false) throws DictionaryGroupException {
        Integer tenantId = Tenants.currentId()
        User user = User.findByUsernameIlike(dgc.owner)
        if (!user) {
            log.error("User not found for userName: ${dgc.owner} for fetchUserDetail")
            throw new DictionaryGroupException('pv.dictionary.group.error.user.not.found', 'User not found')
        }
        if(dgc.groupName == null) {
            throw new DictionaryGroupException(ViewHelper.getMessage(dgc.type == PVDictionaryConfig.PRODUCT_GRP_TYPE ? "app.product.group.name.invalid" : "app.event.group.name.invalid"))
        }
        boolean ifUpdate = false
        DictionaryGroup dictionaryGroup = DictionaryGroup.createCriteria().get {
            eq('id', dgc.id)
            eq('tenantId', tenantId)
            eq('isDeleted', false)
            eq('type', dgc.type)
        }

        if (dictionaryGroup && dictionaryGroup.ownerId != user.id && !user.isAnyAdmin()) {
            throw new DictionaryGroupException('pv.dictionary.group.error.update.permission', "No permissison to edit group :${dictionaryGroup.id}")
        }
        if (dgc.groupName.size() > DictionaryGroup.constrainedProperties.groupName.maxSize) {
            throw new DictionaryGroupException(ViewHelper.getMessage('com.rxlogix.dictionary.DictionaryGroup.groupName.maxSize.exceeded', (dgc.type == PVDictionaryConfig.PRODUCT_GRP_TYPE ? "Product" : "Event"), DictionaryGroup.constrainedProperties.groupName.maxSize))
        }
        DictionaryGroup existingGroup = DictionaryGroup.findByGroupNameAndTypeAndIsDeleted(dgc.groupName, dgc.type, false)
        if ((!dgc.id && existingGroup) || (dgc.id && existingGroup?.id && existingGroup?.id != dgc.id)) {
            throw new DictionaryGroupException(ViewHelper.getMessage(dgc.type == PVDictionaryConfig.PRODUCT_GRP_TYPE ? "app.product.group.exist" : "app.event.group.exist", dgc.groupName.encodeAsHTML())
            )
        }
        if(dictionaryGroup && flag){
            ifUpdate = true
        }
        if (!dictionaryGroup) {
            dictionaryGroup = new DictionaryGroup()
        }
        if (dgc.copyGroupIds) {
            dgc.data = mergeJSONData(dgc.copyGroupIds, dgc.data)
        }
        validateDataJSON(dgc.data)
        if (Holders.config.getProperty('pv.dictionary.group.validate.dataSources.availablity', Boolean)) {
            Set<String> totalDataSources = new HashSet<String>(dictionaryGroup.dataSources ?: [])
            totalDataSources.addAll(dgc.dataSources ?: [])
            validateDataSourcesAvailable(totalDataSources)
        }
        def params = RequestContextHolder.requestAttributes.params
        params.oldJSONData = dictionaryGroup.fetchData()

        String newData = dgc.data
        JSONObject data = new JSONObject()
        List<String> availableDataSources = Holders.getGrailsApplication().getConfig().supported.datasource
        Set<String>dataSources
        if(!flag){
            dataSources = dgc.dataSources
        }
        else{
            dataSources = dgc.dataSourceNames
        }

        availableDataSources.sort().each { dsn ->
            if (dsn in dataSources) {
                String obj = JSON.parse(newData).get(dsn)
                if (obj) {
                    data.put(dsn, JSON.parse(obj))
                }
            } else {
                log.error("Issue in DBs configuartion for $dsn")
            }
        }
        params.newJSONData = data.toString()

        if (dictionaryGroup.sharedWithUser) {
            params.put("oldSharedWithUser", dictionaryGroup.sharedWithUser?.collect { it.toString() })
        }
        if (dictionaryGroup.sharedWithGroup) {
            params.put("oldSharedWithGroup", dictionaryGroup.sharedWithGroup?.collect { it.toString() })
        }
        try {
            dictionaryGroupDaoService.save(dictionaryGroup, dgc, tenantId,flag,ifUpdate)
        } catch (ValidationException e) {
            String errorCode = "pv.dictionary.group.error.create.invalid.values"
            try {
                def error = e.errors.allErrors.first()
                errorCode = "pv.dictionary.group.${dictionaryGroup.isEventGroup()?'event':'product'}.${error.field}.error.${error.code}"
            } catch (ex) {
            }
            throw new DictionaryGroupException(errorCode, 'Input values are invalid')
        }
        return getMapData(dictionaryGroup, true)
    }

    @Override
    void delete(Long id, String userName) throws DictionaryGroupException {
        Integer tenantId = Tenants.currentId()
        DictionaryGroup dictionaryGroup = DictionaryGroup.findByIdAndTenantId(id, tenantId)
        if (!dictionaryGroup) {
            log.error("Dictionary Group not found for id: ${id} for delete")
            throw new DictionaryGroupException('pv.dictionary.group.error.not.found', "Group not found for id: ${id}")
        }
        User user = User.findByUsername(userName)
        if (dictionaryGroup.ownerId != user.id && !user.isAnyAdmin()) {
            throw new DictionaryGroupException('pv.dictionary.group.error.delete.permission', "No permissison to delte group :${dictionaryGroup.id}")
        }
        dictionaryGroupDaoService.delete(dictionaryGroup, userName)
    }

    @Override
    DictionaryGroupCmd groupDetails(Long id, String userName, boolean withData = false) {
        Integer tenantId = Tenants.currentId() ?: Holders.config.getProperty("pvreports.multiTenancy.defaultTenant") as Integer
        DictionaryGroup dictionaryGroup = DictionaryGroup.findByIdAndTenantIdAndIsDeleted(id, tenantId,false)
        if (!dictionaryGroup) {
            log.error("Dictionary Group not found for id: ${id} for details")
            throw new DictionaryGroupException('pv.dictionary.group.error.not.found', "Group not found for id: ${id}")
        }
        return getDictionaryGroupCMD(dictionaryGroup, User.findByUsernameIlike(userName), withData)
    }

    private DictionaryGroupCmd getDictionaryGroupCMD(DictionaryGroup dictionaryGroup, User user, boolean withData = false) {
        DictionaryGroupCmd dictionaryGroupCmd = new DictionaryGroupCmd()
        Preference preference = user.preference
        dictionaryGroupCmd.with {
            id = dictionaryGroup.id
            groupName = dictionaryGroup.groupName
            type = dictionaryGroup.type
            description = dictionaryGroup.description
            owner = dictionaryGroup.owner.username
            tenantId = dictionaryGroup.tenantId
            dataSourceNames = dictionaryGroup.dataSources
            sharedWithUser = dictionaryGroup.fetchSharedWithUserData()
            sharedWithGroup = dictionaryGroup.fetchSharedWithGroupData()
            dateCreated = DateUtil.getLongDateStringForLocaleAndTimeZone(dictionaryGroup.dateCreated, preference.locale, preference.timeZone)
            lastUpdated = DateUtil.getLongDateStringForLocaleAndTimeZone(dictionaryGroup.lastUpdated, preference.locale, preference.timeZone)
            createdBy = dictionaryGroup.createdBy
            modifiedBy = dictionaryGroup.modifiedBy
            canEdit = (dictionaryGroup.ownerId == user.id || user.isAnyAdmin())
            isMultiIngredient = dictionaryGroup.isMultiIngredient
            if (withData)
                data = dictionaryGroup.fetchData()
        }

        return dictionaryGroupCmd
    }


    private Map getMapData(DictionaryGroup dictionaryGroup, boolean canEdit) {
        StringBuilder text = new StringBuilder(dictionaryGroup.groupName)
        text.append(" - Owner : ")
        text.append(dictionaryGroup.ownerFullName)
        if (!dictionaryGroup.eventGroup) {
            text.append(", Data Source:")
            text.append(dictionaryGroup.dataSources.collect {
                Holders.config.product.dictionary.datasources.additionalInfo."$it"?.displayName ?: it
            }.join(','))
        }
        return [
                id               : dictionaryGroup.id,
                text             : text.toString(),
                name             : dictionaryGroup.groupName + " (" + dictionaryGroup.id + ")",
                canEdit          : canEdit,
                isMultiIngredient: dictionaryGroup.isMultiIngredient,
                includeWHODrugs  : dictionaryGroup.includeWHODrugs
        ]
    }

    private String mergeJSONData(List<Long> ids, String jsonData) {
        def obj = JSON.parse(jsonData)
        ids.each {
            DictionaryGroup dictionaryGroup = DictionaryGroup.get(it)
            obj = mergeDicJSON(obj, JSON.parse(dictionaryGroup.fetchData()))
        }
        return (obj as JSON).toString()
    }

    private Map mergeDicJSON(Map onto, Map... overrides) {
        if (!overrides)
            return onto
        else if (overrides.length == 1) {
            overrides[0]?.each { k, v ->
                if (v instanceof Map && onto[k] instanceof Map)
                    mergeDicJSON((Map) onto[k], (Map) v)
                else if (v instanceof List && onto[k] instanceof List) {
                    onto[k] = (onto[k] + v).unique { it.id }
                } else {
                    onto[k] = v
                }
            }
            return onto
        }
        return overrides.inject(onto, { acc, override -> mergeDicJSON(acc, override ?: [:]) })
    }

    private splitResult(items, offset, max, groupList, userList) {

        String groupLabel = ""
        String userLabel = ""
        def selectedGroupItems = []
        def selectedUserItems = []
        if (offset == 0 && groupList.size() > 0) {
            groupLabel = ViewHelper.getMessage("user.group.label")
            selectedGroupItems = groupList.subList(0, Math.min(offset + max, groupList.size()))
        } else if ((offset > 0) && (offset < groupList.size())) {
            groupLabel = ""
            selectedGroupItems = groupList.subList(offset, Math.min(offset + max, groupList.size()))
        }

        int userOffset = offset - groupList.size()
        int usermax = max - selectedGroupItems.size()
        if ((userOffset + max) > 0) {
            if (userOffset <= 0 && userList.size() > 0) {
                userLabel = ViewHelper.getMessage("user.label")
                selectedUserItems = userList.subList(0, Math.min(0 + usermax, userList.size()))
            } else if ((userOffset > 0) && (userOffset < userList.size())) {
                userLabel = ""
                selectedUserItems = userList.subList(userOffset, Math.min(userOffset + usermax, userList.size()))
            }
        }
        if (selectedGroupItems.size() > 0)
            items << ["text": groupLabel, "children": selectedGroupItems]
        if (selectedUserItems.size() > 0)
            items << ["text": userLabel, "children": selectedUserItems]
    }

//    TOOD need to remove once rest login mechanism there.
    private List<User> getAllowedSharedWithUsersForCurrentUser(User user, List<String> authorities, String search) {
        String _search = search?.toLowerCase()
        List<User> users = [];
        if (authorities.any { it == 'ROLE_SHARE_ALL' }) {
            users = userService.getActiveUsers()
        } else if (authorities.any { it == 'ROLE_SHARE_GROUP' }) {
            def activeGroups = UserGroup.fetchAllUserGroupByUser(user)
            activeGroups.each { users.addAll(it.users) }
            users = users.unique { it.id }.sort { it.username }
        }
        if (users.size() == 0) users = [user]
        if (search)
            return users.findAll { (it.fullName ?: it.username).toLowerCase().indexOf(_search) > -1 }
        else
            return users
    }

    private List<UserGroup> getAllowedSharedWithGroupsForCurrentUser(User user, List<String> authorities, String search) {
        String _search = search?.toLowerCase()

        List<UserGroup> groups = []
        if (authorities.any { it == 'ROLE_SHARE_ALL' }) {
            groups = userService.getActiveGroups()
        } else if (authorities.any { it == 'ROLE_SHARE_GROUP' }) {
            groups = UserGroup.fetchAllUserGroupByUser(user).sort { it.name }
        }
        if (search)
            return groups.findAll { it.name.toLowerCase().indexOf(_search) > -1 }
        else
            return groups
    }

    private void validateDataJSON(String jsonData) {
        def obj = JSON.parse(jsonData)
        // Group JSON can't be empty
        if (!obj.any { it.key && it.value.any { it.value.class != Boolean && it.value.size() > 0 } }) {
            throw new DictionaryGroupException('pv.dictionary.group.error.data.invalid', 'Please add atleast one dictionary value in group')
        }
//        eudra specific validation. At max 1 ingriedent that too at level 1
        if (obj.eudra && ((obj.eudra.get('1')?.size() ?: 0) > 1 || obj.eudra.any {
            it.key != '1' && it.value.class != Boolean && it.value.size()
        })) {
            throw new DictionaryGroupException('pv.dictionary.group.error.eudra.data.invalid', "Invalid Eudra group JSON")
        }
    }

    private void validateDataSourcesAvailable(Set<String> dataSources) {
        dataSources.removeAll(Holders.getGrailsApplication().getConfig().supported.datasource)
        if (dataSources) {
            throw new DictionaryGroupException('pv.dictionary.group.error.dataSources.config.invalid', "System has missing datasource configuration")
        }
    }

}
