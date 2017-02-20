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
package org.eclipse.che.plugin.iot.ide.blocklyview;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.parts.PartStackType;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.util.loging.Log;


@Singleton
public class BlocklyViewAction extends Action {

    private WorkspaceAgent workspaceAgent;
    private BlocklyPresenter blocklyPresenter;

    /**
     * Constructor.
     *
     * @param workspaceAgent the {@link WorkspaceAgent} that will open our sample part
     * @param blocklyPresenter the {@link BlocklyPresenter} displaying the view
     *
     */
    @Inject
    public BlocklyViewAction(WorkspaceAgent workspaceAgent, BlocklyPresenter blocklyPresenter) {
        super("Show Blockly View 2");
        this.workspaceAgent = workspaceAgent;
        this.blocklyPresenter = blocklyPresenter;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        Log.info(BlocklyViewAction.class, "actionPerformed() invoked");

        workspaceAgent.openPart(blocklyPresenter, PartStackType.INFORMATION);
        workspaceAgent.setActivePart(blocklyPresenter);
        blocklyPresenter.getView().asWidget().setVisible(true);
//        BlocklyOverlay.startBlockly();
        BlocklyOverlay.startBlocklyResizable();
        blocklyPresenter.getView().asWidget().setVisible(true);

        Log.info(BlocklyViewAction.class, "actionPerformed() finished");
    }
}
