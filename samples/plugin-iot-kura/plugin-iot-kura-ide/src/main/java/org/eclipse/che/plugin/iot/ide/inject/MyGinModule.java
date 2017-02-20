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
package org.eclipse.che.plugin.iot.ide.inject;

import com.google.gwt.inject.client.AbstractGinModule;

import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.plugin.iot.ide.blocklyview.BlocklyView;
import org.eclipse.che.plugin.iot.ide.blocklyview.BlocklyViewImpl;

/**
 * Gin module binding the {@link BlocklyView} to the {@link BlocklyViewImpl} implementation class.
 *
 * @author Vasco
 */
@ExtensionGinModule
public class MyGinModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(BlocklyView.class).to(BlocklyViewImpl.class);
    }

}
