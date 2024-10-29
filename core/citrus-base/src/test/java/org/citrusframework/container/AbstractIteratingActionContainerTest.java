package org.citrusframework.container;

import static java.lang.Thread.currentThread;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.citrusframework.container.AbstractIteratingActionContainerTest.Fixture.FixtureBuilder.fixture;
import static org.mockito.MockitoAnnotations.openMocks;

import java.time.Duration;
import org.apache.commons.lang3.time.StopWatch;
import org.citrusframework.AbstractIteratingContainerBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.mockito.Mock;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class AbstractIteratingActionContainerTest {

    public static final Duration ONE_SECOND = Duration.ofSeconds(1);

    @Mock
    private TestContext testContext;

    private AutoCloseable mockitoContext;

    private static StopWatch getStartedStopWatch() {
        var stopWatch = new StopWatch();
        stopWatch.start();
        return stopWatch;
    }

    @DataProvider
    public static Fixture[] synchronousIterations() {
        return new Fixture[]{
            fixture(ONE_SECOND, null).doBuild(),
            fixture(ONE_SECOND, Duration.ofMillis(0)).doBuild(),
            fixture(ONE_SECOND, Duration.ofMillis(-1)).doBuild()
        };
    }

    @BeforeMethod
    public void beforeMethodSetup() {
        mockitoContext = openMocks(this);
    }

    @AfterMethod
    public void afterMethodTeardown() throws Exception {
        mockitoContext.close();
    }

    @Test(dataProvider = "synchronousIterations")
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
