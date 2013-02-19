/*
 * Copyright 2006-2012 the original author or authors.
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
 */

package com.consol.citrus.doxia.xhtml;

import java.io.IOException;
import java.io.Writer;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML.Tag;

import org.apache.maven.doxia.markup.HtmlMarkup;
import org.apache.maven.doxia.sink.*;
import org.apache.maven.doxia.util.HtmlTools;

/**
 * Sink basically generates HTML output as is. Just takes care on HEAD, BODY and special elements rendering.
 * 
 * @author Christoph Deppisch
 */
public class HtmlSink extends AbstractXmlSink {

    /** Writer */
    private Writer writer;
    
    /**
     * @param writer
     * @param encoding
     */
    public HtmlSink(Writer writer) {
        this.writer = writer;
    }

    @Override
    public void head() {
        init();

        write("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");

        MutableAttributeSet atts = new SinkEventAttributeSet();
        atts.addAttribute("xmlns", "http://www.w3.org/1999/xhtml");

        writeStartTag(HtmlMarkup.HTML, atts);
            
        writeStartTag(HtmlMarkup.HEAD);
    }

    @Override
    public void head_() {
        writeEndTag(HtmlMarkup.HEAD);
    }

    @Override
    public void title() {
        writeStartTag(HtmlMarkup.TITLE);
    }

    @Override
    public void title_() {
        writeEndTag(HtmlMarkup.TITLE);

    }
    
    @Override
    public void body() {
        writeStartTag(HtmlMarkup.BODY);
    }

    @Override
    public void body_() {
        writeEndTag(HtmlMarkup.BODY);
        writeEndTag(HtmlMarkup.HTML);

        flush();
        init();
    }
    
    @Override
    public void lineBreak() {
        lineBreak(null);
    }

    @Override
    public void lineBreak(SinkEventAttributes attributes) {
        MutableAttributeSet atts = SinkUtils.filterAttributes(
                attributes, SinkUtils.SINK_BR_ATTRIBUTES);
        
        writeSimpleTag(HtmlMarkup.BR, atts);
    }

    @Override
    public void horizontalRule() {
        horizontalRule(null);
    }
    
    @Override
    public void horizontalRule(SinkEventAttributes attributes) {
        MutableAttributeSet atts = SinkUtils.filterAttributes(
                attributes, SinkUtils.SINK_HR_ATTRIBUTES);
        
        writeSimpleTag(HtmlMarkup.HR, atts);
    }
    
    @Override
    public void unknown(String name, Object[] requiredParams, SinkEventAttributes attributes) {
        if (requiredParams == null || !(requiredParams[0] instanceof Integer)) {
            getLog().warn("No type information for unknown event: '" + name + "', ignoring!");
            return;
        }

        int tagType = ((Integer) requiredParams[0]).intValue();
        
        Tag tag = HtmlTools.getHtmlTag(name);

        if (tag == null) {
            getLog().warn("No HTML tag found for unknown event: '" + name + "', ignoring!");
        } else {
            if (tagType == HtmlMarkup.TAG_TYPE_SIMPLE) {
                writeSimpleTag(tag, attributes);
            } else if (tagType == HtmlMarkup.TAG_TYPE_START) {
                writeStartTag(tag, attributes);
            } else if (tagType == HtmlMarkup.TAG_TYPE_END) {
                writeEndTag(tag);
            } else {
                getLog().warn("No type information for unknown event: '" + name + "', ignoring!");
            }
        }
    }

    @Override
    protected void write(String text) {
        try {
            writer.write(unifyEOLs(text));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
