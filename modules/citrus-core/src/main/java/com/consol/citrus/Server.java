package com.consol.citrus;

import org.springframework.beans.factory.BeanNameAware;

import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 *
 * @author deppisch Christoph Deppisch ConSol* Software GmbH
 * @since 06.03.2007
 *
 */
public interface Server extends BeanNameAware, Runnable {

    /**
     *
     * @throws CitrusRuntimeException
     */
    public void start() throws CitrusRuntimeException;

    /**
     *
     * @throws CitrusRuntimeException
     */
    public void stop() throws CitrusRuntimeException;

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
