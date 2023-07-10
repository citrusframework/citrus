/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.groovy.dsl

import static org.citrusframework.actions.TransformAction.Builder.transform

name "TransformTest"
author "Christoph"
status "FINAL"
description "Sample test in Groovy"

actions {
    $(transform()
        .source("""
            <TestRequest>
              <Message>Hello World!</Message>
            </TestRequest>
        """)
        .xslt("""
            <xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
              <xsl:template match="/">
              <html>
                  <body>
                      <h2>Test Request</h2>
                      <p>Message: <xsl:value-of select="TestRequest/Message"/></p>
                  </body>
              </html>
              </xsl:template>
            </xsl:stylesheet>
        """)
        .result("result"))

    $(transform()
            .sourceFile("classpath:org/citrusframework/groovy/transform-source.xml")
            .xsltFile("classpath:org/citrusframework/groovy/transform.xslt")
    )
}
