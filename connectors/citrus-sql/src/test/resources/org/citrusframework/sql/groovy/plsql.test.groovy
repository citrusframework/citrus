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

import static org.citrusframework.actions.ExecutePLSQLAction.Builder.plsql

name "PlsqlTest"
author "Christoph"
status "FINAL"
description "Sample test in Groovy"

actions {
    $(plsql()
        .dataSource(dataSource)
        .ignoreErrors(true)
        .sqlResource("classpath:org/citrusframework/integration/actions/plsql.sql")
    )

    $(plsql()
        .dataSource(dataSource)
        .ignoreErrors(true)
        .transactionManager(mockTransactionManager)
        .transactionTimeout(5000)
        .transactionIsolationLevel("ISOLATION_READ_COMMITTED")
        .sqlScript("""
BEGIN
    EXECUTE IMMEDIATE 'create or replace function test (v_id in number) return number is
      begin
       if v_id  is null then
        return 0;
        end if;
        return v_id;
      end;';
END;
/"""))
}
