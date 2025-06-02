UPDATE SQL_TEMPLT SET SELECT_FROM_STMT = ('SELECT cci.patient_initials_1,
cci.country_1a,
cci.pat_dob_day_2,
cci.pat_dob_month_2,
cci.pat_dob_year_2,
cci.pat_age_years_2a,
cci.pat_gender_3,
pat_weight_3a,
cci.react_onset_day_4,
cci.react_onset_month_5,
cci.react_onset_year_6,
cci.case_description_7,
cci.case_comment_7,
cci.other_ser_criteria_7,
cci.FLAG_CONGENITAL_AN_14,
cci.FLAG_OTHER_SER_14,
cci.OTHER_SER_TXT_14,
cci.describe_reaction_7,
cci.pat_died_8,
cci.date_of_death_9,
cci.pat_hospitalised_10,
cci.pat_disability_11,
cci.life_threatening_12,
cci.rel_tests_13,
cci.lab_data_13,
cci.sus_product_name_all_14,
cci.additional_14,
cci.daily_doses_all_15,
cci.route_of_admin_all_16,
cci.indication_all_17,
cci.therapy_dates_all_18,
cci.therapy_duration_19,
cci.react_dechal_outcome_20,
cci.react_rechal_outcome_21,
cci.conmeds_products_dates_22,
cci.other_relevant_history_23,
case when ((SELECT distinct TO_NUMBER(PARAM_VALUE) FROM GTT_REPORT_INPUT_PARAMS WHERE UPPER(PARAM_KEY) = ''IS_ICSR_PROFILE'')=1 OR
            (SELECT distinct TO_NUMBER(PARAM_VALUE) FROM GTT_REPORT_INPUT_PARAMS WHERE UPPER(PARAM_KEY) = ''IS_ICSR_REPORT'')=1)
THEN
   ( nvl2((select PARAM_VALUE from GTT_REPORT_INPUT_PARAMS where PARAM_KEY = ''E2B_SENDER_COMPANY_NAME''),
         (select PARAM_VALUE from GTT_REPORT_INPUT_PARAMS where PARAM_KEY = ''E2B_SENDER_COMPANY_NAME'') || CHR(10),
         nvl2((select PARAM_VALUE from GTT_REPORT_INPUT_PARAMS where PARAM_KEY = ''E2B_SENDER_ORG_NAME''),
         (select PARAM_VALUE from GTT_REPORT_INPUT_PARAMS where PARAM_KEY = ''E2B_SENDER_ORG_NAME'') || CHR(10), NULL)) )
||    ( nvl2((select PARAM_VALUE from GTT_REPORT_INPUT_PARAMS where PARAM_KEY = ''E2B_SENDER_TITLE''),
         (select PARAM_VALUE from GTT_REPORT_INPUT_PARAMS where PARAM_KEY = ''E2B_SENDER_TITLE'') || '' '', NULL) )
||   ( nvl2((select PARAM_VALUE from GTT_REPORT_INPUT_PARAMS where PARAM_KEY = ''E2B_SENDER_FIRST_NAME''),
         (select PARAM_VALUE from GTT_REPORT_INPUT_PARAMS where PARAM_KEY = ''E2B_SENDER_FIRST_NAME'') || '' '', NULL) )
||   ( nvl2((select PARAM_VALUE from GTT_REPORT_INPUT_PARAMS where PARAM_KEY = ''E2B_SENDER_MIDDLE_NAME''),
         (select PARAM_VALUE from GTT_REPORT_INPUT_PARAMS where PARAM_KEY = ''E2B_SENDER_MIDDLE_NAME'') || '' '', NULL) )
||   ( nvl2((select PARAM_VALUE from GTT_REPORT_INPUT_PARAMS where PARAM_KEY = ''E2B_SENDER_LAST_NAME''),
         (select PARAM_VALUE from GTT_REPORT_INPUT_PARAMS where PARAM_KEY = ''E2B_SENDER_LAST_NAME'') || CHR(10), NULL) )
|| ( nvl2((select PARAM_VALUE from GTT_REPORT_INPUT_PARAMS where PARAM_KEY = ''E2B_SENDER_ADDRESS1''),
         (select PARAM_VALUE from GTT_REPORT_INPUT_PARAMS where PARAM_KEY = ''E2B_SENDER_ADDRESS1'') || '', '', NULL) )
|| ( nvl2((select PARAM_VALUE from GTT_REPORT_INPUT_PARAMS where PARAM_KEY = ''E2B_SENDER_ADDRESS2''),
         (select PARAM_VALUE from GTT_REPORT_INPUT_PARAMS where PARAM_KEY = ''E2B_SENDER_ADDRESS2'') || CHR(10), NULL) )
|| ( nvl2((select PARAM_VALUE from GTT_REPORT_INPUT_PARAMS where PARAM_KEY = ''E2B_SENDER_CITY''),
         (select PARAM_VALUE from GTT_REPORT_INPUT_PARAMS where PARAM_KEY = ''E2B_SENDER_CITY'') || '', '', NULL) )
|| ( nvl2((select PARAM_VALUE from GTT_REPORT_INPUT_PARAMS where PARAM_KEY = ''E2B_SENDER_STATE''),
         (select PARAM_VALUE from GTT_REPORT_INPUT_PARAMS where PARAM_KEY = ''E2B_SENDER_STATE'') || '' '', NULL) )
|| ( nvl2((select PARAM_VALUE from GTT_REPORT_INPUT_PARAMS where PARAM_KEY = ''E2B_SENDER_POSTAL''),
         (select PARAM_VALUE from GTT_REPORT_INPUT_PARAMS where PARAM_KEY = ''E2B_SENDER_POSTAL'') || '' '', NULL) )
|| ( nvl2((select PARAM_VALUE from GTT_REPORT_INPUT_PARAMS where PARAM_KEY = ''E2B_SENDER_COUNTRY_CODE''),
         (select PARAM_VALUE from GTT_REPORT_INPUT_PARAMS where PARAM_KEY = ''E2B_SENDER_COUNTRY_CODE'') || CHR(10), NULL) )
|| ( nvl2((select PARAM_VALUE from GTT_REPORT_INPUT_PARAMS where PARAM_KEY = ''E2B_SENDER_PHONE''), ''Phone: ''
|| (select PARAM_VALUE from GTT_REPORT_INPUT_PARAMS where PARAM_KEY = ''E2B_SENDER_PHONE'')
|| CHR(10), NULL) )
ELSE cci.manu_name_address_24a END as manu_name_address_24a,
cci.mfr_control_no_24b,
cci.manu_date_recieved_24c,
cci.report_source_study_24d,
cci.report_source_lit_24d,
cci.report_source_hp_24d,
cci.literature_24d,
cci.report_source_other_24d,
cci.report_source_other_desc_24d,
case when ((SELECT distinct TO_NUMBER(PARAM_VALUE) FROM GTT_REPORT_INPUT_PARAMS WHERE UPPER(PARAM_KEY) = ''IS_ICSR_PROFILE'')=1 OR
            (SELECT distinct TO_NUMBER(PARAM_VALUE) FROM GTT_REPORT_INPUT_PARAMS WHERE UPPER(PARAM_KEY) = ''IS_ICSR_REPORT'')=1)
THEN
    (select decode(UPPER(pect.FOLLOWUP_INFO),''INITIAL'',1,0) from VW_E2B_MW_SUBMISSION pect
        where pect.tenant_id = cm.tenant_id AND pect.case_id = cm.case_id
        AND pect.version_num = cm.version_num AND pect.case_id = cci.case_id
        AND pect.version_num = cci.version_num AND pect.PARTNER_NAME = (select distinct param_value from gtt_report_input_params where upper(param_key) = upper(''REPORT_NAME''))
        AND pect.PARTNER_RECIPIENT_ID = (select distinct param_value from gtt_report_input_params where upper(param_key) = upper(''E2B_RECEIVER_ORG_ID''))
		AND pect.PROCESSED_REPORT_ID = (SELECT TO_NUMBER(param_value) FROM gtt_report_input_params where upper(param_key) = upper(''PROCESSED_REPORT_ID''))
    )
ELSE
    cci.report_type_init_25a
END AS report_type_init_25a,
case when ((SELECT distinct TO_NUMBER(PARAM_VALUE) FROM GTT_REPORT_INPUT_PARAMS WHERE UPPER(PARAM_KEY) = ''IS_ICSR_PROFILE'')=1 OR
            (SELECT distinct TO_NUMBER(PARAM_VALUE) FROM GTT_REPORT_INPUT_PARAMS WHERE UPPER(PARAM_KEY) = ''IS_ICSR_REPORT'')=1)
THEN
    (select decode(UPPER(pect.FOLLOWUP_INFO),''FOLLOWUP'',1,0) from VW_E2B_MW_SUBMISSION pect
        where pect.tenant_id = cm.tenant_id AND pect.case_id = cm.case_id
        AND pect.version_num = cm.version_num AND pect.case_id = cci.case_id
        AND pect.version_num = cci.version_num AND pect.PARTNER_NAME = (select distinct param_value from gtt_report_input_params where upper(param_key) = upper(''REPORT_NAME''))
        AND pect.PARTNER_RECIPIENT_ID = (select distinct param_value from gtt_report_input_params where upper(param_key) = upper(''E2B_RECEIVER_ORG_ID''))
		AND pect.PROCESSED_REPORT_ID = (SELECT TO_NUMBER(param_value) FROM gtt_report_input_params where upper(param_key) = upper(''PROCESSED_REPORT_ID''))
    )
ELSE
    cci.report_type_followup_25a
END AS report_type_followup_25a,
cci.reporter_name_address_25b,
case when ((SELECT distinct TO_NUMBER(PARAM_VALUE) FROM GTT_REPORT_INPUT_PARAMS WHERE UPPER(PARAM_KEY) = ''IS_ICSR_PROFILE'')=1 OR
            (SELECT distinct TO_NUMBER(PARAM_VALUE) FROM GTT_REPORT_INPUT_PARAMS WHERE UPPER(PARAM_KEY) = ''IS_ICSR_REPORT'')=1)
THEN
    (select case when decode(UPPER(pect.FOLLOWUP_INFO),''FOLLOWUP'',1,0)  = 1 then FOLLOWUP_NUM else null end as REPORT_TYPE_FOLLOWUP_NUM_25A from VW_E2B_MW_SUBMISSION pect
        where pect.tenant_id = cm.tenant_id AND pect.case_id = cm.case_id
        AND pect.version_num = cm.version_num AND pect.case_id = cci.case_id
        AND pect.version_num = cci.version_num AND pect.PARTNER_NAME = (select distinct param_value from gtt_report_input_params where upper(param_key) = upper(''REPORT_NAME''))
        AND pect.PARTNER_RECIPIENT_ID = (select distinct param_value from gtt_report_input_params where upper(param_key) = upper(''E2B_RECEIVER_ORG_ID''))
		AND pect.PROCESSED_REPORT_ID = (SELECT TO_NUMBER(param_value) FROM gtt_report_input_params where upper(param_key) = upper(''PROCESSED_REPORT_ID''))
    )
ELSE
    cci.REPORT_TYPE_FOLLOWUP_NUM_25A
END as REPORT_TYPE_FOLLOWUP_NUM_25A,
sysdate AS report_date,
cci.remarks_26,
cci.patient_confidentiality,
cci.version_start_date effective_start_date,
(select NVL(CONFIG_VALUE,0) from vw_admin_app_config where  APPLICATION_NAME=''PVR-DB'' and CONFIG_KEY  = ''CIOMS_PT_OR_LLT'') as cioms_pt_or_llt_switch,
(select NVL(key_value,0) from pvr_app_constants where key_id  = ''CIOMS_SER_CRITERIA_CNT'') as CIOMS_SER_CRITERIA_CNT,
cci.version_num
FROM V_SAFETY_IDENTIFICATION cm
JOIN case_cioms_i cci
ON (cm.case_id = cci.case_id
AND cm.version_num = cci.version_num)') WHERE ID = (select id from RPT_TEMPLT where name='CIOMS I Template' and cioms_i_template=1 and orig_templt_id=0 and is_deleted=0);




update sql_templt set where_stmt='and cci.flag_blinded = 
'':BLINDED_CIOMS_CHECKBOX_VALUE:'' and cci.flag_protect_privacy = 
'':PRIVACY_CIOMS_CHECKBOX_VALUE:'' 
order by CCI.MFR_CONTROL_NO_24B asc' WHERE id= (select id from RPT_TEMPLT where name='CIOMS I Template' and cioms_i_template=1 and orig_templt_id=0 and is_deleted=0);
commit;
