/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.data.crac_impl.json;

import com.farao_community.farao.data.crac_api.json.serializers.*;

import com.farao_community.farao.data.crac_impl.ComplexContingency;
import com.farao_community.farao.data.crac_impl.SimpleCrac;
import com.farao_community.farao.data.crac_impl.cnec.FlowCnecImpl;
import com.farao_community.farao.data.crac_api.json.serializers.network_action.InjectionSetPointSerializer;
import com.farao_community.farao.data.crac_api.json.serializers.network_action.NetworkActionSerializer;
import com.farao_community.farao.data.crac_api.json.serializers.network_action.PstSetPointSerializer;
import com.farao_community.farao.data.crac_api.json.serializers.network_action.TopologySerializer;
import com.farao_community.farao.data.crac_api.json.serializers.range_action.RangeActionSerializer;
import com.farao_community.farao.data.crac_api.json.serializers.usage_rule.FreeToUseSerializer;
import com.farao_community.farao.data.crac_api.json.serializers.usage_rule.OnStateSerializer;
import com.farao_community.farao.data.crac_impl.remedial_action.network_action.*;
import com.farao_community.farao.data.crac_impl.remedial_action.range_action.PstRangeActionImpl;
import com.farao_community.farao.data.crac_impl.usage_rule.FreeToUseImpl;
import com.farao_community.farao.data.crac_impl.usage_rule.OnStateImpl;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
public class CracImplJsonModule extends SimpleModule {

    public CracImplJsonModule() {
        super();
        this.addSerializer(ComplexContingency.class, new ContingencySerializer());
        this.addSerializer(FreeToUseImpl.class, new FreeToUseSerializer());
        this.addSerializer(OnStateImpl.class, new OnStateSerializer());
        this.addSerializer(NetworkActionImpl.class, new NetworkActionSerializer());
        this.addSerializer(PstSetpointImpl.class, new PstSetPointSerializer());
        this.addSerializer(InjectionSetpointImpl.class, new InjectionSetPointSerializer());
        this.addSerializer(TopologicalActionImpl.class, new TopologySerializer());
        this.addSerializer(PstSetpointImpl.class, new PstSetPointSerializer());
        this.addSerializer(PstRangeActionImpl.class, new RangeActionSerializer<>());
        this.addSerializer(SimpleCrac.class, new CracSerializer());
        this.addSerializer(FlowCnecImpl.class, new BranchCnecSerializer<FlowCnecImpl>());
    }
}
