/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.farao_community.farao.linear_rao;

import com.farao_community.farao.data.crac_api.RangeAction;
import com.farao_community.farao.data.crac_result_extensions.RangeActionResultExtension;
import com.farao_community.farao.linear_rao.optimisation.*;
import com.farao_community.farao.linear_rao.optimisation.fillers.AbstractProblemFiller;
import com.farao_community.farao.linear_rao.optimisation.fillers.CoreProblemFiller;
import com.farao_community.farao.linear_rao.optimisation.fillers.MaxLoopFlowFiller;
import com.farao_community.farao.linear_rao.optimisation.fillers.MaxMinMarginFiller;
import com.farao_community.farao.linear_rao.optimisation.post_processors.RaoResultPostProcessor;
import com.farao_community.farao.rao_api.RaoResult;
import com.farao_community.farao.rao_api.RaoParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * A computation engine dedicated to the construction and solving of the linear
 * optimisation problem of the LinearRao.
 *
 * @author Philippe Edwards {@literal <philippe.edwards at rte-france.com>}
 * @author Baptiste Seguinot {@literal <baptiste.seguinot at rte-france.com>}
 */
class LinearOptimisationEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(LinearOptimisationEngine.class);

    /**
     * Linear optimisation problem, core object the LinearOptimisationEngine that
     * is solved each time the run method of this class is called.
     */
    private LinearRaoProblem linearRaoProblem;

    /**
     * Boolean indicating whether the linear problem has been already initialised
     * or not.
     */
    private boolean lpInitialised;

    /**
     * List of problem fillers used by the engine. Each filler is responsible for
     * the creation/update of one part of the optimisation problem (i.e. of some
     * variables and constraints of the optimisation problem.
     */
    private List<AbstractProblemFiller> fillerList;

    /**
     * List of problem fillers used by the engine. Each filler is responsible for
     * the creation/update of one part of the optimisation problem (i.e. of some
     * variables and constraints of the optimisation problem).
     */
    private List<AbstractPostProcessor> postProcessorList;

    /**
     * Constructor
     */
    LinearOptimisationEngine(RaoParameters raoParameters) {

        this.lpInitialised = false;

        // TODO : load the filler list from the config file and make sure they are ordered properly
        this.fillerList = createFillerList(raoParameters);
        this.postProcessorList = createPostProcessorList();
    }

    void run(Situation situation) {
        run(situation, situation.getWorkingVariantId());
    }

    /**
     * The run method of the LinearOptimisationEngine creates and solves the core
     * optimisation problem of the LinearRao. It returns an OptimizedSituation which
     * is set with a new Network variant incorporating the optimal combination of
     * RangeAction set-points and a new Crac ResultVariant which contains the results
     * of the optimisation.
     *
     * @param situation defines the data on which the creation of the optimisation problem
     *                    is based (i.e. a given Network situation with associated Crac
     *                    and sensitivities).
     *
     * @return an OptimizedSituation, set with the optimal combination of RangeAction
     * calculated by the optimisation problem,
     *
     * @throws LinearOptimisationException is the method fails
     */
    void run(Situation situation, String variantForSensitivitiesResult) {
        // prepare optimisation problem
        if (!lpInitialised) {
            this.linearRaoProblem = createLinearRaoProblem();
            buildProblem(situation, sensitivities);
            lpInitialised = true;
        } else {
            updateProblem(situation, sensitivities);
        }

        // solve optimisation problem
        solveProblem();

        // todo : do not create a RaoResult anymore and refactor the post processors
        RaoResult raoResult = new RaoResult(RaoResult.Status.SUCCESS);
        postProcessorList.forEach(postProcessor -> postProcessor.process(linearRaoProblem, situation, raoResult));

        // apply RA on network
        applyRAs(situation);
    }

    private void buildProblem(Situation situation, SystematicSensitivityAnalysisResult sensitivities) {
        try {
            for (AbstractProblemFiller abstractProblemFiller : fillerList) {
                abstractProblemFiller.fill(situation, sensitivities, createLinearRaoProblem());
            }
        } catch (Exception e) {
            String errorMessage = "Linear optimisation failed when building the problem.";
            LOGGER.error(errorMessage);
            throw new LinearOptimisationException(errorMessage, e);
        }
    }

    private void updateProblem(Situation situation, SystematicSensitivityAnalysisResult sensitivities) {
        try {
            fillerList.forEach(abstractProblemFiller -> abstractProblemFiller.update(situation, sensitivities, linearRaoProblem));
        } catch (Exception e) {
            String errorMessage = "Linear optimisation failed when updating the problem.";
            LOGGER.error(errorMessage);
            throw new LinearOptimisationException(errorMessage, e);
        }
    }

    private void solveProblem() {
        try {
            Enum solverResultStatus = linearRaoProblem.solve();
            String solverResultStatusString = solverResultStatus.name();
            if (!solverResultStatusString.equals("OPTIMAL")) {
                String errorMessage = String.format("Solving of the linear problem failed failed with MPSolver status %s", solverResultStatusString);
                LOGGER.error(errorMessage);
                throw new LinearOptimisationException(errorMessage);
            }
        } catch (Exception e) {
            String errorMessage = "Solving of the linear problem failed.";
            LOGGER.error(errorMessage);
            throw new LinearOptimisationException(errorMessage, e);
        }
    }

    List<AbstractProblemFiller> createFillerList(RaoParameters raoParameters) {
        fillerList = new ArrayList<>();
        fillerList.add(new CoreProblemFiller());
        fillerList.add(new MaxMinMarginFiller());
        if (raoParameters.isRaoWithLoopFlowLimitation()) { // && !Objects.isNull(situation.getCrac().getExtension(CracLoopFlowExtension.class)))
            fillerList.add(new MaxLoopFlowFiller());
        }
        return fillerList;
    }

    List<AbstractPostProcessor> createPostProcessorList() {
        postProcessorList = new ArrayList<>();
        postProcessorList.add(new RaoResultPostProcessor());
        return postProcessorList;
    }

    LinearRaoProblem createLinearRaoProblem() {
        return new LinearRaoProblem();
    }

    /**
     * Apply the optimised RangeAction on a Network
     */
    private void applyRAs(Situation situation) {
        String preventiveState = situation.getCrac().getPreventiveState().getId();
        for (RangeAction rangeAction : situation.getCrac().getRangeActions()) {
            RangeActionResultExtension rangeActionResultMap = rangeAction.getExtension(RangeActionResultExtension.class);
            rangeAction.apply(situation.getNetwork(), rangeActionResultMap.getVariant(situation.getWorkingVariantId()).getSetPoint(preventiveState));
        }
    }
}
