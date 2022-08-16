/*
 * Copyright (c) 2008-2020, Hazelcast, Inc. All Rights Reserved.
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

package com.hazelcast.client.impl.protocol;

import com.hazelcast.client.AuthenticationException;
import com.hazelcast.client.UndefinedErrorCodeException;
import com.hazelcast.client.impl.StubAuthenticationException;
import com.hazelcast.client.impl.clientside.ClientExceptionFactory;
import com.hazelcast.test.HazelcastParallelClassRunner;
import com.hazelcast.test.HazelcastTestSupport;
import com.hazelcast.test.annotation.ParallelTest;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import usercodedeployment.CustomExceptions;

import static org.junit.Assert.assertEquals;

@RunWith(HazelcastParallelClassRunner.class)
@Category({QuickTest.class, ParallelTest.class})
public class ClientProtocolErrorCodesTest extends HazelcastTestSupport {

    @Test
    public void testConstructor() {
        assertUtilityConstructor(ClientProtocolErrorCodes.class);
    }

    @Test
    public void testUndefinedException() {
        ClientExceptions exceptions = new ClientExceptions(false);
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        ClientExceptionFactory exceptionFactory = new ClientExceptionFactory(false, contextClassLoader);

        ClientMessage message = exceptions.createExceptionMessage(new CustomExceptions.CustomExceptionNonStandardSignature(1));
        ClientMessage responseMessage = ClientMessage.createForDecode(message.buffer(), 0);
        Throwable resurrectedThrowable = exceptionFactory.createException(responseMessage);
        assertEquals(UndefinedErrorCodeException.class, resurrectedThrowable.getClass());
    }

    @Test
    public void testAuthenticationException() {
        ClientExceptions exceptions = new ClientExceptions(false);
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        ClientExceptionFactory exceptionFactory = new ClientExceptionFactory(false, contextClassLoader);

        ClientMessage exceptionMessage = exceptions.createExceptionMessage(new StubAuthenticationException("failed"));
        ClientMessage responseMessage = ClientMessage.createForDecode(exceptionMessage.buffer(), 0);
        Throwable resurrectedThrowable = exceptionFactory.createException(responseMessage);
        assertEquals(AuthenticationException.class, resurrectedThrowable.getClass());
    }
}