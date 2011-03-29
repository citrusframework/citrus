import com.consol.citrus.*
import com.consol.citrus.variable.*
import com.consol.citrus.context.TestContext
import com.consol.citrus.validation.script.GroovyScriptMessageValidator.ValidationScriptExecutor
import groovy.util.XmlSlurper
import org.springframework.integration.Message

public class ValidationScript implements ValidationScriptExecutor{
    public void validate(Message<?> receivedMessage, TestContext context){
        def root = new XmlSlurper().parseText(receivedMessage.getPayload().toString())
        @SCRIPTBODY@
    }
}