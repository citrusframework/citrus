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

import com.consol.citrus.arquillian.configuration.CitrusConfiguration;
import com.consol.citrus.arquillian.helper.InjectionHelper;
import com.consol.citrus.arquillian.shrinkwrap.CitrusArchiveBuilder;
import org.easymock.EasyMock;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.spec.*;
import org.testng.annotations.Test;

import java.util.Properties;

import static org.easymock.EasyMock.*;

public class CitrusArchiveProcessorTest {

    private CitrusArchiveProcessor archiveProcessor = new CitrusArchiveProcessor();
    private Instance<CitrusConfiguration> configurationInstance = EasyMock.createMock(Instance.class);

    private EnterpriseArchive enterpriseArchive = EasyMock.createMock(EnterpriseArchive.class);
    private JavaArchive javaArchive = EasyMock.createMock(JavaArchive.class);
    private WebArchive webArchive = EasyMock.createMock(WebArchive.class);

    @Test
    public void testProcessEnterpriseArchive() throws Exception {
        CitrusConfiguration configuration = CitrusConfiguration.from(new Properties());

        reset(configurationInstance, enterpriseArchive, javaArchive);

        expect(configurationInstance.get()).andReturn(configuration).anyTimes();
        expect(enterpriseArchive.addAsModules(CitrusArchiveBuilder.latestVersion().all().build())).andReturn(enterpriseArchive).once();

        replay(configurationInstance, enterpriseArchive, javaArchive);

        InjectionHelper.inject(archiveProcessor, "configurationInstance", configurationInstance);

        archiveProcessor.process(enterpriseArchive, new TestClass(this.getClass()));
        archiveProcessor.process(javaArchive, new TestClass(this.getClass()));

        verify(configurationInstance, enterpriseArchive, javaArchive);
    }

    @Test
    public void testProcessExplicitCitrusVersion() throws Exception {
        CitrusConfiguration configuration = CitrusConfiguration.from(new Properties());
        configuration.setCitrusVersion("2.1");

        reset(configurationInstance, enterpriseArchive);

        expect(configurationInstance.get()).andReturn(configuration).anyTimes();
        expect(enterpriseArchive.addAsModules(CitrusArchiveBuilder.version("2.1").all().build())).andReturn(enterpriseArchive).once();

        replay(configurationInstance, enterpriseArchive);

        InjectionHelper.inject(archiveProcessor, "configurationInstance", configurationInstance);

        archiveProcessor.process(enterpriseArchive, new TestClass(this.getClass()));

        verify(configurationInstance, enterpriseArchive);
    }

    @Test
    public void testProcessWebArchive() throws Exception {
        CitrusConfiguration configuration = CitrusConfiguration.from(new Properties());

        reset(configurationInstance, webArchive);

        expect(configurationInstance.get()).andReturn(configuration).anyTimes();
        expect(webArchive.addAsLibraries(CitrusArchiveBuilder.latestVersion().all().build())).andReturn(webArchive).once();

        replay(configurationInstance, webArchive);

        InjectionHelper.inject(archiveProcessor, "configurationInstance", configurationInstance);

        archiveProcessor.process(webArchive, new TestClass(this.getClass()));

        verify(configurationInstance, webArchive);
    }

    @Test
    public void testProcessNoAutoPackage() throws Exception {
        CitrusConfiguration configuration = CitrusConfiguration.from(new Properties());
        configuration.setAutoPackage(false);

        reset(configurationInstance, enterpriseArchive, javaArchive, webArchive);
        expect(configurationInstance.get()).andReturn(configuration).anyTimes();
        replay(configurationInstance, enterpriseArchive, javaArchive, webArchive);

        InjectionHelper.inject(archiveProcessor, "configurationInstance", configurationInstance);

        archiveProcessor.process(enterpriseArchive, new TestClass(this.getClass()));
        archiveProcessor.process(javaArchive, new TestClass(this.getClass()));
        archiveProcessor.process(webArchive, new TestClass(this.getClass()));

        verify(configurationInstance, enterpriseArchive, javaArchive,  webArchive);
    }
}