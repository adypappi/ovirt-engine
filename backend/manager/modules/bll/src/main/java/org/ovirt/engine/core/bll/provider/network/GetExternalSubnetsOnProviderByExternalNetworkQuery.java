/*
* Copyright (c) 2014 Red Hat, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.ovirt.engine.core.bll.provider.network;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.network.ProviderNetwork;
import org.ovirt.engine.core.common.queries.GetExternalSubnetsOnProviderByExternalNetworkQueryParameters;
import org.ovirt.engine.core.dao.provider.ProviderDao;

public class GetExternalSubnetsOnProviderByExternalNetworkQuery
        <P extends GetExternalSubnetsOnProviderByExternalNetworkQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private ProviderDao providerDao;
    @Inject
    private ProviderProxyFactory providerProxyFactory;

    public GetExternalSubnetsOnProviderByExternalNetworkQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        Provider<?> provider = providerDao.get(getParameters().getProviderId());
        if (provider == null) {
            return;
        }

        NetworkProviderProxy client = providerProxyFactory.create(provider);
        ProviderNetwork providedBy = new ProviderNetwork();
        providedBy.setProviderId(getParameters().getProviderId());
        providedBy.setExternalId(getParameters().getNetworkId());
        getQueryReturnValue().setReturnValue(client.getAllSubnets(providedBy));
    }
}
