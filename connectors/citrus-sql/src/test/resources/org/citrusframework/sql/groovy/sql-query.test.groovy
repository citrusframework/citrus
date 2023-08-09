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

package org.citrusframework.sql.groovy

import java.nio.charset.StandardCharsets

import static org.citrusframework.actions.ExecuteSQLAction.Builder.sql

name "SqlQueryTest"
author "Christoph"
status "FINAL"
description "Sample test in Groovy"

actions {
    $(sql()
        .dataSource(dataSource)
        .statement("insert into message values (1000, 'Hello from Citrus!')")
        .statement("insert into message values (1001, 'Citrus rocks!')")
    )

    $(sql()
        .dataSource(dataSource)
        .query()
        .statement("select text from message where id=1000")
        .validate("text", "Hello from Citrus!")
        .extract("text", "greeting")
    )

    $(sql()
        .dataSource(dataSource)
        .query()
        .statement("select text from message where id>=1000")
        .validate("text", "Hello from Citrus!", "Citrus rocks!")
    )

    $(sql()
        .dataSource(dataSource)
        .query()
        .statement("select * from message where id>=1000")
        .validate("id", "1000", "1001")
        .validate("text", "Hello from Citrus!", "Citrus rocks!")
    )

    $(sql()
        .dataSource(dataSource)
        .query()
        .statement("select * from message where id>=1000")
        .validateScript("assert rows.size() == 2", "groovy")
    )

    $(sql()
        .dataSource(dataSource)
        .query()
        .statement("select * from message where id>=1000")
        .validateScriptResource("classpath:org/citrusframework/sql/validate.groovy", "groovy", StandardCharsets.UTF_8)
    )
}
