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

package com.consol.citrus.telnet.integration;

import com.consol.citrus.annotations.CitrusXmlTest;
import com.consol.citrus.testng.AbstractTestNGCitrusTest;

import java.io.IOException;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import com.consol.citrus.telnet.server.*;

/**
 * @author Michael Wurmbrand
 * @since 2.6
 */
@Test
public class TelnetEndpointIT extends AbstractTestNGCitrusTest {

	TelnetSimpleServer telnetServer;
	
    @BeforeSuite
    public void setup() {
    
			try {
				 telnetServer = new TelnetSimpleServer(4011);
			} catch (IOException e) {
				e.printStackTrace();
			}
	
    }
    
    @CitrusXmlTest(name = "TelnetEndpointIT")
    public void testTelnetClient() {
    	telnetServer.run();
    }
}
