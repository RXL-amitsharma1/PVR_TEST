UPDATE SQL_TEMPLT SET SELECT_FROM_STMT = ('SELECT cmw.pat_identifier_1a,
cmw.pat_age_num_2a,
cmw.pat_age_num_unit_2a,
cmw.pat_dob_2a,
cmw.pat_sex_3a,
cmw.pat_weight_4a,
cmw.pat_weight_unit_4a,
cmw.pat_ethnicity_5a,
cmw.pat_race_6a,
cmw.mw_report_type_1b,
cmw.PAT_RACE_OTH_7B,
cmw.ae_outcome_2b,
cmw.death_date_2b,
cmw.ae_date_3b,
to_char(sysdate,''DD-Mon-YYYY'') AS ae_curr_date_4b,
cmw.ae_serious_criteria_1_5b,
COALESCE(cmwd.mw_device_error,dbms_lob.substr(cmw.ae_desc_reactions_2_5b,32767)) as ae_desc_reactions_2_5b,
cmw.ae_case_narrative_3_5b,
cmw.ae_reporter_comment_4_5b,
cmw.ae_rel_test_narrative_1_6b,
cmw.ae_rel_lab_tests_6b,
cmw.ae_other_rel_desc_7b,
cmw.susp_prod_name_strngth_1c,
cmw.susp_prod_ndc_1c,
cmw.susp_prod_manu_1c,
cmw.susp_prod_lot_num_1c,
cmw.list_prod_treatment_at_ae_2c,
cmw.susp_prod_dose_3c,
cmw.susp_prod_frequency_3c,
cmw.susp_prod_route_3c,
cmw.susp_prod_therapy_dates_4c,
cmw.susp_diag_for_use_5c,
cmw.susp_prod_type_6c,
cmw.susp_prod_exp_date_7c,
cmw.susp_prod_ae_abated_8c,
cmw.ae_reapp_post_reintro_1_9c,
cmw.reporter_last_name_1e,
cmw.reporter_first_name_1e,
cmw.reporter_address_1e,
cmw.reporter_city_1e,
cmw.reporter_state_1e,
cmw.reporter_zip_1e,
cmw.reporter_country_1e,
cmw.reporter_phone_1e,
cmw.reporter_email_1e,
cmw.reporter_health_prof_2e,
cmw.reporter_occupation_3e,
cmw.initial_report_to_fda_4e,
case
    when ((SELECT distinct TO_NUMBER(PARAM_VALUE) FROM GTT_REPORT_INPUT_PARAMS WHERE UPPER(PARAM_KEY) = ''IS_ICSR_PROFILE'')=1 OR
            (SELECT distinct TO_NUMBER(PARAM_VALUE) FROM GTT_REPORT_INPUT_PARAMS WHERE UPPER(PARAM_KEY) = ''IS_ICSR_REPORT'')=1)
    then
    case when cmwd.prod_rec_num is not null then
        cmwd.manufacturer_name_dr_1g
    else
       ( nvl2((select PARAM_VALUE from GTT_REPORT_INPUT_PARAMS where PARAM_KEY = ''E2B_SENDER_COMPANY_NAME''),
             (select PARAM_VALUE from GTT_REPORT_INPUT_PARAMS where PARAM_KEY = ''E2B_SENDER_COMPANY_NAME''),
             nvl2((select PARAM_VALUE from GTT_REPORT_INPUT_PARAMS where PARAM_KEY = ''E2B_SENDER_ORG_NAME''),
             (select PARAM_VALUE from GTT_REPORT_INPUT_PARAMS where PARAM_KEY = ''E2B_SENDER_ORG_NAME''), NULL)) )
         || ( nvl2((select PARAM_VALUE from GTT_REPORT_INPUT_PARAMS where PARAM_KEY = ''E2B_SENDER_TITLE''),
         '','' || CHR(10) || (select PARAM_VALUE from GTT_REPORT_INPUT_PARAMS where PARAM_KEY = ''E2B_SENDER_TITLE''), NULL) )
        || ( nvl2((select PARAM_VALUE from GTT_REPORT_INPUT_PARAMS where PARAM_KEY = ''E2B_SENDER_FIRST_NAME''),
        '' '' ||(select PARAM_VALUE from GTT_REPORT_INPUT_PARAMS where PARAM_KEY = ''E2B_SENDER_FIRST_NAME''), NULL) )
        || ( nvl2((select PARAM_VALUE from GTT_REPORT_INPUT_PARAMS where PARAM_KEY = ''E2B_SENDER_MIDDLE_NAME''),
        '' '' || (select PARAM_VALUE from GTT_REPORT_INPUT_PARAMS where PARAM_KEY = ''E2B_SENDER_MIDDLE_NAME''), NULL) )
        || ( nvl2((select PARAM_VALUE from GTT_REPORT_INPUT_PARAMS where PARAM_KEY = ''E2B_SENDER_LAST_NAME''),
        '' '' || (select PARAM_VALUE from GTT_REPORT_INPUT_PARAMS where PARAM_KEY = ''E2B_SENDER_LAST_NAME''), NULL) )
        || ( nvl2((select PARAM_VALUE from GTT_REPORT_INPUT_PARAMS where PARAM_KEY = ''E2B_SENDER_DEPARTMENT''),
        '','' || CHR(10) || (select PARAM_VALUE from GTT_REPORT_INPUT_PARAMS where PARAM_KEY = ''E2B_SENDER_DEPARTMENT''), NULL) )
    end
ELSE cmw.manu_name_1g
END AS manu_name_1g,
case
    when ((SELECT distinct TO_NUMBER(PARAM_VALUE) FROM GTT_REPORT_INPUT_PARAMS WHERE UPPER(PARAM_KEY) = ''IS_ICSR_PROFILE'')=1 OR
            (SELECT distinct TO_NUMBER(PARAM_VALUE) FROM GTT_REPORT_INPUT_PARAMS WHERE UPPER(PARAM_KEY) = ''IS_ICSR_REPORT'')=1)
    then
    case when cmwd.prod_rec_num is not null then
        cmwd.manufacturer_email_dr_1g
    else
        (SELECT DISTINCT PARAM_VALUE FROM GTT_REPORT_INPUT_PARAMS  WHERE PARAM_KEY = ''E2B_SENDER_EMAIL_ADDRESS'')
    end
ELSE cmw.manu_email_1g
END AS manu_email_1g,
case
    when ((SELECT distinct TO_NUMBER(PARAM_VALUE) FROM GTT_REPORT_INPUT_PARAMS WHERE UPPER(PARAM_KEY) = ''IS_ICSR_PROFILE'')=1 OR
            (SELECT distinct TO_NUMBER(PARAM_VALUE) FROM GTT_REPORT_INPUT_PARAMS WHERE UPPER(PARAM_KEY) = ''IS_ICSR_REPORT'')=1)
    then
    case when cmwd.prod_rec_num is not null then
        cmwd.manufacturer_address_dr_1g
    else
        ( nvl2((select PARAM_VALUE from GTT_REPORT_INPUT_PARAMS where PARAM_KEY = ''E2B_SENDER_ADDRESS1''),
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
    end
ELSE cmw.manu_address_1g
END as manu_address_1g,
case
    when ((SELECT distinct TO_NUMBER(PARAM_VALUE) FROM GTT_REPORT_INPUT_PARAMS WHERE UPPER(PARAM_KEY) = ''IS_ICSR_PROFILE'')=1 OR
            (SELECT distinct TO_NUMBER(PARAM_VALUE) FROM GTT_REPORT_INPUT_PARAMS WHERE UPPER(PARAM_KEY) = ''IS_ICSR_REPORT'')=1)
    then
    case when cmwd.prod_rec_num is not null then
        cmwd.manufacturer_phone_dr_1g
    else
        (SELECT DISTINCT PARAM_VALUE FROM GTT_REPORT_INPUT_PARAMS  WHERE PARAM_KEY = ''E2B_SENDER_PHONE'')
    end
ELSE cmw.manu_phn_no_1g
end as manu_phn_no_1g,
cmw.manu_cmp_outsorce_facility_1g,
cmw.manu_outsrc_facility_name_1g,
cmw.manu_report_source_2g,
cmw.manu_report_source_other_2g,
cmw.manu_received_date_3g,
cmw.manu_nda_lic_no_4g,
cmw.manu_anda_lic_no_4g,
cmw.manu_ind_lic_no_4g,
cmw.manu_bla_lic_no_4g,
cmw.manu_pla_lic_no_4g,
cmw.manu_lic_type_4g,
case when cmwd.prod_rec_num is not null then
cmw.manu_nda_protocol_no_5g
ELSE
NULL
end as manu_nda_protocol_no_5g,
case   when      ( (SELECT distinct TO_NUMBER(PARAM_VALUE) FROM GTT_REPORT_INPUT_PARAMS WHERE UPPER(PARAM_KEY) = ''IS_ICSR_PROFILE'')=1 ) THEN
	case when cmwd.prod_rec_num is not null
          THEN
			case when ( select pect.due_days from VW_E2B_MW_SUBMISSION pect
						where pect.tenant_id = cm.tenant_id AND pect.case_id = cm.case_id
						AND pect.version_num = cm.version_num AND pect.case_id = cmw.case_id
						AND pect.version_num = cmw.version_num
						AND pect.PARTNER_NAME = (select distinct param_value from gtt_report_input_params where upper(param_key) = upper(''REPORT_NAME''))
						AND pect.PARTNER_RECIPIENT_ID = (select distinct param_value from gtt_report_input_params where upper(param_key) = upper(''E2B_RECEIVER_ORG_ID''))
						AND pect.PROCESSED_REPORT_ID = (SELECT TO_NUMBER(param_value) FROM gtt_report_input_params where upper(param_key) = upper(''PROCESSED_REPORT_ID'')) )
								between 0 and 5         then ''1''        else ''0''  end
		else ''0''
	end
    ||case when cmwd.prod_rec_num is not null
		then
			case when ( select pect.due_days from VW_E2B_MW_SUBMISSION pect
						where pect.tenant_id = cm.tenant_id AND pect.case_id = cm.case_id
						AND pect.version_num = cm.version_num AND pect.case_id = cmw.case_id
						AND pect.version_num = cmw.version_num
						AND pect.PARTNER_NAME = (select distinct param_value from gtt_report_input_params where upper(param_key) = upper(''REPORT_NAME''))
						AND pect.PARTNER_RECIPIENT_ID = (select distinct param_value from gtt_report_input_params where upper(param_key) = upper(''E2B_RECEIVER_ORG_ID''))
						AND pect.PROCESSED_REPORT_ID = (SELECT TO_NUMBER(param_value) FROM gtt_report_input_params where upper(param_key) = upper(''PROCESSED_REPORT_ID'')) )
								between 6 and 7         then ''1''        else ''0''  end
		else
			case when ( select pect.due_days from VW_E2B_MW_SUBMISSION pect
						where pect.tenant_id = cm.tenant_id AND pect.case_id = cm.case_id
						AND pect.version_num = cm.version_num AND pect.case_id = cmw.case_id
						AND pect.version_num = cmw.version_num
						AND pect.PARTNER_NAME = (select distinct param_value from gtt_report_input_params where upper(param_key) = upper(''REPORT_NAME''))
						AND pect.PARTNER_RECIPIENT_ID = (select distinct param_value from gtt_report_input_params where upper(param_key) = upper(''E2B_RECEIVER_ORG_ID''))
						AND pect.PROCESSED_REPORT_ID = (SELECT TO_NUMBER(param_value) FROM gtt_report_input_params where upper(param_key) = upper(''PROCESSED_REPORT_ID'')) )
                        between 0 and 7         then ''1''        else ''0''  end
	end
    ||    case when ( select pect.due_days from VW_E2B_MW_SUBMISSION pect
						where pect.tenant_id = cm.tenant_id AND pect.case_id = cm.case_id
						AND pect.version_num = cm.version_num AND pect.case_id = cmw.case_id
						AND pect.version_num = cmw.version_num
						AND pect.PARTNER_NAME = (select distinct param_value from gtt_report_input_params where upper(param_key) = upper(''REPORT_NAME''))
						AND pect.PARTNER_RECIPIENT_ID = (select distinct param_value from gtt_report_input_params where upper(param_key) = upper(''E2B_RECEIVER_ORG_ID''))
						AND pect.PROCESSED_REPORT_ID = (SELECT TO_NUMBER(param_value) FROM gtt_report_input_params where upper(param_key) = upper(''PROCESSED_REPORT_ID'')) )
                    between 8 and 15 then ''1'' else ''0'' end
    ||case when cmwd.prod_rec_num is not null
		then
			case when ( select pect.due_days from VW_E2B_MW_SUBMISSION pect
						where pect.tenant_id = cm.tenant_id AND pect.case_id = cm.case_id
						AND pect.version_num = cm.version_num AND pect.case_id = cmw.case_id
						AND pect.version_num = cmw.version_num
						AND pect.PARTNER_NAME = (select distinct param_value from gtt_report_input_params where upper(param_key) = upper(''REPORT_NAME''))
						AND pect.PARTNER_RECIPIENT_ID = (select distinct param_value from gtt_report_input_params where upper(param_key) = upper(''E2B_RECEIVER_ORG_ID''))
						AND pect.PROCESSED_REPORT_ID = (SELECT TO_NUMBER(param_value) FROM gtt_report_input_params where upper(param_key) = upper(''PROCESSED_REPORT_ID'')) )
                    between 16 and 30 then ''1'' else ''0'' end
		else ''0''
	end
    ||    ''0'' --hardcoded for Periodic reports.
           else
          cmw.manu_type_of_report_6g
   end  as  manu_type_of_report_6g,
case when ((SELECT distinct TO_NUMBER(PARAM_VALUE) FROM GTT_REPORT_INPUT_PARAMS WHERE UPPER(PARAM_KEY) = ''IS_ICSR_PROFILE'')=1)
THEN
    (select decode(UPPER(pect.FOLLOWUP_INFO),''INITIAL'',1,0) from VW_E2B_MW_SUBMISSION pect
        where pect.tenant_id = cm.tenant_id AND pect.case_id = cm.case_id
        AND pect.version_num = cm.version_num AND pect.case_id = cmw.case_id
        AND pect.version_num = cmw.version_num
        AND pect.PARTNER_NAME = (select distinct param_value from gtt_report_input_params where upper(param_key) = upper(''REPORT_NAME''))
        AND pect.PARTNER_RECIPIENT_ID = (select distinct param_value from gtt_report_input_params where upper(param_key) = upper(''E2B_RECEIVER_ORG_ID''))
        AND pect.PROCESSED_REPORT_ID = (SELECT TO_NUMBER(param_value) FROM gtt_report_input_params where upper(param_key) = upper(''PROCESSED_REPORT_ID''))
        )
    when ((SELECT distinct TO_NUMBER(PARAM_VALUE) FROM GTT_REPORT_INPUT_PARAMS WHERE UPPER(PARAM_KEY) = ''IS_ICSR_REPORT'')=1)
THEN
    (select decode(UPPER(pect.FOLLOWUP_INFO),''INITIAL'',1,0) from VW_E2B_MW_SUBMISSION pect
        where pect.tenant_id = cm.tenant_id AND pect.case_id = cm.case_id
        AND pect.version_num = cm.version_num AND pect.case_id = cmw.case_id
        AND pect.version_num = cmw.version_num
        AND pect.PARTNER_NAME = (select distinct param_value from gtt_report_input_params where upper(param_key) = upper(''REPORT_NAME''))
        AND pect.PARTNER_RECIPIENT_ID = (select distinct param_value from gtt_report_input_params where upper(param_key) = upper(''E2B_RECEIVER_ORG_ID''))
        )
ELSE
    cmw.manu_init_report_6g
END AS manu_init_report_6g,
case when ((SELECT distinct TO_NUMBER(PARAM_VALUE) FROM GTT_REPORT_INPUT_PARAMS WHERE UPPER(PARAM_KEY) = ''IS_ICSR_PROFILE'')=1)
THEN
    (select decode(UPPER(pect.FOLLOWUP_INFO),''FOLLOWUP'',1,''NULLIFICATION'',1,0) from VW_E2B_MW_SUBMISSION pect
        where pect.tenant_id = cm.tenant_id AND pect.case_id = cm.case_id
        AND pect.version_num = cm.version_num AND pect.case_id = cmw.case_id
        AND pect.version_num = cmw.version_num
        AND pect.PARTNER_NAME = (select distinct param_value from gtt_report_input_params where upper(param_key) = upper(''REPORT_NAME''))
        AND pect.PARTNER_RECIPIENT_ID = (select distinct param_value from gtt_report_input_params where upper(param_key) = upper(''E2B_RECEIVER_ORG_ID''))
        AND pect.PROCESSED_REPORT_ID = (SELECT TO_NUMBER(param_value) FROM gtt_report_input_params where upper(param_key) = upper(''PROCESSED_REPORT_ID''))
   )
  when ((SELECT distinct TO_NUMBER(PARAM_VALUE) FROM GTT_REPORT_INPUT_PARAMS WHERE UPPER(PARAM_KEY) = ''IS_ICSR_REPORT'')=1)
THEN
    (select decode(UPPER(pect.FOLLOWUP_INFO),''FOLLOWUP'',1,''NULLIFICATION'',1,0) from VW_E2B_MW_SUBMISSION pect
        where pect.tenant_id = cm.tenant_id AND pect.case_id = cm.case_id
        AND pect.version_num = cm.version_num AND pect.case_id = cmw.case_id
        AND pect.version_num = cmw.version_num
        AND pect.PARTNER_NAME = (select distinct param_value from gtt_report_input_params where upper(param_key) = upper(''REPORT_NAME''))
        AND pect.PARTNER_RECIPIENT_ID = (select distinct param_value from gtt_report_input_params where upper(param_key) = upper(''E2B_RECEIVER_ORG_ID''))
   )
ELSE
    cmw.manu_followup_report_6g
END AS manu_followup_report_6g,
case when ((SELECT distinct TO_NUMBER(PARAM_VALUE) FROM GTT_REPORT_INPUT_PARAMS WHERE UPPER(PARAM_KEY) = ''IS_ICSR_PROFILE'')=1)
THEN
    case
        when (select decode(UPPER(pect.FOLLOWUP_INFO),''FOLLOWUP'',1,''NULLIFICATION'',1,0) from VW_E2B_MW_SUBMISSION pect
        where pect.tenant_id = cm.tenant_id AND pect.case_id = cm.case_id
        AND pect.version_num = cm.version_num AND pect.case_id = cmw.case_id
        AND pect.version_num = cmw.version_num
        AND pect.PARTNER_NAME = (select distinct param_value from gtt_report_input_params where upper(param_key) = upper(''REPORT_NAME''))
        AND pect.PARTNER_RECIPIENT_ID = (select distinct param_value from gtt_report_input_params where upper(param_key) = upper(''E2B_RECEIVER_ORG_ID''))
        AND pect.PROCESSED_REPORT_ID = (SELECT TO_NUMBER(param_value) FROM gtt_report_input_params where upper(param_key) = upper(''PROCESSED_REPORT_ID''))
    ) = 1
        then
        (select pect.followup_num from VW_E2B_MW_SUBMISSION pect
        where pect.tenant_id = cm.tenant_id AND pect.case_id = cm.case_id
        AND pect.version_num = cm.version_num AND pect.case_id = cmw.case_id
        AND pect.version_num = cmw.version_num
        AND pect.PARTNER_NAME = (select distinct param_value from gtt_report_input_params where upper(param_key) = upper(''REPORT_NAME''))
        AND pect.PARTNER_RECIPIENT_ID = (select distinct param_value from gtt_report_input_params where upper(param_key) = upper(''E2B_RECEIVER_ORG_ID''))
        AND pect.PROCESSED_REPORT_ID = (SELECT TO_NUMBER(param_value) FROM gtt_report_input_params where upper(param_key) = upper(''PROCESSED_REPORT_ID''))
        )
    ELSE
        null
    end
when ((SELECT distinct TO_NUMBER(PARAM_VALUE) FROM GTT_REPORT_INPUT_PARAMS WHERE UPPER(PARAM_KEY) = ''IS_ICSR_REPORT'')=1)
THEN
    case
        when (select decode(UPPER(pect.FOLLOWUP_INFO),''FOLLOWUP'',1,''NULLIFICATION'',1,0) from VW_E2B_MW_SUBMISSION pect
        where pect.tenant_id = cm.tenant_id AND pect.case_id = cm.case_id
        AND pect.version_num = cm.version_num AND pect.case_id = cmw.case_id
        AND pect.version_num = cmw.version_num
        AND pect.PARTNER_NAME = (select distinct param_value from gtt_report_input_params where upper(param_key) = upper(''REPORT_NAME''))
        AND pect.PARTNER_RECIPIENT_ID = (select distinct param_value from gtt_report_input_params where upper(param_key) = upper(''E2B_RECEIVER_ORG_ID''))
    ) = 1
        then
        (select pect.followup_num from VW_E2B_MW_SUBMISSION pect
        where pect.tenant_id = cm.tenant_id AND pect.case_id = cm.case_id
        AND pect.version_num = cm.version_num AND pect.case_id = cmw.case_id
        AND pect.version_num = cmw.version_num
        AND pect.PARTNER_NAME = (select distinct param_value from gtt_report_input_params where upper(param_key) = upper(''REPORT_NAME''))
        AND pect.PARTNER_RECIPIENT_ID = (select distinct param_value from gtt_report_input_params where upper(param_key) = upper(''E2B_RECEIVER_ORG_ID''))
        )
    ELSE
        null
    end
else
    cmw.manu_follow_up_num_6g
END as manu_follow_up_num_6g,
cmw.manu_ae_terms_7g,
cmw.manu_rpt_no_8g,
cmw.country_a3_code_2g,
cmw.lit_add_details_2g,
COALESCE(cmwd.suspect_product_dr_c,cmw.susp_additional_3c) as susp_additional_3c,
cmw.susp_prod_other_frequency_3c,
cmw.susp_prod_other_route_3c,
cmw.pat_gender_3a,
cmw.ae_additional_comments_6b,
cmw.pat_gender_other_3a,
cmw.susp_prod_dose_reduced_4c,
cmw.susp_prod_therapy_duration_4c,
cmwd.brand_name_1d,
cmwd.common_device_name_2d,
cmwd.procode_2d,
cmwd.manufacturer_address_3d,
cmwd.model_no_4d,
cmwd.lot_no_4d,
cmwd.catalog_no_4d,
cmwd.serial_no_4d,
cmwd.expiration_date_4d,
cmwd.unique_device_identifier_4d,
cmwd.operator_of_device_5d,
cmwd.operator_of_device_other_5d,
cmwd.implant_date_6d,
cmwd.explant_date_6d,
cmwd.single_use_device_7d,
cmwd.reprocessor_address_7d,
cmwd.third_party_service_8d,
cmwd.device_available_for_eval_9d,
cmwd.manufacturer_return_date_9d,
cmwd.conc_prod_therapy_dates_10d,
cmwd.reportable_event_type_1h,
cmwd.no_of_events_summarized_1h,
cmwd.follow_up_type_2h,
cmwd.device_evaluated_by_manu_3h,
cmwd.device_manufacture_date_4h,
cmwd.labeled_for_single_use_5h,
cmwd.health_effect_clinical_code_6h,
cmwd.health_effect_impact_code_6h,
cmwd.medical_device_problem_code_6h,
cmwd.component_code_6h,
cmwd.investigation_type_6h,
cmwd.investigation_findings_6h,
cmwd.investigation_conclusions_6h,
cmwd.remedial_action_initiated_7h,
cmwd.remedial_action_init_other_7h,
cmwd.usage_of_device_8h,
cmwd.reported_to_fda_no_9h,
cmwd.related_reported_number_10h,
cmwd.additional_manu_narrative_11h
from
V_SAFETY_IDENTIFICATION cm
JOIN gtt_query_case_list gtt
ON ( cm.tenant_id = gtt.tenant_id
AND  cm.case_id = gtt.case_id
AND  cm.version_num = gtt.version_num)
JOIN case_mw cmw
ON ( cmw.tenant_id = gtt.tenant_id
AND  cmw.case_id = gtt.case_id
AND  cmw.version_num = gtt.version_num )
LEFT JOIN case_mw_device cmwd
ON ( cmw.tenant_id = cmwd.tenant_id
AND  cmw.case_id = cmwd.case_id
AND  cmw.version_num = cmwd.version_num
AND  cmwd.tenant_id = gtt.tenant_id
AND  cmwd.case_id = gtt.case_id
AND  cmwd.version_num = gtt.version_num
AND  NVL(cmwd.prod_rec_num,-1) = NVL(gtt.prod_rec_num,-1) )') where id = (select id from RPT_TEMPLT where name='Medwatch Template' and MEDWATCH_TEMPLATE=1 and orig_templt_id=0 and is_deleted=0);
commit;
