package com.consol.citrus;

import org.springframework.beans.factory.BeanNameAware;

import com.consol.citrus.exceptions.TestSuiteException;

/**
 *
 * @author deppisch Christoph Deppisch ConSol* Software GmbH
 * @since 06.03.2007
 *
 */
public interface Server extends BeanNameAware, Runnable {

    /**
     *
     * @throws TestSuiteException
     */
    public void start() throws TestSuiteException;

    /**
     *
     * @throws TestSuiteException
     */
    public void stop() throws TestSuiteException;

    /**
     *
     * @return
     */
    public boolean isRunning();

    /**
     *
     * @return
     */
    public String getName();
}
