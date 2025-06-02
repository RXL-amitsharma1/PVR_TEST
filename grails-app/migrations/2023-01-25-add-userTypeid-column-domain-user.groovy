import com.rxlogix.enums.UserType
import com.rxlogix.user.User
import grails.util.Holders

databaseChangeLog = {
    changeSet(author: "Amit", id: "202301231452-1") {
        addColumn(tableName: "PVUSER") {
            column(name: "USER_TYPE", type: "varchar2(255 char)") {
                constraints(nullable: true)
            }
        }
    }

    changeSet(author: "Amit" , id:"202301231538-3"){
        grailsChange {
            change {
                    try {
                         sql.execute("UPDATE PVUSER SET USER_TYPE = 'LDAP' WHERE USERNAME != 'pvr_user' AND USER_TYPE='NON_LDAP'")
                    } catch (Exception ex) {
                        println "#### Error while updating User For Non Ldap ####"
                        println(ex.getMessage())
                    }
            }
        }
    }
}