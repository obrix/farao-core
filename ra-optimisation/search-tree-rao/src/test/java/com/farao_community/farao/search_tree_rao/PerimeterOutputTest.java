/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.farao_community.farao.search_tree_rao;

import com.farao_community.farao.commons.FaraoException;
import com.farao_community.farao.commons.Unit;
import com.farao_community.farao.data.crac_api.cnec.FlowCnec;
import com.farao_community.farao.data.crac_api.network_action.NetworkAction;
import com.farao_community.farao.data.crac_api.range_action.PstRangeAction;
import com.farao_community.farao.data.crac_api.range_action.RangeAction;
import com.farao_community.farao.data.rao_result_api.ComputationStatus;
import com.farao_community.farao.rao_commons.result_api.OptimizationResult;
import com.farao_community.farao.rao_commons.result_api.RangeActionResult;
import com.powsybl.sensitivity.factors.variables.LinearGlsk;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Peter Mitri {@literal <peter.mitri at rte-france.com>}
 */
public class PerimeterOutputTest {
    private static final double DOUBLE_TOLERANCE = 1e-3;

    private PerimeterOutput perimeterOutput;
    private RangeActionResult prePerimeterRangeActionResult;
    private OptimizationResult optimizationResult;
    private RangeAction ra1;
    private RangeAction ra2;
    private FlowCnec flowCnec1;
    private  FlowCnec flowCnec2;
    private NetworkAction na1;
    private NetworkAction na2;
    private PstRangeAction pst1;
    private PstRangeAction pst2;

    @Before
    public void setUp() {
        prePerimeterRangeActionResult = mock(RangeActionResult.class);
        optimizationResult = mock(OptimizationResult.class);

        ra1 = mock(RangeAction.class);
        ra2 = mock(RangeAction.class);
        when(prePerimeterRangeActionResult.getRangeActions()).thenReturn(Set.of(ra1, ra2));
        when(optimizationResult.getRangeActions()).thenReturn(Set.of(ra1, ra2));

        flowCnec1 = mock(FlowCnec.class);
        flowCnec2 = mock(FlowCnec.class);

        na1 = mock(NetworkAction.class);
        na2 = mock(NetworkAction.class);

        pst1 = mock(PstRangeAction.class);
        pst2 = mock(PstRangeAction.class);

        perimeterOutput = new PerimeterOutput(prePerimeterRangeActionResult, optimizationResult);
    }

    @Test
    public void testGetActivatedRangeActions() {
        when(prePerimeterRangeActionResult.getOptimizedSetPoint(ra1)).thenReturn(5.);
        when(optimizationResult.getOptimizedSetPoint(ra1)).thenReturn(5.);
        when(prePerimeterRangeActionResult.getOptimizedSetPoint(ra2)).thenReturn(15.);
        when(optimizationResult.getOptimizedSetPoint(ra2)).thenReturn(50.);
        assertEquals(Set.of(ra2), perimeterOutput.getActivatedRangeActions());
    }

    @Test
    public void testGetFlow() {
        when(optimizationResult.getFlow(flowCnec1, Unit.MEGAWATT)).thenReturn(100.);
        when(optimizationResult.getFlow(flowCnec2, Unit.AMPERE)).thenReturn(200.);
        assertEquals(100., perimeterOutput.getFlow(flowCnec1, Unit.MEGAWATT), DOUBLE_TOLERANCE);
        assertEquals(200., perimeterOutput.getFlow(flowCnec2, Unit.AMPERE), DOUBLE_TOLERANCE);
    }

    @Test
    public void testGetCommercialFlow() {
        when(optimizationResult.getCommercialFlow(flowCnec1, Unit.MEGAWATT)).thenReturn(100.);
        when(optimizationResult.getCommercialFlow(flowCnec2, Unit.AMPERE)).thenReturn(200.);
        assertEquals(100., perimeterOutput.getCommercialFlow(flowCnec1, Unit.MEGAWATT), DOUBLE_TOLERANCE);
        assertEquals(200., perimeterOutput.getCommercialFlow(flowCnec2, Unit.AMPERE), DOUBLE_TOLERANCE);
    }

    @Test
    public void testGetPtdfZonalSum() {
        when(optimizationResult.getPtdfZonalSum(flowCnec1)).thenReturn(100.);
        when(optimizationResult.getPtdfZonalSum(flowCnec2)).thenReturn(200.);
        assertEquals(100., perimeterOutput.getPtdfZonalSum(flowCnec1), DOUBLE_TOLERANCE);
        assertEquals(200., perimeterOutput.getPtdfZonalSum(flowCnec2), DOUBLE_TOLERANCE);
    }

    @Test
    public void testGetPtdfZonalSums() {
        Map<FlowCnec, Double> map = Map.of(flowCnec1, 100., flowCnec2, 200.);
        when(optimizationResult.getPtdfZonalSums()).thenReturn(map);
        assertEquals(map, perimeterOutput.getPtdfZonalSums());
    }

    @Test
    public void testIsActivated() {
        when(optimizationResult.isActivated(na1)).thenReturn(true);
        when(optimizationResult.isActivated(na2)).thenReturn(false);
        assertTrue(perimeterOutput.isActivated(na1));
        assertFalse(perimeterOutput.isActivated(na2));
    }

    @Test
    public void testGetActivatedNetworkActions() {
        when(optimizationResult.getActivatedNetworkActions()).thenReturn(Set.of(na1));
        assertEquals(Set.of(na1), perimeterOutput.getActivatedNetworkActions());
    }

    @Test
    public void testGetFunctionalCost() {
        when(optimizationResult.getFunctionalCost()).thenReturn(1000.);
        assertEquals(1000., perimeterOutput.getFunctionalCost(), DOUBLE_TOLERANCE);
    }

    @Test
    public void testGetMostLimitingElements() {
        when(optimizationResult.getMostLimitingElements(anyInt())).thenReturn(List.of(flowCnec2, flowCnec1));
        assertEquals(List.of(flowCnec2, flowCnec1), perimeterOutput.getMostLimitingElements(100));
    }

    @Test
    public void testGetVirtualCost() {
        when(optimizationResult.getVirtualCost()).thenReturn(1000.);
        assertEquals(1000., perimeterOutput.getVirtualCost(), DOUBLE_TOLERANCE);
    }

    @Test
    public void testGetVirtualCostNames() {
        when(optimizationResult.getVirtualCostNames()).thenReturn(Set.of("lf", "mnec"));
        assertEquals(Set.of("lf", "mnec"), perimeterOutput.getVirtualCostNames());
    }

    @Test
    public void testGetVirtualCostByName() {
        when(optimizationResult.getVirtualCost("lf")).thenReturn(1000.);
        when(optimizationResult.getVirtualCost("mnec")).thenReturn(2000.);
        assertEquals(1000., perimeterOutput.getVirtualCost("lf"), DOUBLE_TOLERANCE);
        assertEquals(2000., perimeterOutput.getVirtualCost("mnec"), DOUBLE_TOLERANCE);
    }

    @Test
    public void testGetCostlyElements() {
        when(optimizationResult.getCostlyElements("lf", 100)).thenReturn(List.of(flowCnec2, flowCnec1));
        when(optimizationResult.getCostlyElements("mnec", 100)).thenReturn(List.of(flowCnec1));
        assertEquals(List.of(flowCnec2, flowCnec1), perimeterOutput.getCostlyElements("lf", 100));
        assertEquals(List.of(flowCnec1), perimeterOutput.getCostlyElements("mnec", 100));
    }

    @Test
    public void testGetRangeActions() {
        assertEquals(Set.of(ra1, ra2), perimeterOutput.getRangeActions());
    }

    @Test
    public void testGetOptimizedTap() {
        when(optimizationResult.getOptimizedTap(pst1)).thenReturn(10);
        when(optimizationResult.getOptimizedTap(pst2)).thenThrow(new FaraoException("absent mock"));
        when(prePerimeterRangeActionResult.getOptimizedTap(pst2)).thenReturn(3);

        assertEquals(10, perimeterOutput.getOptimizedTap(pst1));
        assertEquals(3, perimeterOutput.getOptimizedTap(pst2));
    }

    @Test
    public void testGetOptimizedSetPoint() {
        when(optimizationResult.getRangeActions()).thenReturn(Set.of(ra1));
        when(optimizationResult.getOptimizedSetPoint(ra1)).thenReturn(10.7);
        when(prePerimeterRangeActionResult.getOptimizedSetPoint(ra2)).thenReturn(3.5);

        assertEquals(10.7, perimeterOutput.getOptimizedSetPoint(ra1), DOUBLE_TOLERANCE);
        assertEquals(3.5, perimeterOutput.getOptimizedSetPoint(ra2), DOUBLE_TOLERANCE);
    }

    @Test
    public void testGetOptimizedTaps() {
        Map<PstRangeAction, Integer> map = Map.of(pst1, 10, pst2, 5);
        when(optimizationResult.getOptimizedTaps()).thenReturn(map);
        assertEquals(map, perimeterOutput.getOptimizedTaps());
    }

    @Test
    public void testGetOptimizedSetPoints() {
        Map<RangeAction, Double> map = Map.of(pst1, 10.6, pst2, 5.8, ra1, 52.5, ra2, 100.6);
        when(optimizationResult.getOptimizedSetPoints()).thenReturn(map);
        assertEquals(map, perimeterOutput.getOptimizedSetPoints());
    }

    @Test
    public void testGetSensitivityStatus() {
        when(optimizationResult.getSensitivityStatus()).thenReturn(ComputationStatus.DEFAULT);
        assertEquals(ComputationStatus.DEFAULT, perimeterOutput.getSensitivityStatus());
        when(optimizationResult.getSensitivityStatus()).thenReturn(ComputationStatus.FALLBACK);
        assertEquals(ComputationStatus.FALLBACK, perimeterOutput.getSensitivityStatus());
    }

    @Test
    public void testGetSensitivityValueOnRa() {
        assertEquals(0., perimeterOutput.getSensitivityValue(flowCnec1, ra1, Unit.MEGAWATT), DOUBLE_TOLERANCE);
        assertEquals(0., perimeterOutput.getSensitivityValue(flowCnec1, ra2, Unit.AMPERE), DOUBLE_TOLERANCE);
    }

    @Test
    public void testGetSensitivityValueOnGlsk() {
        assertEquals(0., perimeterOutput.getSensitivityValue(flowCnec1, mock(LinearGlsk.class), Unit.MEGAWATT), DOUBLE_TOLERANCE);
        assertEquals(0., perimeterOutput.getSensitivityValue(flowCnec1, mock(LinearGlsk.class), Unit.AMPERE), DOUBLE_TOLERANCE);
    }
}
