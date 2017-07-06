package com.consol.citrus.report;

import com.consol.citrus.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import works.integration.jira.exceptions.DuplicateEntryException;
import works.integration.jira.exceptions.ServiceBindingException;
import works.integration.jira.external.*;

/**
 * Created by sudeep.r on 8/02/2017.
 */
public class JIRAConsumer extends AbstractTestListener {
    /** Logger */
    private static Logger log = LoggerFactory.getLogger(JIRAConsumer.class);

    /** Enables/disables JIRA API execution */
    @Value("${citrus.jira.enabled:true}")
    private String enabled;

    private String getStackTrace(Throwable cause) {
        StringBuilder stackTraceBuilder = new StringBuilder();
        stackTraceBuilder.append(cause.getClass().getName() + ": " + cause.getMessage() + "\n ");
        for (int i = 0; i < cause.getStackTrace().length; i++) {
            stackTraceBuilder.append("\n\t at ");
            stackTraceBuilder.append(cause.getStackTrace()[i]);
        }

        return stackTraceBuilder.toString();
    }

    private String getFailureCause(Throwable cause) {
        if (cause != null && StringUtils.hasText(cause.getLocalizedMessage())) {
            return " Caused by: " + cause.getClass().getSimpleName() + ": " +  cause.getLocalizedMessage();
        } else {
            return " Caused by: Unknown error";
        }
    }

    @Override
    public void onTestSuccess(TestCase test) {
    }

    @Override
    public void onTestFailure(TestCase test, Throwable cause) {
        if(enabled.equalsIgnoreCase(Boolean.TRUE.toString())) {

            Table<PropertyKey, String> props = new Table<PropertyKey, String>();

            props.add(PropertyKey.PROJECT, test.getProject());
            props.add(PropertyKey.EXMESSAGE, getFailureCause(cause));
            props.add(PropertyKey.SUMMARY, test.getName());
            props.add(PropertyKey.DETAILEDEXCEPTION, getStackTrace(cause));

            log.info("Invoking JIRA API...");

            JIRAService service = (JIRAService) ProxyService.getService(ServiceRegistry.IW_JIRA.toString());

            try {
                log.error(service.doExecute(props));
            } catch (DuplicateEntryException e) {
                log.error(e.getLocalizedMessage());
            } catch(ServiceBindingException e) {
                e.printStackTrace();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }

    @Override
    public void onTestSkipped(TestCase test) {
    }
}
