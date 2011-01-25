<?xml version="1.0"?>
<!-- 
 * Copyright 2006-2011 the original author or authors.
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
<xsl:stylesheet xmlns="http://www.w3.org/TR/xhtml1/transitional"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xslthl="http://xslthl.sf.net"
                version="1.0"
                exclude-result-prefixes="#default xslthl">
                
    <xsl:import href="urn:docbkx:stylesheet"/>
                
    <xsl:param name="html.stylesheet">../css/reference.css</xsl:param>
    <xsl:template name="output.html.stylesheets">
        <link href="css/reference.css" rel="stylesheet" type="text/css"/>
    </xsl:template>
    
    <!-- These extensions are required for table printing and other stuff -->
    <xsl:param name="use.extensions">1</xsl:param>
    <xsl:param name="tablecolumns.extension">0</xsl:param>
    <xsl:param name="callout.extensions">1</xsl:param>
    <xsl:param name="graphicsize.extension">0</xsl:param>

<!--###################################################
                      Table Of Contents
    ################################################### -->   

    <!-- Generate the TOCs for named components only -->
    <xsl:param name="generate.toc">
        book   toc
    </xsl:param>
    
    <!-- Show only Sections up to level 3 in the TOCs -->
    <xsl:param name="toc.section.depth">3</xsl:param>
    
<!--###################################################
                         Labels
    ################################################### -->   

    <!-- Label Chapters and Sections (numbering) -->
    <xsl:param name="chapter.autolabel">1</xsl:param>
    <xsl:param name="section.autolabel" select="1"/>
    <xsl:param name="section.label.includes.component.label" select="1"/>
        
<!--###################################################
                         Callouts
    ################################################### -->   

    <!-- Use images for callouts instead of (1) (2) (3) -->
    <xsl:param name="callout.graphics">1</xsl:param>
    <xsl:param name="callout.graphics.extension">.jpg</xsl:param>
    <!-- Place callout marks at this column in annotated areas -->
    <xsl:param name="callout.defaultcolumn">90</xsl:param>

<!--###################################################
                       Admonitions
    ################################################### -->   

    <!-- Use nice graphics for admonitions -->
    <xsl:param name="admon.graphics">1</xsl:param>
    <xsl:param name="admon.graphics.extension">.jpg</xsl:param>
        
<!--###################################################
                          Misc
    ################################################### -->
    
    <xsl:param name="draft.mode">no</xsl:param>
    
    <!-- Placement of titles -->
    <xsl:param name="formal.title.placement">
        figure after
        example before
        equation before
        table before
        procedure before
    </xsl:param>
    
    <xsl:template name="book.titlepage.separator">
        <hr/>
        <img src="images/citrus_logo.png" style="width:25%;float:right;"/>
    </xsl:template>
    
    <xsl:template match="author" mode="titlepage.mode">
        <xsl:if test="name(preceding-sibling::*[1]) = 'author'">
            <xsl:text>, </xsl:text>
        </xsl:if>
        <span class="{name(.)}">
            <xsl:call-template name="person.name" />
            <xsl:apply-templates mode="titlepage.mode" select="./contrib" />
            <xsl:apply-templates mode="titlepage.mode" select="./affiliation" />
        </span>
    </xsl:template>
    <xsl:template match="authorgroup" mode="titlepage.mode">
        <div class="{name(.)}">
            <h2>Authors</h2>
            <p/>
            <xsl:apply-templates mode="titlepage.mode" />
        </div>
    </xsl:template>

<!--###################################################
                       Highlighting
    ################################################### -->
    
    <xsl:param name="highlight.source">1</xsl:param>
    <xsl:param name="highlight.default.language">xml</xsl:param>
    
    <xsl:template match='xslthl:keyword'>
        <span class="hl-keyword"><xsl:apply-templates/></span>
    </xsl:template>

    <xsl:template match='xslthl:string'>
        <span class="hl-string"><xsl:apply-templates/></span>
    </xsl:template>

    <xsl:template match='xslthl:comment'>
        <span class="hl-comment"><xsl:apply-templates/></span>
    </xsl:template>

    <xsl:template match='xslthl:number'>
        <span class="hl-number"><xsl:apply-templates/></span>
    </xsl:template>

    <xsl:template match='xslthl:annotation'>
        <span class="hl-annotation"><xsl:apply-templates/></span>
    </xsl:template>

    <xsl:template match='xslthl:multiline-comment'>
        <span class="hl-multiline-comment"><xsl:apply-templates/></span>
    </xsl:template>
    
    <xsl:template match="xslthl:tag">
        <span class="hl-tag"><xsl:apply-templates/></span>
    </xsl:template>
    
    <xsl:template match='xslthl:attribute'>
      <span class="hl-attribute"><xsl:apply-templates/></span>
    </xsl:template>
    
    <xsl:template match='xslthl:value'>
      <span class="hl-value"><xsl:apply-templates/></span>
    </xsl:template>
      
</xsl:stylesheet>