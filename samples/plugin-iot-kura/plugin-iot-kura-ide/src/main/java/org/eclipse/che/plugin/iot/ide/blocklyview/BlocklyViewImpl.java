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

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.eclipse.che.ide.api.parts.base.BaseView;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.plugin.iot.ide.BlocklyResources;

/**
 * Implementation class of the {@link BlocklyView}.
 *
 * @author Edgar Mueller
 */
public class BlocklyViewImpl extends BaseView<BlocklyView.ActionDelegate> implements BlocklyView{


    interface BlocklyViewImplUiBinder extends UiBinder<Widget, BlocklyViewImpl> {
    }

    private final static BlocklyViewImplUiBinder UI_BINDER = GWT.create(BlocklyViewImplUiBinder.class);

//    @UiField
//    FlowPanel blocklyConfig;

//    @UiField
//    HTMLPanel htmlPanel;

    @Inject
    public BlocklyViewImpl(PartStackUIResources resources){
        super(resources);
        Log.info(BlocklyViewImpl.class, "BlocklyViewImpl() invoked");
        setContentWidget(UI_BINDER.createAndBindUi(this));
    }

}
