package com.consol.citrus.server;

import org.springframework.beans.factory.BeanNameAware;

/**
 *
 * @author deppisch Christoph Deppisch ConSol* Software GmbH
 * @since 06.03.2007
 *
 */
public interface Server extends BeanNameAware, Runnable {

    public void start();

    public void stop();

    public boolean isRunning();

    public String getName();
}
