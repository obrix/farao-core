/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.search_tree_rao;

import com.farao_community.farao.commons.CountryBoundary;
import com.farao_community.farao.commons.CountryGraph;
import com.farao_community.farao.commons.Unit;
import com.farao_community.farao.data.crac_api.Crac;
import com.farao_community.farao.data.crac_api.Instant;
import com.farao_community.farao.data.crac_api.cnec.FlowCnec;
import com.farao_community.farao.data.crac_api.network_action.ActionType;
import com.farao_community.farao.data.crac_api.network_action.NetworkAction;
import com.farao_community.farao.data.crac_api.range_action.PstRangeAction;
import com.farao_community.farao.data.crac_api.range_action.RangeAction;
import com.farao_community.farao.data.crac_api.range_action.RangeType;
import com.farao_community.farao.data.crac_api.threshold.BranchThresholdRule;
import com.farao_community.farao.data.crac_api.usage_rule.UsageMethod;
import com.farao_community.farao.data.crac_impl.utils.CommonCracCreation;
import com.farao_community.farao.data.crac_impl.utils.NetworkImportsUtil;
import com.farao_community.farao.rao_commons.result_api.RangeActionResult;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Network;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;

/**
 * @author Peter Mitri {@literal <peter.mitri at rte-france.com>}
 */
public class SearchTreeBloomerTest {

    private Crac crac;
    private Network network;

    @Before
    public void setUp() {
        network = NetworkImportsUtil.import12NodesNetwork();
        crac = CommonCracCreation.create();
    }

    @Test
    public void testGetOptimizedMostLimitingElementsLocation() {

        SearchTreeBloomer bloomer = new SearchTreeBloomer(network, mock(RangeActionResult.class), 0, Integer.MAX_VALUE, null, null, false, 0, new ArrayList<>());

        Leaf leaf = mock(Leaf.class);
        Mockito.when(leaf.getVirtualCostNames()).thenReturn(Set.of("mnec", "lf"));

        Mockito.when(leaf.getMostLimitingElements(1)).thenReturn(List.of(crac.getFlowCnec("cnec1basecase"))); // be fr
        Mockito.when(leaf.getCostlyElements(eq("mnec"), anyInt())).thenReturn(List.of(crac.getFlowCnec("cnec2basecase"))); // de fr
        Mockito.when(leaf.getCostlyElements(eq("lf"), anyInt())).thenReturn(Collections.emptyList());
        assertEquals(Set.of(Optional.of(Country.BE), Optional.of(Country.FR), Optional.of(Country.DE)), bloomer.getOptimizedMostLimitingElementsLocation(leaf));

        Mockito.when(leaf.getMostLimitingElements(1)).thenReturn(List.of(crac.getFlowCnec("cnec1basecase"))); // be fr
        Mockito.when(leaf.getCostlyElements(eq("mnec"), anyInt())).thenReturn(Collections.emptyList());
        Mockito.when(leaf.getCostlyElements(eq("lf"), anyInt())).thenReturn(Collections.emptyList());
        assertEquals(Set.of(Optional.of(Country.BE), Optional.of(Country.FR)), bloomer.getOptimizedMostLimitingElementsLocation(leaf));

        Mockito.when(leaf.getMostLimitingElements(1)).thenReturn(Collections.emptyList());
        Mockito.when(leaf.getCostlyElements(eq("mnec"), anyInt())).thenReturn(Collections.emptyList());
        Mockito.when(leaf.getCostlyElements(eq("lf"), anyInt())).thenReturn(List.of(crac.getFlowCnec("cnec2basecase"))); // de fr
        assertEquals(Set.of(Optional.of(Country.FR), Optional.of(Country.DE)), bloomer.getOptimizedMostLimitingElementsLocation(leaf));

        Mockito.when(leaf.getMostLimitingElements(1)).thenReturn(Collections.emptyList());
        Mockito.when(leaf.getCostlyElements(eq("mnec"), anyInt())).thenReturn(List.of(crac.getFlowCnec("cnec1basecase"), crac.getFlowCnec("cnec2basecase"))); // be de fr
        Mockito.when(leaf.getCostlyElements(eq("lf"), anyInt())).thenReturn(Collections.emptyList());
        assertEquals(Set.of(Optional.of(Country.BE), Optional.of(Country.FR), Optional.of(Country.DE)), bloomer.getOptimizedMostLimitingElementsLocation(leaf));

        Mockito.when(leaf.getMostLimitingElements(1)).thenReturn(Collections.emptyList());
        Mockito.when(leaf.getCostlyElements(eq("mnec"), anyInt())).thenReturn(List.of(crac.getFlowCnec("cnec2basecase")));
        Mockito.when(leaf.getCostlyElements(eq("lf"), anyInt())).thenReturn(List.of(crac.getFlowCnec("cnec1basecase")));
        assertEquals(Set.of(Optional.of(Country.BE), Optional.of(Country.FR), Optional.of(Country.DE)), bloomer.getOptimizedMostLimitingElementsLocation(leaf));
    }

    @Test
    public void testIsNetworkActionCloseToLocations() {
        NetworkAction na1 = crac.newNetworkAction().withId("na")
            .newTopologicalAction().withNetworkElement("BBE2AA1  FFR3AA1  1").withActionType(ActionType.OPEN).add()
            .newFreeToUseUsageRule().withUsageMethod(UsageMethod.AVAILABLE).withInstant(Instant.PREVENTIVE).add()
            .add();
        NetworkAction na2 = mock(NetworkAction.class);
        Mockito.when(na2.getLocation(network)).thenReturn(Set.of(Optional.of(Country.FR), Optional.empty()));

        HashSet<CountryBoundary> boundaries = new HashSet<>();
        boundaries.add(new CountryBoundary(Country.FR, Country.BE));
        boundaries.add(new CountryBoundary(Country.FR, Country.DE));
        boundaries.add(new CountryBoundary(Country.DE, Country.AT));
        CountryGraph countryGraph = new CountryGraph(boundaries);

        SearchTreeBloomer bloomer = new SearchTreeBloomer(network, mock(RangeActionResult.class), 0, Integer.MAX_VALUE, null, null, false, 0, new ArrayList<>());
        assertTrue(bloomer.isNetworkActionCloseToLocations(na1, Set.of(Optional.empty()), countryGraph));
        assertTrue(bloomer.isNetworkActionCloseToLocations(na1, Set.of(Optional.of(Country.FR)), countryGraph));
        assertTrue(bloomer.isNetworkActionCloseToLocations(na1, Set.of(Optional.of(Country.BE)), countryGraph));
        assertFalse(bloomer.isNetworkActionCloseToLocations(na1, Set.of(Optional.of(Country.DE)), countryGraph));
        assertFalse(bloomer.isNetworkActionCloseToLocations(na1, Set.of(Optional.of(Country.AT)), countryGraph));
        assertTrue(bloomer.isNetworkActionCloseToLocations(na2, Set.of(Optional.of(Country.AT)), countryGraph));

        bloomer = new SearchTreeBloomer(network, mock(RangeActionResult.class), 0, Integer.MAX_VALUE, null, null, true, 1, new ArrayList<>());
        assertTrue(bloomer.isNetworkActionCloseToLocations(na1, Set.of(Optional.of(Country.DE)), countryGraph));
        assertFalse(bloomer.isNetworkActionCloseToLocations(na1, Set.of(Optional.of(Country.AT)), countryGraph));

        bloomer = new SearchTreeBloomer(network, mock(RangeActionResult.class), 0, Integer.MAX_VALUE, null, null, true, 2, new ArrayList<>());
        assertTrue(bloomer.isNetworkActionCloseToLocations(na1, Set.of(Optional.of(Country.AT)), countryGraph));
    }

    private NetworkActionCombination createNetworkActionCombination(String... networkElementIds) {

        for (String networkElementId : networkElementIds) {
            crac.newNetworkAction().withId("na - " + networkElementId)
                .newTopologicalAction().withNetworkElement(networkElementId).withActionType(ActionType.OPEN).add()
                .add();
        }

        return new NetworkActionCombination(Arrays.stream(networkElementIds).map(neId -> crac.getNetworkAction("na - " + neId)).collect(Collectors.toSet()));
    }

    private NetworkAction createNetworkActionWithOperator(String networkElementId, String operator) {
        return crac.newNetworkAction().withId("na - " + networkElementId).withOperator(operator)
            .newTopologicalAction().withNetworkElement(networkElementId).withActionType(ActionType.OPEN).add()
            .add();
    }

    private PstRangeAction createPstRangeActionWithOperator(String networkElementId, String operator) {
        Map<Integer, Double> conversionMap = new HashMap<>();
        conversionMap.put(0, 0.);
        conversionMap.put(1, 1.);
        return crac.newPstRangeAction().withId("pst - " + networkElementId).withOperator(operator).withNetworkElement(networkElementId)
            .newFreeToUseUsageRule().withInstant(Instant.PREVENTIVE).withUsageMethod(UsageMethod.AVAILABLE).add()
            .newTapRange().withRangeType(RangeType.ABSOLUTE).withMinTap(-16).withMaxTap(16).add()
            .withInitialTap(0)
            .withTapToAngleConversionMap(conversionMap)
            .add();
    }

    @Test
    public void testRemoveNetworkActionsFarFromMostLimitingElement() {
        NetworkActionCombination naFrBe = createNetworkActionCombination("BBE2AA1  FFR3AA1  1");
        NetworkActionCombination naDe = createNetworkActionCombination("DDE1AA1  DDE2AA1  1");
        NetworkActionCombination naFr = createNetworkActionCombination("FFR2AA1  FFR3AA1  1");
        NetworkActionCombination naDeNl = createNetworkActionCombination("DDE2AA1  NNL3AA1  1");
        NetworkActionCombination naNlBe = createNetworkActionCombination("NNL2AA1  BBE3AA1  1");
        NetworkActionCombination naNl = createNetworkActionCombination("NNL2AA1  NNL3AA1  1");
        NetworkActionCombination naCombDe = createNetworkActionCombination("DDE1AA1  DDE3AA1  1", "DDE2AA1  DDE3AA1  1");
        NetworkActionCombination naCombDeFrBe = createNetworkActionCombination("FFR2AA1  DDE3AA1  1", "BBE1AA1  BBE2AA1  1");

        List<NetworkActionCombination> naCombinations = List.of(naFrBe, naDe, naFr, naDeNl, naNlBe, naNl, naCombDe, naCombDeFrBe);

        Leaf leaf = mock(Leaf.class);
        Mockito.when(leaf.getVirtualCostNames()).thenReturn(Collections.emptySet());

        SearchTreeBloomer bloomer = new SearchTreeBloomer(network, mock(RangeActionResult.class), 0, Integer.MAX_VALUE, null, null, true, 0, new ArrayList<>());
        Mockito.when(leaf.getMostLimitingElements(1)).thenReturn(List.of(crac.getFlowCnec("cnec1basecase"))); // be fr
        assertEquals(List.of(naFrBe, naFr, naNlBe, naCombDeFrBe), bloomer.removeCombinationsFarFromMostLimitingElement(naCombinations, leaf));

        Mockito.when(leaf.getMostLimitingElements(1)).thenReturn(List.of(crac.getFlowCnec("cnec2basecase"))); // de fr
        assertEquals(List.of(naFrBe, naDe, naFr, naDeNl, naCombDe, naCombDeFrBe), bloomer.removeCombinationsFarFromMostLimitingElement(naCombinations, leaf));

        FlowCnec cnecBe = crac.newFlowCnec()
            .withId("cnecBe")
            .withNetworkElement("BBE1AA1  BBE2AA1  1")
            .withInstant(Instant.PREVENTIVE)
            .withOptimized(true)
            .withOperator("operator1")
            .newThreshold()
            .withUnit(Unit.MEGAWATT)
            .withRule(BranchThresholdRule.ON_LEFT_SIDE)
            .withMin(-1500.)
            .withMax(1500.)
            .add()
            .withNominalVoltage(380.)
            .withIMax(5000.)
            .add();

        bloomer = new SearchTreeBloomer(network, mock(RangeActionResult.class), 0, Integer.MAX_VALUE, null, null, true, 1, new ArrayList<>());
        Mockito.when(leaf.getMostLimitingElements(1)).thenReturn(List.of(cnecBe)); // be
        assertEquals(List.of(naFrBe, naFr, naDeNl, naNlBe, naNl, naCombDeFrBe), bloomer.removeCombinationsFarFromMostLimitingElement(naCombinations, leaf));
    }

    @Test
    public void testGetActivatedTsos() {
        NetworkAction na = createNetworkActionWithOperator("FFR2AA1  FFR3AA1  1", "fr");

        RangeAction activatedRa = createPstRangeActionWithOperator("BBE2AA1  BBE3AA1  1", "be");
        RangeAction nonActivatedRa = createPstRangeActionWithOperator("NNL2AA1  NNL3AA1  1", "nl");
        Set<RangeAction> rangeActions = new HashSet<>();
        rangeActions.add(activatedRa);
        rangeActions.add(nonActivatedRa);

        RangeActionResult prePerimeterRangeActionResult = Mockito.mock(RangeActionResult.class);
        Mockito.when(prePerimeterRangeActionResult.getOptimizedSetPoint(activatedRa)).thenReturn(0.);
        Mockito.when(prePerimeterRangeActionResult.getOptimizedSetPoint(nonActivatedRa)).thenReturn(0.);

        Leaf leaf = Mockito.mock(Leaf.class);
        Mockito.when(leaf.getActivatedNetworkActions()).thenReturn(Collections.singleton(na));
        Mockito.when(leaf.getRangeActions()).thenReturn(rangeActions);
        Mockito.when(leaf.getOptimizedSetPoint(activatedRa)).thenReturn(5.);
        Mockito.when(leaf.getOptimizedSetPoint(nonActivatedRa)).thenReturn(0.);

        SearchTreeBloomer bloomer = new SearchTreeBloomer(network, mock(RangeActionResult.class), 0, Integer.MAX_VALUE, null, null, false, 0, new ArrayList<>());
        Set<String> activatedTsos = bloomer.getActivatedTsos(leaf);

        assertEquals(2, activatedTsos.size());
        assertTrue(activatedTsos.contains("fr"));
        assertTrue(activatedTsos.contains("be"));
    }

    /*
    @Test
    public void testRemoveNetworkActionsNotInTso() {
        NetworkAction nafr1 = createNetworkActionWithOperator("FFR1AA1  FFR2AA1  1", "fr");
        NetworkAction nafr2 = createNetworkActionWithOperator("FFR1AA1  FFR3AA1  1", "fr");
        NetworkAction nabe1 = createNetworkActionWithOperator("BBE1AA1  BBE2AA1  1", "be");
        NetworkAction nabe2 = createNetworkActionWithOperator("BBE1AA1  BBE3AA1  1", "be");

        Set<NetworkAction> allNetworkActions = new HashSet<>();
        allNetworkActions.add(nafr1);
        allNetworkActions.add(nafr2);
        allNetworkActions.add(nabe1);
        allNetworkActions.add(nabe2);

        SearchTreeBloomer bloomer = new SearchTreeBloomer(network, mock(RangeActionResult.class), 0, Integer.MAX_VALUE, null, null, false, 0, new ArrayList<>());
        Set<NetworkAction> filteredNetworkActions = bloomer.removeNetworkActionsTsoNotInSet(allNetworkActions, Collections.singleton("fr"));

        assertEquals(2, filteredNetworkActions.size());
        assertTrue(filteredNetworkActions.contains(nafr1));
        assertTrue(filteredNetworkActions.contains(nafr2));

    }
     */
}
