/*
 * Copyright (c) 2008-2025, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.config;

import com.hazelcast.test.HazelcastParallelClassRunner;
import com.hazelcast.test.HazelcastTestSupport;
import com.hazelcast.test.annotation.ParallelJVMTest;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

@RunWith(HazelcastParallelClassRunner.class)
@Category({QuickTest.class, ParallelJVMTest.class})
public class NearCacheConfigAccessorTest extends HazelcastTestSupport {

    @Test
    public void testConstructor() {
        assertUtilityConstructor(NearCacheConfigAccessor.class);
    }

    @Test
    public void testCopyInitDefaultMaxSizeForOnHeapMaps_whenNull_thenDoNothing() {
        NearCacheConfigAccessor.copyWithInitializedDefaultMaxSizeForOnHeapMaps(null);
    }

    @Test
    public void testCopyInitDefaultMaxSizeForOnHeapMaps_doesNotChangeOriginal_createsChangedCopy() {
        NearCacheConfig nearCacheConfig = new NearCacheConfig();
        NearCacheConfig copy = NearCacheConfigAccessor.copyWithInitializedDefaultMaxSizeForOnHeapMaps(nearCacheConfig);

        assertEquals(MapConfig.DEFAULT_MAX_SIZE, copy.getEvictionConfig().getSize());
        assertEquals(EvictionConfig.DEFAULT_MAX_ENTRY_COUNT, nearCacheConfig.getEvictionConfig().getSize());
    }

    @Test
    public void testCopyInitDefaultMaxSizeForOnHeapMaps_doesNotCopy_whenSizeIsConfigured() {
        NearCacheConfig nearCacheConfig = new NearCacheConfig();
        nearCacheConfig.setEvictionConfig(new EvictionConfig().setSize(10));
        NearCacheConfig copy = NearCacheConfigAccessor.copyWithInitializedDefaultMaxSizeForOnHeapMaps(nearCacheConfig);

        assertSame(nearCacheConfig, copy);
    }
}
