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

import org.apache.maven.doxia.macro.MacroExecutionException;
import org.apache.maven.doxia.markup.HtmlMarkup;
import org.apache.maven.doxia.parser.AbstractXmlParser;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.sink.SinkEventAttributeSet;
import org.codehaus.plexus.util.xml.pull.XmlPullParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * Special HTML parser generates sink events for HTML output.
 * @author Christoph Deppisch
 * 
 * @plexus.component role="org.apache.maven.doxia.parser.Parser" role-hint="html"
 */
public class HtmlParser extends AbstractXmlParser {

    @Override
    protected void handleStartTag(XmlPullParser parser, Sink sink)
            throws XmlPullParserException, MacroExecutionException {
        SinkEventAttributeSet attribs = getAttributesFromParser(parser);
        
        if (parser.getName().equals(HtmlMarkup.HTML.toString())) {
            return; // do nothing
        } else if (parser.getName().equals(HtmlMarkup.HEAD.toString())) {
            sink.head(attribs);
        } else if (parser.getName().equals(HtmlMarkup.TITLE.toString())) {
            sink.title(attribs);
        } else if (parser.getName().equals(HtmlMarkup.BODY.toString())) {
            sink.body(attribs);
        } else if (parser.getName().equals(HtmlMarkup.BR.toString())) {
            sink.lineBreak(attribs);
        } else if (parser.getName().equals(HtmlMarkup.HR.toString())) {
            sink.horizontalRule(attribs);
        } else { // just copy tag as it is
            if (parser.isEmptyElementTag()) {
                sink.unknown(parser.getName(), new Object[] { HtmlMarkup.TAG_TYPE_SIMPLE }, attribs);
            } else {
                sink.unknown(parser.getName(), new Object[] { HtmlMarkup.TAG_TYPE_START }, attribs);
            }
        }
    }
    
    @Override
    protected void handleEndTag(XmlPullParser parser, Sink sink)
            throws XmlPullParserException, MacroExecutionException {
        SinkEventAttributeSet attribs = getAttributesFromParser(parser);
        
        if (parser.getName().equals(HtmlMarkup.HTML.toString())) {
            return; // do nothing
        } else if (parser.getName().equals(HtmlMarkup.HEAD.toString())) {
            sink.head_();
        } else if (parser.getName().equals(HtmlMarkup.TITLE.toString())) {
            sink.title_();
        } else if (parser.getName().equals(HtmlMarkup.BODY.toString())) {
            sink.body_();
        } else if (parser.getName().equals(HtmlMarkup.BR.toString())) {
            return; // do nothing
        } else if (parser.getName().equals(HtmlMarkup.HR.toString())) {
            return; // do nothing
        } else { // just close tag as it is
            sink.unknown(parser.getName(), new Object[] { HtmlMarkup.TAG_TYPE_END }, attribs);
        }
    }
    
}
