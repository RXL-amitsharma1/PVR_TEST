databaseChangeLog = {
    changeSet(author: "Vivek", id: "202404301750-1") {
        sql("""UPDATE SQL_TEMPLT
                SET WHERE_STMT = q'!and cmw.flag_blinded = ':BLINDED_CIOMS_CHECKBOX_VALUE:' and 
                      cmw.flag_privacy_protected= ':PRIVACY_CIOMS_CHECKBOX_VALUE:' and 
                      NVL(cmwd.flag_blinded,':BLINDED_CIOMS_CHECKBOX_VALUE:' ) = ':BLINDED_CIOMS_CHECKBOX_VALUE:' and 
                      NVL(cmwd.flag_privacy_protected,':PRIVACY_CIOMS_CHECKBOX_VALUE:') = ':PRIVACY_CIOMS_CHECKBOX_VALUE:'
                      order by cmw.MANU_RPT_NO_8G asc!'
                WHERE EXISTS (
                    SELECT 1
                    FROM RPT_TEMPLT
                    WHERE is_deleted = 0
                      AND name = 'Medwatch Template'
                      AND MEDWATCH_TEMPLATE = 1
                      AND orig_templt_id = 0
                      AND SQL_TEMPLT.id = RPT_TEMPLT.id
                )"""
        )
    }
}