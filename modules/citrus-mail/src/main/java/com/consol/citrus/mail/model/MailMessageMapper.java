/*
 * Copyright 2006-2013 the original author or authors.
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
package com.consol.citrus.mail.model;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.util.QuickWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.XppDriver;

import java.io.Writer;

/**
 * Creates XML message from model object and vice versa using XStream library. Adds
 * special CDATA section support for binary and text body content.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public class MailMessageMapper extends XStream {

    /**
     * Default constructor.
     */
    public MailMessageMapper() {
        super(getXppDriver());
        alias("mail-message", MailMessage.class);
        alias("attachment", AttachmentPart.class);
    }

    /**
     * Provides driver with cdata support.
     * @return
     */
    private static XppDriver getXppDriver() {
        return new XppDriver() {
            public HierarchicalStreamWriter createWriter(Writer out) {
                return new CDataPrettyPrintWriter(out);
            }
        };
    }

    /**
     * Pretty print writer wraps mail content (binary, text) with CDATA sections.
     */
    private static final class CDataPrettyPrintWriter extends PrettyPrintWriter {
        private boolean cdata = false;

        /**
         * @param writer
         */
        private CDataPrettyPrintWriter(Writer writer) {
            super(writer);
        }

        @SuppressWarnings("rawtypes")
        public void startNode(String name, Class clazz){
            super.startNode(name, clazz);
            cdata = (name.equals("content") ||
                    name.equals("text")  ||
                    name.equals("binary"));
        }

        protected void writeText(QuickWriter writer, String text) {
            if(cdata) {
                writer.write("<![CDATA[");
                writer.write(text);
                writer.write("]]>");
            } else {
                writer.write(text);
            }
        }
    }
}
