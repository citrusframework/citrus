/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.arquillian.client;

import java.lang.reflect.Field;
import java.util.Properties;

import com.consol.citrus.Citrus;
import com.consol.citrus.CitrusVersion;
import com.consol.citrus.arquillian.configuration.CitrusConfiguration;
import com.consol.citrus.arquillian.helper.InjectionHelper;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ConfigurationBuilder;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.base.MemoryMapArchiveImpl;
import org.jboss.shrinkwrap.impl.base.spec.EnterpriseArchiveImpl;
import org.jboss.shrinkwrap.impl.base.spec.JavaArchiveImpl;
import org.jboss.shrinkwrap.impl.base.spec.WebArchiveImpl;
import org.mockito.Mockito;
import org.springframework.util.ReflectionUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public class CitrusArchiveProcessorTest {

    private CitrusArchiveProcessor archiveProcessor = new CitrusArchiveProcessor();
    private Instance<CitrusConfiguration> configurationInstance = Mockito.mock(Instance.class);

    private CitrusConfiguration configuration;

    @BeforeClass
    public void setCitrusVersion() {
        Field version = ReflectionUtils.findField(CitrusVersion.class, "version");
        ReflectionUtils.makeAccessible(version);
        ReflectionUtils.setField(version, CitrusVersion.class, "3.3.0-SNAPSHOT");
    }

    @BeforeMethod
    public void prepareConfiguration() throws IllegalAccessException {
        configuration = CitrusConfiguration.from(new Properties());

        configuration.getExcludedDependencies().add("org.apache.logging.log4j:log4j-api:jar:2.5");

        reset(configurationInstance);
        when(configurationInstance.get()).thenReturn(configuration);

        InjectionHelper.inject(archiveProcessor, "configurationInstance", configurationInstance);
    }

    @Test
    public void testProcessEnterpriseArchive() throws Exception {
        EnterpriseArchive enterpriseArchive = new EnterpriseArchiveImpl(new MemoryMapArchiveImpl(new ConfigurationBuilder().build()));
        archiveProcessor.process(enterpriseArchive, new TestClass(this.getClass()));
        verifyArtifact(enterpriseArchive, "/citrus-core-.*jar");
        verifyArtifact(enterpriseArchive, "/citrus-jms-.*jar");
        verifyArtifact(enterpriseArchive, "/citrus-kafka-.*jar");
        verifyArtifact(enterpriseArchive, "/citrus-jdbc-.*jar");
        verifyArtifact(enterpriseArchive, "/citrus-http-.*jar");
        verifyArtifact(enterpriseArchive, "/citrus-websocket-.*jar");
        verifyArtifact(enterpriseArchive, "/citrus-ws-.*jar");
        verifyArtifact(enterpriseArchive, "/citrus-ftp-.*jar");
        verifyArtifact(enterpriseArchive, "/citrus-camel-.*jar");
        verifyArtifact(enterpriseArchive, "/citrus-docker-.*jar");
        verifyArtifact(enterpriseArchive, "/citrus-kubernetes-.*jar");
        verifyArtifact(enterpriseArchive, "/citrus-sql-.*jar");
        verifyArtifact(enterpriseArchive, "/citrus-groovy-.*jar");
        verifyArtifact(enterpriseArchive, "/citrus-selenium-.*jar");
        verifyArtifact(enterpriseArchive, "/citrus-cucumber-.*jar");
        verifyArtifact(enterpriseArchive, "/citrus-zookeeper-.*jar");
        verifyArtifact(enterpriseArchive, "/citrus-rmi-.*jar");
        verifyArtifact(enterpriseArchive, "/citrus-jmx-.*jar");
        verifyArtifact(enterpriseArchive, "/citrus-restdocs-.*jar");
        verifyArtifact(enterpriseArchive, "/citrus-ssh-.*jar");
        verifyArtifact(enterpriseArchive, "/citrus-mail-.*jar");
        verifyArtifact(enterpriseArchive, "/citrus-vertx-.*jar");
        verifyArtifact(enterpriseArchive, "/citrus-java-dsl-.*jar");

        JavaArchive javaArchive = new JavaArchiveImpl(new MemoryMapArchiveImpl(new ConfigurationBuilder().build()));
        archiveProcessor.process(javaArchive, new TestClass(this.getClass()));
        Assert.assertEquals(javaArchive.getContent().size(), 0L);

    }

    @Test
    public void testProcessExplicitCitrusVersion() throws Exception {
        configuration.setCitrusVersion(Citrus.getVersion());

        EnterpriseArchive enterpriseArchive = new EnterpriseArchiveImpl(new MemoryMapArchiveImpl(new ConfigurationBuilder().build()));
        archiveProcessor.process(enterpriseArchive, new TestClass(this.getClass()));
        verifyArtifact(enterpriseArchive, "/citrus-core-.*jar");
        verifyArtifact(enterpriseArchive, "/citrus-jms-.*jar");
        verifyArtifact(enterpriseArchive, "/citrus-kafka-.*jar");
        verifyArtifact(enterpriseArchive, "/citrus-jdbc-.*jar");
        verifyArtifact(enterpriseArchive, "/citrus-http-.*jar");
        verifyArtifact(enterpriseArchive, "/citrus-websocket-.*jar");
        verifyArtifact(enterpriseArchive, "/citrus-ws-.*jar");
        verifyArtifact(enterpriseArchive, "/citrus-ftp-.*jar");
        verifyArtifact(enterpriseArchive, "/citrus-camel-.*jar");
        verifyArtifact(enterpriseArchive, "/citrus-docker-.*jar");
        verifyArtifact(enterpriseArchive, "/citrus-kubernetes-.*jar");
        verifyArtifact(enterpriseArchive, "/citrus-sql-.*jar");
        verifyArtifact(enterpriseArchive, "/citrus-groovy-.*jar");
        verifyArtifact(enterpriseArchive, "/citrus-selenium-.*jar");
        verifyArtifact(enterpriseArchive, "/citrus-cucumber-.*jar");
        verifyArtifact(enterpriseArchive, "/citrus-zookeeper-.*jar");
        verifyArtifact(enterpriseArchive, "/citrus-rmi-.*jar");
        verifyArtifact(enterpriseArchive, "/citrus-jmx-.*jar");
        verifyArtifact(enterpriseArchive, "/citrus-restdocs-.*jar");
        verifyArtifact(enterpriseArchive, "/citrus-ssh-.*jar");
        verifyArtifact(enterpriseArchive, "/citrus-mail-.*jar");
        verifyArtifact(enterpriseArchive, "/citrus-vertx-.*jar");
        verifyArtifact(enterpriseArchive, "/citrus-java-dsl-.*jar");

    }

    @Test
    public void testProcessWebArchive() throws Exception {
        WebArchive webArchive = new WebArchiveImpl(new MemoryMapArchiveImpl(new ConfigurationBuilder().build()));
        archiveProcessor.process(webArchive, new TestClass(this.getClass()));
        verifyArtifact(webArchive, "/WEB-INF/lib/citrus-core-.*jar");
        verifyArtifact(webArchive, "/WEB-INF/lib/citrus-jms-.*jar");
        verifyArtifact(webArchive, "/WEB-INF/lib/citrus-kafka-.*jar");
        verifyArtifact(webArchive, "/WEB-INF/lib/citrus-jdbc-.*jar");
        verifyArtifact(webArchive, "/WEB-INF/lib/citrus-http-.*jar");
        verifyArtifact(webArchive, "/WEB-INF/lib/citrus-websocket-.*jar");
        verifyArtifact(webArchive, "/WEB-INF/lib/citrus-ws-.*jar");
        verifyArtifact(webArchive, "/WEB-INF/lib/citrus-ftp-.*jar");
        verifyArtifact(webArchive, "/WEB-INF/lib/citrus-camel-.*jar");
        verifyArtifact(webArchive, "/WEB-INF/lib/citrus-docker-.*jar");
        verifyArtifact(webArchive, "/WEB-INF/lib/citrus-kubernetes-.*jar");
        verifyArtifact(webArchive, "/WEB-INF/lib/citrus-sql-.*jar");
        verifyArtifact(webArchive, "/WEB-INF/lib/citrus-groovy-.*jar");
        verifyArtifact(webArchive, "/WEB-INF/lib/citrus-selenium-.*jar");
        verifyArtifact(webArchive, "/WEB-INF/lib/citrus-cucumber-.*jar");
        verifyArtifact(webArchive, "/WEB-INF/lib/citrus-zookeeper-.*jar");
        verifyArtifact(webArchive, "/WEB-INF/lib/citrus-rmi-.*jar");
        verifyArtifact(webArchive, "/WEB-INF/lib/citrus-jmx-.*jar");
        verifyArtifact(webArchive, "/WEB-INF/lib/citrus-restdocs-.*jar");
        verifyArtifact(webArchive, "/WEB-INF/lib/citrus-ssh-.*jar");
        verifyArtifact(webArchive, "/WEB-INF/lib/citrus-mail-.*jar");
        verifyArtifact(webArchive, "/WEB-INF/lib/citrus-vertx-.*jar");
        verifyArtifact(webArchive, "/WEB-INF/lib/citrus-java-dsl-.*jar");

    }

    @Test
    public void testProcessNoAutoPackage() throws Exception {
        configuration.setAutoPackage(false);

        EnterpriseArchive enterpriseArchive = new EnterpriseArchiveImpl(new MemoryMapArchiveImpl(new ConfigurationBuilder().build()));
        archiveProcessor.process(enterpriseArchive, new TestClass(this.getClass()));
        Assert.assertEquals(enterpriseArchive.getContent().size(), 0L);
        JavaArchive javaArchive = new JavaArchiveImpl(new MemoryMapArchiveImpl(new ConfigurationBuilder().build()));
        archiveProcessor.process(javaArchive, new TestClass(this.getClass()));
        Assert.assertEquals(javaArchive.getContent().size(), 0L);
        WebArchive webArchive = new WebArchiveImpl(new MemoryMapArchiveImpl(new ConfigurationBuilder().build()));
        archiveProcessor.process(webArchive, new TestClass(this.getClass()));
        Assert.assertEquals(webArchive.getContent().size(), 0L);

    }

    private void verifyArtifact(Archive archive, String expectedFileNamePattern) {
        for (Object path : archive.getContent().keySet()) {
            if (((ArchivePath) path).get().matches(expectedFileNamePattern)) {
                return;
            }
        }

        Assert.fail("Missing artifact resource for file name pattern: " + expectedFileNamePattern);
    }
}
