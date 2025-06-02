CREATE GLOBAL TEMPORARY TABLE TEMP_IDS (
  ID NUMBER
) ON COMMIT PRESERVE ROWS;

-- Report Results

TRUNCATE TABLE RPT_RESULT;

DELETE FROM RPT_RESULT_DATA;

-- Executed Configurations

TRUNCATE TABLE TEMP_IDS;

INSERT INTO TEMP_IDS (ID) (
  SELECT vp2.PARAM_ID
  FROM VALUES_PARAMS vp2
  WHERE EXISTS(
      SELECT vp.*
      FROM EX_STATUS es
        LEFT JOIN EX_STATUSES_RPT_FORMATS exsrf
          ON exsrf.EX_STATUS_ID = es.ID
        LEFT JOIN EX_STATUSES_SHARED_WITHS essw
          ON essw.EX_STATUS_ID = es.ID
        INNER JOIN EX_RCONFIG ec
          ON ec.ID = es.EXCONFIG_ID
        LEFT JOIN EX_RCONFIGS_TAGS ect
          ON ect.EXC_RCONFIG_ID = ec.ID
        LEFT JOIN EX_DELIVERY ed
          ON ec.EX_DELIVERY_ID = ed.ID
        LEFT JOIN EX_DELIVERIES_EMAIL_USERS edeu
          ON edeu.EX_DELIVERY_ID = ed.ID
        LEFT JOIN EX_DELIVERIES_RPT_FORMATS edrf
          ON edrf.EX_DELIVERY_ID = ed.ID
        LEFT JOIN EX_DELIVERIES_SHARED_WITHS edsw
          ON edsw.EX_DELIVERY_ID = ed.ID
        LEFT JOIN SHARED_WITH sw
          ON sw.EX_RCONFIG_ID = ec.ID
        INNER JOIN EX_TEMPLT_QUERY etq
          ON etq.EX_RCONFIG_ID = ec.ID
        LEFT JOIN EX_TEMPLT_QRS_EX_QUERY_VALUES etqeqv
          ON etqeqv.EX_TEMPLT_QUERY_ID = etq.ID
        LEFT JOIN EX_TEMPLT_QRS_EX_TEMPLT_VALUES etqetv
          ON etqetv.EX_TEMPLT_QUERY_ID = etq.ID
        INNER JOIN EX_DATE_RANGE edr
          ON etq.EX_DATE_RANGE_INFO_ID = edr.ID
        LEFT JOIN EX_QUERY_VALUE eqv
          ON etqeqv.EX_QUERY_VALUE_ID = eqv.ID
        LEFT JOIN QUERY_VALUE qv
          ON qv.ID = eqv.ID
        LEFT JOIN EX_TEMPLT_VALUE etv
          ON etqetv.EX_TEMPLT_VALUE_ID = etv.ID
        LEFT JOIN TEMPLT_VALUE tv
          ON tv.ID = etv.ID
             AND tv.RPT_TEMPLT_ID = etq.EX_TEMPLT_ID
        LEFT JOIN VALUE v
          ON v.ID = tv.ID
             OR v.ID = qv.ID
        LEFT JOIN VALUES_PARAMS vp
          ON vp.VALUE_ID = v.ID
      WHERE es.EX_STATUS <> 'ERROR'
            AND vp.VALUE_ID = vp2.VALUE_ID
  )
);

DELETE FROM VALUES_PARAMS vp
WHERE EXISTS(
    SELECT t.*
    FROM TEMP_IDS t
    WHERE vp.PARAM_ID = t.ID
);

DELETE FROM EX_QUERY_EXP eqe
WHERE EXISTS(
    SELECT t.*
    FROM TEMP_IDS t
    WHERE eqe.ID = t.ID
);

DELETE FROM QUERY_EXP_VALUE qev
WHERE EXISTS(
    SELECT t.*
    FROM TEMP_IDS t
    WHERE qev.ID = t.ID
);

DELETE FROM EX_SQL_VALUE esv
WHERE EXISTS(
    SELECT t.*
    FROM TEMP_IDS t
    WHERE esv.ID = t.ID
);

DELETE FROM SQL_VALUE sv
WHERE EXISTS(
    SELECT t.*
    FROM TEMP_IDS t
    WHERE sv.ID = t.ID
);

DELETE FROM SQL_TEMPLT_VALUE stv
WHERE EXISTS(
    SELECT t.*
    FROM TEMP_IDS t
    WHERE stv.ID = t.ID
);

DELETE FROM PARAM p
WHERE EXISTS(
    SELECT t.*
    FROM TEMP_IDS t
    WHERE p.ID = t.ID
);

DELETE FROM TEMP_IDS;

INSERT INTO TEMP_IDS (ID) (
  SELECT v2.ID
  FROM VALUE v2
  WHERE EXISTS(
      SELECT v.*
      FROM EX_STATUS es
        LEFT JOIN EX_STATUSES_RPT_FORMATS exsrf
          ON exsrf.EX_STATUS_ID = es.ID
        LEFT JOIN EX_STATUSES_SHARED_WITHS essw
          ON essw.EX_STATUS_ID = es.ID
        INNER JOIN EX_RCONFIG ec
          ON ec.ID = es.EXCONFIG_ID
        LEFT JOIN EX_RCONFIGS_TAGS ect
          ON ect.EXC_RCONFIG_ID = ec.ID
        LEFT JOIN EX_DELIVERY ed
          ON ec.EX_DELIVERY_ID = ed.ID
        LEFT JOIN EX_DELIVERIES_EMAIL_USERS edeu
          ON edeu.EX_DELIVERY_ID = ed.ID
        LEFT JOIN EX_DELIVERIES_RPT_FORMATS edrf
          ON edrf.EX_DELIVERY_ID = ed.ID
        LEFT JOIN EX_DELIVERIES_SHARED_WITHS edsw
          ON edsw.EX_DELIVERY_ID = ed.ID
        LEFT JOIN SHARED_WITH sw
          ON sw.EX_RCONFIG_ID = ec.ID
        INNER JOIN EX_TEMPLT_QUERY etq
          ON etq.EX_RCONFIG_ID = ec.ID
        LEFT JOIN EX_TEMPLT_QRS_EX_QUERY_VALUES etqeqv
          ON etqeqv.EX_TEMPLT_QUERY_ID = etq.ID
        LEFT JOIN EX_TEMPLT_QRS_EX_TEMPLT_VALUES etqetv
          ON etqetv.EX_TEMPLT_QUERY_ID = etq.ID
        INNER JOIN EX_DATE_RANGE edr
          ON etq.EX_DATE_RANGE_INFO_ID = edr.ID
        LEFT JOIN EX_QUERY_VALUE eqv
          ON etqeqv.EX_QUERY_VALUE_ID = eqv.ID
        LEFT JOIN QUERY_VALUE qv
          ON qv.ID = eqv.ID
        LEFT JOIN EX_TEMPLT_VALUE etv
          ON etqetv.EX_TEMPLT_VALUE_ID = etv.ID
        LEFT JOIN TEMPLT_VALUE tv
          ON tv.ID = etv.ID
             AND tv.RPT_TEMPLT_ID = etq.EX_TEMPLT_ID
        LEFT JOIN VALUE v
          ON v.ID = tv.ID
             OR v.ID = qv.ID
      WHERE es.EX_STATUS <> 'ERROR'
            AND v.ID = v2.ID
  )
);

DELETE FROM EX_TEMPLT_QRS_EX_TEMPLT_VALUES etqetv
WHERE EXISTS(
    SELECT t.*
    FROM TEMP_IDS t
    WHERE etqetv.EX_TEMPLT_VALUE_ID = t.ID
);

DELETE FROM EX_TEMPLT_VALUE etv
WHERE EXISTS(
    SELECT t.*
    FROM TEMP_IDS t
    WHERE etv.ID = t.ID
);

DELETE FROM TEMPLT_VALUE tv
WHERE EXISTS(
    SELECT t.*
    FROM TEMP_IDS t
    WHERE tv.ID = t.ID
);

DELETE FROM EX_TEMPLT_QRS_EX_QUERY_VALUES etqeqv
WHERE EXISTS(
    SELECT t.*
    FROM TEMP_IDS t
    WHERE etqeqv.EX_QUERY_VALUE_ID = t.ID
);

DELETE FROM EX_QUERY_VALUE eqv
WHERE EXISTS(
    SELECT t.*
    FROM TEMP_IDS t
    WHERE eqv.ID = t.ID
);

DELETE FROM QUERY_VALUE qv
WHERE EXISTS(
    SELECT t.*
    FROM TEMP_IDS t
    WHERE qv.ID = t.ID
);

DELETE FROM VALUE v
WHERE EXISTS(
    SELECT t.*
    FROM TEMP_IDS t
    WHERE v.ID = t.ID
);

DELETE FROM TEMP_IDS;

INSERT INTO TEMP_IDS (ID) (
  SELECT etq2.EX_DATE_RANGE_INFO_ID
  FROM EX_TEMPLT_QUERY etq2
  WHERE EXISTS(
      SELECT etq.*
      FROM EX_STATUS es
        LEFT JOIN EX_STATUSES_RPT_FORMATS exsrf
          ON exsrf.EX_STATUS_ID = es.ID
        LEFT JOIN EX_STATUSES_SHARED_WITHS essw
          ON essw.EX_STATUS_ID = es.ID
        INNER JOIN EX_RCONFIG ec
          ON ec.ID = es.EXCONFIG_ID
        LEFT JOIN EX_RCONFIGS_TAGS ect
          ON ect.EXC_RCONFIG_ID = ec.ID
        LEFT JOIN EX_DELIVERY ed
          ON ec.EX_DELIVERY_ID = ed.ID
        LEFT JOIN EX_DELIVERIES_EMAIL_USERS edeu
          ON edeu.EX_DELIVERY_ID = ed.ID
        LEFT JOIN EX_DELIVERIES_RPT_FORMATS edrf
          ON edrf.EX_DELIVERY_ID = ed.ID
        LEFT JOIN EX_DELIVERIES_SHARED_WITHS edsw
          ON edsw.EX_DELIVERY_ID = ed.ID
        LEFT JOIN SHARED_WITH sw
          ON sw.EX_RCONFIG_ID = ec.ID
        INNER JOIN EX_TEMPLT_QUERY etq
          ON etq.EX_RCONFIG_ID = ec.ID
      WHERE es.EX_STATUS <> 'ERROR'
            AND etq.ID = etq2.ID
  )
);

DELETE
FROM EX_TEMPLT_QUERY etq2
WHERE EXISTS(
    SELECT etq.*
    FROM EX_STATUS es
      LEFT JOIN EX_STATUSES_RPT_FORMATS exsrf
        ON exsrf.EX_STATUS_ID = es.ID
      LEFT JOIN EX_STATUSES_SHARED_WITHS essw
        ON essw.EX_STATUS_ID = es.ID
      INNER JOIN EX_RCONFIG ec
        ON ec.ID = es.EXCONFIG_ID
      LEFT JOIN EX_RCONFIGS_TAGS ect
        ON ect.EXC_RCONFIG_ID = ec.ID
      LEFT JOIN EX_DELIVERY ed
        ON ec.EX_DELIVERY_ID = ed.ID
      LEFT JOIN EX_DELIVERIES_EMAIL_USERS edeu
        ON edeu.EX_DELIVERY_ID = ed.ID
      LEFT JOIN EX_DELIVERIES_RPT_FORMATS edrf
        ON edrf.EX_DELIVERY_ID = ed.ID
      LEFT JOIN EX_DELIVERIES_SHARED_WITHS edsw
        ON edsw.EX_DELIVERY_ID = ed.ID
      LEFT JOIN SHARED_WITH sw
        ON sw.EX_RCONFIG_ID = ec.ID
      RIGHT JOIN EX_TEMPLT_QUERY etq
        ON etq.EX_RCONFIG_ID = ec.ID
    WHERE (es.EX_STATUS <> 'ERROR'
           AND etq.ID = etq2.ID)
          OR (es.EXCONFIG_ID IS NULL)
);

DELETE FROM EX_DATE_RANGE edr
WHERE EXISTS(
    SELECT t.*
    FROM TEMP_IDS t
    WHERE edr.ID = t.ID
);

DELETE FROM TEMP_IDS;

DELETE
FROM SHARED_WITH sw2
WHERE EXISTS(
    SELECT sw.*
    FROM EX_STATUS es
      LEFT JOIN EX_STATUSES_RPT_FORMATS exsrf
        ON exsrf.EX_STATUS_ID = es.ID
      LEFT JOIN EX_STATUSES_SHARED_WITHS essw
        ON essw.EX_STATUS_ID = es.ID
      INNER JOIN EX_RCONFIG ec
        ON ec.ID = es.EXCONFIG_ID
      LEFT JOIN EX_RCONFIGS_TAGS ect
        ON ect.EXC_RCONFIG_ID = ec.ID
      LEFT JOIN EX_DELIVERY ed
        ON ec.EX_DELIVERY_ID = ed.ID
      LEFT JOIN EX_DELIVERIES_EMAIL_USERS edeu
        ON edeu.EX_DELIVERY_ID = ed.ID
      LEFT JOIN EX_DELIVERIES_RPT_FORMATS edrf
        ON edrf.EX_DELIVERY_ID = ed.ID
      LEFT JOIN EX_DELIVERIES_SHARED_WITHS edsw
        ON edsw.EX_DELIVERY_ID = ed.ID
      LEFT JOIN SHARED_WITH sw
        ON sw.EX_RCONFIG_ID = ec.ID
    WHERE es.EX_STATUS <> 'ERROR'
          AND sw.ID = sw2.ID
);

DELETE
FROM EX_DELIVERIES_SHARED_WITHS edsw2
WHERE EXISTS(
    SELECT edsw.*
    FROM EX_STATUS es
      LEFT JOIN EX_STATUSES_RPT_FORMATS exsrf
        ON exsrf.EX_STATUS_ID = es.ID
      LEFT JOIN EX_STATUSES_SHARED_WITHS essw
        ON essw.EX_STATUS_ID = es.ID
      INNER JOIN EX_RCONFIG ec
        ON ec.ID = es.EXCONFIG_ID
      LEFT JOIN EX_RCONFIGS_TAGS ect
        ON ect.EXC_RCONFIG_ID = ec.ID
      LEFT JOIN EX_DELIVERY ed
        ON ec.EX_DELIVERY_ID = ed.ID
      LEFT JOIN EX_DELIVERIES_EMAIL_USERS edeu
        ON edeu.EX_DELIVERY_ID = ed.ID
      LEFT JOIN EX_DELIVERIES_RPT_FORMATS edrf
        ON edrf.EX_DELIVERY_ID = ed.ID
      LEFT JOIN EX_DELIVERIES_SHARED_WITHS edsw
        ON edsw.EX_DELIVERY_ID = ed.ID
    WHERE es.EX_STATUS <> 'ERROR'
          AND edsw.EX_DELIVERY_ID = edsw2.EX_DELIVERY_ID
);

DELETE
FROM EX_DELIVERIES_RPT_FORMATS edrf2
WHERE EXISTS(
    SELECT edrf.*
    FROM EX_STATUS es
      LEFT JOIN EX_STATUSES_RPT_FORMATS exsrf
        ON exsrf.EX_STATUS_ID = es.ID
      LEFT JOIN EX_STATUSES_SHARED_WITHS essw
        ON essw.EX_STATUS_ID = es.ID
      INNER JOIN EX_RCONFIG ec
        ON ec.ID = es.EXCONFIG_ID
      LEFT JOIN EX_RCONFIGS_TAGS ect
        ON ect.EXC_RCONFIG_ID = ec.ID
      LEFT JOIN EX_DELIVERY ed
        ON ec.EX_DELIVERY_ID = ed.ID
      LEFT JOIN EX_DELIVERIES_EMAIL_USERS edeu
        ON edeu.EX_DELIVERY_ID = ed.ID
      LEFT JOIN EX_DELIVERIES_RPT_FORMATS edrf
        ON edrf.EX_DELIVERY_ID = ed.ID
    WHERE es.EX_STATUS <> 'ERROR'
          AND edrf.EX_DELIVERY_ID = edrf2.EX_DELIVERY_ID
);

DELETE
FROM EX_DELIVERIES_EMAIL_USERS edeu2
WHERE EXISTS(
    SELECT edeu.*
    FROM EX_STATUS es
      LEFT JOIN EX_STATUSES_RPT_FORMATS exsrf
        ON exsrf.EX_STATUS_ID = es.ID
      LEFT JOIN EX_STATUSES_SHARED_WITHS essw
        ON essw.EX_STATUS_ID = es.ID
      INNER JOIN EX_RCONFIG ec
        ON ec.ID = es.EXCONFIG_ID
      LEFT JOIN EX_RCONFIGS_TAGS ect
        ON ect.EXC_RCONFIG_ID = ec.ID
      LEFT JOIN EX_DELIVERY ed
        ON ec.EX_DELIVERY_ID = ed.ID
      LEFT JOIN EX_DELIVERIES_EMAIL_USERS edeu
        ON edeu.EX_DELIVERY_ID = ed.ID
    WHERE es.EX_STATUS <> 'ERROR'
          AND edeu.EX_DELIVERY_ID = edeu2.EX_DELIVERY_ID
);

INSERT INTO TEMP_IDS (ID) (
  SELECT ed2.ID
  FROM EX_DELIVERY ed2
  WHERE EXISTS(
      SELECT ed.*
      FROM EX_STATUS es
        LEFT JOIN EX_STATUSES_RPT_FORMATS exsrf
          ON exsrf.EX_STATUS_ID = es.ID
        LEFT JOIN EX_STATUSES_SHARED_WITHS essw
          ON essw.EX_STATUS_ID = es.ID
        INNER JOIN EX_RCONFIG ec
          ON ec.ID = es.EXCONFIG_ID
        LEFT JOIN EX_RCONFIGS_TAGS ect
          ON ect.EXC_RCONFIG_ID = ec.ID
        LEFT JOIN EX_DELIVERY ed
          ON ec.EX_DELIVERY_ID = ed.ID
      WHERE es.EX_STATUS <> 'ERROR'
            AND ed.ID = ed2.ID
  )
);

DELETE
FROM EX_RCONFIGS_TAGS ect2
WHERE EXISTS(
    SELECT ect.*
    FROM EX_STATUS es
      LEFT JOIN EX_STATUSES_RPT_FORMATS exsrf
        ON exsrf.EX_STATUS_ID = es.ID
      LEFT JOIN EX_STATUSES_SHARED_WITHS essw
        ON essw.EX_STATUS_ID = es.ID
      INNER JOIN EX_RCONFIG ec
        ON ec.ID = es.EXCONFIG_ID
      LEFT JOIN EX_RCONFIGS_TAGS ect
        ON ect.EXC_RCONFIG_ID = ec.ID
    WHERE es.EX_STATUS <> 'ERROR'
          AND ect.EXC_RCONFIG_ID = ect2.EXC_RCONFIG_ID
);

DELETE
FROM EX_RCONFIG ec2
WHERE EXISTS(
    SELECT ec.*
    FROM EX_STATUS es
      LEFT JOIN EX_STATUSES_RPT_FORMATS exsrf
        ON exsrf.EX_STATUS_ID = es.ID
      LEFT JOIN EX_STATUSES_SHARED_WITHS essw
        ON essw.EX_STATUS_ID = es.ID
      INNER JOIN EX_RCONFIG ec
        ON ec.ID = es.EXCONFIG_ID
    WHERE es.EX_STATUS <> 'ERROR'
          AND ec.ID = ec2.ID
);

DELETE FROM EX_DELIVERY ed
WHERE EXISTS(
    SELECT t.*
    FROM TEMP_IDS t
    WHERE ed.ID = t.ID
);

DELETE
FROM EX_STATUSES_SHARED_WITHS essw2
WHERE EXISTS(
    SELECT essw.*
    FROM EX_STATUS es
      LEFT JOIN EX_STATUSES_RPT_FORMATS exsrf
        ON exsrf.EX_STATUS_ID = es.ID
      LEFT JOIN EX_STATUSES_SHARED_WITHS essw
        ON essw.EX_STATUS_ID = es.ID
    WHERE es.EX_STATUS <> 'ERROR'
          AND essw.EX_STATUS_ID = essw2.EX_STATUS_ID
);

DELETE
FROM EX_STATUSES_RPT_FORMATS exsrf2
WHERE EXISTS(
    SELECT exsrf.*
    FROM EX_STATUS es
      LEFT JOIN EX_STATUSES_RPT_FORMATS exsrf
        ON exsrf.EX_STATUS_ID = es.ID
    WHERE es.EX_STATUS <> 'ERROR'
          AND exsrf.EX_STATUS_ID = exsrf2.EX_STATUS_ID
);

DELETE
FROM EX_STATUS es2
WHERE EXISTS(
    SELECT es.*
    FROM EX_STATUS es
    WHERE es.EX_STATUS <> 'ERROR'
          AND es.ID = es2.ID
);

TRUNCATE TABLE TEMP_IDS;

-- Executed Queries

-- Query Set

DELETE
FROM QUERY_SETS_SUPER_QRS qssq2
WHERE EXISTS(
    SELECT qssq.*
    FROM SUPER_QUERY sq
      LEFT JOIN SUPER_QRS_TAGS sqt
        ON sq.ID = sqt.SUPER_QUERY_ID
      INNER JOIN QUERY_SET qs
        ON sq.ID = qs.ID
      INNER JOIN EX_QUERY_SET eqs
        ON qs.ID = eqs.ID
      LEFT JOIN QUERY_SETS_SUPER_QRS qssq
        ON qssq.QUERY_SET_ID = eqs.ID
    WHERE sq.ORIG_QUERY_ID > 0
          AND sq.QUERY_TYPE = 'SET_BUILDER'
          AND NOT EXISTS(
        SELECT es.*
        FROM EX_STATUS es
        WHERE es.EX_STATUS = 'ERROR'
              AND es.QUERY_ID = sq.ID
    )
          AND qssq.QUERY_SET_ID = qssq2.QUERY_SET_ID
);

DELETE
FROM EX_QUERY_SET eqs2
WHERE EXISTS(
    SELECT eqs.*
    FROM SUPER_QUERY sq
      LEFT JOIN SUPER_QRS_TAGS sqt
        ON sq.ID = sqt.SUPER_QUERY_ID
      INNER JOIN QUERY_SET qs
        ON sq.ID = qs.ID
      INNER JOIN EX_QUERY_SET eqs
        ON qs.ID = eqs.ID
    WHERE sq.ORIG_QUERY_ID > 0
          AND sq.QUERY_TYPE = 'SET_BUILDER'
          AND NOT EXISTS(
        SELECT es.*
        FROM EX_STATUS es
        WHERE es.EX_STATUS = 'ERROR'
              AND es.QUERY_ID = sq.ID
    )
          AND eqs.ID = eqs2.ID
);

DELETE
FROM QUERY_SET qs2
WHERE EXISTS(
    SELECT qs.*
    FROM SUPER_QUERY sq
      LEFT JOIN SUPER_QRS_TAGS sqt
        ON sq.ID = sqt.SUPER_QUERY_ID
      INNER JOIN QUERY_SET qs
        ON sq.ID = qs.ID
    WHERE sq.ORIG_QUERY_ID > 0
          AND sq.QUERY_TYPE = 'SET_BUILDER'
          AND NOT EXISTS(
        SELECT es.*
        FROM EX_STATUS es
        WHERE es.EX_STATUS = 'ERROR'
              AND es.QUERY_ID = sq.ID
    )
          AND qs.ID = qs2.ID
);

DELETE
FROM SUPER_QRS_TAGS sqt2
WHERE EXISTS(
    SELECT sqt.*
    FROM SUPER_QUERY sq
      LEFT JOIN SUPER_QRS_TAGS sqt
        ON sq.ID = sqt.SUPER_QUERY_ID
    WHERE sq.ORIG_QUERY_ID > 0
          AND sq.QUERY_TYPE = 'SET_BUILDER'
          AND NOT EXISTS(
        SELECT es.*
        FROM EX_STATUS es
        WHERE es.EX_STATUS = 'ERROR'
              AND es.QUERY_ID = sq.ID
    )
          AND sqt.SUPER_QUERY_ID = sqt2.SUPER_QUERY_ID
);

DELETE
FROM SUPER_QUERY sq2
WHERE EXISTS(
    SELECT sq.*
    FROM SUPER_QUERY sq
    WHERE sq.ORIG_QUERY_ID > 0
          AND sq.QUERY_TYPE = 'SET_BUILDER'
          AND NOT EXISTS(
        SELECT es.*
        FROM EX_STATUS es
        WHERE es.EX_STATUS = 'ERROR'
              AND es.QUERY_ID = sq.ID
    )
          AND sq.ID = sq2.ID
);

-- Query

TRUNCATE TABLE TEMP_IDS;

INSERT INTO TEMP_IDS (ID) (SELECT qqev2.QUERY_EXP_VALUE_ID
                           FROM QUERIES_QRS_EXP_VALUES qqev2
                           WHERE EXISTS(
                               SELECT qqev.*
                               FROM SUPER_QUERY sq
                                 LEFT JOIN SUPER_QRS_TAGS sqt
                                   ON sq.ID = sqt.SUPER_QUERY_ID
                                 INNER JOIN QUERY q
                                   ON sq.ID = q.ID
                                 INNER JOIN EX_QUERY eq
                                   ON q.ID = eq.ID
                                 LEFT JOIN QUERIES_QRS_EXP_VALUES qqev
                                   ON qqev.QUERY_ID = eq.ID
                               WHERE sq.ORIG_QUERY_ID > 0
                                     AND sq.QUERY_TYPE = 'QUERY_BUILDER'
                                     AND NOT EXISTS(
                                   SELECT es.*
                                   FROM EX_STATUS es
                                   WHERE es.EX_STATUS = 'ERROR'
                                         AND es.QUERY_ID = sq.ID
                               )
                                     AND qqev.QUERY_ID = qqev2.QUERY_ID
                           )
);

DELETE FROM QUERIES_QRS_EXP_VALUES qqev
WHERE EXISTS(
    SELECT t.*
    FROM TEMP_IDS t
    WHERE qqev.QUERY_EXP_VALUE_ID = t.ID
);

DELETE FROM EX_QUERY_EXP eqev
WHERE EXISTS(
    SELECT t.*
    FROM TEMP_IDS t
    WHERE eqev.ID = t.ID
);

DELETE FROM QUERY_EXP_VALUE qev
WHERE EXISTS(
    SELECT t.*
    FROM TEMP_IDS t
    WHERE qev.ID = t.ID
);

DELETE FROM PARAM p
WHERE EXISTS(
    SELECT t.*
    FROM TEMP_IDS t
    WHERE p.ID = t.ID
);

DELETE
FROM EX_QUERY eq2
WHERE EXISTS(
    SELECT eq.*
    FROM SUPER_QUERY sq
      LEFT JOIN SUPER_QRS_TAGS sqt
        ON sq.ID = sqt.SUPER_QUERY_ID
      INNER JOIN QUERY q
        ON sq.ID = q.ID
      INNER JOIN EX_QUERY eq
        ON q.ID = eq.ID
    WHERE sq.ORIG_QUERY_ID > 0
          AND sq.QUERY_TYPE = 'QUERY_BUILDER'
          AND NOT EXISTS(
        SELECT es.*
        FROM EX_STATUS es
        WHERE es.EX_STATUS = 'ERROR'
              AND es.QUERY_ID = sq.ID
    )
          AND eq.ID = eq2.ID
);

DELETE
FROM QUERY q2
WHERE EXISTS(
    SELECT q.*
    FROM SUPER_QUERY sq
      LEFT JOIN SUPER_QRS_TAGS sqt
        ON sq.ID = sqt.SUPER_QUERY_ID
      INNER JOIN QUERY q
        ON sq.ID = q.ID
    WHERE sq.ORIG_QUERY_ID > 0
          AND sq.QUERY_TYPE = 'QUERY_BUILDER'
          AND NOT EXISTS(
        SELECT es.*
        FROM EX_STATUS es
        WHERE es.EX_STATUS = 'ERROR'
              AND es.QUERY_ID = sq.ID
    )
          AND q.ID = q2.ID
);

DELETE
FROM SUPER_QRS_TAGS sqt2
WHERE EXISTS(
    SELECT sqt.*
    FROM SUPER_QUERY sq
      LEFT JOIN SUPER_QRS_TAGS sqt
        ON sq.ID = sqt.SUPER_QUERY_ID
    WHERE sq.ORIG_QUERY_ID > 0
          AND sq.QUERY_TYPE = 'QUERY_BUILDER'
          AND NOT EXISTS(
        SELECT es.*
        FROM EX_STATUS es
        WHERE es.EX_STATUS = 'ERROR'
              AND es.QUERY_ID = sq.ID
    )
          AND sq.ID = sqt2.SUPER_QUERY_ID
);

DELETE
FROM SUPER_QUERY sq2
WHERE EXISTS(
    SELECT sq.*
    FROM SUPER_QUERY sq
    WHERE sq.ORIG_QUERY_ID > 0
          AND sq.QUERY_TYPE = 'QUERY_BUILDER'
          AND NOT EXISTS(
        SELECT es.*
        FROM EX_STATUS es
        WHERE es.EX_STATUS = 'ERROR'
              AND es.QUERY_ID = sq.ID
    )
          AND sq.ID = sq2.ID
);

-- Custom SQL

TRUNCATE TABLE TEMP_IDS;

INSERT INTO TEMP_IDS (ID) (SELECT sqsv2.SQL_VALUE_ID
                           FROM SQL_QRS_SQL_VALUES sqsv2
                           WHERE EXISTS(
                               SELECT sqsv.*
                               FROM SUPER_QUERY sq
                                 LEFT JOIN SUPER_QRS_TAGS sqt
                                   ON sq.ID = sqt.SUPER_QUERY_ID
                                 INNER JOIN SQL_QUERY q
                                   ON sq.ID = q.ID
                                 INNER JOIN EX_CUSTOM_SQL_QUERY ecsq
                                   ON q.ID = ecsq.ID
                                 LEFT JOIN SQL_QRS_SQL_VALUES sqsv
                                   ON sqsv.SQL_QUERY_ID = ecsq.ID
                               WHERE sq.ORIG_QUERY_ID > 0
                                     AND sq.QUERY_TYPE = 'CUSTOM_SQL'
                                     AND NOT EXISTS(
                                   SELECT es.*
                                   FROM EX_STATUS es
                                   WHERE es.EX_STATUS = 'ERROR'
                                         AND es.QUERY_ID = sq.ID
                               )
                                     AND sqsv.SQL_QUERY_ID = sqsv2.SQL_QUERY_ID
                           )
);

DELETE FROM SQL_QRS_SQL_VALUES sqsv
WHERE EXISTS(
    SELECT t.*
    FROM TEMP_IDS t
    WHERE sqsv.SQL_VALUE_ID = t.ID
);

DELETE FROM EX_SQL_VALUE esv
WHERE EXISTS(
    SELECT t.*
    FROM TEMP_IDS t
    WHERE esv.ID = t.ID
);

DELETE FROM SQL_VALUE sv
WHERE EXISTS(
    SELECT t.*
    FROM TEMP_IDS t
    WHERE sv.ID = t.ID
);

DELETE FROM PARAM p
WHERE EXISTS(
    SELECT t.*
    FROM TEMP_IDS t
    WHERE p.ID = t.ID
);

DELETE
FROM EX_CUSTOM_SQL_QUERY ecsq2
WHERE EXISTS(
    SELECT ecsq.*
    FROM SUPER_QUERY sq
      LEFT JOIN SUPER_QRS_TAGS sqt
        ON sq.ID = sqt.SUPER_QUERY_ID
      INNER JOIN SQL_QUERY q
        ON sq.ID = q.ID
      INNER JOIN EX_CUSTOM_SQL_QUERY ecsq
        ON q.ID = ecsq.ID
    WHERE sq.ORIG_QUERY_ID > 0
          AND sq.QUERY_TYPE = 'CUSTOM_SQL'
          AND NOT EXISTS(
        SELECT es.*
        FROM EX_STATUS es
        WHERE es.EX_STATUS = 'ERROR'
              AND es.QUERY_ID = sq.ID
    )
          AND ecsq.ID = ecsq2.ID
);

DELETE
FROM SQL_QUERY q2
WHERE EXISTS(
    SELECT q.*
    FROM SUPER_QUERY sq
      LEFT JOIN SUPER_QRS_TAGS sqt
        ON sq.ID = sqt.SUPER_QUERY_ID
      INNER JOIN SQL_QUERY q
        ON sq.ID = q.ID
    WHERE sq.ORIG_QUERY_ID > 0
          AND sq.QUERY_TYPE = 'CUSTOM_SQL'
          AND NOT EXISTS(
        SELECT es.*
        FROM EX_STATUS es
        WHERE es.EX_STATUS = 'ERROR'
              AND es.QUERY_ID = sq.ID
    )
          AND q.ID = q2.ID
);

DELETE
FROM SUPER_QRS_TAGS sqt2
WHERE EXISTS(
    SELECT sqt.*
    FROM SUPER_QUERY sq
      LEFT JOIN SUPER_QRS_TAGS sqt
        ON sq.ID = sqt.SUPER_QUERY_ID
    WHERE sq.ORIG_QUERY_ID > 0
          AND sq.QUERY_TYPE = 'CUSTOM_SQL'
          AND NOT EXISTS(
        SELECT es.*
        FROM EX_STATUS es
        WHERE es.EX_STATUS = 'ERROR'
              AND es.QUERY_ID = sq.ID
    )
          AND sqt.SUPER_QUERY_ID = sqt2.SUPER_QUERY_ID
);

DELETE
FROM SUPER_QUERY sq2
WHERE EXISTS(
    SELECT sq.*
    FROM SUPER_QUERY sq
    WHERE sq.ORIG_QUERY_ID > 0
          AND sq.QUERY_TYPE = 'CUSTOM_SQL'
          AND NOT EXISTS(
        SELECT es.*
        FROM EX_STATUS es
        WHERE es.EX_STATUS = 'ERROR'
              AND es.QUERY_ID = sq.ID
    )
          AND sq.ID = sq2.ID
);

-- Executed Templates
-- NOTE: This is deleting improper Executed Template data. Deletion for templates in the future should NOT use only the methods below.

-- Template Set

DELETE
FROM EX_TEMPLT_SET ets2
WHERE EXISTS(
    SELECT ets.*
    FROM RPT_TEMPLT rt
      LEFT JOIN RPT_TEMPLTS_TAGS rtt
        ON rt.ID = rtt.RPT_TEMPLT_ID
      INNER JOIN TEMPLT_SET ts
        ON rt.ID = ts.ID
      INNER JOIN EX_TEMPLT_SET ets
        ON ts.ID = ets.ID
    WHERE rt.ORIG_TEMPLT_ID > 0
          AND rt.TEMPLATE_TYPE = 'TEMPLATE_SET'
          AND NOT EXISTS(
        SELECT es.*
        FROM EX_STATUS es
        WHERE es.EX_STATUS = 'ERROR'
              AND es.TEMPLATE_ID = rt.ID
    )
          AND ets.ID = ets2.ID
);

-- CLL

DELETE
FROM EX_CLL_TEMPLT ect2
WHERE EXISTS(
    SELECT ect.*
    FROM RPT_TEMPLT rt
      LEFT JOIN RPT_TEMPLTS_TAGS rtt
        ON rt.ID = rtt.RPT_TEMPLT_ID
      INNER JOIN CLL_TEMPLT ct
        ON rt.ID = ct.ID
      INNER JOIN EX_CLL_TEMPLT ect
        ON ct.ID = ect.ID
    WHERE rt.ORIG_TEMPLT_ID > 0
          AND rt.TEMPLATE_TYPE = 'CASE_LINE'
          AND NOT EXISTS(
        SELECT es.*
        FROM EX_STATUS es
        WHERE es.EX_STATUS = 'ERROR'
              AND es.TEMPLATE_ID = rt.ID
    )
          AND ect.ID = ect2.ID
);

-- Custom SQL

DELETE
FROM EX_CUSTOM_SQL_TEMPLT ecst2
WHERE EXISTS(
    SELECT ecst.*
    FROM RPT_TEMPLT rt
      LEFT JOIN RPT_TEMPLTS_TAGS rtt
        ON rt.ID = rtt.RPT_TEMPLT_ID
      INNER JOIN SQL_TEMPLT st
        ON rt.ID = st.ID
      INNER JOIN EX_CUSTOM_SQL_TEMPLT ecst
        ON st.ID = ecst.ID
    WHERE rt.ORIG_TEMPLT_ID > 0
          AND rt.TEMPLATE_TYPE = 'CUSTOM_SQL'
          AND NOT EXISTS(
        SELECT es.*
        FROM EX_STATUS es
        WHERE es.EX_STATUS = 'ERROR'
              AND es.TEMPLATE_ID = rt.ID
    )
          AND ecst.ID = ecst2.ID
);

-- Non-case

DELETE
FROM EX_NCASE_SQL_TEMPLT enst2
WHERE EXISTS(
    SELECT enst.*
    FROM RPT_TEMPLT rt
      LEFT JOIN RPT_TEMPLTS_TAGS rtt
        ON rt.ID = rtt.RPT_TEMPLT_ID
      INNER JOIN NONCASE_SQL_TEMPLT nst
        ON rt.ID = nst.ID
      INNER JOIN EX_NCASE_SQL_TEMPLT enst
        ON nst.ID = enst.ID
    WHERE rt.ORIG_TEMPLT_ID > 0
          AND rt.TEMPLATE_TYPE = 'NON_CASE'
          AND NOT EXISTS(
        SELECT es.*
        FROM EX_STATUS es
        WHERE es.EX_STATUS = 'ERROR'
              AND es.TEMPLATE_ID = rt.ID
    )
          AND enst.ID = enst2.ID
);

-- Data Tabulation

DELETE
FROM EX_DTAB_TEMPLT edt2
WHERE EXISTS(
    SELECT edt.*
    FROM RPT_TEMPLT rt
      LEFT JOIN RPT_TEMPLTS_TAGS rtt
        ON rt.ID = rtt.RPT_TEMPLT_ID
      INNER JOIN DTAB_TEMPLT dt
        ON rt.ID = dt.ID
      INNER JOIN EX_DTAB_TEMPLT edt
        ON dt.ID = edt.ID
    WHERE rt.ORIG_TEMPLT_ID > 0
          AND rt.TEMPLATE_TYPE = 'DATA_TAB'
          AND NOT EXISTS(
        SELECT es.*
        FROM EX_STATUS es
        WHERE es.EX_STATUS = 'ERROR'
              AND es.TEMPLATE_ID = rt.ID
    )
          AND edt.ID = edt2.ID
);

TRUNCATE TABLE TEMP_IDS;

DROP TABLE TEMP_IDS;