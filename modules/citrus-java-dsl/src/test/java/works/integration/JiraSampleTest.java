package works.integration;

import com.consol.citrus.report.JIRAConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import works.integration.jira.exceptions.DuplicateEntryException;
import works.integration.jira.exceptions.IllegalInvocationException;
import works.integration.jira.exceptions.ServiceBindingException;
import works.integration.jira.external.*;

/**
 * Created by vihar.naik on 07-Feb-17.
 */

public class JiraSampleTest {
    private static Logger log = LoggerFactory.getLogger(JIRAConsumer.class);

    public static void main(String[] args) throws IllegalInvocationException, ServiceBindingException {
        Consumer con = new Consumer();
        con.serviceExecution();

    }

    /**
     * Created by sudeep.r on 3/02/2017.
     */
    public static class Consumer {
        public void serviceExecution() throws IllegalInvocationException, ServiceBindingException {

            Table<PropertyKey, String> props = new Table<PropertyKey, String>();
            props.add(PropertyKey.PROJECT, "TPKO");
            props.add(PropertyKey.EXMESSAGE, "RuntimeException Forced");
            props.add(PropertyKey.SUMMARY, "Test IW 2");
            props.add(PropertyKey.DETAILEDEXCEPTION, "org.iw.works.ForcedException Caused by User abrupt termination");
            JIRAService service = (JIRAService) ProxyService.getService(ServiceRegistry.IW_JIRA.toString());

            /*ServiceLookup look = new ServiceLookup();
            JIRAService service1 = (JIRAService) look.find(ServiceRegistry.IW_JIRA);*/

            try {
                log.info(service.doExecute(props));
            } catch (DuplicateEntryException e) {
                log.error(e.getLocalizedMessage());
            } catch(ServiceBindingException e) {
                e.printStackTrace();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
//
//
        }
    }
}
