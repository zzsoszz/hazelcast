/*
 * Copyright (c) 2008-2019, Hazelcast, Inc. All Rights Reserved.
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

package com.hazelcast.spi.impl.operationservice.impl;

import com.hazelcast.core.Member;
import com.hazelcast.core.MemberLeftException;
import com.hazelcast.internal.partition.InternalPartition;
import com.hazelcast.internal.partition.PartitionReplica;
import com.hazelcast.nio.Address;
import com.hazelcast.nio.EndpointManager;
import com.hazelcast.spi.ExceptionAction;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.ReadonlyOperation;

import static com.hazelcast.spi.ExceptionAction.THROW_EXCEPTION;

/**
 * A {@link Invocation} evaluates a Operation Invocation for a particular partition running on top of the
 * {@link OperationServiceImpl}.
 */
final class PartitionInvocation extends Invocation<PartitionReplica> {

    private final boolean failOnIndeterminateOperationState;

    PartitionInvocation(Context context,
                        Operation op,
                        Runnable doneCallback,
                        int tryCount,
                        long tryPauseMillis,
                        long callTimeoutMillis,
                        boolean deserialize,
                        boolean failOnIndeterminateOperationState,
                        EndpointManager endpointManager) {
        super(context, op, doneCallback, tryCount, tryPauseMillis, callTimeoutMillis, deserialize, endpointManager);
        this.failOnIndeterminateOperationState = failOnIndeterminateOperationState && !(op instanceof ReadonlyOperation);
    }

    PartitionInvocation(Context context,
                        Operation op,
                        int tryCount,
                        long tryPauseMillis,
                        long callTimeoutMillis,
                        boolean deserialize,
                        boolean failOnIndeterminateOperationState) {
        this(context, op, null, tryCount, tryPauseMillis, callTimeoutMillis, deserialize,
                failOnIndeterminateOperationState, null);
    }

    @Override
    PartitionReplica getInvocationTarget() {
        InternalPartition partition = context.partitionService.getPartition(op.getPartitionId());
        return partition.getReplica(op.getReplicaIndex());
    }

    @Override
    Address toTargetAddress(PartitionReplica replica) {
        return replica.address();
    }

    @Override
    Member toTargetMember(PartitionReplica replica) {
        return context.clusterService.getMember(replica.address(), replica.uuid());
    }

    @Override
    protected boolean shouldFailOnIndeterminateOperationState() {
        return failOnIndeterminateOperationState;
    }

    @Override
    ExceptionAction onException(Throwable t) {
        if (shouldFailOnIndeterminateOperationState() && (t instanceof MemberLeftException)) {
            return THROW_EXCEPTION;
        }

        ExceptionAction action = op.onInvocationException(t);
        return action != null ? action : THROW_EXCEPTION;
    }
}
