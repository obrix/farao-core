/*
 *  Copyright (c) 2020, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.farao_community.farao.data.crac_io_json;

import com.farao_community.farao.commons.FaraoException;
import com.farao_community.farao.commons.Unit;
import com.farao_community.farao.data.crac_api.Instant;
import com.farao_community.farao.data.crac_api.network_action.ActionType;
import com.farao_community.farao.data.crac_api.range_action.RangeType;
import com.farao_community.farao.data.crac_api.threshold.BranchThresholdRule;
import com.farao_community.farao.data.crac_api.usage_rule.UsageMethod;

/**
 * @author Baptiste Seguinot {@literal <baptiste.seguinot at rte-france.com>}
 */
public final class JsonSerializationConstants {

    private JsonSerializationConstants() { }

    // field
    public static final String NETWORK_ELEMENTS_IDS = "networkElementsIds";
    public static final String NETWORK_ELEMENT_ID = "networkElementId";
    public static final String NETWORK_ELEMENTS_NAME_PER_ID = "networkElementsNamePerId";

    public static final String GROUP_ID = "groupId";

    public static final String CONTINGENCIES = "contingencies";
    public static final String CONTINGENCY_ID = "contingencyId";

    public static final String INSTANT = "instant";

    public static final String FLOW_CNECS = "flowCnecs";
    public static final String FLOW_CNEC_ID = "flowCnecId";

    public static final String THRESHOLDS = "thresholds";
    public static final String FRM = "frm";
    public static final String OPTIMIZED = "optimized";
    public static final String MONITORED = "monitored";
    public static final String I_MAX = "iMax";
    public static final String NOMINAL_VOLTAGE = "nominalV";

    public static final String PST_RANGE_ACTIONS = "pstRangeActions";
    public static final String HVDC_RANGE_ACTIONS = "hvdcRangeActions";

    public static final String NETWORK_ACTIONS = "networkActions";
    public static final String TOPOLOGICAL_ACTIONS = "topologicalActions";
    public static final String PST_SETPOINTS = "pstSetpoints";
    public static final String INJECTION_SETPOINTS = "injectionSetpoints";

    public static final String USAGE_METHOD = "usageMethod";
    public static final String FREE_TO_USE_USAGE_RULES = "freeToUseUsageRules";
    public static final String ON_STATE_USAGE_RULES = "onStateUsageRules";
    public static final String ON_FLOW_CONSTRAINT_USAGE_RULES = "onFlowConstraintUsageRules";

    public static final String TYPE = "type";
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String VERSION = "version";
    public static final String INFO = "info";
    public static final String EXTENSIONS = "extensions";

    public static final String RANGES = "ranges";
    public static final String SETPOINT = "setpoint";
    public static final String OPERATOR = "operator";
    public static final String ACTION_TYPE = "actionType";
    public static final String RANGE_TYPE = "rangeType";
    public static final String INITIAL_TAP = "initialTap";
    public static final String TAP_TO_ANGLE_CONVERSION_MAP = "tapToAngleConversionMap";

    public static final String UNIT = "unit";
    public static final String RULE = "rule";
    public static final String MIN = "min";
    public static final String MAX = "max";

    // instants
    public static final String PREVENTIVE_INSTANT = "preventive";
    public static final String OUTAGE_INSTANT = "outage";
    public static final String AUTO_INSTANT = "auto";
    public static final String CURATIVE_INSTANT = "curative";

    // units
    public static final String AMPERE_UNIT = "ampere";
    public static final String MEGAWATT_UNIT = "megawatt";
    public static final String DEGREE_UNIT = "degree";
    public static final String KILOVOLT_UNIT = "kilovolt";
    public static final String PERCENT_IMAX_UNIT = "percent_imax";
    public static final String TAP_UNIT = "tap";

    // rules
    public static final String ON_LOW_VOLTAGE_LEVEL_RULE = "onLowVoltageLevel";
    public static final String ON_HIGH_VOLTAGE_LEVEL_RULE = "onHighVoltageLevel";
    public static final String ON_NON_REGULATED_SIDE_RULE = "onNonRegulatedSide";
    public static final String ON_REGULATED_SIDE_RULE = "onRegulatedSide";
    public static final String ON_LEFT_SIDE_RULE = "onLeftSide";
    public static final String ON_RIGHT_SIDE_RULE = "onRightSide";

    // usage methods
    public static final String UNAVAILABLE_USAGE_METHOD = "unavailable";
    public static final String FORCED_USAGE_METHOD = "forced";
    public static final String AVAILABLE_USAGE_METHOD = "available";
    public static final String UNDEFINED_USAGE_METHOD = "undefined";

    // range types
    public static final String ABSOLUTE_RANGE = "absolute";
    public static final String RELATIVE_TO_PREVIOUS_INSTANT_RANGE = "relativeToPreviousInstant";
    public static final String RELATIVE_TO_INITIAL_NETWORK_RANGE = "relativeToInitialNetwork";

    // action types
    public static final String OPEN_ACTION = "open";
    public static final String CLOSE_ACTION = "close";

    public static String serializeInstant(Instant instant) {
        switch (instant) {
            case PREVENTIVE:
                return PREVENTIVE_INSTANT;
            case OUTAGE:
                return OUTAGE_INSTANT;
            case AUTO:
                return AUTO_INSTANT;
            case CURATIVE:
                return CURATIVE_INSTANT;
            default:
                throw new FaraoException(String.format("Unsupported instant %s", instant));
        }
    }

    public static Instant deserializeInstant(String stringValue) {
        switch (stringValue) {
            case PREVENTIVE_INSTANT:
                return Instant.PREVENTIVE;
            case OUTAGE_INSTANT:
                return Instant.OUTAGE;
            case AUTO_INSTANT:
                return Instant.AUTO;
            case CURATIVE_INSTANT:
                return Instant.CURATIVE;
            default:
                throw new FaraoException(String.format("Unrecognized instant %s", stringValue));
        }
    }

    public static String serializeUnit(Unit unit) {
        switch (unit) {
            case AMPERE:
                return AMPERE_UNIT;
            case DEGREE:
                return DEGREE_UNIT;
            case MEGAWATT:
                return MEGAWATT_UNIT;
            case KILOVOLT:
                return KILOVOLT_UNIT;
            case PERCENT_IMAX:
                return PERCENT_IMAX_UNIT;
            case TAP:
                return TAP_UNIT;
            default:
                throw new FaraoException(String.format("Unsupported unit %s", unit));
        }
    }

    public static Unit deserializeUnit(String stringValue) {
        switch (stringValue) {
            case AMPERE_UNIT:
                return Unit.AMPERE;
            case DEGREE_UNIT:
                return Unit.DEGREE;
            case MEGAWATT_UNIT:
                return Unit.MEGAWATT;
            case KILOVOLT_UNIT:
                return Unit.KILOVOLT;
            case PERCENT_IMAX_UNIT:
                return Unit.PERCENT_IMAX;
            case TAP_UNIT:
                return Unit.TAP;
            default:
                throw new FaraoException(String.format("Unrecognized unit %s", stringValue));
        }
    }

    public static String serializeBranchThresholdRule(BranchThresholdRule rule) {
        switch (rule) {
            case ON_LOW_VOLTAGE_LEVEL:
                return ON_LOW_VOLTAGE_LEVEL_RULE;
            case ON_HIGH_VOLTAGE_LEVEL:
                return ON_HIGH_VOLTAGE_LEVEL_RULE;
            case ON_NON_REGULATED_SIDE:
                return ON_NON_REGULATED_SIDE_RULE;
            case ON_REGULATED_SIDE:
                return ON_REGULATED_SIDE_RULE;
            case ON_LEFT_SIDE:
                return ON_LEFT_SIDE_RULE;
            case ON_RIGHT_SIDE:
                return ON_RIGHT_SIDE_RULE;
            default:
                throw new FaraoException(String.format("Unsupported branch threshold rule %s", rule));
        }
    }

    public static BranchThresholdRule deserializeBranchThresholdRule(String stringValue) {
        switch (stringValue) {
            case ON_LOW_VOLTAGE_LEVEL_RULE:
                return BranchThresholdRule.ON_LOW_VOLTAGE_LEVEL;
            case ON_HIGH_VOLTAGE_LEVEL_RULE:
                return BranchThresholdRule.ON_HIGH_VOLTAGE_LEVEL;
            case ON_NON_REGULATED_SIDE_RULE:
                return BranchThresholdRule.ON_NON_REGULATED_SIDE;
            case ON_REGULATED_SIDE_RULE:
                return BranchThresholdRule.ON_REGULATED_SIDE;
            case ON_LEFT_SIDE_RULE:
                return BranchThresholdRule.ON_LEFT_SIDE;
            case ON_RIGHT_SIDE_RULE:
                return BranchThresholdRule.ON_RIGHT_SIDE;
            default:
                throw new FaraoException(String.format("Unrecognized branch threshold rule %s", stringValue));
        }
    }

    public static String serializeUsageMethod(UsageMethod usageMethod) {
        switch (usageMethod) {
            case UNAVAILABLE:
                return UNAVAILABLE_USAGE_METHOD;
            case FORCED:
                return FORCED_USAGE_METHOD;
            case AVAILABLE:
                return AVAILABLE_USAGE_METHOD;
            case UNDEFINED:
                return UNDEFINED_USAGE_METHOD;
            default:
                throw new FaraoException(String.format("Unsupported usage method %s", usageMethod));
        }
    }

    public static UsageMethod deserializeUsageMethod(String stringValue) {
        switch (stringValue) {
            case UNAVAILABLE_USAGE_METHOD:
                return UsageMethod.UNAVAILABLE;
            case FORCED_USAGE_METHOD:
                return UsageMethod.FORCED;
            case AVAILABLE_USAGE_METHOD:
                return UsageMethod.AVAILABLE;
            case UNDEFINED_USAGE_METHOD:
                return UsageMethod.UNDEFINED;
            default:
                throw new FaraoException(String.format("Unrecognized usage method %s", stringValue));
        }
    }

    public static String serializeRangeType(RangeType rangeType) {
        switch (rangeType) {
            case ABSOLUTE:
                return ABSOLUTE_RANGE;
            case RELATIVE_TO_PREVIOUS_INSTANT:
                return RELATIVE_TO_PREVIOUS_INSTANT_RANGE;
            case RELATIVE_TO_INITIAL_NETWORK:
                return RELATIVE_TO_INITIAL_NETWORK_RANGE;
            default:
                throw new FaraoException(String.format("Unsupported range type %s", rangeType));
        }
    }

    public static RangeType deserializeRangeType(String stringValue) {
        switch (stringValue) {
            case ABSOLUTE_RANGE:
                return RangeType.ABSOLUTE;
            case RELATIVE_TO_PREVIOUS_INSTANT_RANGE:
                return RangeType.RELATIVE_TO_PREVIOUS_INSTANT;
            case RELATIVE_TO_INITIAL_NETWORK_RANGE:
                return RangeType.RELATIVE_TO_INITIAL_NETWORK;
            default:
                throw new FaraoException(String.format("Unrecognized range type %s", stringValue));
        }
    }

    public static String serializeActionType(ActionType actionType) {
        switch (actionType) {
            case OPEN:
                return OPEN_ACTION;
            case CLOSE:
                return CLOSE_ACTION;
            default:
                throw new FaraoException(String.format("Unsupported action type %s", actionType));
        }
    }

    public static ActionType deserializeActionType(String stringValue) {
        switch (stringValue) {
            case OPEN_ACTION:
                return ActionType.OPEN;
            case CLOSE_ACTION:
                return ActionType.CLOSE;
            default:
                throw new FaraoException(String.format("Unrecognized action type %s", stringValue));
        }
    }
}
