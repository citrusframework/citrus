/*
 * Copyright the original author or authors.
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

package org.citrusframework.log;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DefaultLogModifierTest {

    @Test
    public void testMaskKeyValue() {
        Assertions.assertTrue(CitrusLogSettings.isLogModifierEnabled());

        DefaultLogModifier logModifier = new DefaultLogModifier();

        Assertions.assertEquals("password=****", logModifier.mask("password=foo"));
        Assertions.assertEquals("PASSWORD=****", logModifier.mask("PASSWORD=foo"));
        Assertions.assertEquals("SERVICE_PASSWORD=****", logModifier.mask("SERVICE_PASSWORD=foo"));
        Assertions.assertEquals("password=****", logModifier.mask("password=foo bar"));
        Assertions.assertEquals("password=****", logModifier.mask("password=!@#$%^*() -+[]{}:;"));
        Assertions.assertEquals("password = ****", logModifier.mask("password = foo"));
        Assertions.assertEquals("password = ****", logModifier.mask("password = foo  bar"));
        Assertions.assertEquals("password=\"****\"", logModifier.mask("password=\"foo\""));
        Assertions.assertEquals("PASSWORD=\"****\"", logModifier.mask("PASSWORD=\"foo\""));
        Assertions.assertEquals("SERVICE_PASSWORD=\"****\"", logModifier.mask("SERVICE_PASSWORD=\"foo\""));
        Assertions.assertEquals("password=\"\"", logModifier.mask("password=\"\""));
        Assertions.assertEquals("password=\"****\"", logModifier.mask("password=\"!@#$%^*() -+[]{};:\""));
        Assertions.assertEquals("password='****'", logModifier.mask("password='foo'"));
        Assertions.assertEquals("password='****'", logModifier.mask("password='foo bar'"));
        Assertions.assertEquals("password=''", logModifier.mask("password=''"));
        Assertions.assertEquals("password='****'", logModifier.mask("password='!@#$%^*() -+[]{};:'"));

        Assertions.assertEquals("secret=****", logModifier.mask("secret=foo"));
        Assertions.assertEquals("secretKey=****", logModifier.mask("secretKey=foo"));

        Assertions.assertEquals("password=****,secret=****,secretKey=****", logModifier.mask("password=foo,secret=foo,secretKey=foo"));
        Assertions.assertEquals("password=\"****\",secret='****',secretKey=\"****\"", logModifier.mask("password=\"foo\",secret='foo',secretKey=\"foo\""));
        Assertions.assertEquals("password=****, secret=****, secretKey=****", logModifier.mask("password=foo, secret=foo, secretKey=foo"));
        Assertions.assertEquals("password=\"****\", secret='****', secretKey=\"****\"", logModifier.mask("password=\"foo\", secret='foo', secretKey=\"foo\""));
        Assertions.assertEquals("a=foo, secret=****, b=foo", logModifier.mask("a=foo, secret=foo, b=foo"));
    }

    @Test
    public void testMaskFormUrlEncoded() {
        Assertions.assertTrue(CitrusLogSettings.isLogModifierEnabled());

        DefaultLogModifier logModifier = new DefaultLogModifier();

        Assertions.assertEquals("password=****&secret=****&foo=bar", logModifier.mask("password=foo&secret=bar&foo=bar"));
        Assertions.assertEquals("password=****&secret=****&foo=bar", logModifier.mask("password=foo bar&secret=bar foo&foo=bar"));
        Assertions.assertEquals("password=****&secret=****&foo=", logModifier.mask("password=&secret=&foo="));
        Assertions.assertEquals("password=****&secret=****&foo=bar", logModifier.mask("password=!@#$%^*() -+[]{};:&secret=!@#$%^*() -+[]{};:&foo=bar"));
    }

    @Test
    public void testMaskXml() {
        Assertions.assertTrue(CitrusLogSettings.isLogModifierEnabled());

        DefaultLogModifier logModifier = new DefaultLogModifier();

        Assertions.assertEquals("<password>****</password>", logModifier.mask("<password></password>"));
        Assertions.assertEquals("<password>****</password>", logModifier.mask("<password>foo</password>"));
        Assertions.assertEquals("<password>****</password>", logModifier.mask("<password>foo bar</password>"));
        Assertions.assertEquals("<password>****</password>", logModifier.mask("<password>!@#$%^&*() -+[]{};:</password>"));
        Assertions.assertEquals("<element password=\"****\"></element>", logModifier.mask("<element password=\"foo\"></element>"));
        Assertions.assertEquals("<secret password=\"****\"/>", logModifier.mask("<secret password=\"foo\"/>"));
        Assertions.assertEquals("<secret password=\"****\"/>", logModifier.mask("<secret password=\"!@#$%^&*() -+[]{};:\"/>"));

        Assertions.assertEquals("<secret>****</secret>", logModifier.mask("<secret>foo</secret>"));
        Assertions.assertEquals("<secretKey>****</secretKey>", logModifier.mask("<secretKey>foo</secretKey>"));

        Assertions.assertEquals("<password>****</password><secret>****</secret><secretKey>****</secretKey>",
                logModifier.mask("<password>foo</password><secret>foo</secret><secretKey>foo</secretKey>"));
        Assertions.assertEquals("<a>foo</a><secret>****</secret><b>foo</b>",
                logModifier.mask("<a>foo</a><secret>foo</secret><b>foo</b>"));
    }

    @Test
    public void testMaskJson() {
        Assertions.assertTrue(CitrusLogSettings.isLogModifierEnabled());

        DefaultLogModifier logModifier = new DefaultLogModifier();

        Assertions.assertEquals("{\"password\":\"****\"}", logModifier.mask("{\"password\":\"foo\"}"));
        Assertions.assertEquals("{\"password\":\"****\"}", logModifier.mask("{\"password\":\"foo bar\"}"));
        Assertions.assertEquals("{\"password\":\"****\"}", logModifier.mask("{\"password\":\"\"}"));
        Assertions.assertEquals("{\"password\":\"****\"}", logModifier.mask("{\"password\":\"!@#$%^&*() -+[]{};:\"}"));

        Assertions.assertEquals("{\"secret\":\"****\"}", logModifier.mask("{\"secret\":\"foo\"}"));
        Assertions.assertEquals("{\"secretKey\":\"****\"}", logModifier.mask("{\"secretKey\":\"foo\"}"));

        Assertions.assertEquals("{\"password\": \"****\", \"secret\": \"****\", \"secretKey\": \"****\"}",
                logModifier.mask("{\"password\": \"foo\", \"secret\": \"foo\", \"secretKey\": \"foo\"}"));
        Assertions.assertEquals("{\"a\": \"foo\", \"b\": \"foo\", \"secretKey\": \"****\"}",
                logModifier.mask("{\"a\": \"foo\", \"b\": \"foo\", \"secretKey\": \"foo\"}"));
    }

    @Test
    public void testMaskYaml() {
        Assertions.assertTrue(CitrusLogSettings.isLogModifierEnabled());

        DefaultLogModifier logModifier = new DefaultLogModifier();

        Assertions.assertEquals("- password: '****'", logModifier.mask("- password: 'foo'"));
        Assertions.assertEquals("- password: '****'", logModifier.mask("- password: 'foo bar'"));
        Assertions.assertEquals("- password: ****", logModifier.mask("- password: {}"));
        Assertions.assertEquals("- password: '****'", logModifier.mask("- password: '!@#$%^&*() -+[]{};:'"));

        Assertions.assertEquals("- password: \"****\"", logModifier.mask("- password: \"foo\""));
        Assertions.assertEquals("- password: \"****\"", logModifier.mask("- password: \"foo bar\""));
        Assertions.assertEquals("- password: \"****\"", logModifier.mask("- password: \"!@#$%^&*() -+[]{};:\""));

        Assertions.assertEquals("- secret: '****'", logModifier.mask("- secret: 'foo'"));
        Assertions.assertEquals("- secretKey: '****'", logModifier.mask("- secretKey: 'foo'"));

        Assertions.assertEquals("- secret: \"****\"", logModifier.mask("- secret: \"foo\""));
        Assertions.assertEquals("- secretKey: \"****\"", logModifier.mask("- secretKey: \"foo\""));

        Assertions.assertEquals("""
            - password: '****'
              secret: '****'
              secretKey: '****'
            """,
            logModifier.mask("""
            - password: 'foo'
              secret: 'foo'
              secretKey: 'foo'
            """));
        Assertions.assertEquals("""
            - password: "****"
              secret: "****"
              secretKey: "****"
            """,
            logModifier.mask("""
            - password: "foo"
              secret: "foo"
              secretKey: "foo"
            """));

        Assertions.assertEquals("""
            - nested:
                password: '****'
                secret: '****'
                secretKey: '****'
            """,
            logModifier.mask("""
            - nested:
                password: 'foo'
                secret: 'foo'
                secretKey: 'foo'
            """));
        Assertions.assertEquals("""
            - nested:
                password: "****"
                secret: "****"
                secretKey: "****"
            """,
            logModifier.mask("""
            - nested:
                password: "foo"
                secret: "foo"
                secretKey: "foo"
            """));

        Assertions.assertEquals("""
            - a: 'foo'
              b: 'foo'
              secretKey: '****'
            """,
            logModifier.mask("""
            - a: 'foo'
              b: 'foo'
              secretKey: 'foo'
            """));
    }
}
