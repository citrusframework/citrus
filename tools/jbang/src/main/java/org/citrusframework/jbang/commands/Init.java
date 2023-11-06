/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.citrusframework.jbang.commands;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Stack;

import org.citrusframework.util.FileUtils;
import org.citrusframework.jbang.CitrusJBangMain;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "init", description = "Creates a new Citrus test")
public class Init extends CitrusCommand {

    @Parameters(description = "Name of integration file (or a github link)", arity = "1",
                paramLabel = "<file>", parameterConsumer = FileConsumer.class)
    private Path filePath; // Defined only for file path completion; the field never used

    private String file;

    @Option(names = {
            "-dir",
            "--directory" }, description = "Directory where the files will be created", defaultValue = ".")
    private String directory;

    public Init(CitrusJBangMain main) {
        super(main);
    }

    @Override
    public Integer call() throws Exception {
        String ext = FileUtils.getFileExtension(file);
        String name = FileUtils.getBaseName(file);
        String content;
        try (InputStream is = Init.class.getClassLoader().getResourceAsStream("templates/" + ext + ".tmpl")) {
            if (is == null) {
                System.out.println("Error: Unsupported file type: " + ext);
                return 1;
            }
            content = FileUtils.readToString(is, StandardCharsets.UTF_8);
        }

        if (!directory.equals(".")) {
            File dir = new File(directory);
            // ensure target dir is created
            dir.mkdirs();
        }
        File target = new File(directory, file);
        content = content.replaceFirst("\\{\\{ \\.Name }}", name);

        Files.write(target.toPath(), content.getBytes(StandardCharsets.UTF_8));
        return 0;
    }

    static class FileConsumer extends ParameterConsumer<Init> {
        @Override
        protected void doConsumeParameters(Stack<String> args, Init cmd) {
            String arg = args.pop();
            cmd.file = arg;
        }
    }

}
