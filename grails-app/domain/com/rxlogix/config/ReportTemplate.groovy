package com.rxlogix.config


import com.rxlogix.LibraryFilter

import com.rxlogix.OrderByUtil
import com.rxlogix.enums.ReportFormatEnum
import com.rxlogix.enums.TemplateTypeEnum
import com.rxlogix.hibernate.EscapedILikeExpression
import com.rxlogix.repo.RepoFileResource
import com.rxlogix.reportTemplate.ReassessListednessEnum
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.user.UserGroupUser
import grails.gorm.DetachedCriteria
import grails.plugins.orm.auditable.AuditEntityIdentifier
import net.sf.jasperreports.engine.xml.JRXmlLoader
import org.apache.commons.lang.builder.HashCodeBuilder
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.criterion.Order
import org.hibernate.criterion.Restrictions
import org.hibernate.sql.JoinType

class ReportTemplate implements Comparable<ReportTemplate> {

    transient def userService
    transient def templateService
    @AuditEntityIdentifier
    String name
    String description
    Category category
    User owner
    boolean isDeleted = false
    List tags
    TemplateTypeEnum templateType

    Long originalTemplateId = 0          //If > 0, it is an Executed Report Template

    boolean factoryDefault = false
    boolean editable = true // Used for CIOMS I Template.
    boolean ciomsI = false // only one COIMS I Template in the app
    boolean medWatch = false

    String templateFooter
    boolean showTemplateFooter

    //todo:  hasBlanks is only relevant for SQL/Non Case templates. Move to those subclasses. - morett
    boolean hasBlanks = false
    boolean qualityChecked = false
    ReassessListednessEnum reassessListedness
    boolean reassessForProduct = false
    Date templtReassessDate

    //Standard fields
    Date dateCreated = new Date()
    Date lastUpdated = new Date()
    String createdBy
    String modifiedBy
    Date lastExecuted
    RepoFileResource fixedTemplate
    boolean useFixedTemplate = false
    boolean showChartSheet = false
    Integer maxChartPoints
    boolean interactiveOutput = false

    Boolean autoTimeStampOff = false

    public static final String CIOMS_I_TEMPLATE_NAME = "CIOMS I Template"
    public static final String MEDWATCH_TEMPLATE_NAME = "Medwatch Template"

    static hasMany = [tags: Tag, userGroupTemplates: UserGroupTemplate, userTemplates: UserTemplate, templateUserStates: TemplateUserState]

    static transients = ['showTemplateFooter', 'autoTimeStampOff']

    static mapping = {
        autoTimestamp false
        tablePerHierarchy false
        table name: "RPT_TEMPLT"

        tags joinTable: [name: "RPT_TEMPLTS_TAGS", column: "TAG_ID", key: "RPT_TEMPLT_ID"], indexColumn: [name: "TAG_IDX"], fetch: 'join'
        userTemplates cascade: "all-delete-orphan", fetch: 'join', lazy: false
        userGroupTemplates cascade: "all-delete-orphan", fetch: 'join', lazy: false
        name column: "NAME"
        description column: "DESCRIPTION"
        category column: "CATEGORY_ID"
        owner column: "PV_USER_ID", fetch: "join", cascade: 'none'
        isDeleted column: "IS_DELETED"
        templateType column: "TEMPLATE_TYPE"
        originalTemplateId column: "ORIG_TEMPLT_ID"

        reassessListedness column: "REASSESS_LISTEDNESS"
        reassessForProduct column: "REASSESS_FOR_PRODUCT", defaultValue: 0
        templtReassessDate column: "TEMPLT_REASSESS_DATE", defaultValue: null
        hasBlanks column: "HASBLANKS"
        editable column: "EDITABLE"
        ciomsI column: "CIOMS_I_TEMPLATE"
        medWatch column: "MEDWATCH_TEMPLATE"
        templateFooter column: "TEMPLATE_FOOTER"
        qualityChecked column: "QUALITY_CHECKED"
        lastExecuted column: "LAST_EXECUTED"
        fixedTemplate column: "FIXED_TEMPLT_ID", cascade: 'all'
        useFixedTemplate column: "USE_FIXED_TEMPLATE"
        showChartSheet column: "SHOW_CHART_SHEET", defaultValue: 0
        maxChartPoints column: "MAX_CHART_POINTS"
        templateUserStates joinTable: [name: "TEMPLATE_USER_STATE", column: "ID", key: "TEMPLATE_ID"]
        interactiveOutput column: "INTERACTIVE_OUTPUT"
    }

    static constraints = {
        name(nullable: false, maxSize: 1000, validator: { val, obj ->
            //Name is unique to user
            if ((!obj.id || obj.isDirty("name") || obj.isDirty("owner")) && obj.originalTemplateId == 0L) {
                long count = ReportTemplate.createCriteria().count {
                    ilike('name', "${obj.name}")
                    eq('owner', obj.owner)
                    eq('isDeleted', false)
                    eq('originalTemplateId', 0L)
                    if (obj.id) {
                        ne('id', obj.id)
                    }
                }
                if (count) {
                    return "com.rxlogix.config.template.name.unique.per.user"
                }
                if (obj.ciomsI && cioms1Id() != 0) {
                    return "com.rxlogix.config.template.single.cioms"
                }
                if (obj.medWatch && medWatchId() != 0) {
                    return "com.rxlogix.config.template.single.cioms"
                }
            }
            if (val == CIOMS_I_TEMPLATE_NAME && !obj.ciomsI) {
                return "com.rxlogix.config.CustomSQLTemplate.unique.COIMSI"
            }
            if (val == MEDWATCH_TEMPLATE_NAME && !obj.medWatch) {
                return "com.rxlogix.config.CustomSQLTemplate.unique.Medwatch"
            }
        })
        templateFooter(nullable: true, maxSize: 1000)
        description(nullable: true, maxSize: 1000)
        category(nullable: true)
        tags(nullable: true)
        createdBy(nullable: false, maxSize: 255)
        modifiedBy(nullable: false, maxSize: 255)
        reassessListedness(nullable: true)
        reassessForProduct(nullable: false)
        templtReassessDate(nullable: true)
        showTemplateFooter bindable: true
        lastExecuted(nullable: true)
        fixedTemplate(nullable: true, validator: { val, obj ->
            if (val?.data) {
                try {
                    JRXmlLoader.load(new ByteArrayInputStream(val.data))
                } catch (Exception e) {
                    return ["invalid", val.name, e.message]
                }
            }
            return true
        })
        dateCreated(nullable: true)
        lastUpdated(nullable: true)
        maxChartPoints(nullable: true)
    }

    def beforeInsert() {
        dateCreated = new Date()
        lastUpdated = new Date()
    }

    def beforeUpdate() {
        if (!autoTimeStampOff) {
            lastUpdated = new Date()
        }
    }


    static namedQueries = {

        fetchAllByUser { User user ->
            createAlias('userTemplates', 'ut',  CriteriaSpecification.LEFT_JOIN)
            createAlias('userGroupTemplates', 'ugt',  CriteriaSpecification.LEFT_JOIN)
            createAlias('owner', 'owner',  CriteriaSpecification.LEFT_JOIN)
            eq('isDeleted', false)
            eq('originalTemplateId', 0L)
            if (!user.isAnyAdmin()) {
                or {
                    eq('owner.id', user.id)
                    eq('ut.user.id', user.id)
                    if (UserGroup.countAllUserGroupByUser(user)) {
                        'in'('ugt.userGroup', UserGroup.fetchAllUserGroupByUser(user))
                    }
                }
            }
        }

        fetchAllByUserFiltered { User user, String searchString, String sort, String dir ->
            fetchAllByUser(user)
            createAlias('category', 'cg',  CriteriaSpecification.LEFT_JOIN)
            if(searchString){
                or {
                    iLikeWithEscape('name', "%${EscapedILikeExpression.escapeString(searchString)}%")
                    iLikeWithEscape('description', "%${EscapedILikeExpression.escapeString(searchString)}%")
                    iLikeWithEscape('cg.name', "%${EscapedILikeExpression.escapeString(searchString)}%")
                    iLikeWithEscape('owner.fullName', "%${EscapedILikeExpression.escapeString(searchString)}%")
                }
            }
            if (sort == 'category') {
                order('cg.name', "${dir}")
            } else if (sort == 'owner.fullName') {
                order('owner', "${dir}")
            } else if (sort == 'checkUsage') {
                order('lastUpdated', "${dir}")
            } else {
                order("${sort}", "${dir}")
            }
        }

        fetchCountByUserFiltered { User user, String searchString, String sort, String dir ->
            projections {
                countDistinct("id")
            }
            fetchAllByUserFiltered(user, searchString, sort, dir)
        }

        fetchAllOwners { User user, search ->
            projections {
                distinct("owner")
            }
            ownedByUser(user)
            'owner' {
                iLikeWithEscape('fullName', "%${EscapedILikeExpression.escapeString(search)}%")
            }
        }

        getLatestExRptTempltByOrigTempltId { Long id ->
            projections {
                property('id')
            }
            eq('originalTemplateId', id)
            order("dateCreated", "desc")
            maxResults(1)
        }

        countAllOwners { User user, search ->
            projections {
                countDistinct("owner")
            }
            ownedByUser(user)
            'owner' {
                iLikeWithEscape('fullName', "%${EscapedILikeExpression.escapeString(search)}%")
            }
        }

        getAllRecordsBySearchString { LibraryFilter filter ->
            createAlias('tags', 'tag', CriteriaSpecification.LEFT_JOIN)
            createAlias('category', 'category', CriteriaSpecification.LEFT_JOIN)
            eq('isDeleted', false)
//            Only Non Executed one
            eq("originalTemplateId", 0L)
            if (filter.search) {

                or {
                    iLikeWithEscape('name', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    iLikeWithEscape('description', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    'owner' {
                        iLikeWithEscape('fullName', "%${EscapedILikeExpression.escapeString(filter.search)}%")
                    }
                    iLikeWithEscape('tag.name', "%${EscapedILikeExpression.escapeString(filter.search)}%")

                    iLikeWithEscape('category.name', "%${EscapedILikeExpression.escapeString(filter.search)}%")
// We are using ENUM display value as well for querying. We need to have request scope available.
                    TemplateTypeEnum.searchBy(filter.search)?.each {
                        eq('templateType', it)
                    }

                    if (filter.search == "qced") {
                        eq('qualityChecked', true)
                    }
                }
            }
            ownedByUser(filter.user)
            or {
                if (filter.sharedWith?.ownerId) {
                    eq('owner.id', filter.sharedWith.ownerId)
                }
                if (filter.sharedWith?.usersId) {
                    or {
                        'in'('ut.user.id', filter.sharedWith.usersId)
                        'in'('ugt.userGroup.id', new DetachedCriteria(UserGroupUser).build {
                            projections {
                                distinct('userGroup.id')
                            }
                            'in'('user.id', filter.sharedWith.usersId)
                        })
                    }
                }
                if (filter.sharedWith?.groupsId) {
                    'in'('ugt.userGroup.id', filter.sharedWith.groupsId)
                }
                if (filter.sharedWith?.team && filter.user) {
                    or {
                        filter.user.getUserTeamIds()?.collate(999)?.each { 'in'('owner.id', it) }
                    }
                }
            }
            createAlias('templateUserStates', 'state', JoinType.LEFT_OUTER_JOIN, Restrictions.eq('user', filter.user))
            if (filter.advancedFilterCriteria) {
                filter.advancedFilterCriteria.each { cl ->
                    cl.delegate = delegate
                    cl.call()
                }
            }
            if (filter.favoriteSort) {
                and {
                    order('state.isFavorite', 'asc')
                    order('lastUpdated', 'desc')
                }
            }
        }

        //TODO: Remove this showXMLOption param and usages when XML templates are properly integrated. Please refer ticket: PVR-7866 pull request #3629
        countRecordsBySearchString { LibraryFilter filter, boolean showXMLOption = false ->
            projections {
                countDistinct("id")
            }
            getAllRecordsBySearchString(filter)

            if(!showXMLOption){
                ne("templateType", TemplateTypeEnum.ICSR_XML)
            }
        }

        //TODO: Remove this showXMLOption param and usages when XML templates are properly integrated. Please refer ticket: PVR-7866 pull request #3629
        fetchAllIdsBySearchString { LibraryFilter filter, boolean showXMLOption = false, String sortBy = null, String sortDirection = "asc" ->
            projections {
                distinct('id')
                property("dateCreated")
                property("lastUpdated")
                property("lastExecuted")
                property("category.name", "categoryName")
                property("category")
                property("name")
                property("description")
                property("templateType")
                property("qualityChecked")
                'owner' {
                    property("fullName", "fullName")
                }
                property("state.isFavorite", "isFavorite")
            }
            getAllRecordsBySearchString(filter)

            if(!showXMLOption){
                ne("templateType", TemplateTypeEnum.ICSR_XML)
            }

            if (sortBy) {
                if (sortBy == 'category') {
                    order(new Order("categoryName", "${sortDirection.toLowerCase()}" == "asc").ignoreCase())
                } else if (sortBy == 'qualityChecked') {
                    order(OrderByUtil.booleanOrder(sortBy, sortDirection))
                } else if (sortBy == 'owner.fullName') {
                    order(new Order("fullName", "${sortDirection.toLowerCase()}" == "asc").ignoreCase())
                } else if (['templateType', 'name', 'templateName', 'description'].contains(sortBy)) {
                    order(new Order("${sortBy}", "${sortDirection.toLowerCase()}" == "asc").ignoreCase())
                } else {
                    order("${sortBy}", "${sortDirection}")
                }
            }
        }


        ownedByUser { User currentUser ->
            createAlias('userTemplates', 'ut', CriteriaSpecification.LEFT_JOIN)
            createAlias('userGroupTemplates', 'ugt', CriteriaSpecification.LEFT_JOIN)
            eq('isDeleted', false)
            //            Only Non Executed one
            eq("originalTemplateId", 0L)
            if (!currentUser?.isAdmin()) {
                or {
                    currentUser?.getUserTeamIds()?.collate(999).each { 'in'('owner.id', it) }
                    eq('owner.id', currentUser.id)
                    'in'('ut.user', currentUser)
                    if (UserGroup.countAllUserGroupByUser(currentUser)) {
                        'in'('ugt.userGroup', UserGroup.fetchAllUserGroupByUser(currentUser))
                    }
                }
            }
        }
        ownedByUserWithSearchNoBlank { User user, String search, TemplateTypeEnum templateTypeEnum, boolean includeCllWithCustomSql ->
            ownedByUserWithSearch(user, search, templateTypeEnum, includeCllWithCustomSql)
            eq('hasBlanks', false)
        }

        countOwnedByUserWithSearch { User user, String search, TemplateTypeEnum templateTypeEnum = null, boolean includeCllWithCustomSql = false ->
            projections {
                countDistinct("id")
            }
            ownedByUserWithSearchQuery ( user, search, templateTypeEnum , includeCllWithCustomSql)
        }
        ownedByUserWithSearch { User user, String search, TemplateTypeEnum templateTypeEnum = null, boolean includeCllWithCustomSql = false ->
            projections {
                distinct('id')
                property("name")
                property("description")
                property("qualityChecked")
                property("hasBlanks")
                property("state.isFavorite", "isFavorite")
                'owner' {
                    property("fullName", "fullName")
                }
            }
            ownedByUserWithSearchQuery ( user, search, templateTypeEnum , includeCllWithCustomSql)
        }
        //TODO: Remove this showXMLOption param once XML templates are properly integrated. Please refer ticket: PVR-7866 pull request #3629
        ownedByUserWithSearchQuery { User user, String search, TemplateTypeEnum templateTypeEnum = null, boolean includeCllWithCustomSql = false ->
            ownedByUser(user)
            def _search = search
            boolean qc = false
            if (search?.toLowerCase()?.startsWith("qced")) {
                qc = true
                _search = search.substring(4).trim()
            }
            and {
                if (qc) eq('qualityChecked', true)
                if (_search) {
                    or {
                        iLikeWithEscape('name', "%${EscapedILikeExpression.escapeString(_search)}%")
                        iLikeWithEscape('description', "%${EscapedILikeExpression.escapeString(_search)}%")
                        'owner' {
                            iLikeWithEscape('fullName', "%${EscapedILikeExpression.escapeString(_search)}%")
                        }
                    }
                }
            }
            createAlias('templateUserStates', 'state', JoinType.LEFT_OUTER_JOIN, Restrictions.eq('user', user))
            and {
                order('qualityChecked', 'desc')
                order('state.isFavorite', 'asc')
                order('name', 'asc')
            }

            if(templateTypeEnum ){
                if(templateTypeEnum==TemplateTypeEnum.ICSR_XML){
                    or {
                        eq("templateType", TemplateTypeEnum.ICSR_XML)
                        and {
                            eq("name", ReportTemplate.CIOMS_I_TEMPLATE_NAME)
                            eq("ciomsI", true)
                        }
                        and {
                            eq("name", ReportTemplate.MEDWATCH_TEMPLATE_NAME)
                            eq("medWatch", true)
                        }
                    }
                } else {
                    eq("templateType", templateTypeEnum)
                }
            } else {
                ne("templateType", TemplateTypeEnum.ICSR_XML)
            }

            if (includeCllWithCustomSql) {
                eq("medWatch", false)
                eq("ciomsI", false)
                or {
                    eq("templateType", TemplateTypeEnum.CASE_LINE)
                    eq("templateType", TemplateTypeEnum.CUSTOM_SQL)
                }
            }
        }
    }

    transient List<String> getFieldsToValidate() {
        return this.getClass().declaredFields
                .collectMany { !it.synthetic ? [it.name] : [] }
    }

    boolean validateExcluding(List additionalFields = []) {
        List<String> allFields = this.getFieldsToValidate()
//Gather excluded fields
        List<String> defaultExcludedFields = ['dateCreated', 'lastUpdated', 'createdBy', 'modifiedBy'] + (additionalFields ?: [])
        //Add other fields if necessary
        List<String> allButExcluded = allFields - defaultExcludedFields
        return this.validate(allButExcluded)
    }

    boolean isEditableBy(User currentUser) {
        return (editable && (currentUser.isAdmin() || owner.id == currentUser.id || (owner.id in currentUser.getUserTeamIds())))
    }

    boolean isCiomsITemplate() {
        return ciomsI
    }

    boolean isMedWatchTemplate() {
        return medWatch
    }

    boolean getShowTemplateFooter() {
        templateFooter?.trim() ? true : false
    }

    boolean isViewableBy(User currentUser) {
        return (currentUser.isAdmin() || owner.id == currentUser.id || isVisible(currentUser) || (owner.id in currentUser.getUserTeamIds()))
    }

    boolean showTempltReassessDate() {
        return  this && (this.instanceOf(DataTabulationTemplate) || this.instanceOf(CaseLineListingTemplate)) && this.reassessListedness == ReassessListednessEnum.CUSTOM_START_DATE  && !this?.templtReassessDate
    }

    int countUsage() {
        return templateService.getUsagesCount(this)
    }

    def getNameWithDescription() {
        return this.name + " " + (this?.description ? "(" + this.description + ")" : "") + " - Owner: " + this.owner.fullName
    }

    def getSectionsName() {
        return this.description ? "${this.name} (${this.description})" : "${this.name}"
    }

    transient List<ReportFieldInfo> getAllSelectedFieldsInfo() { [] }

    List sortInfo() {
        List result = []
        if (templateType == TemplateTypeEnum.CASE_LINE || templateType == TemplateTypeEnum.DATA_TAB) {
            List sortFields = []
            getAllSelectedFieldsInfo().eachWithIndex { rfInfo, index ->
                if (rfInfo.sortLevel > 0) {
                    sortFields.add([rfInfo, index])
                }
            }
            sortFields.sort { it[0].sortLevel }
            sortFields.each {
                result.add([it[1], it[0].sort.value()])
            }
        }
        return result
    }

    @Override
    boolean equals(other) {
        if (!(other instanceof ReportTemplate)) {
            return false
        }

        this.name == other?.name &&
                this.createdBy == other?.createdBy && this.isDeleted == other?.isDeleted
    }

    @Override
    int hashCode() {
        def builder = new HashCodeBuilder()
        if (this.name) builder.append(this.name)
        if (this.createdBy) builder.append(this.createdBy)
        if (this.isDeleted) builder.append(this.isDeleted)
        builder.toHashCode()
    }

    //https://dev.to/joemccall86/groovy-s-compareto-operator-and-equality-312n
    //https://stackoverflow.com/questions/24963680/groovy-override-compareto
    @Override
    int compareTo(ReportTemplate obj) {
        //ascending order
        int value = this.name <=> obj?.name

        if (!value) {
            value = this.createdBy <=> obj?.createdBy
        }
        if (!value) {
            value = this.isDeleted <=> obj?.isDeleted
        }
        return value
    }

    boolean isNotExportable(ReportFormatEnum format) {
        return format == ReportFormatEnum.XML || ((ciomsI || medWatch) && (format == ReportFormatEnum.HTML || format == ReportFormatEnum.XLSX))
    }

    boolean isFavorite(User user) {
        return templateUserStates.find { item -> item.user == user }?.isFavorite
    }

    public Set<User> getShareWithUsers() {
        Set<User> users = []
        if (userTemplates) {
            users.addAll(userTemplates.collect { it.user })
        }
        users
    }

    public Set<UserGroup> getShareWithGroups() {
        Set<UserGroup> userGroups = []
        if (userGroupTemplates) {
            userGroups.addAll(userGroupTemplates.collect { it.userGroup })
        }
        return userGroups
    }

    boolean isVisible(User currentUser) {
        if (userTemplates?.user?.any { it.id == currentUser.id }) {
            return true
        }
        List<UserGroup> userGroups = UserGroup.fetchAllUserGroupByUser(currentUser).flatten()
        return userGroupTemplates?.userGroup?.any { it.id in userGroups*.id }
    }

    Set<String> getPOIInputsKeys() {
        Set<String> poiInputsSet = []
        return poiInputsSet
    }

    static getActiveUsersAndUserGroups(User user, String term) {
        def result = [users: [], userGroups: []]
        def groupIdsForUser = (UserGroup.fetchAllUserGroupByUser(user) ?: [[id: 0L]])*.id
        String userViewableTemplateSql = "and q.id in (select q1.id  from ReportTemplate as q1 left join q1.userGroupTemplates as ugq1 left join q1.userTemplates as uq1 " +
                "where q1.isDeleted=false and (q1.owner=:user or uq1.user=:user or ugq1.userGroup.id in (:groupIdsForUser)))"
        String groupsSql = "from UserGroup as ug where " +
                (term?" lower(ug.name) like :term and ":"")+
                " ug.id in (select ugq.userGroup.id from ReportTemplate as q join q.userGroupTemplates as ugq  where q.isDeleted=false " +
                (user.isAdmin() ? "" : userViewableTemplateSql) + ")";
        String usersSQL = "from User as u where " +
                (term?" ((u.fullName is not null and lower(u.fullName) like :term) or (u.fullName is null and lower(u.username) like :term)) and ":"") +
                " (u.id in (select uq.user.id from ReportTemplate as q join q.userTemplates as uq  where q.isDeleted=false " +
                (user.isAdmin() ? "" : userViewableTemplateSql) + ") or " +
                "u.id in (select ugu.user.id from UserGroupUser as ugu where ugu.userGroup.id in (:groups)))";
        Map groupParams = user.isAdmin() ? [:] : [user: user, groupIdsForUser: groupIdsForUser]
        if(term) groupParams.put('term','%'+term.toLowerCase()+'%')
        result.userGroups = UserGroup.findAll(groupsSql, groupParams, [sort: 'name'])
        Map userParams = user.isAdmin() ? [groups: result.userGroups ? result.userGroups*.id : [0L]] :
                [user: user, groupIdsForUser: groupIdsForUser, groups: result.userGroups ? result.userGroups*.id : [0L]]
        if(term) userParams.put('term','%'+term.toLowerCase()+'%')
        result.users = User.findAll(usersSQL, userParams, [sort: 'username'])
        result
    }

    static Long cioms1Id() {
        Long id = ReportTemplate.createCriteria().list {
            projections {
                property('id')
            }
            eq('ciomsI', true)
            eq('originalTemplateId', 0L)
            eq('name', CIOMS_I_TEMPLATE_NAME)
            eq('isDeleted', false)
            maxResults(1)
        }?.find()
        return id?:0
    }

    static Long medWatchId() {
        Long id = ReportTemplate.createCriteria().list {
            projections {
                property('id')
            }
            eq('medWatch', true)
            eq('originalTemplateId', 0L)
            eq('name', MEDWATCH_TEMPLATE_NAME)
            eq('isDeleted', false)
            maxResults(1)
        }?.find()
        return id?:0
    }


    @Override
    public String toString() {
        return "$name - $owner"
    }

    static ReportTemplate fetchOriginalByName(String name) {
        List<Long> ids = ReportTemplate.createCriteria().list {
            projections {
                property('id')
            }
            eq('name', name)
            eq('isDeleted', false)
            eq('originalTemplateId', 0L)
        }
        if (ids) {
            return ReportTemplate.load(ids.first())
        }
        return null
    }
}
