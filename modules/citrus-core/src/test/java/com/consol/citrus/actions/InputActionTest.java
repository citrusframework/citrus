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

package com.consol.citrus.actions;

import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.IOException;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 */
public class InputActionTest extends AbstractTestNGUnitTest {

    private BufferedReader inputReader = EasyMock.createMock(BufferedReader.class);

    @Test
    public void testInput() throws IOException {
        reset(inputReader);

        expect(inputReader.readLine()).andReturn("yes").once();

        replay(inputReader);

        InputAction input = initializeInputAction();
        input.setMessage("Is that correct?");

        input.execute(context);

        Assert.assertEquals(context.getVariable("userinput"), "yes");

        verify(inputReader);
    }

    @Test
	public void testExistingInputVariable() {
        reset(inputReader);

        replay(inputReader);

	    context.setVariable("userinput", "yes");
	    
		InputAction input = initializeInputAction();
		input.setMessage("Is that correct?");
		
		input.execute(context);

        Assert.assertEquals(context.getVariable("userinput"), "yes");

        verify(inputReader);
	}
	
	@Test
    public void testValidAnswers() throws IOException {
        reset(inputReader);

        expect(inputReader.readLine()).andReturn("i dont know").once();
        expect(inputReader.readLine()).andReturn("no").once();

        replay(inputReader);

        InputAction input = initializeInputAction();
        input.setValidAnswers("yes/no");
        input.setMessage("Is that correct?");

        input.execute(context);

        Assert.assertEquals(context.getVariable("userinput"), "no");

        verify(inputReader);
    }

    private InputAction initializeInputAction() {
        return new InputAction() {
            @Override
            protected BufferedReader getInputReader() {
                // returning reader mock instead of system input reader
                return inputReader;
            }
        };
    }
}
