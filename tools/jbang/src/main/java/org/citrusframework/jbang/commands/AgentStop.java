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

import java.util.ArrayList;
import java.util.Arrays;

import org.citrusframework.jbang.CitrusJBangMain;
import org.citrusframework.jbang.Printer;
import org.citrusframework.jbang.StringPrinter;
import picocli.CommandLine.Command;

@Command(name = "stop", description = "Stop the Citrus agent service")
public class AgentStop extends CitrusCommand {

    public AgentStop(CitrusJBangMain main) {
        super(main);
    }

    @Override
    public Integer call() throws Exception {
        return stop();
    }

    private int stop() throws Exception {
        Printer original = getMain().getOut();
        try {
            StringPrinter printer = new StringPrinter();
            ListTests ls = new ListTests(getMain().withPrinter(printer));
            ls.call();

            Long pid = printer.getLines().stream()
                    .map(line -> new ArrayList<>(Arrays.asList(line.trim().split("\\s+"))))
                    .filter(rows -> "citrus-agent".equals(rows.get(1)))
                    .map(rows -> rows.get(0))
                    .map(Long::parseLong)
                    .findFirst()
                    .orElse(0L);

            if (pid > 0) {
                ProcessHandle.of(pid).ifPresent(ph -> {
                    if (ph.destroyForcibly()) {
                        original.printf("Stopped Citrus agent process (pid: %s)%n", pid);
                    } else {
                        original.printErr("Failed to stop Citrus agent process (pid: %s)".formatted(pid));
                    }
                });
            }
        } finally {
            getMain().withPrinter(original);
        }

        return 0;
    }

}
