/*
 * Copyright the original author or authors.
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

package org.citrusframework.sql.integration;

import java.io.File;
import java.net.URISyntaxException;

import javax.sql.DataSource;

import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.junit.jupiter.spring.CitrusSpringSupport;
import org.citrusframework.spring.config.CitrusSpringConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

@CitrusSpringSupport
@Isolated("ITs share one HSQLDB in-memory database (jdbc:hsqldb:mem:test)")
@ContextConfiguration(classes = CitrusSpringConfig.class)
public class ExecuteSQLFileResourceIT implements TestActionSupport {

    @Autowired
    @Qualifier("testDataSource")
    private DataSource dataSource;

    @Test
    @CitrusTest
    public void executeSQLActionWithFileSystemResources(@CitrusResource TestCaseRunner runner) {
        runner.run(sql().dataSource(dataSource)
            .sqlResource("file:" + absolutePathOf("org/citrusframework/sql/integration/script.sql"))
            .sqlResource("file:" + absolutePathOf("org/citrusframework/sql/integration/second-script.sql")));

        runner.run(query().dataSource(dataSource)
            .statement("select COUNT(1) as overall_cnt from CUSTOMERS")
            .validate("OVERALL_CNT", "2"));

        runner.run(query().dataSource(dataSource)
            .statement("select NAME from CUSTOMERS where CUSTOMER_ID = 2")
            .validate("NAME", "John"));
    }

    private String absolutePathOf(String classpathLocation) {
        try {
            return new File(getClass().getClassLoader().getResource(classpathLocation).toURI()).getAbsolutePath();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Unable to resolve file system path for %s".formatted(classpathLocation), e);
        }
    }
}
