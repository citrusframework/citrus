import org.citrusframework.TestActionRunner;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.annotations.CitrusResource;

public class ParameterizedRowTest implements Runnable, TestActionSupport {

    @CitrusResource
    TestActionRunner t;

    @Override
    public void run() {
        t.run(echo("row: ${row}"));
        t.run(action(context -> {
            if (context.getVariable("row").equals("first")) {
                throw new IllegalStateException("deliberate failure in the first row");
            }
        }));
    }

}
