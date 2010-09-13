import com.consol.citrus.*
import com.consol.citrus.variable.*
import com.consol.citrus.context.TestContext
import com.consol.citrus.validation.GroovyScriptValidator.ValidationScriptExecutor
import groovy.util.XmlSlurper
import org.springframework.integration.core.Message

public class ValidationScript implements ValidationScriptExecutor{
    public void validate(Message<?> receivedMessage, TestContext context){
        def root = new XmlSlurper().parseText(receivedMessage.getPayload().toString())
        @SCRIPTBODY@
    }
}