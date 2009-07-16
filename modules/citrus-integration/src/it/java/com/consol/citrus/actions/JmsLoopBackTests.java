package com.consol.citrus.actions;

import org.testng.annotations.Test;

import com.consol.citrus.AbstractIntegrationTest;

/**
 * 
 * @author deppisch Christoph Deppisch Consol* Software GmbH
 * @since 31.10.2008
 */
public class JmsLoopBackTests extends AbstractIntegrationTest {
    @Test
    public void jmsLoopBackTest() {
        executeTest();
    }
}