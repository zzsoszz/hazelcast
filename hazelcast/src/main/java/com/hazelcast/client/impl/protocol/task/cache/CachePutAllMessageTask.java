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

package com.hazelcast.client.impl.protocol.task.cache;

import com.hazelcast.cache.impl.CacheOperationProvider;
import com.hazelcast.client.impl.protocol.ClientMessage;
import com.hazelcast.client.impl.protocol.codec.CachePutAllCodec;
import com.hazelcast.instance.impl.Node;
import com.hazelcast.internal.nio.Connection;
import com.hazelcast.internal.serialization.Data;
import com.hazelcast.security.SecurityInterceptorConstants;
import com.hazelcast.security.permission.ActionConstants;
import com.hazelcast.security.permission.CachePermission;
import com.hazelcast.spi.impl.operationservice.Operation;

import javax.cache.expiry.ExpiryPolicy;
import java.security.Permission;
import java.util.Map;

import static com.hazelcast.internal.util.MapUtil.createHashMap;

/**
 * This client request specifically calls {@link com.hazelcast.cache.impl.operation.CachePutAllOperation} on the server side.
 *
 * @see com.hazelcast.cache.impl.operation.CachePutAllOperation
 */
public class CachePutAllMessageTask
        extends AbstractCacheMessageTask<CachePutAllCodec.RequestParameters> {

    public CachePutAllMessageTask(ClientMessage clientMessage, Node node, Connection connection) {
        super(clientMessage, node, connection);
    }

    @Override
    protected Operation prepareOperation() {
        CacheOperationProvider operationProvider = getOperationProvider(parameters.name);
        ExpiryPolicy expiryPolicy = (ExpiryPolicy) nodeEngine.toObject(parameters.expiryPolicy);
        return operationProvider
                .createPutAllOperation(parameters.entries, expiryPolicy, parameters.completionId);
    }

    @Override
    protected CachePutAllCodec.RequestParameters decodeClientMessage(ClientMessage clientMessage) {
        return CachePutAllCodec.decodeRequest(clientMessage);
    }

    @Override
    protected ClientMessage encodeResponse(Object response) {
        return CachePutAllCodec.encodeResponse();
    }

    @Override
    public Permission getRequiredPermission() {
        return new CachePermission(parameters.name, ActionConstants.ACTION_PUT);
    }

    @Override
    public String getDistributedObjectName() {
        return parameters.name;
    }

    @Override
    public Object[] getParameters() {
        Map<Data, Data> map = createMap();

        if (parameters.expiryPolicy == null) {
            return new Object[]{map};
        }
        return new Object[]{map, parameters.expiryPolicy};
    }

    @Override
    public String getMethodName() {
        return SecurityInterceptorConstants.PUT_ALL;
    }

    private Map<Data, Data> createMap() {
        Map<Data, Data> map = createHashMap(parameters.entries.size());
        for (Map.Entry<Data, Data> entry : parameters.entries) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }
}
