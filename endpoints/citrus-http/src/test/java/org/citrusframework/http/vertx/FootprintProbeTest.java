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

package org.citrusframework.http.vertx;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class FootprintProbeTest {
    @Test
    public void nearestRankStatisticsPreserveRawOrderAndHandleSmallSamples() {
        long[] raw = {9, 1, 7, 3};
        assertEquals(FootprintProbe.percentile(raw, 50), 3L);
        assertEquals(FootprintProbe.percentile(raw, 95), 9L);
        assertEquals(raw, new long[] {9, 1, 7, 3});
        assertEquals(FootprintProbe.percentile(new long[] {42}, 95), 42L);
        expectThrows(IllegalArgumentException.class, () -> FootprintProbe.percentile(new long[0], 50));
    }
}
