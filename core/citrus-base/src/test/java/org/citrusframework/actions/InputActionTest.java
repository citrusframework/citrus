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

package org.citrusframework.actions;

import java.io.BufferedReader;
import java.io.IOException;

import org.citrusframework.UnitTestSupport;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;


/**
 * @author Christoph Deppisch
 */
public class InputActionTest extends UnitTestSupport {

    private BufferedReader inputReader = Mockito.mock(BufferedReader.class);

    @Test
    public void testInput() throws IOException {
        reset(inputReader);

        when(inputReader.readLine()).thenReturn("yes");

        InputAction input = new InputAction.Builder()
                .reader(inputReader)
                .message("Is that correct?")
                .build();

        input.execute(context);

        Assert.assertEquals(context.getVariable("userinput"), "yes");

    }

    @Test
	public void testExistingInputVariable() {
        reset(inputReader);

	    context.setVariable("userinput", "yes");

		InputAction input = new InputAction.Builder()
                .reader(inputReader)
		        .message("Is that correct?")
                .build();

		input.execute(context);

        Assert.assertEquals(context.getVariable("userinput"), "yes");

	}

	@Test
    public void testValidAnswers() throws IOException {
        reset(inputReader);

        when(inputReader.readLine()).thenReturn("i dont know");
        when(inputReader.readLine()).thenReturn("no");

        InputAction input = new InputAction.Builder()
                .reader(inputReader)
                .message("Is that correct?")
                .answers("yes/no")
                .build();

        input.execute(context);

        Assert.assertEquals(context.getVariable("userinput"), "no");

    }
}
