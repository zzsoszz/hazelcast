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

package com.hazelcast.client.impl.protocol.codec;

import com.hazelcast.client.impl.protocol.ClientMessage;
import com.hazelcast.client.impl.protocol.Generated;
import com.hazelcast.client.impl.protocol.codec.builtin.*;
import com.hazelcast.client.impl.protocol.codec.custom.*;
import com.hazelcast.logging.Logger;

import javax.annotation.Nullable;

import static com.hazelcast.client.impl.protocol.ClientMessage.*;
import static com.hazelcast.client.impl.protocol.codec.builtin.FixedSizeTypesCodec.*;

/*
 * This file is auto-generated by the Hazelcast Client Protocol Code Generator.
 * To change this file, edit the templates or the protocol
 * definitions on the https://github.com/hazelcast/hazelcast-client-protocol
 * and regenerate it.
 */

/**
 * Adds listener for backup acks
 */
@SuppressWarnings("unused")
@Generated("441d4fcd554e6ffc0ea5ed32b499b0e4")
public final class ClientLocalBackupListenerCodec {
    //hex: 0x000F00
    public static final int REQUEST_MESSAGE_TYPE = 3840;
    //hex: 0x000F01
    public static final int RESPONSE_MESSAGE_TYPE = 3841;
    private static final int REQUEST_INITIAL_FRAME_SIZE = PARTITION_ID_FIELD_OFFSET + INT_SIZE_IN_BYTES;
    private static final int RESPONSE_RESPONSE_FIELD_OFFSET = RESPONSE_BACKUP_ACKS_FIELD_OFFSET + BYTE_SIZE_IN_BYTES;
    private static final int RESPONSE_INITIAL_FRAME_SIZE = RESPONSE_RESPONSE_FIELD_OFFSET + UUID_SIZE_IN_BYTES;
    private static final int EVENT_BACKUP_SOURCE_INVOCATION_CORRELATION_ID_FIELD_OFFSET = PARTITION_ID_FIELD_OFFSET + INT_SIZE_IN_BYTES;
    private static final int EVENT_BACKUP_INITIAL_FRAME_SIZE = EVENT_BACKUP_SOURCE_INVOCATION_CORRELATION_ID_FIELD_OFFSET + LONG_SIZE_IN_BYTES;
    //hex: 0x000F02
    private static final int EVENT_BACKUP_MESSAGE_TYPE = 3842;

    private ClientLocalBackupListenerCodec() {
    }

    public static ClientMessage encodeRequest() {
        ClientMessage clientMessage = ClientMessage.createForEncode();
        clientMessage.setRetryable(false);
        clientMessage.setOperationName("Client.LocalBackupListener");
        ClientMessage.Frame initialFrame = new ClientMessage.Frame(new byte[REQUEST_INITIAL_FRAME_SIZE], UNFRAGMENTED_MESSAGE);
        encodeInt(initialFrame.content, TYPE_FIELD_OFFSET, REQUEST_MESSAGE_TYPE);
        encodeInt(initialFrame.content, PARTITION_ID_FIELD_OFFSET, -1);
        clientMessage.add(initialFrame);
        return clientMessage;
    }

    public static ClientMessage encodeResponse(java.util.UUID response) {
        ClientMessage clientMessage = ClientMessage.createForEncode();
        ClientMessage.Frame initialFrame = new ClientMessage.Frame(new byte[RESPONSE_INITIAL_FRAME_SIZE], UNFRAGMENTED_MESSAGE);
        encodeInt(initialFrame.content, TYPE_FIELD_OFFSET, RESPONSE_MESSAGE_TYPE);
        encodeUUID(initialFrame.content, RESPONSE_RESPONSE_FIELD_OFFSET, response);
        clientMessage.add(initialFrame);

        return clientMessage;
    }

    /**
     * Returns the registration id for the listener.
     */
    public static java.util.UUID decodeResponse(ClientMessage clientMessage) {
        ClientMessage.ForwardFrameIterator iterator = clientMessage.frameIterator();
        ClientMessage.Frame initialFrame = iterator.next();
        return decodeUUID(initialFrame.content, RESPONSE_RESPONSE_FIELD_OFFSET);
    }

    public static ClientMessage encodeBackupEvent(long sourceInvocationCorrelationId) {
        ClientMessage clientMessage = ClientMessage.createForEncode();
        ClientMessage.Frame initialFrame = new ClientMessage.Frame(new byte[EVENT_BACKUP_INITIAL_FRAME_SIZE], UNFRAGMENTED_MESSAGE);
        initialFrame.flags |= ClientMessage.IS_EVENT_FLAG;
        encodeInt(initialFrame.content, TYPE_FIELD_OFFSET, EVENT_BACKUP_MESSAGE_TYPE);
        encodeInt(initialFrame.content, PARTITION_ID_FIELD_OFFSET, -1);
        encodeLong(initialFrame.content, EVENT_BACKUP_SOURCE_INVOCATION_CORRELATION_ID_FIELD_OFFSET, sourceInvocationCorrelationId);
        clientMessage.add(initialFrame);

        return clientMessage;
    }

    public abstract static class AbstractEventHandler {

        public void handle(ClientMessage clientMessage) {
            int messageType = clientMessage.getMessageType();
            ClientMessage.ForwardFrameIterator iterator = clientMessage.frameIterator();
            if (messageType == EVENT_BACKUP_MESSAGE_TYPE) {
                ClientMessage.Frame initialFrame = iterator.next();
                long sourceInvocationCorrelationId = decodeLong(initialFrame.content, EVENT_BACKUP_SOURCE_INVOCATION_CORRELATION_ID_FIELD_OFFSET);
                handleBackupEvent(sourceInvocationCorrelationId);
                return;
            }
            Logger.getLogger(super.getClass()).finest("Unknown message type received on event handler :" + messageType);
        }

        /**
         * @param sourceInvocationCorrelationId correlation id of the invocation that backup acks belong to
         */
        public abstract void handleBackupEvent(long sourceInvocationCorrelationId);
    }
}
