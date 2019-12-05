/*
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.search_tree_rao.perimeter_optimisation;

import com.farao_community.farao.commons.FaraoException;
import com.farao_community.farao.data.crac_api.Crac;
import com.farao_community.farao.data.crac_api.NetworkAction;
import com.farao_community.farao.ra_optimisation.RaoComputationResult;
import com.powsybl.iidm.network.Network;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * A "leaf" is a junction of the search tree
 * Each leaf contains a Network Action, which should be tested in combination with
 * it's parent Leaves' Network Actions
 *
 * @author Baptiste Seguinot {@literal <baptiste.seguinot at rte-international.com>}
 */
class Leaf {

    /**
     * Parent Leaf (or null for initial/root Leaf)
     */
    private final Leaf parentLeaf;

    /**
     * Network Action which will be tested (can be null)
     */
    private final NetworkAction networkAction;

    /**
     * Name of the network variant associated with this Leaf
     */
    private String networkVariant;

    /**
     * Impact of the network action, quantified with a Linear
     * Range Action Optimisation and reported in a RaoComputationResult
     */
    private RaoComputationResult actionImpact;

    /**
     * Status of the Leaf evaluation : Created, Running, Evaluated or
     * Error
     */
    private Status status;

    enum Status {
        CREATED,
        EVALUATION_RUNNING,
        EVALUATED,
        EVALUATION_ERROR
    }

    /**
     * Initial Leaf constructor, set parent Leaf and networkAction to null
     */
    Leaf() {
        this.parentLeaf = null;
        this.networkAction = null;
        this.status = Status.CREATED;
    }

    /**
     * Leaf constructor, set parentLeaf and networkAction
     */
    private Leaf(Leaf parentLeaf, NetworkAction networkAction) {
        this.parentLeaf = parentLeaf;
        this.networkAction = networkAction;
        this.status = Status.CREATED;
    }

    Leaf getParent() {
        return parentLeaf;
    }

    RaoComputationResult getActionImpact() {
        return actionImpact;
    }

    Status getStatus() {
        return status;
    }

    String getNetworkVariant() {
        return networkVariant;
    }

    boolean isRoot() {
        return parentLeaf == null;
    }

    /**
     * Extend the tree from the current Leaf with N new children Leaves
     * for the N Network Actions given in argument
     */
    List<Leaf> bloom(List<NetworkAction> availableNetworkActions) {
        return availableNetworkActions.stream().map(na -> new Leaf(this, na)).collect(Collectors.toList());
    }

    /**
     * Evaluate the Leaf : test the NetworkActions (from the Leaf and its
     * parents) and report the results in actionImpact
     */
    void evaluate(Network network, Crac crac) {
        if (isRoot()) {
            throw new FaraoException("When evaluating the root leaf, a network variant must be specified.");
        }
        evaluate(network, getParent().getNetworkVariant(), crac);
    }

    void evaluate(Network network, String referenceNetworkVariant, Crac crac) {

        this.status = Status.EVALUATION_RUNNING;

        // get network variant and apply network action
        this.networkVariant = createVariant(network, referenceNetworkVariant);
        network.getVariantManager().setWorkingVariant(this.networkVariant);
        applyAction(network);

        //todo : run LinearRangeActionOptimisation and store RaoComputationResult
        this.status = Status.EVALUATED;
    }

    private String getUniqueVariantId(Network network) {
        String uniqueId;
        do {
            uniqueId = UUID.randomUUID().toString();
        } while (network.getVariantManager().getVariantIds().contains(uniqueId));
        return uniqueId;
    }

    private String createVariant(Network network, String referenceNetworkVariant) {
        String uniqueId = getUniqueVariantId(network);

        if (isRoot()) {
            network.getVariantManager().cloneVariant(referenceNetworkVariant, this.networkVariant);
        } else {
            network.getVariantManager().cloneVariant(referenceNetworkVariant, this.networkVariant);
        }
        return uniqueId;
    }

    private void applyAction(Network network) {
        if (this.networkAction != null) {
            networkAction.apply(network);
        }
    }

}
