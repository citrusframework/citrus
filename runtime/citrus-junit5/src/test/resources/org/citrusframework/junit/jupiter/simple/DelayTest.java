import org.citrusframework.TestActionRunner;
import org.citrusframework.annotations.CitrusResource;

import static org.citrusframework.actions.SleepAction.Builder.delay;

public class DelayTest implements Runnable {

    @CitrusResource
    TestActionRunner t;

    @Override
    public void run() {
        t.run(
            delay().milliseconds(200L)
        );
    }

}
