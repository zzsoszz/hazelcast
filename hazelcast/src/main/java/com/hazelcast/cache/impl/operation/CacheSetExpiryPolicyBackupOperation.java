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

package com.hazelcast.cache.impl.operation;

import com.hazelcast.cache.impl.CacheDataSerializerHook;
import com.hazelcast.cache.impl.record.CacheRecord;
import com.hazelcast.internal.nio.IOUtil;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.internal.serialization.Data;
import com.hazelcast.spi.impl.operationservice.BackupOperation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CacheSetExpiryPolicyBackupOperation
        extends CacheOperation
        implements BackupOperation {

    private List<Data> keys;
    private Data expiryPolicy;

    public CacheSetExpiryPolicyBackupOperation() {

    }

    public CacheSetExpiryPolicyBackupOperation(String name, List<Data> keys, Data expiryPolicy) {
        super(name);
        this.keys = keys;
        this.expiryPolicy = expiryPolicy;
    }

    @Override
    public void run() throws Exception {
        if (recordStore == null) {
            return;
        }
        recordStore.setExpiryPolicy(keys, expiryPolicy, null);
    }

    @Override
    public void afterRun() throws Exception {
        super.afterRun();
        if (recordStore.isWanReplicationEnabled()) {
            for (Data key : keys) {
                CacheRecord cacheRecord = recordStore.getRecord(key);
                publishWanUpdate(key, cacheRecord);
            }
        }
    }

    @Override
    public int getFactoryId() {
        return CacheDataSerializerHook.F_ID;
    }

    @Override
    public int getClassId() {
        return CacheDataSerializerHook.SET_EXPIRY_POLICY_BACKUP;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);
        out.writeInt(keys.size());
        for (Data key: keys) {
            IOUtil.writeData(out, key);
        }
        IOUtil.writeData(out, expiryPolicy);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);
        int s = in.readInt();
        keys = new ArrayList<>(s);
        while (s-- > 0) {
            keys.add(IOUtil.readData(in));
        }
        expiryPolicy = IOUtil.readData(in);

    }

    @Override
    public boolean requiresTenantContext() {
        return true;
    }
}
