/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.linear_rao;

import com.farao_community.farao.commons.FaraoException;
import com.farao_community.farao.data.crac_api.Crac;
import com.farao_community.farao.data.crac_api.PstRange;
import com.farao_community.farao.data.crac_api.RangeAction;
import com.farao_community.farao.data.crac_result_extensions.*;
import com.farao_community.farao.util.SystematicSensitivityAnalysisResult;
import com.powsybl.iidm.network.Network;

import java.util.*;

/**
 * A Situation is an object that gathers Network, Crac and SystematicSensitivityAnalysisResult data. It manages
 * variants of these objects to ensure data consistency at any moment. It is a single point of entry to manipulate
 * all network situation data with variant management.
 *
 * @author Philippe Edwards {@literal <philippe.edwards at rte-france.com>}
 * @author Baptiste Seguinot {@literal <baptiste.seguinot at rte-france.com>}
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
public class VariantCatalog {

    private static final String NO_WORKING_VARIANT = "No working variant is defined.";
    private static final String UNKNOWN_VARIANT = "Unknown variant %s";

    private Network network;
    private Crac crac;
    private Map<String, Variant> variants;

    private final String initialVariantId;
    private String workingVariantId;

    /**
     * This constructor creates a new situation variant and set it as the working variant. So accessing data
     * after this constructor will lead directly to the newly created variant data. CRAC and sensitivity data will
     * be empty and network data will be copied from the initial network variant. It will create a CRAC
     * ResultVariantManager if it does not exist yet.
     *
     * @param network: Network object.
     * @param crac: CRAC object.
     */
    public VariantCatalog(Network network, Crac crac) {
        this.network = network;
        this.crac = crac;
        this.variants = new HashMap<>();

        ResultVariantManager resultVariantManager = crac.getExtension(ResultVariantManager.class);
        if (resultVariantManager == null) {
            resultVariantManager = new ResultVariantManager();
            crac.addExtension(ResultVariantManager.class, resultVariantManager);
        }

        this.initialVariantId = getUniqueVariantId("initial");

        Variant initialVariant = new Variant(network.getVariantManager().getWorkingVariantId(), initialVariantId, null);
        this.variants.put("initialVariant", initialVariant);
    }

    // SETTERS AND GETTERS

    public String getWorkingVariantId() {
        return workingVariantId;
    }

    public Variant getWorkingVariant() {
        return getVariant(workingVariantId);
    }

    public Variant getInitialVariant() {
        return getVariant(initialVariantId);
    }

    public Variant getVariant(String variantId) {
        Variant variant = variants.get(variantId);
        if (variant == null) {
            throw new FaraoException(String.format(UNKNOWN_VARIANT, variantId));
        }
        return variant;
    }

    public void setWorkingVariant(String variantId) {
        if (!variants.containsKey(variantId)) {
            throw new FaraoException(String.format(UNKNOWN_VARIANT, variantId));
        }
        this.workingVariantId = variantId;
    }

    public Set<String> getVariantIds() {
        return variants.keySet();
    }

    public Network getNetwork() {
        if (workingVariantId == null) {
            throw new FaraoException(NO_WORKING_VARIANT);
        }
        network.getVariantManager().setWorkingVariant(workingVariantId);
        return network;
    }

    public Crac getCrac() {
        return crac;
    }

    public CracResult getCracResult(String variantId) {
        Variant variant = getVariant(variantId);
        return crac.getExtension(CracResultExtension.class).getVariant(variant.getCracVariantId());
    }

    public CracResult getCracResult() {
        return getCracResult(workingVariantId);
    }

    // VARIANT MANAGEMENT METHODS

    public String cloneVariant(String referenceVariantId) {
        if (!variants.containsKey(referenceVariantId)) {
            throw new FaraoException(String.format(UNKNOWN_VARIANT, referenceVariantId));
        }

        String situationVariantId = getUniqueVariantId("optimized");

        network.getVariantManager().cloneVariant(referenceVariantId, situationVariantId);
        crac.getExtension(ResultVariantManager.class).createVariant(situationVariantId);

        Variant clonedVariant = new Variant(situationVariantId, situationVariantId, variants.get(referenceVariantId).getSystematicSensitivityAnalysisResult());
        variants.put(situationVariantId, clonedVariant);

        return situationVariantId;
    }

    /**
     * This method deletes a variant according to its ID. If the working variant is the variant to be deleted nothing
     * would be done.
     *
     * @param variantId: Variant ID that is required to delete.
     * @param deleteCracVariant: If true it will delete the variant as situation variant and the related network variant
     *                      but it will keep the crac variant.
     * @throws FaraoException if variantId is not an existing variant of the situation.
     */
    void deleteVariant(String variantId, boolean deleteNetworkVariant, boolean deleteCracVariant) {
        Variant variantToDelete = getVariant(variantId);

        if (deleteNetworkVariant) {
            variantToDelete.clearNetwork(network);
        }
        if (deleteCracVariant) {
            variantToDelete.clearCrac(crac);
        }
        variants.remove(variantId);
    }

    /**
     * This method clear all the situation variants with their related variants in the different data objects. It
     * enables to keep some CRAC result variants as it is results.
     *
     * @param remainingVariantsForCrac IDs of the variants we want to keep the results in CRAC
     */
    void clearCatalog(List<String> remainingVariantsForCrac) {
        // set network to initial variant
        getVariant(initialVariantId).setNetworkVariant(network);

        String[] copiedIds = new String[variants.size()];
        variants.keySet().toArray(copiedIds);

        for (String variantId: copiedIds) {
            deleteVariant(variantId, !variantId.equals(initialVariantId), remainingVariantsForCrac.contains(variantId));
        }
        variants.clear();
    }


    // HANDLE VARIANT IDs

    /**
     * This method generates a unique variant ID of a network according to the IDs already present in the network.
     *
     * @return A unique ID of the network as a string.
     */

    private String getUniqueVariantId(String prefix) {

        String uniqueId;
        do {
            uniqueId = prefix.concat(UUID.randomUUID().toString());
        } while (isIdAlreadyUsed(uniqueId));

        return uniqueId;
    }

    private boolean isIdAlreadyUsed(String id) {
        return network.getVariantManager().getVariantIds().contains(id) ||
            crac.getExtension(ResultVariantManager.class).getVariants().contains(id) ||
            variants.keySet().contains(id);
    }

}
