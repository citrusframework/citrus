/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.samples.flightbooking;

import org.testng.ITestContext;
import org.testng.annotations.*;

import com.consol.citrus.testng.AbstractTestNGCitrusTest;

/**
 * @author deppisch Christoph Deppisch Consol* Software GmbH
 * @since 03.01.2010
 */
public class FlightBookingTest extends AbstractTestNGCitrusTest {
    
    FlightBookingDemo demo = new FlightBookingDemo();
    
    @BeforeTest
    public void init() {
        demo.start();
    }
    
    @Test
    public void flightBookingTest(ITestContext testContext) {
        executeTest(testContext);
    }
    
    @AfterTest
    public void destroy() {
        demo.stop();
    }
}
