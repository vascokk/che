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

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.ScriptElement;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.plugin.iot.ide.BlocklyResources;
import org.vectomatic.dom.svg.ui.SVGResource;

@Singleton
public class BlocklyPresenter extends BasePresenter implements BlocklyView.ActionDelegate{

    public final static String START_BLOCKLY_FILE_ID = "start_blockly.js";
    public final static String START_BLOCKLY_COMPRESSED_FILE_ID = "blockly/blockly_compressed.js";
    public final static String START_BLOCKS_COMPRESSED_FILE_ID = "blockly/blocks_compressed.js";
    public final static String START_BLOCKLY_MESSAGES_FILE_ID = "blockly/msg/js/en.js";

    private BlocklyView blockleyView;

    private boolean started = false;

    @Inject
    public BlocklyPresenter(final BlocklyView blockleyView) {
        this.blockleyView = blockleyView;
        Log.info(BlocklyPresenter.class, "BlocklyPresenter() invoked");
        injectBlocklyScript();
        Log.info(BlocklyPresenter.class, "injectBlocklyScript() invoked - end");
    }


    private void injectBlocklyScript(){
        Log.info(BlocklyPresenter.class, "injectBlocklyScript() invoked - begin");
        ScriptInjector.fromUrl(GWT.getModuleBaseURL() + START_BLOCKLY_COMPRESSED_FILE_ID)
                .setWindow(ScriptInjector.TOP_WINDOW)
                .setCallback(new Callback<Void, Exception>() {
                    @Override
                    public void onSuccess(final Void result) {
                        Log.info(BlocklyPresenter.class, START_BLOCKLY_COMPRESSED_FILE_ID + " loaded.");
                        injectBlocksScript();
                    }

                    @Override
                    public void onFailure(final Exception e) {
                        Log.error(BlocklyPresenter.class, "Unable to load "+START_BLOCKLY_COMPRESSED_FILE_ID, e);
                    }
                }).inject();

    }

    private void injectBlocksScript(){
        Log.info(BlocklyPresenter.class, "injectBlocksScript() invoked");
        ScriptInjector.fromUrl(GWT.getModuleBaseURL() + START_BLOCKS_COMPRESSED_FILE_ID)
                .setWindow(ScriptInjector.TOP_WINDOW)
                .setCallback(new Callback<Void, Exception>() {
                    @Override
                    public void onSuccess(final Void result) {
                        Log.info(BlocklyPresenter.class, START_BLOCKS_COMPRESSED_FILE_ID + " loaded.");
                        injectBlocklyMessagesScript();
                    }

                    @Override
                    public void onFailure(final Exception e) {
                        Log.error(BlocklyPresenter.class, "Unable to load "+START_BLOCKS_COMPRESSED_FILE_ID, e);
                    }
                }).inject();

    }

    private void injectBlocklyMessagesScript(){
        Log.info(BlocklyPresenter.class, "injectBlocklyMessagesScript() invoked - begin");
        ScriptInjector.fromUrl(GWT.getModuleBaseURL() + START_BLOCKLY_MESSAGES_FILE_ID)
                .setWindow(ScriptInjector.TOP_WINDOW)
                .setCallback(new Callback<Void, Exception>() {
                    @Override
                    public void onSuccess(final Void result) {
                        Log.info(BlocklyPresenter.class, START_BLOCKLY_MESSAGES_FILE_ID + " loaded.");
                        injectBlocklyDiv();
                    }

                    @Override
                    public void onFailure(final Exception e) {
                        Log.error(BlocklyPresenter.class, "Unable to load "+START_BLOCKLY_MESSAGES_FILE_ID, e);
                    }
                }).inject();

    }

    private void injectBlocklyDiv(){
        Log.info(BlocklyPresenter.class, "injectBlocklyDiv() invoked");
        ScriptInjector.fromUrl(GWT.getModuleBaseURL() + START_BLOCKLY_FILE_ID)
                .setWindow(ScriptInjector.TOP_WINDOW)
                .setCallback(new Callback<Void, Exception>() {
                    @Override
                    public void onSuccess(final Void result) {
                        Log.info(BlocklyPresenter.class, START_BLOCKLY_FILE_ID + " loaded.");
                    }

                    @Override
                    public void onFailure(final Exception e) {
                        Log.error(BlocklyPresenter.class, "Unable to load "+START_BLOCKLY_FILE_ID, e);
                    }
                }).inject();

    }

    @Override
    public String getTitle() {
        return "Blockly View 3";
    }

    @Override
    public SVGResource getTitleImage() {
        return (BlocklyResources.INSTANCE.icon());
    }

    @Override
    public IsWidget getView() {
        return blockleyView;
    }

    @Override
    public String getTitleToolTip() {
        return "Blockly View 3 Tooltip";
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(blockleyView);
    }

}
