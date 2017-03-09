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

package com.consol.citrus.zookeeper;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Martin Maher
 * @since 2.5
 */
public class ZookeeperIT extends TestNGCitrusTestDesigner {

    @Test
    @CitrusTest(name = "Zookeeper_01_IT")
    public void zookeeper01IT() {
        variable("expectedConnectionState", "CONNECTED");
        variable("randomString", "citrus:randomString(10)");

        zookeeper()
            .validate("$.responseData.state", "${expectedConnectionState}")
            .info();

        zookeeper()
            .extract("$.responseData.path", "path")
            .create("/${randomString}", "some test data")
            .acl("OPEN_ACL_UNSAFE")
            .mode("PERSISTENT")
            .validateCommandResult((result, context) -> Assert.assertEquals(result.getResponseData().get("path"), context.replaceDynamicContentInString("/${randomString}")));

        zookeeper()
            .exists("${path}")
            .validateCommandResult((result, context) -> Assert.assertEquals(result.getResponseData().get("version"), 0));

        zookeeper()
            .get("${path}")
            .validateCommandResult((result, context) -> Assert.assertEquals(result.getResponseData().get("data"), "some test data"));

        zookeeper()
            .set("${path}", "new data");

        zookeeper()
           .get("${path}")
           .validateCommandResult((result, context) -> Assert.assertEquals(result.getResponseData().get("data"), "new data"));

        zookeeper()
            .create("/${randomString}/child1", "")
            .acl("OPEN_ACL_UNSAFE")
            .mode("PERSISTENT")
            .validateCommandResult((result, context) -> Assert.assertEquals(result.getResponseData().get("path"), context.replaceDynamicContentInString("/${randomString}/child1")));

        zookeeper()
            .create("/${randomString}/child2", "")
            .acl("OPEN_ACL_UNSAFE")
            .mode("PERSISTENT")
            .validateCommandResult((result, context) -> Assert.assertEquals(result.getResponseData().get("path"), context.replaceDynamicContentInString("/${randomString}/child2")));

        zookeeper()
            .children("/${randomString}")
            .validateCommandResult((result, context) -> Assert.assertEquals(result.getResponseData().get("children").toString(), "[child1, child2]"));
    }
}
