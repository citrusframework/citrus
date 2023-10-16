/*
 * Copyright 2006-2018 the original author or authors.
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

package org.citrusframework.generate.javadsl;

import java.io.File;
import java.io.IOException;

import org.citrusframework.CitrusSettings;
import org.citrusframework.generate.TestGenerator;
import org.citrusframework.generate.UnitFramework;
import org.citrusframework.util.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class SwaggerJavaTestGeneratorTest {

    @Test
    public void testCreateTestAsClient() throws IOException {
        SwaggerJavaTestGenerator generator = new SwaggerJavaTestGenerator();

        generator.withAuthor("Christoph")
                .withDescription("This is a sample test")
                .usePackage("org.citrusframework")
                .withFramework(UnitFramework.TESTNG);

        generator.withNamePrefix("UserLoginClient_");
        generator.withSpec("org/citrusframework/swagger/user-login-api.json");

        generator.create();

        verifyTest("UserLoginClient_createUser_IT");
        verifyTest("UserLoginClient_loginUser_IT");
        verifyTest("UserLoginClient_logoutUser_IT");
        verifyTest("UserLoginClient_getUserByName_IT");
    }

    @Test
    public void testCreateTestAsServer() throws IOException {
        SwaggerJavaTestGenerator generator = new SwaggerJavaTestGenerator();

        generator.withAuthor("Christoph")
                .withDescription("This is a sample test")
                .usePackage("org.citrusframework")
                .withFramework(UnitFramework.TESTNG);

        generator.withMode(TestGenerator.GeneratorMode.SERVER);
        generator.withSpec("org/citrusframework/swagger/user-login-api.json");

        generator.create();

        verifyTest("UserLoginService_createUser_IT");
        verifyTest("UserLoginService_loginUser_IT");
        verifyTest("UserLoginService_logoutUser_IT");
        verifyTest("UserLoginService_getUserByName_IT");
    }

    private void verifyTest(String name) throws IOException {
        File javaFile = new File(CitrusSettings.DEFAULT_TEST_SRC_DIRECTORY + "java/org/citrusframework/" +
                name + FileUtils.FILE_EXTENSION_JAVA);
        Assert.assertTrue(javaFile.exists());

        String javaContent = FileUtils.readToString(javaFile);
        Assert.assertTrue(javaContent.contains("@author Christoph"));
        Assert.assertTrue(javaContent.contains("public class " + name));
        Assert.assertTrue(javaContent.contains("* This is a sample test"));
        Assert.assertTrue(javaContent.contains("package org.citrusframework;"));
        Assert.assertTrue(javaContent.contains("extends TestNGCitrusSupport"));
    }

}
