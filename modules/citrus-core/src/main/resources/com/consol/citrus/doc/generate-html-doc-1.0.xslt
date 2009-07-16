<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:spring="http://www.springframework.org/schema/beans">
	
	<xsl:output method="text" version="1.0" encoding="UTF-8" indent="yes" cdata-section-elements="data" />

	<xsl:template match="/spring:beans">
		<xsl:value-of select="comment()" />

		<xsl:apply-templates select="spring:bean[@parent='testCase']" />
	</xsl:template>

	<xsl:template match="/spring:beans/spring:bean[@parent='testCase']">
		<xsl:param name="ident"	select="concat(//spring:bean[@parent='testCase']/@name, generate-id())" />

		<u><xsl:value-of select="@name" /></u>:
		<br />
		<xsl:value-of select="spring:description" />
		<br />

		<xsl:element name="div">
			<xsl:attribute name="id"><xsl:value-of select="$ident" />+</xsl:attribute>
			<xsl:attribute name="style">font-size:10pt;display:block</xsl:attribute>

			<xsl:element name="a">
				<xsl:attribute name="href">javascript:showDetails('<xsl:value-of select="$ident" />')</xsl:attribute>
				<xsl:attribute name="style">text-decoration:none</xsl:attribute>
				+ show details ...
			</xsl:element>
		</xsl:element>

		<xsl:element name="div">
			<xsl:attribute name="id"><xsl:value-of select="$ident" />-</xsl:attribute>
			<xsl:attribute name="style">font-size:10pt;display:none</xsl:attribute>

			<xsl:element name="a">
				<xsl:attribute name="href">javascript:hideDetails('<xsl:value-of select="$ident" />')</xsl:attribute>
				<xsl:attribute name="style">text-decoration:none</xsl:attribute>
				- hide details ...
			</xsl:element>
		</xsl:element>

		<xsl:element name="div">
			<xsl:attribute name="id"><xsl:value-of select="$ident" /></xsl:attribute>
			<xsl:attribute name="style">display:none</xsl:attribute>
			<ul>
				<xsl:apply-templates select="spring:property[@name='testChain']/*" />
			</ul>
		</xsl:element>

		<br />
	</xsl:template>

	<xsl:template match="spring:bean">
		<xsl:if test="@parent != 'echo'">
			<li>
				<xsl:value-of select="@parent" />
			</li>
			<xsl:apply-templates select="spring:property" />
		</xsl:if>
	</xsl:template>

	<xsl:template match="spring:bean/spring:property[@name='xmlData'] | spring:bean/spring:property[@name='statement'] | spring:bean/spring:property[@name='statements']">
		<xsl:param name="ident" select="concat(//spring:bean[@parent='testCase']/@name, generate-id())" />

		<xsl:element name="div">
			<xsl:attribute name="id"><xsl:value-of select="$ident" />+</xsl:attribute>
			<xsl:attribute name="style">font-size:10pt;display:block</xsl:attribute>

			<xsl:element name="a">
				<xsl:attribute name="href">javascript:showDetails('<xsl:value-of select="$ident" />')</xsl:attribute>
				<xsl:attribute name="style">text-decoration:none</xsl:attribute>
				+ show details ...
			</xsl:element>
		</xsl:element>

		<xsl:element name="div">
			<xsl:attribute name="id"><xsl:value-of select="$ident" />-</xsl:attribute>
			<xsl:attribute name="style">font-size:10pt;display:none</xsl:attribute>

			<xsl:element name="a">
				<xsl:attribute name="href">javascript:hideDetails('<xsl:value-of select="$ident" />')</xsl:attribute>
				<xsl:attribute name="style">text-decoration:none</xsl:attribute>
				- hide details ...
			</xsl:element>
		</xsl:element>

		<xsl:element name="div">
			<xsl:attribute name="id"><xsl:value-of select="$ident" /></xsl:attribute>
			<xsl:attribute name="style">font-size:8pt;display:none</xsl:attribute>
			<xsl:value-of select="normalize-space(.)" />
		</xsl:element>

	</xsl:template>

	<xsl:template match="text()" />

</xsl:stylesheet>