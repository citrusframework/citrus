<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:spring="http://www.springframework.org/schema/beans" 
	xmlns:tsf="http://www.consol.de/testframework"
	xmlns="urn:schemas-microsoft-com:office:spreadsheet"
	xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet"
	exclude-result-prefixes="ss tsf spring">

	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" omit-xml-declaration="yes"/>

	<xsl:template match="/spring:beans">
		<xsl:apply-templates select="tsf:testcase" />
	</xsl:template>

	<xsl:template match="/spring:beans/tsf:testcase">
		<Cell>
			<Data ss:Type="String">
				<xsl:value-of select="@name" />
			</Data>
			<NamedCell ss:Name="_FilterDatabase"/>
		</Cell>
		
		<Cell>
			<Data ss:Type="String">
				<xsl:value-of select="tsf:meta-info/tsf:author" />
			</Data>
			<NamedCell ss:Name="_FilterDatabase"/>
		</Cell>
		
		<Cell>
			<Data ss:Type="String">
				<xsl:value-of select="tsf:meta-info/tsf:status" />
			</Data>
			<NamedCell ss:Name="_FilterDatabase"/>
		</Cell>

		<Cell>
			<Data ss:Type="String">
				<xsl:value-of select="normalize-space(tsf:description)" />
			</Data>
			<NamedCell ss:Name="_FilterDatabase"/>
		</Cell>				
		
		<Cell>
			<Data ss:Type="String">
				<xsl:value-of select="tsf:meta-info/tsf:creationdate" />
			</Data>
			<NamedCell ss:Name="_FilterDatabase"/>
		</Cell>
	</xsl:template>
	
	<xsl:template match="text()" />

</xsl:stylesheet>