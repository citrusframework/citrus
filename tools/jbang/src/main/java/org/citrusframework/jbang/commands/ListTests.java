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

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;
import com.github.freva.asciitable.OverflowBehaviour;
import main.CitrusJBang;
import org.citrusframework.jbang.CitrusJBangMain;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "ls", description = "List running Citrus tests")
public class ListTests extends CitrusCommand {

    @CommandLine.Option(names = { "--sort" },
                        description = "Sort by pid, name or age", defaultValue = "pid")
    String sort;

    @CommandLine.Option(names = { "--pid" },
                        description = "List only pid in the output")
    boolean pid;

    public ListTests(CitrusJBangMain main) {
        super(main);
    }

    @Override
    public Integer call() throws Exception {
        List<Row> rows = new ArrayList<>();

        final long cur = ProcessHandle.current().pid();
        ProcessHandle.allProcesses()
                .filter(ph -> ph.pid() != cur)
                .filter(ph -> isTestProcess(ph.info()))
                .forEach(ph -> {
                    Row row = new Row();
                    row.pid = "" + ph.pid();
                    row.uptime = extractSince(ph);
                    row.ago = printSince(row.uptime);
                    row.name = extractName(ph);
                    row.status = ph.isAlive() ? "Running" : "Finished";
                    rows.add(row);
                });

        // sort rows
        rows.sort(this::sortRow);

        if (pid) {
            rows.forEach(r -> System.out.println(r.pid));
        } else {
            System.out.println(AsciiTable.getTable(AsciiTable.NO_BORDERS, rows, Arrays.asList(
                    new Column().header("PID").headerAlign(HorizontalAlign.CENTER).with(r -> r.pid),
                    new Column().header("NAME").dataAlign(HorizontalAlign.LEFT)
                            .maxWidth(40, OverflowBehaviour.ELLIPSIS_RIGHT)
                            .with(r -> r.name),
                    new Column().header("STATUS").headerAlign(HorizontalAlign.CENTER).with(r -> r.status),
                    new Column().header("AGE").headerAlign(HorizontalAlign.CENTER).with(r -> r.ago))));
        }

        return 0;
    }

    protected boolean isTestProcess(ProcessHandle.Info info) {
        return info.commandLine().orElse("").contains(CitrusJBang.class.getSimpleName());
    }

    private String extractName(ProcessHandle ph) {
        String cl = ph.info().commandLine().orElse("");
        String citrusJBangRun = getJBangRunCommand();
        if (cl.contains(citrusJBangRun)) {
            return cl.substring(cl.indexOf(citrusJBangRun) + citrusJBangRun.length());
        }

        return "";
    }

    protected String getJBangRunCommand() {
        return String.format("main.%s run ", CitrusJBang.class.getSimpleName());
    }

    private String printSince(long timestamp) {
        long age = System.currentTimeMillis() - timestamp;
        Duration duration = Duration.ofMillis(age);

        StringBuilder sb = new StringBuilder();
        if (duration.toDays() > 0) {
            sb.append(duration.toDays()).append("d").append(duration.toHours() % 24).append("h");
        } else if (duration.toHours() > 0) {
            sb.append(duration.toHours() % 24).append("h").append(duration.toMinutes() % 60).append("m");
        } else if (duration.toMinutes() > 0) {
            sb.append(duration.toMinutes() % 60).append("m").append(duration.toSeconds() % 60).append("s");
        } else if (duration.toSeconds() > 0) {
            sb.append(duration.toSeconds() % 60).append("s");
        } else if (duration.toMillis() > 0) {
            // less than a second so just report it as zero
            sb.append("0s");
        }

        return sb.toString();
    }

    protected int sortRow(Row o1, Row o2) {
        String s = sort;
        int negate = 1;
        if (s.startsWith("-")) {
            s = s.substring(1);
            negate = -1;
        }
        switch (s) {
            case "pid":
                return Long.compare(Long.parseLong(o1.pid), Long.parseLong(o2.pid)) * negate;
            case "name":
                return o1.name.compareToIgnoreCase(o2.name) * negate;
            case "age":
                return Long.compare(o1.uptime, o2.uptime) * negate;
            default:
                return 0;
        }
    }

    protected static long extractSince(ProcessHandle ph) {
        long since = 0;
        if (ph.info().startInstant().isPresent()) {
            since = ph.info().startInstant().get().toEpochMilli();
        }
        return since;
    }

    protected static class Row {
        String pid;
        String name;
        String status;
        String ago;
        long uptime;
    }

}
