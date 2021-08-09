/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.data.crac_creation_util;

import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;
import org.junit.Test;

import static com.farao_community.farao.data.crac_creation_util.UcteMatchingResult.MatchStatus.*;
import static junit.framework.TestCase.*;

/**
 * @author Peter Mitri {@literal <peter.mitri at rte-france.com>}
 * @author Baptiste Seguinot{@literal <baptiste.seguinot at rte-france.com>}
 */
public class UcteConnectableCollectionTest {

    private Network network;
    private UcteConnectableCollection ucteConnectableCollection;

    private void init(String networkFile) {
        network = Importers.loadNetwork(networkFile, getClass().getResourceAsStream("/" + networkFile));
        ucteConnectableCollection = new UcteConnectableCollection(network);
    }

    @Test
    public void testInternalBranch() {
        init("TestCase_severalVoltageLevels_Xnodes.uct");

        // internal branch with order code, from/to same as network
        UcteMatchingResult result = ucteConnectableCollection.lookForConnectable("BBE1AA1 ", "BBE2AA1 ", "1");
        assertTrue(result.hasMatched());
        assertFalse(result.isInverted());
        assertEquals(UcteConnectable.Side.BOTH, result.getSide());
        assertSame(network.getIdentifiable("BBE1AA1  BBE2AA1  1"), result.getIidmIdentifiable());

        // internal branch with element name, from/to same as network
        result = ucteConnectableCollection.lookForConnectable("FFR1AA1*", "FFR3AA1*", "BR FR1FR3");
        assertTrue(result.hasMatched());
        assertFalse(result.isInverted());
        assertEquals(UcteConnectable.Side.BOTH, result.getSide());
        assertSame(network.getIdentifiable("FFR1AA1  FFR3AA1  2"), result.getIidmIdentifiable());

        // internal branch with order code, from/to different from network
        result = ucteConnectableCollection.lookForConnectable("BBE2AA1 ", "BBE1AA1 ", "1");
        assertTrue(result.hasMatched());
        assertTrue(result.isInverted());
        assertEquals(UcteConnectable.Side.BOTH, result.getSide());
        assertSame(network.getIdentifiable("BBE1AA1  BBE2AA1  1"), result.getIidmIdentifiable());

        // internal branch with element name, from/to different from network
        result = ucteConnectableCollection.lookForConnectable("BBE3AA1*", "BBE1AA1 ", "BR BE1BE3");
        assertTrue(result.hasMatched());
        assertTrue(result.isInverted());
        assertEquals(UcteConnectable.Side.BOTH, result.getSide());
        assertSame(network.getIdentifiable("BBE1AA1  BBE3AA1  1"), result.getIidmIdentifiable());
    }

    @Test
    public void testInvalidInternalBranch() {
        init("TestCase_severalVoltageLevels_Xnodes.uct");

        // unknown from
        UcteMatchingResult result = ucteConnectableCollection.lookForConnectable("UNKNOW1 ", "BBE1AA1 ", "1");
        assertFalse(result.hasMatched());
        assertEquals(NOT_FOUND, result.getStatus());

        // unknown to
        result = ucteConnectableCollection.lookForConnectable("BBE3AA1 ", "UNKNOW1 ", "1");
        assertFalse(result.hasMatched());
        assertEquals(NOT_FOUND, result.getStatus());

        // branch exists but not with this order code
        result = ucteConnectableCollection.lookForConnectable("BBE1AA1 ", "BBE2AA1 ", "4");
        assertFalse(result.hasMatched());
        assertEquals(NOT_FOUND, result.getStatus());

        // branch exists but not with this element name
        result = ucteConnectableCollection.lookForConnectable("BBE1AA1 ", "BBE3AA1 ", "COUCOU");
        assertFalse(result.hasMatched());
        assertEquals(NOT_FOUND, result.getStatus());
    }

    @Test
    public void testValidTransformer() {
        init("TestCase_severalVoltageLevels_Xnodes.uct");

        /*
        note that transformers from/to node are systematically inverted by PowSyBl UCTE importer
        For instance, the transformer with id "UCTNODE1 UCTNODE2 1" have :
            - terminal1 = UCTNODE2
            - terminal2 = UCTNODE1
        That's why the branch is inverted when from/to is aligned with what is defined in the UCTE file, and vice versa.
        This is note the case for the other type of Branch, where terminal 1 and 2 match the id.

         */

        // transformer with order code, from/to same as network
        UcteMatchingResult result = ucteConnectableCollection.lookForConnectable("BBE2AA1 ", "BBE3AA1 ", "1");
        assertTrue(result.hasMatched());
        assertTrue(result.isInverted());
        assertEquals(UcteConnectable.Side.BOTH, result.getSide());
        assertSame(network.getIdentifiable("BBE2AA1  BBE3AA1  1"), result.getIidmIdentifiable());

        result = ucteConnectableCollection.lookForConnectable("FFR1AA2*", "FFR1AA1 ", "5");
        assertTrue(result.hasMatched());
        assertTrue(result.isInverted());
        assertEquals(UcteConnectable.Side.BOTH, result.getSide());
        assertSame(network.getIdentifiable("FFR1AA2  FFR1AA1  5"), result.getIidmIdentifiable());

        // transformer with element name, from/to same as network
        result = ucteConnectableCollection.lookForConnectable("BBE2AA1 ", "BBE3AA1 ", "PST BE");
        assertTrue(result.hasMatched());
        assertTrue(result.isInverted());
        assertEquals(UcteConnectable.Side.BOTH, result.getSide());
        assertSame(network.getIdentifiable("BBE2AA1  BBE3AA1  1"), result.getIidmIdentifiable());

        result = ucteConnectableCollection.lookForConnectable("BBE1AA1*", "BBE1AA2*", "TR BE1");
        assertTrue(result.hasMatched());
        assertTrue(result.isInverted());
        assertEquals(UcteConnectable.Side.BOTH, result.getSide());
        assertSame(network.getIdentifiable("BBE1AA1  BBE1AA2  1"), result.getIidmIdentifiable());

        // transformer with order code, from/to different from network
        result = ucteConnectableCollection.lookForConnectable("BBE3AA1 ", "BBE2AA1 ", "1");
        assertTrue(result.hasMatched());
        assertFalse(result.isInverted());
        assertEquals(UcteConnectable.Side.BOTH, result.getSide());
        assertSame(network.getIdentifiable("BBE2AA1  BBE3AA1  1"), result.getIidmIdentifiable());

        result = ucteConnectableCollection.lookForConnectable("BBE2AA1 ", "BBE2AA2*", "2");
        assertTrue(result.hasMatched());
        assertFalse(result.isInverted());
        assertEquals(UcteConnectable.Side.BOTH, result.getSide());
        assertSame(network.getIdentifiable("BBE2AA2  BBE2AA1  2"), result.getIidmIdentifiable());

        // transformer with element name, from/to different from network
        result = ucteConnectableCollection.lookForConnectable("BBE3AA1*", "BBE2AA1 ", "PST BE");
        assertTrue(result.hasMatched());
        assertFalse(result.isInverted());
        assertEquals(UcteConnectable.Side.BOTH, result.getSide());
        assertSame(network.getIdentifiable("BBE2AA1  BBE3AA1  1"), result.getIidmIdentifiable());

        result = ucteConnectableCollection.lookForConnectable("FFR1AA1 ", "FFR1AA2 ", "TR FR1");
        assertTrue(result.hasMatched());
        assertFalse(result.isInverted());
        assertEquals(UcteConnectable.Side.BOTH, result.getSide());
        assertSame(network.getIdentifiable("FFR1AA2  FFR1AA1  5"), result.getIidmIdentifiable());
    }

    @Test
    public void testInvalidTransformer() {
        init("TestCase_severalVoltageLevels_Xnodes.uct");

        // transformer exists but not with this order code
        assertFalse(ucteConnectableCollection.lookForConnectable("BBE2AA1 ", "BBE3AA1 ", "2").hasMatched());

        // transformer exists but not with this element name
        assertFalse(ucteConnectableCollection.lookForConnectable("FFR1AA2 ", "FFR1AA1 ", "COUCOU").hasMatched());
    }

    @Test
    public void testValidTieLine() {
        init("TestCase_severalVoltageLevels_Xnodes.uct");

        // tie-line with order code
        UcteMatchingResult result = ucteConnectableCollection.lookForConnectable("XFRDE11 ", "DDE3AA1 ", "1");
        assertTrue(result.hasMatched());
        assertTrue(result.isInverted());
        assertEquals(UcteConnectable.Side.ONE, result.getSide());
        assertSame(network.getIdentifiable("XFRDE11  DDE3AA1  1 + XFRDE11  FFR2AA1  1"), result.getIidmIdentifiable());

        result = ucteConnectableCollection.lookForConnectable("DDE3AA1 ", "XFRDE11 ", "1");
        assertTrue(result.hasMatched());
        assertFalse(result.isInverted());
        assertEquals(UcteConnectable.Side.ONE, result.getSide());
        assertSame(network.getIdentifiable("XFRDE11  DDE3AA1  1 + XFRDE11  FFR2AA1  1"), result.getIidmIdentifiable());

        result = ucteConnectableCollection.lookForConnectable("XFRDE11*", "FFR2AA1*", "1");
        assertTrue(result.hasMatched());
        assertFalse(result.isInverted());
        assertEquals(UcteConnectable.Side.TWO, result.getSide());
        assertSame(network.getIdentifiable("XFRDE11  DDE3AA1  1 + XFRDE11  FFR2AA1  1"), result.getIidmIdentifiable());

        result = ucteConnectableCollection.lookForConnectable("FFR2AA1 ", "XFRDE11 ", "1");
        assertTrue(result.hasMatched());
        assertTrue(result.isInverted());
        assertEquals(UcteConnectable.Side.TWO, result.getSide());
        assertSame(network.getIdentifiable("XFRDE11  DDE3AA1  1 + XFRDE11  FFR2AA1  1"), result.getIidmIdentifiable());

        // tie-line with element name
        result = ucteConnectableCollection.lookForConnectable("NNL2AA1*", "XNLBE11*", "TL NL2X");
        assertTrue(result.hasMatched());
        assertFalse(result.isInverted());
        assertEquals(UcteConnectable.Side.ONE, result.getSide());
        assertSame(network.getIdentifiable("NNL2AA1  XNLBE11  1 + XNLBE11  BBE3AA1  1"), result.getIidmIdentifiable());

        result = ucteConnectableCollection.lookForConnectable("XNLBE11 ", "NNL2AA1 ", "TL NL2X");
        assertTrue(result.hasMatched());
        assertTrue(result.isInverted());
        assertEquals(UcteConnectable.Side.ONE, result.getSide());
        assertSame(network.getIdentifiable("NNL2AA1  XNLBE11  1 + XNLBE11  BBE3AA1  1"), result.getIidmIdentifiable());

        result = ucteConnectableCollection.lookForConnectable("XNLBE11 ", "BBE3AA1*", "TL BE3X");
        assertTrue(result.hasMatched());
        assertFalse(result.isInverted());
        assertEquals(UcteConnectable.Side.TWO, result.getSide());
        assertSame(network.getIdentifiable("NNL2AA1  XNLBE11  1 + XNLBE11  BBE3AA1  1"), result.getIidmIdentifiable());

        result = ucteConnectableCollection.lookForConnectable("BBE3AA1*", "XNLBE11 ", "TL BE3X");
        assertTrue(result.hasMatched());
        assertTrue(result.isInverted());
        assertEquals(UcteConnectable.Side.TWO, result.getSide());
        assertSame(network.getIdentifiable("NNL2AA1  XNLBE11  1 + XNLBE11  BBE3AA1  1"), result.getIidmIdentifiable());
    }

    @Test
    public void testInvalidTieLine() {
        init("TestCase_severalVoltageLevels_Xnodes.uct");

        // tie-line exists but not with this order code
        assertFalse(ucteConnectableCollection.lookForConnectable("XFRDE11 ", "FFR2AA1 ", "7").hasMatched());

        // tie-line exists but not with this element name
        assertFalse(ucteConnectableCollection.lookForConnectable("NNL2AA1 ", "XNLBE11 ", "COUCOU").hasMatched());
    }

    @Test
    public void testValidDanglingLine() {
        init("TestCase_severalVoltageLevels_Xnodes.uct");

        // dangling-line with order code
        UcteMatchingResult result = ucteConnectableCollection.lookForConnectable("BBE2AA1 ", "XBE2AL1 ", "1");
        assertTrue(result.hasMatched());
        assertTrue(result.isInverted());
        assertEquals(UcteConnectable.Side.BOTH, result.getSide());
        assertSame(network.getIdentifiable("BBE2AA1  XBE2AL1  1"), result.getIidmIdentifiable());

        result = ucteConnectableCollection.lookForConnectable("XBE2AL1 ", "BBE2AA1 ", "1");
        assertTrue(result.hasMatched());
        assertFalse(result.isInverted());
        assertEquals(UcteConnectable.Side.BOTH, result.getSide());
        assertSame(network.getIdentifiable("BBE2AA1  XBE2AL1  1"), result.getIidmIdentifiable());

        // dangling-line with element name
        result = ucteConnectableCollection.lookForConnectable("XDE2AL1*", "DDE2AA1*", "DL AL");
        assertTrue(result.hasMatched());
        assertFalse(result.isInverted());
        assertEquals(UcteConnectable.Side.BOTH, result.getSide());
        assertSame(network.getIdentifiable("XDE2AL1  DDE2AA1  1"), result.getIidmIdentifiable());

        result = ucteConnectableCollection.lookForConnectable("DDE2AA1*", "XDE2AL1*", "DL AL");
        assertTrue(result.hasMatched());
        assertTrue(result.isInverted());
        assertEquals(UcteConnectable.Side.BOTH, result.getSide());
        assertSame(network.getIdentifiable("XDE2AL1  DDE2AA1  1"), result.getIidmIdentifiable());
    }

    @Test
    public void testInvalidDanglingLine() {
        init("TestCase_severalVoltageLevels_Xnodes.uct");

        // dangling-line exists but not with this order code
        assertFalse(ucteConnectableCollection.lookForConnectable("XBE2AL1 ", "BBE2AA1 ", "2").hasMatched());

        // dangling-line exists but not with this element name
        assertFalse(ucteConnectableCollection.lookForConnectable("DDE2AA1 ", "XDE2AL1 ", "COUCOU").hasMatched());
    }

    @Test
    public void testSwitch() {
        init("TestCase16Nodes_with_different_imax.uct");

        UcteMatchingResult result = ucteConnectableCollection.lookForConnectable("BBE1AA1 ", "BBE4AA1 ", "1");
        assertTrue(result.hasMatched());
        assertFalse(result.isInverted());
        assertEquals(UcteConnectable.Side.BOTH, result.getSide());
        assertSame(network.getIdentifiable("BBE1AA1  BBE4AA1  1"), result.getIidmIdentifiable());

        result = ucteConnectableCollection.lookForConnectable("BBE4AA1*", "BBE1AA1*", "1");
        assertTrue(result.hasMatched());
        assertTrue(result.isInverted());
        assertEquals(UcteConnectable.Side.BOTH, result.getSide());
        assertSame(network.getIdentifiable("BBE1AA1  BBE4AA1  1"), result.getIidmIdentifiable());
    }

    @Test
    public void someMoreTestsWithWildcards() {
        init("TestCase_severalVoltageLevels_Xnodes_8characters.uct");

        UcteMatchingResult result = ucteConnectableCollection.lookForConnectable("DDE1AA1*", "DDE2AA1*", "2");
        assertTrue(result.hasMatched());
        assertFalse(result.isInverted());
        assertEquals(UcteConnectable.Side.BOTH, result.getSide());
        assertSame(network.getIdentifiable("DDE1AA12 DDE2AA11 2"), result.getIidmIdentifiable());

        result = ucteConnectableCollection.lookForConnectable("DDE2AA1*", "DDE1AA1*", "E_NAME_2");
        assertTrue(result.hasMatched());
        assertTrue(result.isInverted());
        assertEquals(UcteConnectable.Side.BOTH, result.getSide());
        assertSame(network.getIdentifiable("DDE1AA12 DDE2AA11 2"), result.getIidmIdentifiable());

        result = ucteConnectableCollection.lookForConnectable("XNLBE11*", "BBE3AA1*", "1");
        assertTrue(result.hasMatched());
        assertFalse(result.isInverted());
        assertEquals(UcteConnectable.Side.TWO, result.getSide());
        assertSame(network.getIdentifiable("NNL2AA13 XNLBE111 1 + XNLBE111 BBE3AA12 1"), result.getIidmIdentifiable());

        result = ucteConnectableCollection.lookForConnectable("BBE3AA1*", "BBE2AA1*", "PST BE");
        assertTrue(result.hasMatched());
        assertFalse(result.isInverted());
        assertEquals(UcteConnectable.Side.BOTH, result.getSide());
        assertSame(network.getIdentifiable("BBE2AA11 BBE3AA12 1"), result.getIidmIdentifiable());
    }

    @Test
    public void testTooManyMatches() {
        init("TestCase_severalVoltageLevels_Xnodes_8characters.uct");

        UcteMatchingResult result = ucteConnectableCollection.lookForConnectable("DDE1AA1*", "DDE2AA1*", "1");
        assertFalse(result.hasMatched());
        assertEquals(SEVERAL_MATCH, result.getStatus());

        result = ucteConnectableCollection.lookForConnectable("DDE1AA1*", "DDE2AA1*", "E_NAME_1");
        assertFalse(result.hasMatched());
        assertEquals(SEVERAL_MATCH, result.getStatus());
    }
}
