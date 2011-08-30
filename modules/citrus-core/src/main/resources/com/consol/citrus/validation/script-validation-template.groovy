import com.consol.citrus.*
import com.consol.citrus.variable.*
import com.consol.citrus.context.TestContext
import com.consol.citrus.validation.script.GroovyScriptExecutor
import org.springframework.integration.Message
import org.springframework.integration.MessageHeaders

public class ValidationScript implements GroovyScriptExecutor{
    public void validate(Message<?> receivedMessage, TestContext context){
        MessageHeaders headers = receivedMessage.getHeaders()
        String payload = receivedMessage.getPayload().toString()
        
        @SCRIPTBODY@
    }
}