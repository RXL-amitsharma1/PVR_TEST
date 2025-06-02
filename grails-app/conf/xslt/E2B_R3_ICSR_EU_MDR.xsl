<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:template match="incident[not(@version)]">
        <xsl:copy>
            <xsl:attribute name="version">V5.0 2018-05-30</xsl:attribute>
            <xsl:attribute name="xsi:noNamespaceSchemaLocation">incident-Initial-v7.2.1.xsd</xsl:attribute>
            <xsl:attribute name="sCreateTimeStamp">2020-11-29T19:51:53</xsl:attribute>
            <xsl:attribute name="sFormLanguage">en</xsl:attribute>
            <xsl:apply-templates select="node()|@*"/>
        </xsl:copy>
    </xsl:template>

    <!-- Identity template for copying everything else -->
    <xsl:template match="node()|@*">
        <xsl:copy>
            <xsl:apply-templates select="node()|@*"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>