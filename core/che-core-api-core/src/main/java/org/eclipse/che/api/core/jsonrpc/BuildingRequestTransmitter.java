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
package org.eclipse.che.api.core.jsonrpc;

import org.eclipse.che.api.core.jsonrpc.transmission.EndpointIdConfigurator;

/**
 * Simple factory that provides facilities to manually build JSON RPC requests
 */
public interface BuildingRequestTransmitter {
    EndpointIdConfigurator newRequest();
}
