/*
 * Copyright 2006-2009 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 *  Citrus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Citrus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Citrus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import com.consol.citrus.report.TestListeners;
import com.consol.citrus.report.TestSuiteListeners;
import com.consol.citrus.testng.AbstractBaseTest;

public class TestMetaInfoTest extends AbstractBaseTest {
    
    @Autowired
    TestSuiteListeners testSuiteListeners;
    
    @Autowired
    TestListeners testListeners;
    
    @Test
    public void testExcludeDraftTests() {
        //TODO code this test
    }
    
    @Test
    public void testIncludeDraftTestWhenExecutingSingleTest() {
        //TODO code this test
    }
    
    @Test
    public void testExcludeDraftTestsEvenIfNoTestAtAllAreRun() {
        //TODO code this test
    }
}
