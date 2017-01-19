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

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;

import javax.validation.constraints.NotNull;

/**
 * The class contains methods to display terminal.
 *
 * @author Dmitry Shnurenko
 */
final class TerminalViewImpl extends Composite implements TerminalView, RequiresResize {

    interface TerminalViewImplUiBinder extends UiBinder<Widget, TerminalViewImpl> {
    }

    private final static TerminalViewImplUiBinder UI_BINDER = GWT.create(TerminalViewImplUiBinder.class);

    @UiField
    FlowPanel terminalPanel;

    @UiField
    Label     unavailableLabel;

    private ActionDelegate delegate;

    private double charWidth;
    private double charHeight;
    private Element termElem;

    public TerminalViewImpl() {
        initWidget(UI_BINDER.createAndBindUi(this));
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    /** {@inheritDoc} */
    @Override
    public void openTerminal(@NotNull final TerminalJso terminal) {
        unavailableLabel.setVisible(false);

        terminalPanel.setVisible(true);
        terminalPanel.getElement().getStyle().setProperty("opacity", "0");

        terminal.open(terminalPanel.getElement());
        //terminal.fit();

        TerminalGeometryJso termGeometry = terminal.proposeGeometry();

        this.termElem  = terminal.getElement();
        this.charWidth = getWidth()/termGeometry.getCols();
        this.charHeight = getHeight()/termGeometry.getRows();

        terminalPanel.getElement().getFirstChildElement().getStyle().clearProperty("backgroundColor");
        terminalPanel.getElement().getFirstChildElement().getStyle().clearProperty("color");
        terminalPanel.getElement().getStyle().clearProperty("opacity");
    }

    private int getWidth() {
        if (termElem != null) {
            int scrollWidth = 17;
            Element parent = termElem.getParentElement();
            return Math.max(0, parent.getOffsetWidth() - scrollWidth);
        }
        return 0;
    }

    private int getHeight() {
        if (termElem != null) {
            return termElem.getParentElement().getOffsetHeight();
        }
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public void showErrorMessage(@NotNull String message) {
        unavailableLabel.setText(message);
        unavailableLabel.setVisible(true);

        terminalPanel.setVisible(false);
    }

    @Override
    public void onResize() {
        if (termElem != null) {
            resizeTimer.cancel();
            resizeTimer.schedule(200);
        }
    }

    private Timer resizeTimer = new Timer() {
        @Override
        public void run() {
            resizeTerminal();
        }
    };

    private void resizeTerminal() {
        int offsetWidth = getWidth();
        int offsetHeight = getHeight();
        if (offsetWidth <= 0 || offsetHeight <= 0) {
            resizeTimer.cancel();
            resizeTimer.schedule(500);
            return;
        }

        int cols = (int)Math.floor(offsetWidth/charWidth);
        int rows = (int)Math.floor(offsetHeight/charHeight);
        delegate.setTerminalSize(cols, rows);
    }
}
