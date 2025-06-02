databaseChangeLog = {

    changeSet(author: "anurag (generated)", id: "050420231655-1") {
        dropForeignKeyConstraint(baseTableName: "LOCALIZATION_HELP", constraintName: "FK_LOCALIZATION_ID")
        sql("ALTER TABLE LOCALIZATION_HELP ADD CONSTRAINT FK_LOCALIZATION_ID FOREIGN KEY (LOCALIZATION_ID) REFERENCES LOCALIZATION on delete cascade")
    }

}