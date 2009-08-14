package com.consol.citrus;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.consol.citrus.actions.StartupBean;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.server.Server;
import com.consol.citrus.server.ServerShutdownThread;

/**
 * Runs the loop back dummies using classpath references to the applicationContext.xml and test.properties. Required files only
 * have to be available in classpath.
 *
 * @author deppisch Christoph Deppisch ConSol* Software GmbH
 * @since 08.02.2007
 *
 */
public class LoopBackRunner {
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(LoopBackRunner.class);

    /**
     * Main method doing all work
     * java TestSuiteClasspathRunner applicationContext.xml [TestCase1, TestCase2, ...] [-Xdir=subDirectory]
     * @param args
     */
    public static void main(String[] args) {
        /* version of testsuite loaded from test.properties */
        String version = "";

        /* root context file name */
        String applicationContext = "";

        /* list to hold all test defining xml files */
        List startBeans = new ArrayList();

        /* Load root applicationContext file name from arguments */
        if (args.length != 0) {
            applicationContext = args[0];
        } else {
            applicationContext = CitrusConstants.DEFAULT_APPLICATIONCONTEXT;
        }

        /* Build root application context without any test files */
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(new String[]{applicationContext});
        ctx.setAllowBeanDefinitionOverriding(false);
        
        version = ctx.getMessage("testsuite.version", null, "", null);

        log.info("TESTSUITE " + version);
        log.info("");

        log.info("Loading Configuration");

        /* check if command line arguments contain test-names to be executed explicitly */
        if (args.length > 1) {
            /* search test files in test directory for the specific tests and load add them to context list */
            for (int i = 1; i < args.length; i++) {
                if (!args[i].equals("")) {
                    startBeans.add(args[i]);
                }
            }
        }

        if (startBeans.isEmpty()) {
            /* no test-names in arguments - load all test files available */
            String[] beanNames = ctx.getBeanNamesForType(StartupBean.class);
            for (int i = 0; i < beanNames.length; i++) {
                startBeans.add(beanNames[i]);
            }
        }

        log.info("");

        List servers = new ArrayList();
        boolean exitWithError = false;
        /* finally starting all found dummy instances */
        for (int i = 0; i < startBeans.size(); i++) {
            StartupBean starter = (StartupBean)ctx.getBean(startBeans.get(i).toString());

            try {
                starter.execute(null);
            } catch (CitrusRuntimeException e) {
                log.error("Failed to start jms loopback", e);
                exitWithError = true;
            }

            if (starter.getServer() != null) {
                Runtime.getRuntime().addShutdownHook(new ServerShutdownThread(starter.getServer()));
                servers.add(starter.getServer());
            }

            if (starter.getServerList() != null) {
                Runtime.getRuntime().addShutdownHook(new ServerShutdownThread(starter.getServerList()));
                for (int j = 0; j < starter.getServerList().size(); j++) {
                    servers.add(starter.getServerList().get(j));
                }
            }
        }

        boolean running = true;
        while (running) {
            for (int i = 0; i < servers.size(); i++) {
                while (((Server)servers.get(i)).isRunning()) {
                    synchronized(servers.get(i)) {
                        try {
                            log.info("Waiting for server" + ((Server)servers.get(i)).getName() + " to stop");
                            servers.get(i).wait();
                        } catch (InterruptedException e) {
                            log.error("Error while waiting for server to stop", e);
                        }
                    }
                }
            }
        }

        if (exitWithError)
            System.exit(1);
    }
}
