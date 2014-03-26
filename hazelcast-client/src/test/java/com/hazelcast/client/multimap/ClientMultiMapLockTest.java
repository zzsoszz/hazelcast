/*
 * Copyright (c) 2008-2013, Hazelcast, Inc. All Rights Reserved.
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

package com.hazelcast.client.multimap;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.*;
import com.hazelcast.test.HazelcastParallelClassRunner;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.hazelcast.test.HazelcastTestSupport.*;
import static org.junit.Assert.*;

@RunWith(HazelcastParallelClassRunner.class)
@Category(QuickTest.class)
public class ClientMultiMapLockTest {

    static HazelcastInstance server;
    static HazelcastInstance client;

    @BeforeClass
    public static void init() {
        server = Hazelcast.newHazelcastInstance();
        client = HazelcastClient.newHazelcastClient();
    }

    @AfterClass
    public static void destroy() {
        HazelcastClient.shutdownAll();
        Hazelcast.shutdownAll();
    }

    @Test
    public void testIsLocked_whenNotLocked() throws Exception {
        final MultiMap mm = client.getMultiMap(randomString());
        final Object key = "KeyNotLocked";
        final boolean result = mm.isLocked(key);
        assertFalse(result);
    }

    @Test
    public void testLock() throws Exception {
        final MultiMap mm = client.getMultiMap(randomString());
        final Object key = "Key";
        mm.lock(key);
        assertTrue(mm.isLocked(key));
    }

    @Test(expected = NullPointerException.class)
    public void testLock_whenKeyNull() throws Exception {
        final MultiMap mm = client.getMultiMap(randomString());
        mm.lock(null);
    }

    @Test(expected = NullPointerException.class)
    public void testUnLock_whenNullKey() throws Exception {
        final MultiMap mm = client.getMultiMap(randomString());
        mm.unlock(null);
    }

    @Test(expected = IllegalMonitorStateException.class)
    public void testUnlock_whenNotLocked() throws Exception {
        final MultiMap mm = client.getMultiMap(randomString());
        mm.unlock("NOT_LOCKED");
    }

    @Test
    public void testUnLock() throws Exception {
        final MultiMap mm = client.getMultiMap(randomString());
        final Object key = "key";

        mm.lock(key);
        mm.unlock(key);
        assertFalse(mm.isLocked(key));
    }

    @Test
    public void testUnlock_whenRentrantlyLockedBySelf() throws Exception {
        final MultiMap mm = client.getMultiMap(randomString());
        final Object key = "key";

        mm.lock(key);
        mm.lock(key);
        mm.unlock(key);
        assertTrue(mm.isLocked(key));
    }

    @Test(expected = IllegalMonitorStateException.class)
    public void testUnlock_whenLockedByOther() throws Exception {
        final MultiMap mm = client.getMultiMap(randomString());
        final Object key = "key";
        mm.lock(key);

        UnLockThread t = new UnLockThread(mm, key);
        t.start();
        assertJoinable(t);
        throw t.exception;
    }

    static class UnLockThread extends Thread{
        public Exception exception=null;
        public MultiMap mm=null;
        public Object key=null;

        public UnLockThread(MultiMap mm, Object key){
            this.mm = mm;
            this.key = key;
        }

        public void run() {
            try{
                mm.unlock(key);
            }catch (Exception e){
                exception = e;
            }
        }
    };

    @Test
    public void testMultiLockCalls() throws Exception {
        final MultiMap mm = client.getMultiMap(randomString());
        final Object key = "Key";
        mm.lock(key);
        mm.lock(key);
        assertTrue(mm.isLocked(key));
    }

    @Test
    public void testTryLock() throws Exception {
        final MultiMap mm = client.getMultiMap(randomString());
        Object key = "key";
        assertTrue(mm.tryLock(key));
    }

    @Test(expected = NullPointerException.class)
    public void testTryLock_whenNullKey() throws Exception {
        final MultiMap mm = client.getMultiMap(randomString());
        mm.tryLock(null);
    }

    @Test
    public void testTryLock_whenLockedBySelf() throws Exception {
        final MultiMap mm = client.getMultiMap(randomString());
        final Object key = "Key";
        mm.lock(key);
        assertTrue(mm.tryLock(key));
    }


    @Test
    public void testTryLock_whenLockedByOther() throws Exception {
        final MultiMap mm = client.getMultiMap(randomString());
        final Object key = "Key1";
        mm.lock(key);
        final CountDownLatch tryLockFailed = new CountDownLatch(1);
        new Thread() {
            public void run() {
                if (mm.tryLock(key) == false) {
                    tryLockFailed.countDown();
                }
            }
        }.start();
        assertTrue(tryLockFailed.await(5, TimeUnit.SECONDS));
    }

    @Test(expected = NullPointerException.class)
    public void testForceUnlock_whenKeyNull() throws Exception {
        final MultiMap mm = client.getMultiMap(randomString());
        mm.forceUnlock(null);
    }

    @Test
    public void testForceUnlock_whenKeyLocked() throws Exception {
        final MultiMap mm = client.getMultiMap(randomString());
        final Object key = "Key";
        mm.lock(key);
        mm.lock(key);
        mm.forceUnlock(key);
        assertFalse(mm.isLocked(key));
    }

    @Test
    public void testForceUnlock_whenKeyLockedByOther() throws Exception {
        final MultiMap mm = client.getMultiMap(randomString());
        final Object key = "key";
        mm.lock(key);
        mm.lock(key);
        final CountDownLatch forceUnlock = new CountDownLatch(1);
        new Thread(){
            public void run() {
                mm.forceUnlock(key);
                forceUnlock.countDown();
            }
        }.start();
        assertTrue(forceUnlock.await(30, TimeUnit.SECONDS));
        assertFalse(mm.isLocked(key));
    }

    @Test
    public void testLockTTL() throws Exception {
        final MultiMap mm = client.getMultiMap(randomString());
        final Object key = "Key";

        mm.lock(key, 1, TimeUnit.SECONDS);
        assertTrue(mm.isLocked(key));
    }

    @Test
    public void testLockTTLExpired() throws Exception {
        final MultiMap mm = client.getMultiMap(randomString());
        final Object key = "Key";

        mm.lock(key, 1, TimeUnit.SECONDS);
        sleepSeconds(2);
        assertFalse(mm.isLocked(key));
    }

    @Test
    public void testLockTTLExpires_andOtherThreadCanObtain() throws Exception {
        final MultiMap mm = client.getMultiMap(randomString());
        final Object key = "Key";

        mm.lock(key, 2, TimeUnit.SECONDS);
        final CountDownLatch tryLockSuccess = new CountDownLatch(1);
        new Thread() {
            public void run() {
                try {
                    if (mm.tryLock(key, 4, TimeUnit.SECONDS)) {
                        tryLockSuccess.countDown();
                    }
                } catch (InterruptedException e) {
                    fail(e.getMessage());
                }
            }
        }.start();
        assertTrue(tryLockSuccess.await(10, TimeUnit.SECONDS));
    }

    @Test
    public void testTryLockWaitingOnLockedKey_thenKeyUnlockedByOtherThread() throws Exception {
        final MultiMap mm = client.getMultiMap(randomString());
        final Object key = "keyZ";

        mm.lock(key);

        final CountDownLatch tryLockReturnsTrue = new CountDownLatch(1);
        new Thread(){
            public void run() {
                try {
                    if(mm.tryLock(key, 10, TimeUnit.SECONDS)){
                        tryLockReturnsTrue.countDown();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        sleepSeconds(1);
        mm.unlock(key);

        assertTrue(tryLockReturnsTrue.await(20, TimeUnit.SECONDS));
        assertTrue(mm.isLocked(key));
    }
}