/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.data.crac_creation.creator.cse.parameters;

import com.farao_community.farao.data.crac_creation.creator.api.parameters.CracCreationParameters;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Baptiste Seguinot {@literal <baptiste.seguinot at rte-france.com>}
 */
public class CseCracCreationParametersTest {

    @Test
    public void testDefaultParameters() {
        CseCracCreationParameters parameters = new CseCracCreationParameters();

        assertEquals(0, parameters.getRangeActionGroupsAsString().size());
        assertEquals(0, parameters.getRangeActionGroups().size());
    }

    @Test
    public void testParallelRaConf() {

        CseCracCreationParameters parameters = new CseCracCreationParameters();
        List<String> parallelRaAsConcatenatedString = new ArrayList<>();
        parallelRaAsConcatenatedString.add("rangeAction1 + rangeAction3 + rangeAction7");
        parallelRaAsConcatenatedString.add("errorInThisOne");

        parameters.setRangeActionGroupsAsString(parallelRaAsConcatenatedString);

        assertEquals(1, parameters.getRangeActionGroupsAsString().size());
        assertEquals(1, parameters.getRangeActionGroups().size());
        assertEquals("rangeAction1 + rangeAction3 + rangeAction7", parameters.getRangeActionGroups().get(0).toString());
    }

    @Test
    public void testParametersWithinExtendable() {
        CracCreationParameters parameters = new CracCreationParameters();
        assertNull(parameters.getExtension(CseCracCreationParameters.class));

        parameters.addExtension(CseCracCreationParameters.class, new CseCracCreationParameters());
        assertNotNull(parameters.getExtension(CseCracCreationParameters.class));
    }

}
