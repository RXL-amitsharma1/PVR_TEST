package com.rxlogix

import com.rxlogix.config.SourceProfile
import com.rxlogix.enums.SourceProfileTypeEnum
import grails.converters.JSON
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import org.grails.web.json.JSONArray
import spock.lang.Specification

class SeedDataServiceSpec extends Specification implements DataTest, ControllerUnitTest<SeedDataService> {


    static final int EXPECTED_TEMPLATES_SIZE = 80
    static final Set<String> EXPECTED_TEMPLATE_NAMES = ['症例記述情報リスト', 'DSUR SAR Line Listing Blinded', 'DSUR Death Line Listing Blinded', 'DSUR Death Line Listing', 'DSUR SAR Line Listing', 'PADER 15 day Submitted Report', 'Company Causality Comment', 'Product-Event Information', 'Cause of Death Information', 'Reporter Information', 'Patient Medical Condition', 'Pregnancy Information', 'PADER non-15 day Submitted Report', 'Product Information', 'Event Information', 'Study Information', 'Patient Demographic Information', 'Literature Case Listing', 'General Case Information', 'Case Submission Listing', 'Cases Submitted in Another NDA PADER', 'Case Narrative Listing', 'Case Line Listing (Study)', 'Case Line Listing (Marketed)', 'Patient Lab Test Results', 'Narrative Information', 'Case Compliance Listing', 'Literature Information', '症例提出ラインリスト', '症例ラインリスト(市販後)', '症例ラインリスト(治験 )', '文献症例ラインリスト', '症例調整リスト', '妊娠症例ラインリスト', '報告者情報', '患者統計学的情報', '症例一般情報', '企業因果関係コメント', '症例記述情報', '患者検査結果', '患者治療歴', '妊娠情報', '文献情報', '製品-有害事象情報', '製品情報', '死因情報', '有害事象情報', '試験情報', 'CIOMS IIラインリスト', 'CIOMS I Template', '有害事象-器官別大分類集計', 'PBRER PM Tabulation', 'SAR CT Tabulation Blinded', 'Number of Adverse Event Preferred Terms by Body System, Seriousness, Listedness and Geographic - PADER', 'Lot By SOC', 'SAE CT Tabulation Blinded', 'AE By SOC', '症例フォームレポート', 'Case Form Template', 'Blinded CIOMS II Line Listing', 'CIOMS II Line Listing', 'SAE CT Tabulation', 'SAR CT Tabulation', 'ETL Status', '再審査報告別紙様式9', 'ReSD Form 10', 'ReSD Form 11', '再審査報告別紙様式13', '再審査報告別紙様式14', '未知非重篤定期報告別紙様式7-1', '未知非重篤定期報告別紙様式7-2', '治験安全性最新報告別紙様式1', '治験安全性最新報告別紙様式2', '製薬協ラインリスト(標準テンプレート)', 'サマリーテーブル', '安全性定期報告別紙様式3-S1', '安全性定期報告別紙様式4-S2', 'IPR FORM 2', 'IPR FORM 4', 'IPR FORM 5']

    static final int EXPECTED_QUERIES_SIZE = 228
    static final Set<String> EXPECTED_QUERY_NAMES = ['症例記述情報概要', '妊娠症例', '報告区分', '海外症例', '報告対象有害事象', '国内症例', 'JPSRのクエリ', '症例 分類', '器官別大分類(SOC)', '死亡症例', '遅延報告', '追加報告症例', '死に至るもしくは生命を脅かす有害事象症例', '有害事象SMQ（狭域）', '有害事象SMQ（広域）', '重篤性 - 生命を脅かすもの', '重篤性 - 入院または入院期間の延長が必要なもの', '基本語(PT)', 'WHO医薬品コード', '重篤な関連する有害事象症例', '重篤性 - 先天異常を来すもの', '高位語(HLT)', '投与中止症例', '注目する製品の症例', '未知・非重篤副作用定期報告標準クエリ', '未提出レポート', '治験薬または製品名', '症例入手日（初回報告と重要な追加報告）', '症例の重篤性', '治療歴:メドラLLT', 'アーカイブ日付', '症例分類', '症例盲検状況', '重篤な有害事象のある試験クエリ', 'SUSAR症例', '未完了アクションアイテム症例', '下位語(LLT)', '患者の年齢群', '非小児用途症例', '治療歴:メドラSOC', '小児症例', '治療歴SMQ（狭域）', '長期使用症例', '報告期限', '報告種類', 'JDSURのクエリ', '臓器障害使用症例 - 腎臓', '試験種類', '症例初回報告日', '症例番号', '治療歴:メドラHLGT', '薬効欠如症例', '高齢者症例', '医薬品種類', '乱用と誤用症例', '未知事象症例', '医学的に確認された症例', '治療歴医薬品', '再投与症例', '妊娠報告種類', '妊娠結果', '治験施設', '報告先', '決定された関連ありの症例', '重篤性 - 大事に至らぬように処置を要するもの', '重篤性 - 死に至るもの', '重篤性 - 永続的又は顕著な障害・機能不全', '治療歴SMQ（広域）', '製品一般名', '投与経路', '機器症例', '医薬品コード', '提出日', '試験ID', 'バッチナンバー/ロットナンバー', '新規性(Core)', '死因基本語', '完了状態の症例', '固定日', '関連ありの症例', '使用理由PT', '高位グループ語(HLGT)', '製薬協ラインリスト海外一変症例', '製薬協ラインリスト国内試験症例', '有害事象新規性(JPPI)', '投薬過誤症例', '参照番号の種類と参照番号', '患者の性別', '治療歴:メドラPT', '重篤性 - 医学的に重大なもの', '治療歴:メドラHLT', '製品ファミリ', '発現国', '重篤な有害事象', '小児用途症例', '非高齢者症例', '臓器障害使用症例 -肝臓', '報告された因果関係が関連ありの症例', 'Product of Interest Cases', 'WHO Drug Code', 'Vaccine Cases', 'Vaccination of Immunocompromised Individuals Cases', 'Use in Organ Impaired Cases - Renal', 'Use in Organ Impaired Cases - Hepatic', 'Unsubmitted Reports', 'Unlisted cases', 'Submission Date', 'Study Type', 'Study ID', 'Study Drug or Product Name', 'Serious Unlisted Cases', 'Serious Events', 'SAR Cases', 'Safety Receipt Date (Initial)', 'Case Category Event SMQ (Broad)', 'DSUR Global Query', 'SAE CT Query', 'PBRER Pregnancy Cases', 'SUSAR cases', 'DSUR SAR Query', 'PADER Tabulation cases', 'PADER non-15 day cases', 'Region: Rest of the World', 'Region: EU Countries', 'Region: EEA Countries', 'Reference Type and Referrence Number', 'Case Category SUSAR cases', 'Case Category Serious Unlisted Cases', 'Patient Gender', 'Patient Age Range', 'Open Action Item Cases', 'Non-Pediatric Use Cases', 'Non-Elderly Cases', 'NDA Number', 'Medication Errors Cases', 'Medically Confirmed Cases', 'Case Category Pediatric Use Cases', 'Case Category Pediatric Cases', 'Case Category Medication Errors Cases', 'Case Category Lack of Efficacy Cases', 'Case Category Fatal Cases', 'Case Category Fatal and LT Cases', 'Case Category Event MedDRA PT', 'Case Category Drug Abuse and Misuse Cases', 'Pediatric Use Cases', 'Pediatric Cases', 'Medical Condition: MedDRA SOC', 'Medical Condition: MedDRA PT', 'Medical Condition: MedDRA LLT', 'Medical Condition: MedDRA HLT', 'Medical Condition: MedDRA HLGT', 'Medical Condition SMQ (Narrow)', 'Medical Condition SMQ (Broad)', 'Medical Condition Historical Drug', 'Long-term Use Cases', 'Rechallenge Cases', 'Product Generic Name', 'Product Family', 'Pregnancy Report Type', 'Pregnancy Outcome', 'PBRER PM Query', 'PBRER Global Query', 'Case Category Product Family/Product Name/Study ID', 'Study Center', 'Case Category', 'Case Category Vaccination of Immunocompromised Individuals Cases', 'Case Category Use in Organ Impaired Cases - Renal', 'Case Category Use in Organ Impaired Cases - Hepatic', 'Safety Receipt Date (Initial and Follow-up)', 'Route of Adminstration', 'Reporting Destination', 'Reported Related Cases', 'Report Type', 'Report Due Date', 'Related cases', 'Region: US Cases', 'Lock Date', 'Legal Cases', 'Late Reports', 'Lack of Efficacy Cases', 'Indication PT', 'IND Number', 'Followup cases', 'Fatal Cases', 'Fatal and LT Cases', 'Event SMQ (Narrow)', 'Event SMQ (Broad)', 'Event Seriousness Criteria - Medically Significant', 'Event Seriousness Criteria - Life Threatening', 'Event Seriousness Criteria - Intervention Required', 'Event Seriousness Criteria - Hospitalization', 'Event Seriousness Criteria - Disability', 'Event Seriousness Criteria - Death', 'Event Seriousness Criteria - Congenital Anomaly', 'Event Listedness (US)', 'Event MedDRA PT', 'Event MedDRA LLT', 'Event MedDRA HLT', 'Event MedDRA HLGT', 'Event Listedness (IB)', 'Event Listedness (CORE)', 'Event Listedness (CCDS)', 'Elderly Cases', 'Drug Type', 'Drug Code', 'Drug Abuse and Misuse Cases', 'Device cases', 'Determined Related Cases', 'Dechallenge Cases', 'Country of Incidence', 'Consumer Cases', 'Cause of Death Preferred Term', 'Cases in completed status', 'Case Seriousness', 'Case Receipt Date (Initial and Sign. Fup)', 'Case Number', 'Case Initial Receipt Date', 'Case Blinding Status', 'Batch Number/Lot Number', 'Archival Date', 'PADER Report Destination', 'Case Narrative', 'Non-Valid Cases', 'Case Classification', 'Event MedDRA SOC', 'Case Category Long-term Use Cases', 'Case Category Elderly Cases', 'Deleted Cases']

    def setupSpec() {
        mockDomain SourceProfile
    }

    def "Validate Template JSON file and templates names"() {
        given:
        //To extract Root Project path.
        String filePath = getClass().getResource('cll_report_data.csv.gzip').getFile().replace("out/test/resources/com/rxlogix/cll_report_data.csv.gzip", "grails-app/conf/metadata/templates.json").replace('build/resources/test/com/rxlogix/cll_report_data.csv.gzip','build/resources/main/metadata/templates.json')
        File file = new File(filePath)
        def actualTemplateNames = (JSON.parse("[${file.text}]") as JSONArray).collect { it.name }
        expect:
        actualTemplateNames.size() == EXPECTED_TEMPLATES_SIZE
        !(actualTemplateNames - EXPECTED_TEMPLATE_NAMES).size()
        !(EXPECTED_TEMPLATE_NAMES - actualTemplateNames).size()
    }

    def "Validate Query JSON file and queries names"() {
        given:
        String filePath = getClass().getResource('cll_report_data.csv.gzip').getFile().replace("out/test/resources/com/rxlogix/cll_report_data.csv.gzip", "grails-app/conf/metadata/queries.json").replace('build/resources/test/com/rxlogix/cll_report_data.csv.gzip','build/resources/main/metadata/queries.json')
        File file = new File(filePath)
        def actualQueriesNames = (JSON.parse("[${file.text}]") as JSONArray).collect { it.name }
        expect:
        actualQueriesNames.size() == EXPECTED_QUERIES_SIZE
        !(actualQueriesNames - EXPECTED_QUERY_NAMES).size()
        !(EXPECTED_QUERY_NAMES - actualQueriesNames).size()
    }
    void  "Test seed source profile table for source name PVCM"(){
        given:
        def mockCRUDService=Mock(CRUDService)
        SourceProfile newRecord = new SourceProfile(sourceId: 1, sourceName: 'PVCM', sourceAbbrev: 'OTH', sourceProfileTypeEnum: SourceProfileTypeEnum.SINGLE, isCentral: true, isDeleted: false)
        newRecord.save(failOnError:true,validate:false,flush:true)
        SourceProfile existingRecord = new SourceProfile(sourceId: 1, sourceName: 'Argus', sourceAbbrev: 'ARG', sourceProfileTypeEnum: SourceProfileTypeEnum.SINGLE, isCentral: true, isDeleted: false)
        existingRecord.save(failOnError:true,validate:false,flush:true)
        when:
        def test = existingRecord.equals(newRecord)
        then:
        assert !test
    }
}
