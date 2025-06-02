package com.rxlogix

import com.rxlogix.audit.AuditTrail
import com.rxlogix.audit.AuditTrailChild
import com.rxlogix.config.ReportField
import com.rxlogix.dto.AuditTrailChildDTO
import com.rxlogix.dto.PrivacyProfileResponseDTO
import com.rxlogix.signal.SignalIntegrationService
import com.rxlogix.user.FieldProfile
import com.rxlogix.user.FieldProfileFields
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import com.rxlogix.user.UserGroupUser
import com.rxlogix.util.AuditLogConfigUtil
import com.rxlogix.util.ViewHelper
import grails.gorm.multitenancy.Tenants
import grails.util.Holders
import grails.validation.ValidationException
import groovy.sql.Sql
import org.springframework.http.HttpStatus
import org.springframework.web.multipart.MultipartFile
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.sql.Connection
import org.springframework.jdbc.datasource.SimpleDriverDataSource

import java.sql.SQLException

class UtilService {
    static transactional = false
    def grailsApplication
    private SimpleDriverDataSource reportDataSource
    private SimpleDriverDataSource reportDataSourcePVR
    CRUDService CRUDService
    SignalIntegrationService signalIntegrationService
    UserService userService
    ReportFieldService reportFieldService

    String getApplicationUserForSeeding() {
        return grailsApplication.config.pvreports.seeding.user ?: ''
    }

    String getJobUser() {
        return grailsApplication.config.pvreports.job.user ?: ''
    }

    String getHostIdentifier() {
        grailsApplication.config.getProperty('hazelcast.server.instance.name') ?: (InetAddress.getLocalHost().getHostName())
    }

    public Connection getReportConnection() {
        if (!reportDataSource) {
            reportDataSource = new SimpleDriverDataSource()
            reportDataSource.driverClass = Class.forName(grailsApplication.config.dataSources.pva.driverClassName)
            reportDataSource.username = grailsApplication.config.dataSources.pva.username
            if (grailsApplication.config.dataSources.pva.passwordEncryptionCodec) {
                reportDataSource.password = Class.forName(grailsApplication.config.dataSources.pva.passwordEncryptionCodec).decode(grailsApplication.config.dataSources.pva.password)
            } else {
                reportDataSource.password = grailsApplication.config.dataSources.pva.password
            }
            reportDataSource.url = grailsApplication.config.dataSources.pva.url
            Properties properties = new Properties()
            properties.put("defaultRowPrefetch", grailsApplication.config.jdbcProperties.fetch_size ?: 50)
            properties.put("defaultBatchValue", grailsApplication.config.jdbcProperties.batch_size ?: 5)
            reportDataSource.setConnectionProperties(properties)
        }
        Connection connection = reportDataSource.getConnection()
        //Set the context as per current tenant thread context.
        new Sql(connection).call("{call p_set_context('${grailsApplication.config.app.p.set.session.context.key}','${grailsApplication.config.app.p.set.session.context.value}')}")
        if (Holders.config.getProperty('pvreports.multiTenancy.enabled', Boolean))
            new Sql(connection).call('{call pkg_mart_set_context.set_context(?,?)}',[Holders.config.getRequiredProperty('pvreports.multiTenancy.martDBName'), Tenants.currentId()])
        return connection
    }

    public Connection getReportConnectionForPVR() {
        if (!reportDataSourcePVR) {
            reportDataSourcePVR = new SimpleDriverDataSource()
            reportDataSourcePVR.driverClass = Class.forName(grailsApplication.config.dataSource.driverClassName)
            reportDataSourcePVR.username = grailsApplication.config.dataSource.username
            if (grailsApplication.config.dataSource.passwordEncryptionCodec) {
                reportDataSourcePVR.password = Class.forName(grailsApplication.config.dataSource.passwordEncryptionCodec).decode(grailsApplication.config.dataSource.password)
            } else {
                reportDataSourcePVR.password = grailsApplication.config.dataSource.password
            }
            reportDataSourcePVR.url = grailsApplication.config.dataSource.url
            Properties properties = new Properties()
            properties.put("defaultRowPrefetch", grailsApplication.config.jdbcProperties.fetch_size ?: 50)
            properties.put("defaultBatchValue", grailsApplication.config.jdbcProperties.batch_size ?: 5)
            reportDataSourcePVR.setConnectionProperties(properties)
        }
        return reportDataSourcePVR.getConnection()
    }
    /*
        Method to fetch user based on username irrespective of casing as in PVS username-casing is handled differently.
     */

    public User getUserForPVS(String username, boolean isEnabled = false){
        String query = "from User u where LOWER(u.username) = :username"
        if(isEnabled){
            query = query.concat(" and enabled = true")
        }
        List<User> result = User.executeQuery(query, [username: username.toLowerCase()])
        return result.get(0)
    }

    boolean containsOnlyValues(List dirtyProperties, List executionProperties) {
        boolean containsOnly = true
        dirtyProperties.every { property ->
            if (!executionProperties.contains(property) && property != "scheduleDateJSON") {
                containsOnly = false
            }
        }
        return containsOnly
    }

    /*
    This method take the input stream and check that it is a valid UTF_16, UTF_8
     */
    public Charset detectEncoding(InputStream input) throws IOException {
        byte[] buffer = new byte[4];
        input.mark(4);
        input.read(buffer, 0, buffer.length);
        input.reset();

        // Check for UTF-16 BOM
        if ((buffer[0] == (byte) 0xFF && buffer[1] == (byte) 0xFE) ||
                (buffer[0] == (byte) 0xFE && buffer[1] == (byte) 0xFF)) {
            return StandardCharsets.UTF_16;
        }
        // Default to UTF-8 if no BOM is found
        return StandardCharsets.UTF_8;
    }

    /*
    This method take the multipart file and process the encoded data in the string form
     */
    public String readFileToString(MultipartFile file) throws IOException {
        InputStream input = new BufferedInputStream(file.getInputStream())
        Charset encoding = detectEncoding(input);
        byte[] encoded = file.getBytes();
        return new String(encoded, encoding);
    }
    //Creates custom audit log
    void createAuditLog(String category, User user, String description, String entityName, String moduleName, String entityValue, String transactionId, List<AuditTrailChildDTO> auditTrailChildList){
        String applicationName = Holders.config.getProperty('grails.plugin.auditLog.applicationName')
        entityValue = entityValue?.length() > 32000 ? entityValue?.substring(0, 32000) : entityValue
        AuditTrail auditTrail = new AuditTrail(category: category, username: user ? user.username : "PVR System User", fullname: user ? user.fullName : "", applicationName: applicationName, description: description, entityName: entityName, moduleName: moduleName, entityValue: entityValue, transactionId: transactionId + System.currentTimeMillis())
        auditTrail.save(flush: true, failOnError: true)
        auditTrailChildList.each{
            AuditTrailChild auditTrailChild = new AuditTrailChild(propertyName: it.propertyName, oldValue: it.oldValue, newValue: it.newValue, auditTrail: auditTrail)
            auditTrailChild.save(flush: true, failOnError: true)
        }
    }

    /**
     * This method is used to update the Privacy Field Profile with data sent from PV Admin.
     *
     * @param fields A Map containing the list of fields to be added in the Field Profile
     * @param fieldProfile The Field Profile that needs to be updated
     *
     * @return ResponseDTO – A DTO containing the details about the status of the process. It has the following attributes ->
     *     <ul>
     *       <li><code>code</code> – Status code</li>
     *       <li><code>status</code> – "SUCCESS" or "FAILURE"</li>
     *       <li><code>message</code> – Additional detail or error message</li>
     *     </ul>
     */
    PrivacyProfileResponseDTO updatePrivacyFieldProfile(Map<String, List<Map>> fields, FieldProfile fieldProfile) {
        PrivacyProfileResponseDTO responseDTO = new PrivacyProfileResponseDTO()
        try {
            Map<String, String> oldFieldProfileAuditMap = getPropertyMapForAudit(fieldProfile)

            //Clear existing fields from Field Profile
            clearFieldsFromProfile(fieldProfile)

            //Add new fields to Field Profile and get fields that do not exist in PVR
            List<String> nonExistingFields = populateReportFields(fieldProfile, fields.privacyFieldList)

            CRUDService.instantUpdateWithoutAuditLog(fieldProfile)
            Map<String, String> newFieldProfileAuditMap = getPropertyMapForAudit(fieldProfile)

            AuditLogConfigUtil.logChanges(fieldProfile, newFieldProfileAuditMap, oldFieldProfileAuditMap, Constants.AUDIT_LOG_UPDATE, " " + ViewHelper.getMessage("privacy.profile.auditLog.extraValue"))

            //Update the Users and User Groups that are linked to the Field Profile
            updateUsersAndGroups(fieldProfile)

            if (nonExistingFields?.size() > 0) {
                log.info("${nonExistingFields.join(", ")} not found while updating Privacy Field Profile")
                responseDTO.setSuccessResponse(HttpStatus.PARTIAL_CONTENT.value(), "Privacy Field Profile updated successfully, ${nonExistingFields.size()} report fields either not found or are deleted in PVR")
            } else {
                log.info("Privacy Field Profile ${fieldProfile.name} updated successfully")
                responseDTO.setSuccessResponse(HttpStatus.OK.value(), "Privacy Field Profile updated successfully")
            }

            //Sync the updated Field Profile with PVS
            syncRedactedDataWithPVS(fieldProfile)

        } catch (ValidationException | SQLException ex) {
            log.error("Unable to update privacy profile", ex)
            responseDTO.setFailureResponse(ex.getMessage())
        }
        return responseDTO
    }

    /**
     * This method validates the JSON received from PV Admin.
     * <p>
     * The JSON received from PV Admin should contain a list of data of the report fields.
     * <p>
     * This method checks whether the data has the following required attributes :
     * <ul>
     *   <li><code>fieldId</code></li>
     *   <li><code>tenantId</code></li>
     *   <li><code>langId</code></li>
     * </ul>
     *
     * @param fieldData A List containing data of the fields sent from PV Admin
     * @return String The key that is missing from the JSON
     */
    String validateFieldJSON(List<Map> fieldData) {
        for (Map item : fieldData) {
            if (!item.containsKey("fieldId")) {
                return "fieldId"
            }
            if (!item.containsKey("tenantId")) {
                return "tenantId"
            }
            if (!item.containsKey("langId")) {
                return "langId"
            }
        }
        return null
    }

    private Map<String, String> getPropertyMapForAudit(FieldProfile fieldProfile) {
        Set<ReportField> blindedFields = FieldProfileFields.findAllByFieldProfileAndIsBlinded(fieldProfile, true).collect {it.reportField}
        Set<ReportField> protectedFields = FieldProfileFields.findAllByFieldProfileAndIsProtected(fieldProfile, true).collect {it.reportField}
        Set<ReportField> hiddenFields = FieldProfileFields.findAllByFieldProfileAndIsHidden(fieldProfile, true).collect {it.reportField}
        return [
                "name"           : fieldProfile.name,
                "description"    : fieldProfile.description,
                "isDeleted"      : fieldProfile.isDeleted as String,
                "blindedFields"  : blindedFields ? getReportFieldNameString(blindedFields) : "",
                "protectedFields": protectedFields ? getReportFieldNameString(protectedFields) : "",
                "reportFields"   : hiddenFields ? getReportFieldNameString(hiddenFields) : ""
        ]
    }

    private String getReportFieldNameString(Set<ReportField> reportFields) {
        List<String> fieldNameList = []
        reportFields.each {
            fieldNameList.add(it.getDisplayName())
        }
        return fieldNameList.sort().join(", ")
    }

    /**
     * This method is used to clear the existing report fields present in the field.
     * @param fieldProfile The Field Profile that needs to be cleared
     * @return void
     */
    private void clearFieldsFromProfile(FieldProfile fieldProfile) {
        FieldProfileFields.executeUpdate("delete from FieldProfileFields where fieldProfile.id = :fp", [fp: fieldProfile.id])
    }

    /**
     * This method is used to update the field profile with new report fields.
     * @param fieldProfile The Field Profile that needs to be updated
     * @param fieldList A List containing data of the new report fields to be added
     * @return List - List of names of report fields not found in PVR
     */
    private List<String> populateReportFields(FieldProfile fieldProfile, List<Map> fieldList) {
        List<String> nonExistingFields = []
        fieldList.each {
            it.langId = (it.langId.toString().toLowerCase().trim() == 'en') ? '*' : it.langId
            String fieldName = reportFieldService.getFieldVariableForUniqueId(it.fieldId as String, it.langId as String, it.tenantId as Long)
            if (!fieldName) {
                nonExistingFields << fieldName
            } else {
                ReportField field = ReportField.findByNameAndIsDeleted(fieldName, false)
                if (!field) {
                    nonExistingFields << fieldName
                } else {
                    field.isProtected = true
                    userService.addToFieldsWithFlag(fieldProfile, field, false, true, false)
                }
            }
        }
        return nonExistingFields
    }

    /**
     * This method is used to sync the updated field profile data to PVS.
     * @param fieldProfile The Field Profile that has been updated
     * @return void
     */
    private void syncRedactedDataWithPVS(FieldProfile fieldProfile) {
        if (Holders.config.pvsignal.url) {
            UserGroup.findAllByFieldProfile(fieldProfile).each {
                signalIntegrationService.updateBlindedDataToSignal(it, true)
            }
        }
    }

    /**
     * This method is used to update the Blinded and Protected flags of users and user groups that are linked to the updated field profile.
     * @param fieldProfile The Field Profile that has been updated
     * @return void
     */
    private void updateUsersAndGroups(FieldProfile fieldProfile) {
        userService.updateBlindedFlagForUsersAndGroups()
        userService.updateProtectedFlagForUsersAndGroups()
        List<UserGroup> groups = UserGroup.findAllByFieldProfile(fieldProfile)
        if (groups && UserGroupUser.countByUserAndUserGroupInList(userService.getUser(), groups)) {
            log.info("Clearing cache for QUERY Report field group (For updation in Field Profile - ${fieldProfile?.name})")
            reportFieldService.clearCacheReportFields()
        }
    }

}
