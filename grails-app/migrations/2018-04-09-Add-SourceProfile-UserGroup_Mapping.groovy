import com.rxlogix.config.SourceProfile
import com.rxlogix.user.UserGroup

databaseChangeLog = {

    changeSet(author: "prashantsahi (generated)", id: "1523257222353-104") {
        createTable(tableName: "USER_GRP_SRC_PROFILE") {
            column(name: "USER_GROUP_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "SRC_PROFILE_ID", type: "number(19,0)")

            column(name: "SRC_PROFILE_IDX", type: "number(10,0)")

        }
    }

    changeSet(author: "prashantsahi (generated)", id: "1523257222353-105") {
        addForeignKeyConstraint(baseColumnNames: "SRC_PROFILE_ID", baseTableName: "USER_GRP_SRC_PROFILE", constraintName: "FK_9hbyvbsq9pjxegfteue0lq4fm", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "SOURCE_PROFILE", referencesUniqueColumn: "false")
    }

    changeSet(author: "prashantsahi (generated)", id: "1523257222353-106") {
        addForeignKeyConstraint(baseColumnNames: "USER_GROUP_ID", baseTableName: "USER_GRP_SRC_PROFILE", constraintName: "FK_22uvj5wm0m1qpgj01wm8lry8u", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "USER_GROUP", referencesUniqueColumn: "false")
    }
}