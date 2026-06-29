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
import java.nio.file.Paths;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.jbang.CitrusJBangMain;
import org.citrusframework.jbang.Printer;
import org.citrusframework.jbang.StringPrinter;
import org.testng.annotations.BeforeMethod;

public class CommandTest {

    protected final Path workingDir = Paths.get("target").toAbsolutePath();
    protected StringPrinter printer;

    @BeforeMethod
    public void setup() {
        printer = new StringPrinter();
    }

    /**
     * Creates CitrusJBangMain instance ready for unit testing.
     * Automatically sets the out printer.
     */
    protected CitrusJBangMain createCitrusJBangMain() {
        return createCitrusJBangMain(0);
    }

    /**
     * Creates CitrusJBangMain instance ready for unit testing.
     * Automatically sets the out printer.
     */
    protected CitrusJBangMain createCitrusJBangMain(int expectedExitCode) {
        return new CitrusJBangMain() {
            @Override
            public void quit(int exitCode) {
                if (exitCode != expectedExitCode) {
                    throw new CitrusRuntimeException("Unexpected exit code: " + exitCode);
                }
            }

            @Override
            public int execute(String... args) {
                int exitCode = super.execute(args);
                if (exitCode != expectedExitCode) {
                    throw new CitrusRuntimeException("Unexpected exit code: " + exitCode);
                }
                return exitCode;
            }

            @Override
            public Printer getOut() {
                return printer;
            }
        };
    }
}
