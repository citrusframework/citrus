import org.citrusframework.TestActionRunner;
import org.citrusframework.annotations.CitrusResource;

import static org.citrusframework.actions.EchoAction.Builder.echo;

public class EchoTest implements Runnable {

    @CitrusResource
    TestActionRunner t;

    @Override
    public void run() {
        t.run(
            echo().message("Hello Citrus!")
        );
    }

}
