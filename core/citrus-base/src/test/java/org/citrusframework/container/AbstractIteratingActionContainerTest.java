package org.citrusframework.container;

import static java.lang.Thread.currentThread;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.citrusframework.container.AbstractIteratingActionContainerTest.Fixture.FixtureBuilder.fixture;

import java.time.Duration;
import org.apache.commons.lang3.time.StopWatch;
import org.citrusframework.AbstractIteratingContainerBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AbstractIteratingActionContainerTest {

    public static final Duration ONE_SECOND = Duration.ofSeconds(1);

    @Mock
    private TestContext testContext;

    private static StopWatch getStartedStopWatch() {
        var stopWatch = new StopWatch();
        stopWatch.start();
        return stopWatch;
    }

    public static Fixture[] synchronousIterations() {
        return new Fixture[]{
            fixture(ONE_SECOND, null).doBuild(),
            fixture(ONE_SECOND, Duration.ofMillis(0)).doBuild(),
            fixture(ONE_SECOND, Duration.ofMillis(-1)).doBuild()
        };
    }

    @ParameterizedTest
    @MethodSource("synchronousIterations")
    public void synchronousIteration(Fixture iteratingActionContainer) {
        var stopWatch = getStartedStopWatch();

        iteratingActionContainer.doExecute(testContext);

        assertThat(stopWatch.getDuration())
            .isGreaterThan(ONE_SECOND);
    }

    @Test
    public void asynchronousIteration_doesExecuteWithTimeout() {
        var timeout = Duration.ofSeconds(2);
        var iteratingActionContainer = fixture(ONE_SECOND, timeout).doBuild();

        var stopWatch = getStartedStopWatch();

        iteratingActionContainer.doExecute(testContext);

        assertThat(stopWatch.getDuration())
            .isGreaterThan(ONE_SECOND)
            .isLessThan(timeout);
    }

    @Test
    public void asynchronousIteration_throwsTimeoutException_whenSleepTimeGreaterThanTimeout() {
        var sleepTime = Duration.ofSeconds(2);
        var iteratingActionContainer = fixture(sleepTime, ONE_SECOND).doBuild();

        var stopWatch = getStartedStopWatch();

        assertThatThrownBy(() -> iteratingActionContainer.doExecute(testContext))
            .isInstanceOf(CitrusRuntimeException.class)
            .hasMessage("Iteration reached timeout!");

        assertThat(stopWatch.getDuration())
            .isGreaterThan(ONE_SECOND)
            .isLessThan(sleepTime);
    }

    public static class Fixture extends AbstractIteratingActionContainer {

        private final Duration sleepTime;

        private Fixture(Duration sleepTime, FixtureBuilder builder) {
            super("AbstractIteratingActionContainerTest", builder);
            this.sleepTime = sleepTime;
        }

        @Override
        protected void executeIteration(TestContext context) {
            try {
                Thread.sleep(sleepTime.toMillis());
            } catch (InterruptedException e) {
                currentThread().interrupt();
                throw new IllegalArgumentException(e);
            }
        }

        static class FixtureBuilder extends
            AbstractIteratingContainerBuilder<Fixture, FixtureBuilder> {

            private final Duration sleepTime;

            public FixtureBuilder(Duration sleepTime) {
                super();
                this.sleepTime = sleepTime;
            }

            static FixtureBuilder fixture(Duration sleepTime, Duration timeout) {
                return new FixtureBuilder(sleepTime)
                    .timeout(timeout);
            }

            @Override
            protected Fixture doBuild() {
                return new Fixture(sleepTime, this);
            }
        }
    }
}
