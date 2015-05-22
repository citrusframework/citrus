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

package com.consol.citrus.arquillian.configuration;

import com.consol.citrus.arquillian.CitrusExtensionConstants;
import org.easymock.EasyMock;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.descriptor.api.ExtensionDef;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

import static org.easymock.EasyMock.*;

public class CitrusConfigurationTest {

    private ExtensionDef extension = EasyMock.createMock(ExtensionDef.class);
    private ArquillianDescriptor descriptor = EasyMock.createMock(ArquillianDescriptor.class);

    @Test
    public void testFromArchiveDescriptorMissing() throws Exception {
        reset(descriptor, extension);

        expect(descriptor.getExtensions()).andReturn(Collections.singletonList(extension)).once();
        expect(extension.getExtensionName()).andReturn("otherExtension").once();

        replay(descriptor, extension);

        CitrusConfiguration configuration = CitrusConfiguration.from(descriptor);

        Assert.assertNull(configuration.getCitrusVersion());
        Assert.assertTrue(configuration.isAutoPackage());

        verify(descriptor, extension);
    }

    @Test
    public void testFromArchiveDescriptor() throws Exception {
        Map<String, String> properties = new HashMap<>();
        properties.put("citrusVersion", "0.1");
        properties.put("autoPackage", "false");
        properties.put("suiteName", "testsuite");

        reset(descriptor, extension);

        expect(descriptor.getExtensions()).andReturn(Collections.singletonList(extension)).once();
        expect(extension.getExtensionName()).andReturn(CitrusExtensionConstants.CITRUS_EXTENSION_QUALIFIER).once();
        expect(extension.getExtensionProperties()).andReturn(properties).once();

        replay(descriptor, extension);

        CitrusConfiguration configuration = CitrusConfiguration.from(descriptor);

        Assert.assertEquals(configuration.getCitrusVersion(), "0.1");
        Assert.assertFalse(configuration.isAutoPackage());
        Assert.assertEquals(configuration.getSuiteName(), "testsuite");

        verify(descriptor, extension);
    }
}