/*
 * Copyright 2006-2013 the original author or authors.
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

package com.consol.citrus.admin.handlebars;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.helper.BlockHelper;
import com.github.jknack.handlebars.helper.StringHelpers;
import com.github.jknack.handlebars.springmvc.HandlebarsViewResolver;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.*;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 */
public class HandlebarsHelperPostProcessorTest {

    @Test
    public void testPostProcess() throws Exception {
        HandlebarsHelperPostProcessor postProcessor = new HandlebarsHelperPostProcessor();

        Map<String, Helper<?>> helpers = new HashMap<String, Helper<?>>();
        HelperMock helperMock = new HelperMock();
        helpers.put("foo", helperMock);
        postProcessor.setHelpers(helpers);

        List<Object> helperSources = new ArrayList<Object>();
        HelperSourceMock helperSourceMock = new HelperSourceMock();
        helperSources.add(helperSourceMock);
        postProcessor.setHelperSources(helperSources);

        HandlebarsViewResolver viewResolver = EasyMock.createMock(HandlebarsViewResolver.class);

        Object otherBean = EasyMock.createMock(Object.class);

        reset(viewResolver, otherBean);

        expect(viewResolver.registerHelper("foo", helperMock)).andReturn(viewResolver).once();
        expect(viewResolver.registerHelpers(helperSourceMock)).andReturn(viewResolver).once();

        replay(viewResolver, otherBean);

        postProcessor.postProcessBeforeInitialization(viewResolver, "handlebarsViewResolver");
        postProcessor.postProcessBeforeInitialization(otherBean, "otherBean");
        postProcessor.postProcessAfterInitialization(viewResolver, "handlebarsViewResolver");
        postProcessor.postProcessAfterInitialization(otherBean, "otherBean");

        verify(viewResolver, otherBean);
    }

    /** Helper source mock implementation */
    private static class HelperSourceMock {
        public String foo(String context) {
            return context;
        }
    }

    /** Helper mock implementation */
    private static class HelperMock implements Helper<String> {
        @Override
        public CharSequence apply(String context, Options options) throws IOException {
            return "foo";
        }
    }
}
