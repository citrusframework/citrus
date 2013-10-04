/*
 * Copyright 2006-2013 the original author or authors.
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

package com.consol.citrus.admin.launcher;

/**
 * Process listener for receiving start, success, fail, stop and output events for processes launched
 * using the {@link com.consol.citrus.admin.launcher.ProcessLauncher}.
 *
 * @author Martin.Maher@consol.de
 * @since 1.3
 */
public interface ProcessListener {

    /**
     * Invoked on start process event
     *
     * @param processId the id of the process
     */
    void onProcessStart(String processId);

    /**
     * Invoked on successful completion event
     *
     * @param processId the id of the completed process
     */
    void onProcessSuccess(String processId);

    /**
     * Invoked on failed completion event, with the process exit code
     *
     * @param processId the id of the process
     * @param exitCode the exitcode returned from the process
     */
    void onProcessFail(String processId, int exitCode);

    /**
     * Invoked on failed completion event, with the exception that was caught
     *
     * @param processId the id of the process
     * @param e the exception caught within the ProcessLauncher
     */
    void onProcessFail(String processId, Throwable e);

    /**
     * Invoked on process output with output data from process. This method is called
     * in cache mode. In contrast to process activity which is called immediately after
     * process output was detected (@see onProcessActivity).
     *
     * @param processId the id of the process
     * @param output
     */
    void onProcessOutput(String processId, String output);

    /**
     * Invoked on process activity with output data from process. Called immediately
     * after process activity was detected. In contrast to process output which is cached
     * first and notified separately with less frequency (@see onProcessOutput).
     *
     * @param processId the id of the process
     * @param output
     */
    void onProcessActivity(String processId, String output);
}
