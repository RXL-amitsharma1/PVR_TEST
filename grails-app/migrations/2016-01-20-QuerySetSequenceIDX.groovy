databaseChangeLog = {
    changeSet(author: "Sherry Huang", id:'added index to QUERY_SETS_SUPER_QRS') {
        sql("""merge INTO QUERY_SETS_SUPER_QRS t USING
(SELECT
  row_number() over (partition BY QUERY_SET_ID order by null) - 1 value,
  ROWID
FROM QUERY_SETS_SUPER_QRS
WHERE SUPER_QUERY_IDX   IS NULL
) s ON (t.ROWID = s.rowid)
WHEN matched THEN
  UPDATE SET t.SUPER_QUERY_IDX = s.value;""")
    }
}