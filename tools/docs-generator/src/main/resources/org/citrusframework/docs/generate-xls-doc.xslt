<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- 
 * Copyright 2006-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:spring="http://www.springframework.org/schema/beans" 
	xmlns:citrus="http://www.citrusframework.org/schema/testcase"
    xmlns:header="http://www.citrusframework.org/schema/doc/header"
	xmlns="urn:schemas-microsoft-com:office:spreadsheet"
	xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet"
	exclude-result-prefixes="ss citrus spring header">

	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" omit-xml-declaration="yes"/>

    <xsl:template match="/header:headers">
        <Row>
            <xsl:for-each select="./header:header">
                <Cell ss:StyleID="sTop">
                    <Data ss:Type="String">
                        <xsl:value-of select="."/>
                    </Data>
                    <NamedCell ss:Name="_FilterDatabase"/>
                </Cell>
            </xsl:for-each>
        </Row>
    </xsl:template>
    
	<xsl:template match="/spring:beans">
		<xsl:apply-templates select="citrus:testcase" />
	</xsl:template>

	<xsl:template match="/spring:beans/citrus:testcase">
		<Cell>
			<Data ss:Type="String">
				<xsl:value-of select="@name" />
			</Data>
			<NamedCell ss:Name="_FilterDatabase"/>
		</Cell>
		
		<Cell>
			<Data ss:Type="String">
				<xsl:value-of select="citrus:meta-info/citrus:author" />
			</Data>
			<NamedCell ss:Name="_FilterDatabase"/>
		</Cell>
		
		<Cell>
			<Data ss:Type="String">
				<xsl:value-of select="citrus:meta-info/citrus:status" />
			</Data>
			<NamedCell ss:Name="_FilterDatabase"/>
		</Cell>

		<Cell>
			<Data ss:Type="String">
				<xsl:value-of select="normalize-space(citrus:description)" />
			</Data>
			<NamedCell ss:Name="_FilterDatabase"/>
		</Cell>				
		
		<Cell>
			<Data ss:Type="String">
				<xsl:value-of select="citrus:meta-info/citrus:creationdate" />
			</Data>
			<NamedCell ss:Name="_FilterDatabase"/>
		</Cell>
        
        <xsl:apply-templates select="citrus:meta-info/*"/>
	</xsl:template>
    
    <xsl:template match="citrus:meta-info/*">
        <xsl:if test="namespace-uri(.) != 'http://www.citrusframework.org/schema/testcase'">
            <Cell>
                <Data ss:Type="String">
                    <xsl:value-of select="." />
                </Data>
                <NamedCell ss:Name="_FilterDatabase"/>
            </Cell>
        </xsl:if> 
        
        <xsl:apply-templates/>       
    </xsl:template>
    
	<xsl:template match="text()" />

</xsl:stylesheet>