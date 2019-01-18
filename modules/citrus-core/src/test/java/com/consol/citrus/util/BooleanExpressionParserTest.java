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

package com.consol.citrus.util;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * @author Christoph Deppisch
 */
public class BooleanExpressionParserTest {
    
    @Test
    public void testExpressionParser() {
        Assert.assertTrue(BooleanExpressionParser.evaluate("1 = 1"));
        Assert.assertFalse(BooleanExpressionParser.evaluate("1 = 2"));
        Assert.assertTrue(BooleanExpressionParser.evaluate("1 lt 2"));
        Assert.assertFalse(BooleanExpressionParser.evaluate("2 lt 1"));
        Assert.assertTrue(BooleanExpressionParser.evaluate("2 gt 1"));
        Assert.assertFalse(BooleanExpressionParser.evaluate("1 gt 2"));
        Assert.assertTrue(BooleanExpressionParser.evaluate("2 lt= 2"));
        Assert.assertTrue(BooleanExpressionParser.evaluate("2 gt= 2"));
        Assert.assertFalse(BooleanExpressionParser.evaluate("2 lt= 1"));
        Assert.assertFalse(BooleanExpressionParser.evaluate("2 gt= 3"));
        Assert.assertTrue(BooleanExpressionParser.evaluate("(1 = 1)"));
        Assert.assertTrue(BooleanExpressionParser.evaluate("(1 = 1) and (2 = 2)"));
        Assert.assertTrue(BooleanExpressionParser.evaluate("(1 lt= 1) and (2 gt= 2)"));
        Assert.assertTrue(BooleanExpressionParser.evaluate("(1 gt 2) or (2 = 2)"));
        Assert.assertTrue(BooleanExpressionParser.evaluate("((1 = 5) and (2 = 6)) or (2 gt 1)"));
        Assert.assertFalse(BooleanExpressionParser.evaluate("(1 = 2)"));
        Assert.assertFalse(BooleanExpressionParser.evaluate("(1 = 1) and (2 = 3)"));
        Assert.assertFalse(BooleanExpressionParser.evaluate("(1 lt 1) and (2 gt 2)"));
        Assert.assertFalse(BooleanExpressionParser.evaluate("(1 gt 2) or (2 = 3)"));
        Assert.assertFalse(BooleanExpressionParser.evaluate("((1 = 5) and (2 = 6)) or (2 lt 1)"));
    }

    @Test
    public void testExpressionParserWithStringValues() {
        Assert.assertTrue(BooleanExpressionParser.evaluate("true"));
        Assert.assertTrue(BooleanExpressionParser.evaluate("true = true"));
        Assert.assertTrue(BooleanExpressionParser.evaluate("false = false"));
        Assert.assertFalse(BooleanExpressionParser.evaluate("false"));
        Assert.assertFalse(BooleanExpressionParser.evaluate("true = false"));
        Assert.assertFalse(BooleanExpressionParser.evaluate("false = true"));
        Assert.assertTrue(BooleanExpressionParser.evaluate("( false = false ) and ( true = true )"));
        Assert.assertFalse(BooleanExpressionParser.evaluate("( false = false ) and ( true = false )"));
        Assert.assertTrue(BooleanExpressionParser.evaluate("(false = false) and (true = true)"));
        Assert.assertFalse(BooleanExpressionParser.evaluate("(false = false) and (true = false)"));
        Assert.assertTrue(BooleanExpressionParser.evaluate("(   false = false) and (true = true    )"));
        Assert.assertFalse(BooleanExpressionParser.evaluate("(false = false    ) and     (    true = false)"));
        Assert.assertTrue(BooleanExpressionParser.evaluate("( true = false ) or ( false = false )"));
        Assert.assertTrue(BooleanExpressionParser.evaluate("(false = false) or (true = true)"));
        Assert.assertTrue(BooleanExpressionParser.evaluate("(false = false) or (true = false)"));
        Assert.assertTrue(BooleanExpressionParser.evaluate("(false = false    ) or (    true = false)"));
    }
    
    @Test
    public void testExpressionParserWithUnknownOperator() {
        try {
            BooleanExpressionParser.evaluate("wahr");
        } catch(CitrusRuntimeException e) {
            Assert.assertEquals(e.getLocalizedMessage(), "Unknown operator 'wahr'");
            return;
        }
        
        Assert.fail("Missing " + CitrusRuntimeException.class + " because of unknown operator");
    }
    
    @Test
    public void testExpressionParserWithBrokenExpression() {
        try {
            BooleanExpressionParser.evaluate("1 = ");
        } catch(CitrusRuntimeException e) {
            Assert.assertEquals(e.getLocalizedMessage(), "Unable to parse boolean expression '1 = '. Maybe expression is incomplete!");
            return;
        }
        
        Assert.fail("Missing " + CitrusRuntimeException.class + " because of broken expression");
    }
}
