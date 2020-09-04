/*
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.farao_community.farao.data.crac_impl.usage_rule;

import com.farao_community.farao.data.crac_api.*;
import com.farao_community.farao.data.crac_impl.ComplexContingency;
import com.farao_community.farao.data.crac_impl.SimpleState;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
public class OnStateTest {

    private State initialState;
    private State curativeState1;
    private State curativeState2;

    @Before
    public void setUp() {
        initialState = new SimpleState(Optional.empty(), new Instant("initial-instant", 0));
        Instant curativeInstant = new Instant("curative", 1200);
        curativeState1 = new SimpleState(Optional.of(new ComplexContingency("contingency1")), curativeInstant);
        curativeState2 = new SimpleState(Optional.of(new ComplexContingency("contingency2")), curativeInstant);
    }

    @Test
    public void getCnec() {
        OnState rule1 = new OnState(UsageMethod.AVAILABLE, initialState);
        assertEquals("none-initial-instant", rule1.getState().getId());
    }

    @Test
    public void testEqualsSameObject() {
        OnState rule1 = new OnState(UsageMethod.AVAILABLE, initialState);
        assertEquals(rule1, rule1);
    }

    @Test
    public void testEqualsTrue() {
        OnState rule1 = new OnState(UsageMethod.AVAILABLE, initialState);
        OnState rule2 = new OnState(UsageMethod.AVAILABLE, initialState);
        assertEquals(rule1, rule2);
    }

    @Test
    public void testEqualsFalseNotTheSameObject() {
        OnState rule1 = new OnState(UsageMethod.AVAILABLE, initialState);
        assertNotEquals(rule1, new Instant("fail", 10));
    }

    @Test
    public void testEqualsFalseForUsageMethod() {
        OnState rule1 = new OnState(UsageMethod.AVAILABLE, initialState);
        OnState rule2 = new OnState(UsageMethod.FORCED, initialState);
        assertNotEquals(rule1, rule2);
    }

    @Test
    public void testEqualsFalseForState() {
        OnState rule1 = new OnState(UsageMethod.AVAILABLE, curativeState1);
        OnState rule2 = new OnState(UsageMethod.AVAILABLE, curativeState2);
        assertNotEquals(rule1, rule2);
    }

    @Test
    public void testHashCode() {
        OnState rule1 = new OnState(UsageMethod.AVAILABLE, initialState);
        OnState rule2 = new OnState(UsageMethod.AVAILABLE, initialState);
        assertEquals(rule1.hashCode(), rule2.hashCode());
    }

    @Test
    public void testHashCodeFalseForUsageMethod() {
        OnState rule1 = new OnState(UsageMethod.AVAILABLE, initialState);
        OnState rule2 = new OnState(UsageMethod.FORCED, initialState);
        assertNotEquals(rule1.hashCode(), rule2.hashCode());
    }

    @Test
    public void testHashCodeFalseForContingency() {
        OnState rule1 = new OnState(UsageMethod.AVAILABLE, curativeState1);
        OnState rule2 = new OnState(UsageMethod.AVAILABLE, curativeState2);
        assertNotEquals(rule1.hashCode(), rule2.hashCode());
    }

}
