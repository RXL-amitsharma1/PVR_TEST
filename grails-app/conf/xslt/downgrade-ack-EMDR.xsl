<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:hl7="urn:hl7-org:v3" xmlns:mif="urn:hl7-org:v3/mif"  exclude-result-prefixes="hl7 xsi xsl fo mif">

    <xsl:template match="submission">
        <submission lang="en">
            <environment>
                <detail><xsl:value-of select="environment/detail/text()"/></detail>
            </environment>

            <coreId><xsl:value-of select="coreId/text()"/></coreId>
            <batchId><xsl:value-of select="batchId/text()"/></batchId>
            <dateEntered><xsl:value-of select="dateEntered/text()"/></dateEntered>
            <numReportFailed><xsl:value-of select="numReportFailed/text()"/></numReportFailed>
            <numReportPassed><xsl:value-of select="numReportPassed/text()"/></numReportPassed>
            <submissionType><xsl:value-of select="submissionType/text()"/></submissionType>

            <report>
                <localReportNumber><xsl:value-of select="report/@id"/></localReportNumber>
                <status><xsl:value-of select="report/status/text()"/></status>
            </report>

            <failure>
                <reportId><xsl:value-of select="failure/reportId/text()"/></reportId>
                <detail>
                    <section><xsl:value-of select="failure/detail/section/text()"/></section>
                    <errorMessage><xsl:value-of select="failure/detail/errorMessage/text()"/></errorMessage>
                    <messageValue><xsl:value-of select="failure/detail/messageValue/text()"/></messageValue>
                    <xPath><xsl:value-of select="failure/detail/xPath/text()"/></xPath>
                </detail>
            </failure>
        </submission>
    </xsl:template>

</xsl:stylesheet>

