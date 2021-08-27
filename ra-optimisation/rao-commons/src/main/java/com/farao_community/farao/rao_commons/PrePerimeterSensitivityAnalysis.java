/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.rao_commons;

import com.farao_community.farao.data.crac_api.cnec.FlowCnec;
import com.farao_community.farao.data.crac_api.range_action.RangeAction;
import com.farao_community.farao.rao_api.parameters.RaoParameters;
import com.farao_community.farao.rao_api.parameters.UnoptimizedCnecParameters;
import com.farao_community.farao.rao_commons.objective_function_evaluator.ObjectiveFunction;
import com.farao_community.farao.rao_commons.objective_function_evaluator.ObjectiveFunctionHelper;
import com.farao_community.farao.rao_commons.result.EmptyFlowResult;
import com.farao_community.farao.rao_commons.result.RangeActionResultImpl;
import com.farao_community.farao.rao_commons.result_api.FlowResult;
import com.farao_community.farao.rao_commons.result_api.ObjectiveFunctionResult;
import com.farao_community.farao.rao_commons.result_api.OptimizationResult;
import com.farao_community.farao.rao_commons.result_api.PrePerimeterResult;
import com.farao_community.farao.sensitivity_analysis.AppliedRemedialActions;
import com.powsybl.iidm.network.Network;

import java.util.HashSet;
import java.util.Set;

/**
 * This class aims at performing the sensitivity analysis before the optimization of a perimeter. At these specific
 * instants we actually want to compute all the results on the network. They will be useful either for the optimization
 * or to fill results in the final output.
 *
 * @author Baptiste Seguinot {@literal <baptiste.seguinot at rte-france.com>}
 */
public class PrePerimeterSensitivityAnalysis {
    private final Set<FlowCnec> flowCnecs;
    private final Set<RangeAction> rangeActions;
    private final ToolProvider toolProvider;
    private final RaoParameters raoParameters;

    private SensitivityComputer sensitivityComputer;

    public PrePerimeterSensitivityAnalysis(Set<RangeAction> rangeActions,
                                           Set<FlowCnec> flowCnecs,
                                           ToolProvider toolProvider,
                                           RaoParameters raoParameters) {
        this.toolProvider = toolProvider;
        this.flowCnecs = flowCnecs;
        this.rangeActions = rangeActions;
        this.raoParameters = raoParameters;
    }

    public PrePerimeterResult run(Network network) {
        return run(network, null);
    }

    public PrePerimeterResult run(Network network, AppliedRemedialActions appliedRemedialActions) {
        SensitivityComputer.SensitivityComputerBuilder sensitivityComputerBuilder = getBuilder();
        if (raoParameters.isRaoWithLoopFlowLimitation()) {
            sensitivityComputerBuilder.withCommercialFlowsResults(toolProvider.getLoopFlowComputation(), toolProvider.getLoopFlowCnecs(flowCnecs));
        }
        if (raoParameters.getObjectiveFunction().doesRequirePtdf()) {
            sensitivityComputerBuilder.withPtdfsResults(toolProvider.getAbsolutePtdfSumsComputation(), flowCnecs);
        }
        sensitivityComputerBuilder.withAppliedRemedialActions(appliedRemedialActions);
        sensitivityComputer = sensitivityComputerBuilder.build();
        return runAndGetResult(network);
    }

    public PrePerimeterResult runBasedOn(Network network, OptimizationResult optimizationResult) {
        SensitivityComputer.SensitivityComputerBuilder sensitivityComputerBuilder = getBuilder();
        if (raoParameters.isRaoWithLoopFlowLimitation()) {
            if (raoParameters.getLoopFlowApproximationLevel().shouldUpdatePtdfWithTopologicalChange()) {
                sensitivityComputerBuilder.withCommercialFlowsResults(toolProvider.getLoopFlowComputation(), toolProvider.getLoopFlowCnecs(flowCnecs));
            } else {
                sensitivityComputerBuilder.withCommercialFlowsResults(optimizationResult);
            }
        }
        if (raoParameters.getObjectiveFunction().doesRequirePtdf()) {
            sensitivityComputerBuilder.withPtdfsResults(optimizationResult);
        }
        sensitivityComputer = sensitivityComputerBuilder.build();
        return runAndGetResult(network);
    }

    private SensitivityComputer.SensitivityComputerBuilder getBuilder() {
        return SensitivityComputer.create()
                .withToolProvider(toolProvider)
                .withCnecs(flowCnecs)
                .withRangeActions(rangeActions);
    }

    private PrePerimeterResult runAndGetResult(Network network) {
        sensitivityComputer.compute(network);
        ObjectiveFunction objectiveFunction = getInitialMinMarginObjectiveFunction();
        ObjectiveFunctionResult objectiveFunctionResult = objectiveFunction.evaluate(sensitivityComputer.getBranchResult(), sensitivityComputer.getSensitivityResult().getSensitivityStatus());
        return new PrePerimeterSensitivityOutput(
                sensitivityComputer.getBranchResult(),
                sensitivityComputer.getSensitivityResult(),
                new RangeActionResultImpl(network, rangeActions),
                objectiveFunctionResult
        );
    }

    private ObjectiveFunction getInitialMinMarginObjectiveFunction() {
        ObjectiveFunction.ObjectiveFunctionBuilder builder = ObjectiveFunction.create();
        UnoptimizedCnecParameters emptyUnoptimizedCnecParameters = new UnoptimizedCnecParameters(new HashSet<>(), 0.0);
        FlowResult emptyFlowResult = new EmptyFlowResult();
        ObjectiveFunctionHelper.addMinMarginObjectiveFunction(flowCnecs, emptyFlowResult, builder, raoParameters.getObjectiveFunction().relativePositiveMargins(), emptyUnoptimizedCnecParameters, raoParameters.getObjectiveFunction().getUnit());
        return builder.build();
    }
}
