<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ Copyright 2006-2014 the original author or authors.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  -->
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:spring="http://www.springframework.org/schema/beans">
  <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" cdata-section-elements="data"/>

  <xsl:param name="bean_id" />
  <xsl:preserve-space elements="*"/>

  <xsl:template match="spring:beans">
    <xsl:copy>
      <xsl:copy-of select="/node()/@*[local-name()='schemaLocation']"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="node()">
    <xsl:choose>
      <xsl:when test="local-name(.) = beans">
        <xsl:apply-templates/>
      </xsl:when>
      <xsl:when test="@id = $bean_id">
        <xsl:text>BEAN</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy-of select="."/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="text() | comment()">
    <xsl:copy-of select="."/>
  </xsl:template>

</xsl:stylesheet>