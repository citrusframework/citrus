/*
 * Copyright 2006-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.zookeeper.integration;

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.citrusframework.zookeeper.client.ZooClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.dsl.JsonPathSupport.jsonPath;
import static org.citrusframework.zookeeper.actions.ZooExecuteAction.Builder.zookeeper;

/**
 * @author Martin Maher
 * @since 2.5
 */
public class ZooExecuteJavaIT extends TestNGCitrusSpringSupport {

    @Autowired
    private ZooClient zooClient;

    @Test
    @CitrusTest(name = "Zookeeper_01_IT")
    public void zookeeper01IT() {
        variable("expectedConnectionState", "CONNECTED");
        variable("randomString", "citrus:randomString(10)");

        run(zookeeper()
            .client(zooClient)
            .validate("$.responseData.state", "${expectedConnectionState}")
            .info());

        run(zookeeper()
            .client(zooClient)
            .extract(jsonPath()
                    .expression("$.responseData.path", "path"))
            .create("/${randomString}", "some test data")
            .acl("OPEN_ACL_UNSAFE")
            .mode("PERSISTENT")
            .validateCommandResult((result, context) -> Assert.assertEquals(result.getResponseData().get("path"),
                    context.replaceDynamicContentInString("/${randomString}"))));

        run(zookeeper()
            .client(zooClient)
            .exists("${path}")
            .validateCommandResult((result, context) -> Assert.assertEquals(result.getResponseData().get("version"), 0)));

        run(zookeeper()
            .client(zooClient)
            .get("${path}")
            .validateCommandResult((result, context) -> Assert.assertEquals(result.getResponseData().get("data"), "some test data")));

        run(zookeeper()
            .client(zooClient)
            .set("${path}", "new data"));

        run(zookeeper()
           .client(zooClient)
           .get("${path}")
           .validateCommandResult((result, context) -> Assert.assertEquals(result.getResponseData().get("data"), "new data")));

        run(zookeeper()
            .client(zooClient)
            .create("/${randomString}/child1", "")
            .acl("OPEN_ACL_UNSAFE")
            .mode("PERSISTENT")
            .validateCommandResult((result, context) -> Assert.assertEquals(result.getResponseData().get("path"),
                    context.replaceDynamicContentInString("/${randomString}/child1"))));

        run(zookeeper()
            .client(zooClient)
            .create("/${randomString}/child2", "")
            .acl("OPEN_ACL_UNSAFE")
            .mode("PERSISTENT")
            .validateCommandResult((result, context) -> Assert.assertEquals(result.getResponseData().get("path"),
                    context.replaceDynamicContentInString("/${randomString}/child2"))));

        run(zookeeper()
            .client(zooClient)
            .children("/${randomString}")
            .validateCommandResult((result, context) -> Assert.assertEquals(result.getResponseData().get("children").toString(), "[child1, child2]")));
    }
}
