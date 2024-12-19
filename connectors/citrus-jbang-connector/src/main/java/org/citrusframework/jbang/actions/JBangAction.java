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

package org.citrusframework.jbang.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.citrusframework.AbstractTestActionBuilder;
import org.citrusframework.actions.AbstractTestAction;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.jbang.JBangSupport;
import org.citrusframework.jbang.ProcessAndOutput;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;
import org.citrusframework.validation.ValidationProcessor;
import org.citrusframework.validation.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action runs scripts with JBang. Spawns a new process with the JBang executable.
 * Arguments and system properties are provided and the process exit code and output may be validated.
 */
public class JBangAction extends AbstractTestAction {

    private final String app;
    private final String scriptOrFile;
    private final List<String> args;
    private final Map<String, String> systemProperties;
    private final int[] exitCodes;
    private final String pidVar;
    private final String outputVar;
    private final String verifyOutput;
    private final ValidationProcessor validationProcessor;
    private final boolean printOutput;

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(JBangAction.class);

    public JBangAction(Builder builder) {
        super("jbang", builder);

        this.app = builder.app;
        this.scriptOrFile = builder.scriptOrFile;
        this.args = builder.args;
        this.systemProperties = builder.systemProperties;
        this.exitCodes = builder.exitCodes;
        this.pidVar = builder.pidVar;
        this.outputVar = builder.outputVar;
        this.verifyOutput = builder.verifyOutput;
        this.validationProcessor = builder.validationProcessor;
        this.printOutput = builder.printOutput;
    }

    @Override
    public void doExecute(TestContext context) {
        String scriptName = FileUtils.getFileName(context.replaceDynamicContentInString(scriptOrFile));
        logger.info("Running JBang script '%s'".formatted(scriptName));

        ProcessAndOutput result = JBangSupport.jbang()
                .app(app)
                .withSystemProperties(systemProperties)
                .run(context.replaceDynamicContentInString(scriptOrFile), context.resolveDynamicValuesInList(args));

        result.setApp(Objects.requireNonNullElse(app, scriptName));

        if (printOutput) {
            logger.info("JBang script '%s' output:".formatted(scriptName));
            logger.info(result.getOutput());
        }

        if (pidVar != null) {
            context.setVariable(pidVar, result.getProcessId());
        }

        int exitValue = result.getProcess().exitValue();
        if (Arrays.stream(exitCodes).noneMatch(exit -> exit == exitValue)) {
            if (exitCodes.length == 1) {
                throw new ValidationException(("Error while running JBang script or file. " +
                        "Expected exit code %s, but was %d").formatted(exitCodes[0], exitValue));
            } else {
                throw new ValidationException(("Error while running JBang script or file. " +
                        "Expected one of exit codes %s, but was %d").formatted(Arrays.toString(exitCodes), exitValue));
            }
        }

        if (validationProcessor != null) {
            validationProcessor.validate(new DefaultMessage(result.getOutput().trim())
                    .setHeader("pid", result.getProcessId())
                    .setHeader("exitCode", result.getProcess().exitValue()), context);
        }

        if (verifyOutput != null) {
            ValidationUtils.validateValues(result.getOutput().trim(), verifyOutput, "jbang-output", context);
        }

        if (outputVar != null) {
            context.setVariable(context.replaceDynamicContentInString(outputVar), result.getOutput().trim());
        }

        logger.info("JBang script '%s' finished successfully".formatted(scriptName));
    }

    /**
     * Action builder.
     */
    public static final class Builder extends AbstractTestActionBuilder<JBangAction, Builder> {

        private String app;
        private String scriptOrFile;
        private final List<String> args = new ArrayList<>();
        private final Map<String, String> systemProperties = new HashMap<>();
        private int[] exitCodes = new int[] { JBangSupport.OK_EXIT_CODE, 1 };
        private String pidVar;
        private String outputVar;
        private String verifyOutput;
        private ValidationProcessor validationProcessor;
        private boolean printOutput = true;

        /**
         * Fluent API action building entry method used in Java DSL.
         * @return
         */
        public static Builder jbang() {
            return new Builder();
        }

        public Builder app(String name) {
            this.app = name;
            return this;
        }

        public Builder command(String command) {
            this.scriptOrFile = command;
            return this;
        }

        public Builder script(String script) {
            this.scriptOrFile = script;
            return this;
        }

        public Builder file(String path) {
            this.scriptOrFile = Resources.create(path).getFile().getAbsolutePath();
            return this;
        }

        public Builder file(Resource resource) {
            this.scriptOrFile = resource.getFile().getAbsolutePath();
            return this;
        }

        public Builder arg(String name, String value) {
            this.args.add("%s=%s".formatted(name, value));
            return this;
        }

        public Builder arg(String value) {
            this.args.add(value);
            return this;
        }

        public Builder args(String... args) {
            this.args.addAll(List.of(args));
            return this;
        }

        public Builder systemProperty(String name, String value) {
            this.systemProperties.put(name, value);
            return this;
        }

        public Builder exitCode(int code) {
            this.exitCodes = new int[] {code};
            return this;
        }

        public Builder exitCodes(int... codes) {
            this.exitCodes = codes;
            return this;
        }

        public Builder printOutput(boolean enabled) {
            this.printOutput = enabled;
            return this;
        }

        public Builder verifyOutput(String expected) {
            this.verifyOutput = expected;
            return this;
        }

        public Builder verifyOutput(ValidationProcessor validationProcessor) {
            this.validationProcessor = validationProcessor;
            return this;
        }

        public Builder savePid(String variable) {
            this.pidVar = variable;
            return this;
        }

        public Builder saveOutput(String variable) {
            this.outputVar = variable;
            return this;
        }

        @Override
        public JBangAction build() {
            return new JBangAction(this);
        }
    }

    public int[] getExitCodes() {
        return exitCodes;
    }

    public List<String> getArgs() {
        return args;
    }

    public String getApp() {
        return app;
    }

    public String getScriptOrFile() {
        return scriptOrFile;
    }

    public String getOutputVar() {
        return outputVar;
    }

    public String getPidVar() {
        return pidVar;
    }

    public String getVerifyOutput() {
        return verifyOutput;
    }

    public boolean isPrintOutput() {
        return printOutput;
    }

    public Map<String, String> getSystemProperties() {
        return systemProperties;
    }

    public ValidationProcessor getValidationProcessor() {
        return validationProcessor;
    }
}
