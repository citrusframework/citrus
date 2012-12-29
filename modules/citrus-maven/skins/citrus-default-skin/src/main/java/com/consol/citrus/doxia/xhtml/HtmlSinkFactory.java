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

import java.io.Writer;

import org.apache.maven.doxia.sink.AbstractXmlSinkFactory;
import org.apache.maven.doxia.sink.Sink;

/**
 * @author Christoph Deppisch
 * 
 * @plexus.component role="org.apache.maven.doxia.sink.SinkFactory" role-hint="html"
 */
public class HtmlSinkFactory extends AbstractXmlSinkFactory {
    /** {@inheritDoc} */
    protected Sink createSink( Writer writer, String encoding ) {
        return new HtmlSink( writer, encoding );
    }

    /** {@inheritDoc} */
    protected Sink createSink( Writer writer, String encoding, String languageId ) {
        return new HtmlSink( writer, encoding, languageId );
    }
}
