/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.testcontainers;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.PingCmd;
import org.citrusframework.kubernetes.KubernetesSettings;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public class TestcontainersActorTest {

    @Mock
    DockerClient dockerClient;

    @Mock
    PingCmd connected;

    @Mock
    PingCmd disconnected;

    @BeforeClass
    public void setupMocks() {
        MockitoAnnotations.openMocks(this);

        doNothing().when(connected).exec();
        doThrow(IllegalStateException.class).when(disconnected).exec();
    }

    @Test
    public void shouldVerifyConnectedState() {
        try {
            when(dockerClient.pingCmd()).thenReturn(connected);
            Assert.assertFalse(new TestcontainersActor(dockerClient).isDisabled());
        } finally {
            TestcontainersActor.resetConnectionState();
            reset(dockerClient);
        }

        try {
            when(dockerClient.pingCmd()).thenReturn(disconnected);
            Assert.assertTrue(new TestcontainersActor(dockerClient).isDisabled());
        } finally {
            TestcontainersActor.resetConnectionState();
            reset(dockerClient);
        }
    }

    @Test
    public void shouldOverruleConnectedState() {
        boolean initial = KubernetesSettings.isEnabled();
        try {
            System.setProperty("citrus.testcontainers.enabled", "false");
            when(dockerClient.pingCmd()).thenReturn(connected);
            Assert.assertTrue(new TestcontainersActor(dockerClient).isDisabled());
        } finally {
            System.setProperty("citrus.testcontainers.enabled", Boolean.toString(initial));
            reset(dockerClient);
        }

        initial = Boolean.parseBoolean(System.getProperty("citrus.test.actor.testcontainers.enabled", "true"));
        try {
            System.setProperty("citrus.test.actor.testcontainers.enabled", "false");
            when(dockerClient.pingCmd()).thenReturn(connected);
            Assert.assertTrue(new TestcontainersActor(dockerClient).isDisabled());
        } finally {
            System.setProperty("citrus.test.actor.testcontainers.enabled", Boolean.toString(initial));
        }
    }
}
