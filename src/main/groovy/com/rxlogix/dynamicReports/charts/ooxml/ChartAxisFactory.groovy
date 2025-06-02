package com.rxlogix.dynamicReports.charts.ooxml;

/**
 * A factory for different chart axis.
 */
interface ChartAxisFactory {

	/**
	 * @return new value axis
	 */
	ValueAxis createValueAxis(Map yAxis);

	/**
	 * @return new category axis.
	 */
	ChartAxis createCategoryAxis(Map xAxis);
}
