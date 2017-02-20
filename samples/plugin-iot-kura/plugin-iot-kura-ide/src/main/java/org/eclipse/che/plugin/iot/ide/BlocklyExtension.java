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
package org.eclipse.che.plugin.iot.ide;

import com.google.inject.Inject;

import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.plugin.iot.ide.blocklyview.BlocklyViewAction;

import static org.eclipse.che.ide.api.action.IdeActions.GROUP_MAIN_MENU;

@Extension(title = "Blockly Extension")
public class BlocklyExtension {

    /**
     * Constructor.
     *
     * @param actionManager the {@link ActionManager} that is used to register the action
     * @param action the {@link BlocklyViewAction} that display the Blockly view
     */
    @Inject
    public BlocklyExtension(ActionManager actionManager, BlocklyViewAction action){
        actionManager.registerAction("blocklyViewAction",action);
        DefaultActionGroup mainMenu = (DefaultActionGroup)actionManager.getAction(GROUP_MAIN_MENU);

        DefaultActionGroup sampleActionGroup = new DefaultActionGroup("Open Blockly", true, actionManager);
        sampleActionGroup.add(action);

        mainMenu.add(sampleActionGroup);

        DefaultActionGroup mainContextMenuGroup = (DefaultActionGroup) actionManager.getAction("resourceOperation");
        mainContextMenuGroup.add(sampleActionGroup);
    }
}
