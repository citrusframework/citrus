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

package org.citrusframework.jbang.commands;

import java.nio.file.Path;
import java.util.Stack;

import org.citrusframework.jbang.CitrusJBangMain;
import org.citrusframework.jbang.JsonSupport;
import org.citrusframework.jbang.util.CodeAnalyzer;
import org.citrusframework.jbang.util.DelegatingCodeAnalyzer;
import org.citrusframework.message.MessagePayloadUtils;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "inspect", description = "Inspect a Citrus test and its source code in order to provide detailed information " +
        "such as used endpoints as well as required modules and dependencies.")
public class Inspect extends CitrusCommand {

    @Parameters(description = "Path to the test file (or a github link)", arity = "1",
            paramLabel = "<file>", parameterConsumer = Inspect.FileConsumer.class)
    private Path filePath; // Defined only for file path completion; the field never used

    private String file;

    public Inspect(CitrusJBangMain main) {
        super(main);
    }

    @Override
    public Integer call() throws Exception {
        Resource sourceFile = Resources.create(file);
        CodeAnalyzer.ScanResult result = new DelegatingCodeAnalyzer().scan(sourceFile);

        printer().println(MessagePayloadUtils.prettyPrintJson(JsonSupport.json().writeValueAsString(result)));

        return 0;
    }

    private String[] inspectEndpoints(String content) {
        return new String[]{};
    }

    static class FileConsumer extends CitrusCommand.ParameterConsumer<Inspect> {
        @Override
        protected void doConsumeParameters(Stack<String> args, Inspect cmd) {
            cmd.file = args.pop();
        }
    }
}
