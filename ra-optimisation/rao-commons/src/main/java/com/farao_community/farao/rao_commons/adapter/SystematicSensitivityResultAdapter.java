/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.farao_community.farao.rao_commons.adapter;

import com.farao_community.farao.rao_api.results.SensitivityResult;
import com.farao_community.farao.rao_commons.result.SensitivityResultImpl;
import com.farao_community.farao.sensitivity_analysis.SystematicSensitivityResult;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
public class SystematicSensitivityResultAdapter implements SensitivityResultAdapter {
    @Override
    public SensitivityResult getResult(SystematicSensitivityResult systematicSensitivityResult) {
        return new SensitivityResultImpl(systematicSensitivityResult);
    }
}
