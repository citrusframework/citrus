/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.log;

import org.citrusframework.CitrusSettings;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class DefaultLogModifierTest {

    @Test
    public void testMaskKeyValue() {
        Assert.assertTrue(CitrusSettings.isLogModifierEnabled());

        DefaultLogModifier logModifier = new DefaultLogModifier();

        Assert.assertEquals(logModifier.mask("password=foo"), "password=****");
        Assert.assertEquals(logModifier.mask("password=foo bar"), "password=****");
        Assert.assertEquals(logModifier.mask("password=!@#$%^*() -+[]{};:"), "password=****");
        Assert.assertEquals(logModifier.mask("password = foo"), "password = ****");
        Assert.assertEquals(logModifier.mask("password = foo  bar"), "password = ****");
        Assert.assertEquals(logModifier.mask("password=\"foo\""), "password=\"****\"");
        Assert.assertEquals(logModifier.mask("password=\"\""), "password=\"\"");
        Assert.assertEquals(logModifier.mask("password=\"!@#$%^*() -+[]{};:\""), "password=\"****\"");
        Assert.assertEquals(logModifier.mask("password='foo'"), "password='****'");
        Assert.assertEquals(logModifier.mask("password='foo bar'"), "password='****'");
        Assert.assertEquals(logModifier.mask("password=''"), "password=''");
        Assert.assertEquals(logModifier.mask("password='!@#$%^*() -+[]{};:'"), "password='****'");

        Assert.assertEquals(logModifier.mask("secret=foo"), "secret=****");
        Assert.assertEquals(logModifier.mask("secretKey=foo"), "secretKey=****");

        Assert.assertEquals(logModifier.mask("password=foo,secret=foo,secretKey=foo"), "password=****,secret=****,secretKey=****");
        Assert.assertEquals(logModifier.mask("password=\"foo\",secret='foo',secretKey=\"foo\""), "password=\"****\",secret='****',secretKey=\"****\"");
        Assert.assertEquals(logModifier.mask("password=foo, secret=foo, secretKey=foo"), "password=****, secret=****, secretKey=****");
        Assert.assertEquals(logModifier.mask("password=\"foo\", secret='foo', secretKey=\"foo\""), "password=\"****\", secret='****', secretKey=\"****\"");
        Assert.assertEquals(logModifier.mask("a=foo, secret=foo, b=foo"), "a=foo, secret=****, b=foo");
    }

    @Test
    public void testMaskFormUrlEncoded() {
        Assert.assertTrue(CitrusSettings.isLogModifierEnabled());

        DefaultLogModifier logModifier = new DefaultLogModifier();

        Assert.assertEquals(logModifier.mask("password=foo&secret=bar&foo=bar"), "password=****&secret=****&foo=bar");
        Assert.assertEquals(logModifier.mask("password=foo bar&secret=bar foo&foo=bar"), "password=****&secret=****&foo=bar");
        Assert.assertEquals(logModifier.mask("password=&secret=&foo="), "password=****&secret=****&foo=");
        Assert.assertEquals(logModifier.mask("password=!@#$%^*() -+[]{};:&secret=!@#$%^*() -+[]{};:&foo=bar"), "password=****&secret=****&foo=bar");
    }

    @Test
    public void testMaskXml() {
        Assert.assertTrue(CitrusSettings.isLogModifierEnabled());

        DefaultLogModifier logModifier = new DefaultLogModifier();

        Assert.assertEquals(logModifier.mask("<password></password>"), "<password>****</password>");
        Assert.assertEquals(logModifier.mask("<password>foo</password>"), "<password>****</password>");
        Assert.assertEquals(logModifier.mask("<password>foo bar</password>"), "<password>****</password>");
        Assert.assertEquals(logModifier.mask("<password>!@#$%^&*() -+[]{};:</password>"), "<password>****</password>");
        Assert.assertEquals(logModifier.mask("<element password=\"foo\"></element>"), "<element password=\"****\"></element>");
        Assert.assertEquals(logModifier.mask("<secret password=\"foo\"/>"), "<secret password=\"****\"/>");
        Assert.assertEquals(logModifier.mask("<secret password=\"!@#$%^&*() -+[]{};:\"/>"), "<secret password=\"****\"/>");

        Assert.assertEquals(logModifier.mask("<secret>foo</secret>"), "<secret>****</secret>");
        Assert.assertEquals(logModifier.mask("<secretKey>foo</secretKey>"), "<secretKey>****</secretKey>");

        Assert.assertEquals(logModifier.mask("<password>foo</password><secret>foo</secret><secretKey>foo</secretKey>"),
                "<password>****</password><secret>****</secret><secretKey>****</secretKey>");
        Assert.assertEquals(logModifier.mask("<a>foo</a><secret>foo</secret><b>foo</b>"),
                "<a>foo</a><secret>****</secret><b>foo</b>");
    }

    @Test
    public void testMaskJson() {
        Assert.assertTrue(CitrusSettings.isLogModifierEnabled());

        DefaultLogModifier logModifier = new DefaultLogModifier();

        Assert.assertEquals(logModifier.mask("{\"password\":\"foo\"}"), "{\"password\":\"****\"}");
        Assert.assertEquals(logModifier.mask("{\"password\":\"foo bar\"}"), "{\"password\":\"****\"}");
        Assert.assertEquals(logModifier.mask("{\"password\":\"\"}"), "{\"password\":\"****\"}");
        Assert.assertEquals(logModifier.mask("{\"password\":\"!@#$%^&*() -+[]{};:\"}"), "{\"password\":\"****\"}");

        Assert.assertEquals(logModifier.mask("{\"secret\":\"foo\"}"), "{\"secret\":\"****\"}");
        Assert.assertEquals(logModifier.mask("{\"secretKey\":\"foo\"}"), "{\"secretKey\":\"****\"}");

        Assert.assertEquals(logModifier.mask("{\"password\": \"foo\", \"secret\": \"foo\", \"secretKey\": \"foo\"}"),
                "{\"password\": \"****\", \"secret\": \"****\", \"secretKey\": \"****\"}");
        Assert.assertEquals(logModifier.mask("{\"a\": \"foo\", \"b\": \"foo\", \"secretKey\": \"foo\"}"),
                "{\"a\": \"foo\", \"b\": \"foo\", \"secretKey\": \"****\"}");
    }
}
