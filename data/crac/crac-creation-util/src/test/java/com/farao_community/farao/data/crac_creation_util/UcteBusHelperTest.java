/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.farao_community.farao.data.crac_creation_util;

/**
 * @author Peter Mitri {@literal <peter.mitri at rte-france.com>}
 */
public class UcteBusHelperTest {

    /*
    @Test
    public void testWithNetwork() {
        Network network = Importers.loadNetwork("TestCase_severalVoltageLevels_Xnodes_8characters.uct", getClass().getResourceAsStream("/TestCase_severalVoltageLevels_Xnodes_8characters.uct"));
        UcteNetworkAnalyzer ucteNetworkAnalyzerWhiteSpaces = new UcteNetworkAnalyzer(network, new UcteNetworkAnalyzerProperties(COMPLETE_WITH_WHITESPACES));
        UcteNetworkAnalyzer ucteNetworkAnalyzerWildCards = new UcteNetworkAnalyzer(network, new UcteNetworkAnalyzerProperties(COMPLETE_WITH_WILDCARDS));

        UcteBusHelper busHelper = new UcteBusHelper("DDE2AA1*", ucteNetworkAnalyzerWhiteSpaces);
        assertTrue(busHelper.isValid());
        assertEquals("DDE2AA11", busHelper.getBusIdInNetwork());
        assertNull(busHelper.getInvalidReason());

        busHelper = new UcteBusHelper("DDE2AA1", ucteNetworkAnalyzerWildCards);
        assertTrue(busHelper.isValid());
        assertEquals("DDE2AA11", busHelper.getBusIdInNetwork());
        assertNull(busHelper.getInvalidReason());

        busHelper = new UcteBusHelper("DDE2AA1", ucteNetworkAnalyzerWhiteSpaces);
        assertFalse(busHelper.isValid());
        assertNull(busHelper.getBusIdInNetwork());
        assertNotNull(busHelper.getInvalidReason());

        busHelper = new UcteBusHelper("DDE1AA11", ucteNetworkAnalyzerWhiteSpaces);
        assertTrue(busHelper.isValid());
        assertEquals("DDE1AA11", busHelper.getBusIdInNetwork());
        assertNull(busHelper.getInvalidReason());

        // doesn't exist
        busHelper = new UcteBusHelper("AAAAAAAA", ucteNetworkAnalyzerWhiteSpaces);
        assertFalse(busHelper.isValid());
        assertNull(busHelper.getBusIdInNetwork());
        assertNotNull(busHelper.getInvalidReason());

        // Too many matches
        busHelper = new UcteBusHelper("DDE1AA1*", ucteNetworkAnalyzerWhiteSpaces);
        assertFalse(busHelper.isValid());
        assertNull(busHelper.getBusIdInNetwork());
        assertNotNull(busHelper.getInvalidReason());
    }

     */
}
