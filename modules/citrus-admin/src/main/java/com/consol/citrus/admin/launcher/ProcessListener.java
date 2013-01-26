package com.consol.citrus.admin.launcher;

/**
 * Process listener for receiving start, success, fail, stop and output events for processes launched
 * using the {@link com.consol.citrus.admin.launcher.ProcessLauncher}.
 *
 * @author Martin.Maher@consol.de
 * @version $Id$
 * @since 2012.11.30
 */
public interface ProcessListener {

    /**
     * Invoked on start process event
     *
     * @param processId the id of the process
     */
    void start(String processId);

    /**
     * Invoked on successful completion event
     *
     * @param processId the id of the completed process
     */
    void success(String processId);

    /**
     * Invoked on failed completion event, with the process exit code
     *
     * @param processId the id of the process
     * @param exitCode the exitcode returned from the process
     */
    void fail(String processId, int exitCode);

    /**
     * Invoked on failed completion event, with the exception that was caught
     *
     * @param processId the id of the process
     * @param e the exception caught within the ProcessLauncher
     */
    void fail(String processId, Exception e);

    /**
     * Invoked on output message event with output data from process
     *
     * @param processId the id of the process
     * @param output
     */
    void output(String processId, String output);
}
