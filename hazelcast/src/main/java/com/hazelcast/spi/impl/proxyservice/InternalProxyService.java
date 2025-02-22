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

package com.hazelcast.spi.impl.proxyservice;

import java.util.UUID;

/**
 * The API for the internal {@link ProxyService}.
 *
 * The ProxyService is responsible for managing proxies and it part of the SPI. The InternalProxyService extends this
 * interface and additional methods we don't want to expose in the SPI, we can add here.
 */
public interface InternalProxyService extends ProxyService {

    void destroyLocalDistributedObject(String serviceName, String name, UUID source, boolean fireEvent);
}
