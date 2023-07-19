/*
 * Copyright 2022 the original author or authors.
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

package org.citrusframework.groovy.dsl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.citrusframework.Citrus;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.context.TestContext;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;

/**
 * @author Christoph Deppisch
 */
public class GroovyShellUtils {

    private static final Pattern COMMENTS = Pattern.compile("^(?:\\s*//|/\\*|\\s+\\*).*$", Pattern.MULTILINE);

    private GroovyShellUtils() {
        // prevent instantiation of utility class
    }

    /**
     * Run given scriptCode with GroovyShell.
     * @param ic import customizer
     * @param scriptCode code to evaluate in shell
     * @param context the current test context
     * @param <T> return type
     * @return script result
     */
    public static <T> T run(ImportCustomizer ic, String scriptCode, Citrus citrus, TestContext context) {
        return run(ic, null, scriptCode, citrus, context);
    }

    /**
     * Run given scriptCode with GroovyShell and delegate execution to given instance.
     * @param delegate instance providing methods and properties
     * @param scriptCode code to evaluate in shell
     * @param context the current test context
     * @param <T> return type
     * @return script result
     */
    public static <T> T run(Object delegate, String scriptCode, Citrus citrus, TestContext context) {
        return run(new ImportCustomizer(), delegate, scriptCode, citrus, context);
    }

    /**
     * Run given scriptCode with GroovyShell and delegate execution to given instance.
     * @param ic import customizer
     * @param delegate instance providing methods and properties
     * @param scriptCode code to evaluate in shell
     * @param context the current test context
     * @param <T> return type
     * @return script result
     */
    public static <T> T run(ImportCustomizer ic, Object delegate, String scriptCode, Citrus citrus, TestContext context) {
        CompilerConfiguration cc = new CompilerConfiguration();
        cc.addCompilationCustomizers(ic);
        cc.setScriptBaseClass(GroovyScript.class.getName());

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        GroovyShell sh = new GroovyShell(cl, new Binding(), cc);

        Script script = sh.parse(scriptCode);

        if (script instanceof GroovyScript) {
            if (delegate != null) {
                // set the delegate target
                ((GroovyScript) script).setDelegate(delegate);
            }

            ((GroovyScript) script).setCitrusFramework(citrus);
            ((GroovyScript) script).setContext(context);
        }

        return (T) script.run();
    }

    /**
     * Remove leading comments such as license header.
     * @param script
     * @return
     */
    public static String removeComments(String script) {
        Matcher matcher = COMMENTS.matcher(script);
        if (matcher.find()) {
            return matcher.replaceAll("").trim();
        } else {
            return script.trim();
        }
    }

    /**
     * Automatically adds static imports for TestAction builders used in given script.
     * @param source the script source code.
     * @param ic the import customizer.
     */
    public static void autoAddImports(String source, ImportCustomizer ic) {
        TestActionBuilder.lookup()
                .entrySet()
                .stream()
                .filter(entry -> !entry.getKey().equals("send") && !entry.getKey().equals("receive") )
                .filter(entry -> !source.contains("import static " + String.format("%s.%s", entry.getValue().getClass().getCanonicalName(), entry.getKey())))
                .filter(entry -> source.contains(String.format("$(%s(", entry.getKey())) || source.contains(String.format("%s()", entry.getKey())))
                .peek(entry -> System.out.println(entry.getKey()))
                .forEach(entry -> ic.addStaticImport(entry.getValue().getClass().getCanonicalName(), entry.getKey()));
    }
}
