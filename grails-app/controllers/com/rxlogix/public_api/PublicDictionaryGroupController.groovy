package com.rxlogix.public_api

import com.rxlogix.Constants
import com.rxlogix.LookupController
import com.rxlogix.dictionary.DictionaryGroup
import com.rxlogix.dictionary.DictionaryGroupData
import com.rxlogix.pvdictionary.DictionaryGroupCmd
import com.rxlogix.pvdictionary.exception.DictionaryGroupException
import com.rxlogix.pvdictionary.product.view.LmProdDic200
import com.rxlogix.pvdictionary.product.view.LmProdDic201
import com.rxlogix.pvdictionary.product.view.LmProdDic202
import com.rxlogix.pvdictionary.product.view.LmProdDic203
import com.rxlogix.pvdictionary.product.view.LmProdDic204
import com.rxlogix.pvdictionary.product.view.LmProdDic205
import com.rxlogix.pvdictionary.product.view.LmProdDic206
import com.rxlogix.pvdictionary.product.view.LmProdDic207
import com.rxlogix.pvdictionary.product.view.LmProdDic208
import com.rxlogix.pvdictionary.product.view.LmProdDic209
import com.rxlogix.pvdictionary.product.view.LmProdDic210
import com.rxlogix.pvdictionary.product.view.LmProdDic211
import com.rxlogix.pvdictionary.product.view.LmProdDic212
import com.rxlogix.pvdictionary.product.view.LmProdDic213
import com.rxlogix.pvdictionary.product.view.LmProdDic214
import com.rxlogix.pvdictionary.product.view.LmProdDic215
import com.rxlogix.pvdictionary.product.view.LmProdDic216
import com.rxlogix.pvdictionary.product.view.LmProdDic217
import com.rxlogix.pvdictionary.product.view.LmProdDic218
import com.rxlogix.pvdictionary.product.view.LmProdDic219
import com.rxlogix.user.User
import com.rxlogix.util.MiscUtil
import grails.converters.JSON
import grails.util.Holders
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import grails.plugin.springsecurity.annotation.Secured
import org.grails.web.json.JSONElement


@Secured(['permitAll'])
class PublicDictionaryGroupController {

    def dictionaryGroupService

    def fetchUserDetail(String userName) {
        String userType = Constants.USER_TOKEN
        String userNameString = userName
        if(userName.indexOf('_:_')>-1 ) {
            userType = userName.substring(0,userName.indexOf('_:_')+1);
            userNameString = userName.substring(userName.indexOf('_:_')+3,userName.length());
        }
        render(dictionaryGroupService.fetchUserDetail(userNameString,userType) as JSON)
    }


    def fetchUserShareList(String term, Integer page, Integer max, String userName) {
        render(dictionaryGroupService.fetchUserShareList(term, page, max, userName) as JSON)
    }

    def fetchList(Integer dictionaryType, String term, String dataSource, Integer page, Integer max, String userName, Boolean exactSearch, Boolean isMultiIngredient) {
        render(dictionaryGroupService.fetchList(dictionaryType, term, dataSource, page, max, userName, exactSearch, isMultiIngredient) as JSON)
    }

    def checkIfProductsAreValid(Map data) {
        List<String> dataSources = Holders.getGrailsApplication().getConfig().supported.datasource
        for (dataSource in dataSources) {
            if (data[dataSource]) {
                for (level_products in data[dataSource]) {
                    String level = level_products.key
                    def products = level_products.value
                    for (product in products) {
                        String productName = product.name
                        String productId = product.id.toString()
                        Map viewClass = ["1": LmProdDic200, "2": LmProdDic201, "3": LmProdDic202, "4": LmProdDic203, "5": LmProdDic204, "6": LmProdDic205, "7": LmProdDic206, "8": LmProdDic207, "9": LmProdDic208, "10": LmProdDic209, "11": LmProdDic210, "12": LmProdDic211, "13": LmProdDic212, "14": LmProdDic213, "15": LmProdDic214, "16": LmProdDic215, "17": LmProdDic216, "18": LmProdDic217, "19": LmProdDic218, "20": LmProdDic219]
                        def viewName = viewClass[level]
                        List productexists = []
                        viewName."$dataSource".withNewSession {
                            productexists = viewName."$dataSource".createCriteria().list {
                                eq('name', productName)
                                eq('viewId', productId)
                            }
                        }
                        if (productexists.size() == 0) {
                            return false
                        }
                    }
                }
            }
        }
        return true
    }

    def checkValidFaersData(Map data) {
        if (data["faers"]['2'].size() != 0 || data["faers"]['4'].size() != 0) {
            return false
        }
        return true
    }

    def addDataSources(Map data) {
        List<String> dataSources = Holders.getGrailsApplication().getConfig().supported.datasource
        Set<String> includedDataSources = []
        for (dataSource in dataSources) {
            if (data[dataSource]) {
                for (level_products in data[dataSource]) {
                    def products = level_products.value
                    if (products.size() != 0) {
                        includedDataSources.add(dataSource)
                    }
                }
            }
        }
        return includedDataSources
    }

    boolean checkIfProductGroupExist(String username, String groupName) {
        User owner = User.findByUsernameIlike(username)
        if (owner != null && DictionaryGroup.findByOwnerAndGroupNameAndIsDeleted(owner, groupName, false)) {
            return true
        }
        return false
    }

    boolean checkIfDataIsEmpty(Map data, String datasource) {
        if (data[datasource]) {
            for (level_products in data[datasource]) {
                def products = level_products.value    // can't specify data type as in case of empty products this is considered as list and in case there are products then it is considered map.
                if (products.size() != 0) {
                    return false
                }
            }
        }
        return true
    }

    def saveBulkUpdate(String username) {
        def productGroupData = request.JSON
        Map status = [:]
        productGroupData.productGroups.each { prodGrp ->
            DictionaryGroupCmd dictionaryGroupCmd = new DictionaryGroupCmd()
            dictionaryGroupCmd.data = prodGrp['data']
            Map prodGrp_Data = (new JsonSlurper()).parseText(prodGrp['data'])
            if (!checkValidFaersData(prodGrp_Data)) {
                status[prodGrp['oldName']] = message(code:'app.product.group.faers.data.invalid')
                return
            }
            if (!checkIfProductsAreValid(prodGrp_Data)) {
                status[prodGrp['oldName']] = message(code:'app.product.group.product.name.invalid')
                return
            }
            boolean flag = checkIfProductGroupExist(prodGrp['owner'], prodGrp['oldName'])
            dictionaryGroupCmd.groupName = prodGrp['newName']
            dictionaryGroupCmd.description = prodGrp['description']
            dictionaryGroupCmd.copyGroups = prodGrp['copyGroups']
            dictionaryGroupCmd.sharedWith = prodGrp['sharedWith']
            dictionaryGroupCmd.owner = prodGrp['owner']
            dictionaryGroupCmd.type = prodGrp['type']
            boolean checkIfRemoveCase = true
            if (!flag) {
                dictionaryGroupCmd.dataSourceNames = addDataSources(prodGrp_Data)
            } else {
                User owner = User.findByUsernameIlike(prodGrp['owner'])
                DictionaryGroup dg = DictionaryGroup.findByOwnerAndGroupNameAndIsDeleted(owner, prodGrp['oldName'], false)
                List<String> existingDatasources = new ArrayList<>(dg.dataSources)
                String newDataString = '{'
                prodGrp['dataSources'].each { dataSrc ->
                    if (checkIfDataIsEmpty(prodGrp_Data, dataSrc)) {
                        checkIfRemoveCase = false
                        existingDatasources.remove(dataSrc)
                        newDataString = newDataString + "$dataSrc" + ":" + new JsonBuilder(prodGrp_Data[dataSrc]) + ","
                    }
                }
                if (!checkIfRemoveCase) {
                    existingDatasources.each { dsn ->
                        DictionaryGroupData."$dsn".withNewSession { session ->
                            DictionaryGroupData dGD = DictionaryGroupData."$dsn".get(dg.id)
                            Map prodGrpData = (new JsonSlurper()).parseText(dGD.data)
                            newDataString = newDataString + "$dsn" + ":" + new JsonBuilder(prodGrpData) + ","
                        }
                    }
                    newDataString = newDataString.substring(0, newDataString.size() - 1) + '}'
                    dictionaryGroupCmd.data = newDataString
                    dictionaryGroupCmd.dataSourceNames = existingDatasources
                } else {
                    dictionaryGroupCmd.dataSourceNames = prodGrp['dataSources']
                }
            }
            dictionaryGroupCmd.createdBy = prodGrp['createdBy']
            dictionaryGroupCmd.modifiedBy = prodGrp['modifiedBy']
            dictionaryGroupCmd.lastUpdated = prodGrp['lastUpdated']
            dictionaryGroupCmd.dateCreated = prodGrp['dateCreated']
            dictionaryGroupCmd.sharedWithUser = prodGrp['sharedWithUser']
            dictionaryGroupCmd.sharedWithGroup = prodGrp['sharedWithGroup']
            dictionaryGroupCmd.tenantId = prodGrp['tenantId']

            if (prodGrp['oldName'] == prodGrp['newName'] && flag) {
                try {
                    User owner = User.findByUsernameIlike(prodGrp['owner'])
                    dictionaryGroupCmd.id = DictionaryGroup.findByOwnerAndGroupName(owner, prodGrp['oldName']).id
                    setSignalUserToSession(username)
                    dictionaryGroupService.save(dictionaryGroupCmd, checkIfRemoveCase)
                    status[prodGrp['oldName']] = message(code:'app.product.group.update.status.success')
                } catch (DictionaryGroupException ex) {
                    status[prodGrp['oldName']] = ex.message

                }
            } else if (prodGrp['oldName'] == prodGrp['newName'] && !flag) {
                try {
                    dictionaryGroupCmd.id = null
                    setSignalUserToSession(username)
                    dictionaryGroupService.save(dictionaryGroupCmd, true)
                    status[prodGrp['oldName']] = message(code:'app.product.group.create.status.success')
                } catch (DictionaryGroupException ex) {
                    status[prodGrp['oldName']] = ex.message

                }
            } else if (prodGrp['oldName'] != null && prodGrp['oldName'] != prodGrp['newName'] && flag) {
                try {
                    User owner = User.findByUsernameIlike(prodGrp['owner'])
                    dictionaryGroupCmd.id = DictionaryGroup.findByOwnerAndGroupName(owner, prodGrp['oldName']).id
                    setSignalUserToSession(username)
                    dictionaryGroupService.save(dictionaryGroupCmd, checkIfRemoveCase)
                    status[prodGrp['oldName']] = message(code:'app.product.group.update.status.success')
                } catch (DictionaryGroupException ex) {
                    status[prodGrp['oldName']] = ex.message

                }
            } else if (prodGrp['oldName'] != null && prodGrp['oldName'] != prodGrp['newName'] && !flag) {
                DictionaryGroup dg = DictionaryGroup.findByGroupName(prodGrp['oldName'])
                if (dg != null) {
                    status[prodGrp['oldName']] = message(code:'app.product.group.owner.nopermission')
                } else {
                    status[prodGrp['oldName']] = message(code:'app.product.group.oldname.notExist')
                }

            }

        }
        render(status as JSON)
    }

    def save(DictionaryGroupCmd dgc) {
        try {
            setSignalUserToSession(dgc.modifiedBy)
            render(dictionaryGroupService.save(dgc) as JSON)
        } catch (DictionaryGroupException ex) {
            log.error("${ex.message}")
            response.status = 400
            render([error: ex.message, errorCode: ex.errorCode] as JSON)
        }
    }

    def delete(Long id, String userName) {
        try {
            setSignalUserToSession(userName)
            dictionaryGroupService.delete(id, userName)
            render([result: 'success'] as JSON)
        } catch (DictionaryGroupException ex) {
            log.error("${ex.message}")
            response.status = 400
            render([error: ex.message, errorCode: ex.errorCode] as JSON)
        }
    }

    def groupDetails(Long id, String userName, Boolean withData) {
        render(MiscUtil.getObjectNonSyntheticProperties(dictionaryGroupService.groupDetails(id, userName, !!withData)) as JSON)
    }

    void setSignalUserToSession(String userName) {
        session.signalUsername = userName
        session.signalFullname = User.findByUsernameIlike(userName)?.fullName ?: "PVS User"
    }
}
