package com.farao_community.farao.linear_rao;

import com.farao_community.farao.data.crac_api.Crac;
import com.farao_community.farao.data.crac_api.PstRange;
import com.farao_community.farao.data.crac_api.RangeAction;
import com.farao_community.farao.data.crac_result_extensions.PstRangeResult;
import com.farao_community.farao.data.crac_result_extensions.RangeActionResult;
import com.farao_community.farao.data.crac_result_extensions.RangeActionResultExtension;
import com.farao_community.farao.data.crac_result_extensions.ResultVariantManager;
import com.farao_community.farao.util.SystematicSensitivityAnalysisResult;
import com.powsybl.iidm.network.Network;

public class Variant {

    private String networkVariantId;
    private String cracVariantId;
    private SystematicSensitivityAnalysisResult systematicSensitivityAnalysisResult;

    Variant(String networkVariantId, String cracVariantId, SystematicSensitivityAnalysisResult systematicSensitivityAnalysisResult) {
        this.networkVariantId = networkVariantId;
        this.cracVariantId = cracVariantId;
        this.systematicSensitivityAnalysisResult = systematicSensitivityAnalysisResult;
    }

    SystematicSensitivityAnalysisResult getSystematicSensitivityAnalysisResult() {
        return systematicSensitivityAnalysisResult;
    }

    void setNetworkVariant(Network network) {
        network.getVariantManager().setWorkingVariant(networkVariantId);
    }

    String getCracVariantId() {
        return cracVariantId;
    }

    void clearNetwork(Network network) {
        network.getVariantManager().removeVariant(networkVariantId);
    }

    void clearCrac(Crac crac) {
        crac.getExtension(ResultVariantManager.class).deleteVariant(cracVariantId);
    }

    /**
     * This method works from the working variant. It is filling CRAC result extension of the working variant
     * with values in network of the working variant.
     */
    public void fillRangeActionResultsWithNetworkValues(Crac crac, Network network) {

        network.getVariantManager().setWorkingVariant(networkVariantId);
        String preventiveState = crac.getPreventiveState().getId();
        for (RangeAction rangeAction : crac.getRangeActions()) {
            double valueInNetwork = rangeAction.getCurrentValue(network);
            RangeActionResultExtension rangeActionResultMap = rangeAction.getExtension(RangeActionResultExtension.class);
            RangeActionResult rangeActionResult = rangeActionResultMap.getVariant(cracVariantId);
            rangeActionResult.setSetPoint(preventiveState, valueInNetwork);
            if (rangeAction instanceof PstRange) {
                ((PstRangeResult) rangeActionResult).setTap(preventiveState, ((PstRange) rangeAction).computeTapPosition(valueInNetwork));
            }
        }
    }

    /**
     * This method works from the working variant. It is applying on the network working variant
     * according to the values present in the CRAC result extension of the working variant.
     */
    public void applyRangeActionResultsOnNetwork(Crac crac, Network network) {
        String preventiveState = crac.getPreventiveState().getId();
        for (RangeAction rangeAction : crac.getRangeActions()) {
            RangeActionResultExtension rangeActionResultMap = rangeAction.getExtension(RangeActionResultExtension.class);
            rangeAction.apply(network, rangeActionResultMap.getVariant(cracVariantId).getSetPoint(preventiveState));
        }
    }


}
