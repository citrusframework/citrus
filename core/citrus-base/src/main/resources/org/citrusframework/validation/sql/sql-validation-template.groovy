import org.citrusframework.*
import org.citrusframework.variable.*
import org.citrusframework.context.TestContext
import org.citrusframework.validation.script.sql.SqlResultSetScriptExecutor

import java.util.List;
import java.util.Map;

public class ValidationScript implements SqlResultSetScriptExecutor{
    public void validate(List<Map<String, Object>> rows, TestContext context){
        @SCRIPTBODY@
    }
}
