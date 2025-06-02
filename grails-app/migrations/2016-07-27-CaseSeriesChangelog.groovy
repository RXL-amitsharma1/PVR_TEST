import com.rxlogix.config.*
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.util.MiscUtil

databaseChangeLog = {

	changeSet(author: "gautammalhotra (generated)", id: "1469623242764-3") {
		createTable(tableName: "CASE_SERIES_QUERY_VALUES") {
			column(name: "CASE_SERIES_ID", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "QUERY_VALUE_ID", type: "number(19,0)")

			column(name: "QUERY_VALUE_IDX", type: "number(10,0)")
		}
	}

	changeSet(author: "gautammalhotra (generated)", id: "1469623242764-4") {
		createTable(tableName: "CASE_SERIRES_DATE_RANGE_INFO") {
			column(name: "ID", type: "number(19,0)") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "CASE_SERIRES_PK")
			}

			column(name: "VERSION", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "DATE_RNG_END_ABSOLUTE", type: "timestamp")

			column(name: "DATE_RNG_ENUM", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "DATE_RNG_START_ABSOLUTE", type: "timestamp")

			column(name: "RELATIVE_DATE_RNG_VALUE", type: "number(10,0)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "gautammalhotra (generated)", id: "1469623242764-5") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'CASE_SERIES', columnName: 'AS_OF_VERSION_DATE')
			}
		}
		addColumn(tableName: "CASE_SERIES") {
			column(name: "AS_OF_VERSION_DATE", type: "timestamp")
		}
	}

	changeSet(author: "gautammalhotra (generated)", id: "1469623242764-6") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'CASE_SERIES', columnName: 'CASE_SERIES_DATE_RANGE_INFO_ID')
			}
		}
		addColumn(tableName: "CASE_SERIES") {
			column(name: "CASE_SERIES_DATE_RANGE_INFO_ID", type: "number(19,0)") {
				constraints(nullable: "true")
			}
		}
	}

	changeSet(author: "gautammalhotra (generated)", id: "1469623242764-7") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'CASE_SERIES', columnName: 'PVUSER_ID')
			}
		}
		addColumn(tableName: "CASE_SERIES") {
			column(name: "PVUSER_ID", type: "number(19,0)") {
				constraints(nullable: "true")
			}
		}
	}

	changeSet(author: "gautammalhotra (generated)", id: "1469623242764-34") {
		addForeignKeyConstraint(baseColumnNames: "CASE_SERIES_DATE_RANGE_INFO_ID", baseTableName: "CASE_SERIES", constraintName: "FK43668666E678A734", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "CASE_SERIRES_DATE_RANGE_INFO", referencesUniqueColumn: "false")
	}

	changeSet(author: "gautammalhotra (generated)", id: "1469623242764-35") {
		addForeignKeyConstraint(baseColumnNames: "PVUSER_ID", baseTableName: "CASE_SERIES", constraintName: "FK4366866649425289", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "PVUSER", referencesUniqueColumn: "false")
	}

	changeSet(author: "gautammalhotra (generated)", id: "1469623242764-36") {
		addForeignKeyConstraint(baseColumnNames: "QUERY_VALUE_ID", baseTableName: "CASE_SERIES_QUERY_VALUES", constraintName: "FK14EDD9D240891065", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "QUERY_VALUE", referencesUniqueColumn: "false")
	}

	changeSet(author: "gautammalhotra (generated)", id: "1469768624032-17") {
		dropColumn(columnName: "USE_CASE_SERIES_ID", tableName: "CASE_SERIES")
	}

	changeSet(author: "gautammalhotra (generated)", id: "1469768624032-18") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'CASE_SERIES', columnName: 'IS_DELETED')
			}
		}
		addColumn(tableName: "CASE_SERIES") {
			column(name: "IS_DELETED", type: "number(1,0)") {
				constraints(nullable: "true")
			}
		}
		sql("update CASE_SERIES set IS_DELETED = 0;")
		addNotNullConstraint(tableName: "CASE_SERIES", columnName: "IS_DELETED")
	}

	changeSet(author: "gautammalhotra (generated)", id: "1469768624032-21") {
		sql("update CASE_SERIES set DATE_RANGE_TYPE='CASE_RECEIPT_DATE';")
	}

    changeSet(author: "gautammalhotra (generated)", id: "1469768624032-20") {
        grailsChange {
            change {
                try {
                    int count = ExecutedPeriodicReportConfiguration.countByCaseSeriesIsNotNullOrCumulativeCaseSeriesIsNotNull()
                    int max = 10
                    int offset = 0
                    while (offset < count) {
                        ExecutedPeriodicReportConfiguration.withNewSession { session ->
                            ExecutedPeriodicReportConfiguration.findAllByCaseSeriesIsNotNullOrCumulativeCaseSeriesIsNotNull([max: max, offset: offset, order: 'asc', sort: 'id']).each { ExecutedPeriodicReportConfiguration executedReportConfiguration ->
                                [executedReportConfiguration.caseSeries, executedReportConfiguration.cumulativeCaseSeries].findAll {
                                    it
                                }.each { CaseSeries caseSeries ->
                                    caseSeries.seriesName = executedReportConfiguration.reportName + (executedReportConfiguration.executedGlobalDateRangeInformation.dateRangeEnum == DateRangeEnum.CUMULATIVE ? "-CUM" : "") + "-" + executedReportConfiguration.numOfExecutions
                                    caseSeries.dateRangeType = executedReportConfiguration.dateRangeType
                                    caseSeries.owner = executedReportConfiguration.owner
                                    caseSeries.asOfVersionDate = executedReportConfiguration.asOfVersionDate
                                    caseSeries.caseSeriesDateRangeInformation = new CaseSeriesDateRangeInformation(MiscUtil.getObjectProperties(executedReportConfiguration.executedGlobalDateRangeInformation, CaseSeriesDateRangeInformation.propertiesToUseForCopying))
                                    caseSeries.evaluateDateAs = executedReportConfiguration.evaluateDateAs
                                    caseSeries.excludeFollowUp = executedReportConfiguration.excludeFollowUp
                                    caseSeries.includeLockedVersion = executedReportConfiguration.includeLockedVersion
                                    caseSeries.excludeNonValidCases = executedReportConfiguration.excludeNonValidCases
                                    caseSeries.suspectProduct = executedReportConfiguration.suspectProduct
                                    caseSeries.productSelection = executedReportConfiguration.productSelection
                                    caseSeries.eventSelection = executedReportConfiguration.usedEventSelection
                                    caseSeries.globalQuery = executedReportConfiguration.executedGlobalQuery
                                    caseSeries.createdBy = executedReportConfiguration.createdBy
                                    caseSeries.modifiedBy = executedReportConfiguration.modifiedBy
                                    caseSeries.globalQueryValueLists?.clear()
                                    executedReportConfiguration.executedGlobalQueryValueLists.each {
                                        QueryValueList queryValueList = new QueryValueList(query: it.query)
                                        it.parameterValues.each { parameterValue ->
                                            ParameterValue tempValue = null
                                            if (parameterValue instanceof ExecutedCustomSQLValue) {
                                                tempValue = new CustomSQLValue(it.properties)
                                            } else if (parameterValue instanceof ExecutedQueryExpressionValue) {
                                                tempValue = new QueryExpressionValue(it.properties)
                                            }
                                            queryValueList.addToParameterValues(tempValue)
                                        }
                                        caseSeries.addToGlobalQueryValueLists(queryValueList)
                                    }
                                    caseSeries.save(failOnError: true)
                                }
                            }
                            offset += max
                            session.flush()
                            session.clear()
                            session.close()
                            session.connection()?.close()
                        }
                    }
                } catch (Exception ex) {
                    println "##### Error Occurred while updating old records for CaseSeries liquibase changeset 1469768624032-20 ####"
                    ex.printStackTrace(System.out)
                }
            }
        }
    }

}
