/*
 * Copyright 2006-2010 the original author or authors.
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

package com.consol.citrus.validation.script;

import org.springframework.core.io.ClassPathResource;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * @author Christoph Deppisch
 */
public class TemplateBasedScriptBuilderTest {

    @Test
    public void testTemplateScriptData() {
        Assert.assertEquals(TemplateBasedScriptBuilder.fromTemplateScript("+++HEAD+++@SCRIPTBODY@+++TAIL+++")
                .withCode("BODY")
                .build(), "+++HEAD+++BODY+++TAIL+++");
    }
    
    @Test
    public void testTemplateScriptResource() {
        Assert.assertEquals(TemplateBasedScriptBuilder.fromTemplateResource(
                new ClassPathResource("com/consol/citrus/validation/script/script-template.groovy"))
                .withCode("BODY")
                .build(), "+++HEAD+++\n\nBODY\n\n+++TAIL+++");
    }
    
    @Test
    public void testMissingScriptBody() {
        Assert.assertEquals(TemplateBasedScriptBuilder.fromTemplateScript("+++HEAD+++@SCRIPTBODY@+++TAIL+++")
                .build(), "+++HEAD++++++TAIL+++");
    }
    
    @Test
    public void testInvalidScriptTemplate() {
        try {
            TemplateBasedScriptBuilder.fromTemplateScript("+++HEAD++++++TAIL+++")
                    .withCode("BODY")
                    .build();
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("@SCRIPTBODY@"));
            return;
        }
        
        Assert.fail("Missing error due to invalid script template");
    }
}
