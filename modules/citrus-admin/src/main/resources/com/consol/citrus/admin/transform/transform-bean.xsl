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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="text"/>
  <xsl:preserve-space elements="*"/>

  <xsl:param name="bean_id" />

  <xsl:template match="node()">
    <xsl:choose>
      <xsl:when test="local-name(.) = 'beans'">&lt;?xml version="1.0" encoding="UTF-8"?&gt;
&lt;<xsl:value-of select="name(.)"/><xsl:call-template name="namespaces"/><xsl:call-template name="attributes"/>&gt;<xsl:apply-templates/><xsl:call-template name="add-bean"/>&lt;/<xsl:value-of select="name(.)"/>&gt;</xsl:when>
      <xsl:when test="@id = $bean_id"><xsl:call-template name="update-bean"/></xsl:when>
      <xsl:when test="@name = $bean_id"><xsl:call-template name="update-bean"/></xsl:when>
      <xsl:when test="child::*">&lt;<xsl:value-of select="name(.)"/><xsl:call-template name="attributes"/>&gt;<xsl:apply-templates/>&lt;/<xsl:value-of select="name(.)"/>&gt;</xsl:when>
      <xsl:when test="text()">&lt;<xsl:value-of select="name(.)"/><xsl:call-template name="attributes"/>&gt;<xsl:value-of select="text()"/>&lt;/<xsl:value-of select="name(.)"/>&gt;</xsl:when>
      <xsl:otherwise>&lt;<xsl:value-of select="name(.)"/><xsl:call-template name="attributes"/>/&gt;</xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="namespaces">
    <xsl:for-each select="namespace::*">
      <xsl:if test="not(name(.) = 'xml')">
        <xsl:choose>
          <xsl:when test="string-length(name(.))">
            <xsl:text> </xsl:text>xmlns:<xsl:value-of select="name(.)"/>=&quot;<xsl:value-of select="."/>&quot;
          </xsl:when>
          <xsl:otherwise>
            <xsl:text> </xsl:text>xmlns=&quot;<xsl:value-of select="."/>&quot;
          </xsl:otherwise>
        </xsl:choose>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="attributes">
    <xsl:for-each select="@*">
      <xsl:choose>
        <xsl:when test="local-name(.) = 'schemaLocation'"><xsl:text> </xsl:text><xsl:value-of select="name(.)"/>=&quot;<xsl:value-of select="normalize-space(.)"/>&quot;</xsl:when>
        <xsl:when test="count(parent::node()/@*) > 2 and position() != last() and contains(namespace-uri(parent::node()), 'citrus')"><xsl:text> </xsl:text><xsl:value-of select="name(.)"/>=&quot;<xsl:value-of select="."/>&quot;
        </xsl:when>
        <xsl:otherwise><xsl:text> </xsl:text><xsl:value-of select="name(.)"/>=&quot;<xsl:value-of select="."/>&quot;</xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="comment()">&lt;!--<xsl:value-of select="."/>--&gt;</xsl:template>
  <xsl:template match="text()"><xsl:value-of select="."/></xsl:template>

</xsl:stylesheet>
