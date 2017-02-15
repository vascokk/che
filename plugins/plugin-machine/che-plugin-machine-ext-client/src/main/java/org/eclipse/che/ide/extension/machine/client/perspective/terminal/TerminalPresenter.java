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
package org.eclipse.che.ide.extension.machine.client.perspective.terminal;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.machine.MachineEntity;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.collections.Jso;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.content.TabPresenter;
import org.eclipse.che.ide.extension.machine.client.processes.AddTerminalClickHandler;
import org.eclipse.che.plugin.requirejs.ide.ModuleHolder;
import org.eclipse.che.ide.websocket.WebSocket;
import org.eclipse.che.ide.websocket.events.ConnectionClosedHandler;
import org.eclipse.che.ide.websocket.events.ConnectionErrorHandler;
import org.eclipse.che.ide.websocket.events.ConnectionOpenedHandler;
import org.eclipse.che.ide.websocket.events.MessageReceivedEvent;
import org.eclipse.che.ide.websocket.events.MessageReceivedHandler;
import org.eclipse.che.ide.websocket.events.WebSocketClosedEvent;

import javax.validation.constraints.NotNull;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.NOT_EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.websocket.events.WebSocketClosedEvent.CLOSE_NORMAL;

/**
 * The class defines methods which contains business logic to control machine's terminal.
 *
 * @author Dmitry Shnurenko
 */
public class TerminalPresenter implements TabPresenter, TerminalView.ActionDelegate {

    //event which is performed when user input data into terminal
    private static final String DATA_EVENT_NAME          = "data";
    private static final int    TIME_BETWEEN_CONNECTIONS = 2_000;

    private final TerminalView                    view;
    private final Object                          source;
    private final NotificationManager             notificationManager;
    private final MachineLocalizationConstant     locale;
    private final MachineEntity                   machine;
    private final TerminalInitializePromiseHolder terminalHolder;
    private final ModuleHolder                    moduleHolder;

    private WebSocket             socket;
    private boolean               connected;
    private int                   countRetry;
    private TerminalJso           terminal;
    private TerminalStateListener terminalStateListener;
    private int                   width;
    private int                   height;

    @Inject
    public TerminalPresenter(TerminalView view,
                             NotificationManager notificationManager,
                             MachineLocalizationConstant locale,
                             @Assisted MachineEntity machine,
                             @Assisted Object source,
                             final TerminalInitializePromiseHolder terminalHolder,
                             final ModuleHolder moduleHolder) {
        this.view = view;
        this.source = source;
        view.setDelegate(this);
        this.notificationManager = notificationManager;
        this.locale = locale;
        this.machine = machine;

        connected = false;
        countRetry = 2;
        this.terminalHolder = terminalHolder;
        this.moduleHolder = moduleHolder;
    }

    /**
     * Connects to special WebSocket which allows get information from terminal on server side. The terminal is initialized only
     * when the method is called the first time.
     */
    public void connect() {
        if (countRetry == 0) {
            return;
        }

        if (!connected) {
            terminalHolder.getInitializerPromise().then(new Operation<Void>() {
                @Override
                public void apply(Void arg) throws OperationException {
                    connectToTerminalWebSocket(machine.getTerminalUrl());
                }
            }).catchError(new Operation<PromiseError>() {
                @Override
                public void apply(PromiseError arg) throws OperationException {
                    notificationManager.notify(locale.failedToConnectTheTerminal(),
                                               locale.terminalCanNotLoadScript(),
                                               FAIL,
                                               NOT_EMERGE_MODE);
                    reconnect();
                }
            });
        }
    }

    private void reconnect() {
        if (countRetry <= 0) {
            view.showErrorMessage(locale.terminalErrorStart());
        } else {
            view.showErrorMessage(locale.terminalTryRestarting());
            new Timer() {
                @Override
                public void run() {
                    connect();
                }
            }.schedule(TIME_BETWEEN_CONNECTIONS);
        }
    }

    private void connectToTerminalWebSocket(@NotNull String wsUrl) {
        countRetry--;

        socket = WebSocket.create(wsUrl);

        socket.setOnMessageHandler(new MessageReceivedHandler() {
            @Override
            public void onMessageReceived(MessageReceivedEvent event) {
                terminal.write(event.getMessage());
            }
        });

        socket.setOnCloseHandler(new ConnectionClosedHandler() {
            @Override
            public void onClose(WebSocketClosedEvent event) {
                if (CLOSE_NORMAL == event.getCode()) {
                    connected = false;
                    terminalStateListener.onExit();
                }
            }
        });

        socket.setOnOpenHandler(new ConnectionOpenedHandler() {
            @Override
            public void onOpen() {
                JavaScriptObject terminalJso = moduleHolder.getModule("Xterm");
                // if terminal was created programmatically then we don't set focus on it
                TerminalOptionsJso terminalOptionsJso = TerminalOptionsJso.createDefault();
                if (source instanceof AddTerminalClickHandler || source instanceof Action) {
                    terminalOptionsJso.withFocusOnOpen(true);
                }
                terminal = TerminalJso.create(terminalJso, terminalOptionsJso);
                connected = true;

                view.openTerminal(terminal);

                terminal.on(DATA_EVENT_NAME, new Operation<String>() {
                    @Override
                    public void apply(String arg) throws OperationException {
                        Jso jso = Jso.create();
                        jso.addField("type", "data");
                        jso.addField("data", arg);
                        socket.send(jso.serialize());
                    }
                });
            }
        });

        socket.setOnErrorHandler(new ConnectionErrorHandler() {
            @Override
            public void onError() {
                connected = false;

                if (countRetry == 0) {
                    view.showErrorMessage(locale.terminalErrorStart());
                    notificationManager.notify(locale.connectionFailedWithTerminal(), locale.terminalErrorConnection(), FAIL, FLOAT_MODE);
                } else {
                    reconnect();
                }
            }
        });
    }

    /**
     * Sends 'close' message on server side to stop terminal.
     */
    public void stopTerminal() {
        if (connected) {
            Jso jso = Jso.create();
            jso.addField("type", "close");
            socket.send(jso.serialize());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }

    /** {@inheritDoc} */
    @Override
    @NotNull
    public IsWidget getView() {
        return view;
    }

    /** {@inheritDoc} */
    @Override
    public void setVisible(boolean visible) {
        view.setVisible(visible);
    }

    @Override
    public void setTerminalSize(int x, int y) {
        if (!connected) {
            return;
        }

        if (width == x && height == y) {
            return;
        }

        terminal.resize(x, y);
        width = x;
        height = y;

        Jso jso = Jso.create();
        JsArrayInteger arr = Jso.createArray().cast();
        arr.set(0, x);
        arr.set(1, y);
        jso.addField("type", "resize");
        jso.addField("data", arr);
        socket.send(jso.serialize());
    }

    /**
     * Sets listener that will be called when a terminal state changed
     */
    public void setListener(TerminalStateListener listener) {
        this.terminalStateListener = listener;
    }

    /** Listener that will be called when a terminal state changed. */
    public interface TerminalStateListener {
        void onExit();
    }

}
