<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:spring="http://www.springframework.org/schema/beans" 
	xmlns:tsf="http://www.citrusframework.org/schema/testcase"
	exclude-result-prefixes="spring tsf">

	<xsl:output method="text" version="1.0" encoding="UTF-8" indent="yes" cdata-section-elements="data" />

	<xsl:template match="/spring:beans">
		<xsl:value-of select="comment()" />

		<xsl:apply-templates select="tsf:testcase" />
	</xsl:template>

	<xsl:template match="/spring:beans/tsf:testcase">
		<xsl:param name="ident"	select="concat(//tsf:testcase/@name, generate-id())" />

		<u><xsl:value-of select="@name" /></u>:
		<br />
		<xsl:value-of select="tsf:description" />
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
				<li>Variable definitions</li>

				<xsl:element name="div">
					<xsl:attribute name="id"><xsl:value-of select="concat(//tsf:testcase/@name, generate-id(), 'variables')" />+</xsl:attribute>
					<xsl:attribute name="style">font-size:10pt;display:block</xsl:attribute>
		
					<xsl:element name="a">
						<xsl:attribute name="href">javascript:showDetails('<xsl:value-of select="concat(//tsf:testcase/@name, generate-id(), 'variables')" />')</xsl:attribute>
						<xsl:attribute name="style">text-decoration:none</xsl:attribute>
						+ show variables ...
					</xsl:element>
				</xsl:element>
		
				<xsl:element name="div">
					<xsl:attribute name="id"><xsl:value-of select="concat(//tsf:testcase/@name, generate-id(), 'variables')" />-</xsl:attribute>
					<xsl:attribute name="style">font-size:10pt;display:none</xsl:attribute>
		
					<xsl:element name="a">
						<xsl:attribute name="href">javascript:hideDetails('<xsl:value-of select="concat(//tsf:testcase/@name, generate-id(), 'variables')" />')</xsl:attribute>
						<xsl:attribute name="style">text-decoration:none</xsl:attribute>
						- hide variables ...
					</xsl:element>
				</xsl:element>
		
				<xsl:element name="div">
					<xsl:attribute name="id"><xsl:value-of select="concat(//tsf:testcase/@name, generate-id(), 'variables')" /></xsl:attribute>
					<xsl:attribute name="style">font-size:8pt;display:none</xsl:attribute>
					<ul>
						<xsl:apply-templates select="tsf:variables/*"/>
					</ul>
				</xsl:element>
				
				<xsl:apply-templates select="tsf:actions/*" />
			</ul>
		</xsl:element>

		<br />
	</xsl:template>

	<xsl:template match="tsf:send | tsf:receive | tsf:expectTimeout | tsf:sleep | tsf:updateDatabase | tsf:queryDatabase | tsf:java | tsf:createVariables | tsf:purgeJmsQueues">
		<li>
			<xsl:value-of select="local-name()" />
		</li>
		<xsl:apply-templates />
	</xsl:template>
	
	<xsl:template match="tsf:echo">		
		<br/>
		<font style="font-size:smaller;font-style:italic;">
			<xsl:value-of select="." />
		</font>
		<xsl:apply-templates />
	</xsl:template>

	<xsl:template match="tsf:message/tsf:data | tsf:statement">
		<xsl:param name="ident" select="concat(//tsf:testcase/@name, generate-id())" />

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
	
	<xsl:template match="tsf:message/tsf:resource">
		<xsl:param name="ident" select="concat(//tsf:testcase/@name, generate-id())" />

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
			<xsl:value-of select="@file" />
		</xsl:element>

	</xsl:template>
	
	<xsl:template match="tsf:variables/tsf:variable | tsf:createVariables/tsf:variable">
		<li>
			<xsl:value-of select="@name" /><xsl:text>=</xsl:text><xsl:value-of select="@value" />
		</li>
	</xsl:template>

	<xsl:template match="text()" />

</xsl:stylesheet>