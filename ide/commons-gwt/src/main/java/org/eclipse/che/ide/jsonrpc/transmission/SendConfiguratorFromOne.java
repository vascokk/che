/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.jsonrpc.transmission;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Executor;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.api.promises.client.js.RejectFunction;
import org.eclipse.che.api.promises.client.js.ResolveFunction;
import org.eclipse.che.ide.jsonrpc.JsonRpcFactory;
import org.eclipse.che.ide.jsonrpc.JsonRpcParams;
import org.eclipse.che.ide.jsonrpc.JsonRpcRequest;
import org.eclipse.che.ide.jsonrpc.ResponseDispatcher;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.ng.WebSocketMessageTransmitter;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Configurator defines the type of a result (if present) and send a request.
 * Result types that are supported: {@link String}, {@link Boolean},
 * {@link Double}, {@link Void} and DTO. This configurator is used when we
 * have defined request params as a single object.
 *
 * @param <P> type of params objects
 */
public class SendConfiguratorFromOne<P> {
    private final ResponseDispatcher          dispatcher;
    private final WebSocketMessageTransmitter transmitter;
    private final JsonRpcFactory              factory;
    private final String                      method;
    private final P                           pValue;
    private final String                      endpointId;

    public SendConfiguratorFromOne(ResponseDispatcher dispatcher, WebSocketMessageTransmitter transmitter,
                                   JsonRpcFactory factory, String method, P pValue, String endpointId) {
        this.dispatcher = dispatcher;
        this.transmitter = transmitter;
        this.factory = factory;
        this.method = method;
        this.pValue = pValue;
        this.endpointId = endpointId;
    }

    public void sendAndSkipResult() {
        Log.debug(getClass(), "Transmitting request: " +
                              "endpoint ID: " + endpointId + ", " +
                              "method: " + method + ", " +
                              (pValue != null ? "params object class: " + pValue.getClass() + ", " : "") +
                              "params list value" + pValue);

        transmitNotification();
    }

    public <R> Promise<R> sendAndReceiveResultAsDto(final Class<R> rClass) {
        checkNotNull(rClass, "Result class value must not be null");

        final String requestId = transmitRequest();

        Log.debug(getClass(), "Transmitting request: " +
                              "endpoint ID: " + endpointId + ", " +
                              "request ID: " + requestId + ", " +
                              "method: " + method + ", " +
                              "params object class: " + pValue.getClass() + ", " +
                              "params list value" + pValue + ", " +
                              "result object class: " + rClass);

        return Promises.create(new Executor.ExecutorBody<R>() {
            @Override
            public void apply(ResolveFunction<R> resolve, RejectFunction reject) {
                dispatcher.registerPromiseOfOne(endpointId, requestId, rClass, resolve, reject);
            }
        });
    }

    public Promise<String> sendAndReceiveResultAsString() {
        final String requestId = transmitRequest();

        Log.debug(getClass(), "Transmitting request: " +
                              "endpoint ID: " + endpointId + ", " +
                              "request ID: " + requestId + ", " +
                              "method: " + method + ", " +
                              "params object class: " + pValue.getClass() + ", " +
                              "params list value" + pValue + ", " +
                              "result object class: " + String.class);

        return Promises.create(new Executor.ExecutorBody<String>() {
            @Override
            public void apply(ResolveFunction<String> resolve, RejectFunction reject) {
                dispatcher.registerPromiseOfOne(endpointId, requestId, String.class, resolve, reject);
            }
        });
    }

    public Promise<Double> sendAndReceiveResultAsDouble() {
        final String requestId = transmitRequest();

        Log.debug(getClass(), "Transmitting request: " +
                              "endpoint ID: " + endpointId + ", " +
                              "request ID: " + requestId + ", " +
                              "method: " + method + ", " +
                              "params object class: " + pValue.getClass() + ", " +
                              "params list value" + pValue + ", " +
                              "result object class: " + Double.class);

        return Promises.create(new Executor.ExecutorBody<Double>() {
            @Override
            public void apply(ResolveFunction<Double> resolve, RejectFunction reject) {
                dispatcher.registerPromiseOfOne(endpointId, requestId, Double.class, resolve, reject);
            }
        });
    }

    public Promise<Boolean> sendAndReceiveResultAsBoolean() {
        final String requestId = transmitRequest();

        Log.debug(getClass(), "Transmitting request: " +
                              "endpoint ID: " + endpointId + ", " +
                              "request ID: " + requestId + ", " +
                              "method: " + method + ", " +
                              "params object class: " + pValue.getClass() + ", " +
                              "params list value" + pValue + ", " +
                              "result object class: " + Boolean.class);

        return Promises.create(new Executor.ExecutorBody<Boolean>() {
            @Override
            public void apply(ResolveFunction<Boolean> resolve, RejectFunction reject) {
                dispatcher.registerPromiseOfOne(endpointId, requestId, Boolean.class, resolve, reject);
            }
        });
    }

    public <R> Promise<List<R>> sendAndReceiveResultAsListOfDto(final Class<R> rClass) {
        checkNotNull(rClass, "Result class value must not be null");

        final String requestId = transmitRequest();

        Log.debug(getClass(), "Transmitting request: " +
                              "endpoint ID: " + endpointId + ", " +
                              "request ID: " + requestId + ", " +
                              "method: " + method + ", " +
                              "params object class: " + pValue.getClass() + ", " +
                              "params list value" + pValue + ", " +
                              "result list items class: " + rClass);

        return Promises.create(new Executor.ExecutorBody<List<R>>() {
            @Override
            public void apply(ResolveFunction<List<R>> resolve, RejectFunction reject) {
                dispatcher.registerPromiseOfMany(endpointId, requestId, rClass, resolve, reject);
            }
        });
    }

    public Promise<List<String>> sendAndReceiveResultAsListOfString() {
        final String requestId = transmitRequest();

        Log.debug(getClass(), "Transmitting request: " +
                              "endpoint ID: " + endpointId + ", " +
                              "request ID: " + requestId + ", " +
                              "method: " + method + ", " +
                              "params object class: " + pValue.getClass() + ", " +
                              "params list value" + pValue + ", " +
                              "result list items class: " + String.class);

        return Promises.create(new Executor.ExecutorBody<List<String>>() {
            @Override
            public void apply(ResolveFunction<List<String>> resolve, RejectFunction reject) {
                dispatcher.registerPromiseOfMany(endpointId, requestId, String.class, resolve, reject);
            }
        });
    }

    public Promise<List<Boolean>> sendAndReceiveResultAsListOfBoolean() {
        final String requestId = transmitRequest();

        Log.debug(getClass(), "Transmitting request: " +
                              "endpoint ID: " + endpointId + ", " +
                              "request ID: " + requestId + ", " +
                              "method: " + method + ", " +
                              "params object class: " + pValue.getClass() + ", " +
                              "params list value" + pValue + ", " +
                              "result list items class: " + Boolean.class);

        return Promises.create(new Executor.ExecutorBody<List<Boolean>>() {
            @Override
            public void apply(ResolveFunction<List<Boolean>> resolve, RejectFunction reject) {
                dispatcher.registerPromiseOfMany(endpointId, requestId, Boolean.class, resolve, reject);
            }
        });
    }

    public Promise<Void> sendAndReceiveResultAsEmpty() {
        final String requestId = transmitRequest();

        Log.debug(getClass(), "Transmitting request: " +
                              "endpoint ID: " + endpointId + ", " +
                              "request ID: " + requestId + ", " +
                              "method: " + method + ", " +
                              "params object class: " + pValue.getClass() + ", " +
                              "params list value" + pValue + ", " +
                              "result list items class: " + Void.class);

        return Promises.create(new Executor.ExecutorBody<Void>() {
            @Override
            public void apply(ResolveFunction<Void> resolve, RejectFunction reject) {
                dispatcher.registerPromiseOfOne(endpointId, requestId, Void.class, resolve, reject);
            }
        });
    }

    private String transmitRequest() {
        String requestId = Integer.valueOf(MethodNameConfigurator.id.incrementAndGet()).toString();

        JsonRpcParams params = factory.createParams(pValue);
        JsonRpcRequest request = factory.createRequest(requestId, method, params);
        transmitter.transmit(endpointId, request.toString());
        return requestId;
    }

    private void transmitNotification() {
        JsonRpcParams params = factory.createParams(pValue);
        JsonRpcRequest request = factory.createRequest(method, params);
        transmitter.transmit(endpointId, request.toString());
    }
}
