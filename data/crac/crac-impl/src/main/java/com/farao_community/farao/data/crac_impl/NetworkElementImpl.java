/*
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.data.crac_impl;

import com.farao_community.farao.commons.FaraoException;
import com.farao_community.farao.data.crac_api.NetworkElement;
import com.powsybl.iidm.network.*;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class NetworkElementImpl extends AbstractIdentifiable<NetworkElement> implements NetworkElement {

    NetworkElementImpl(String id, String name) {
        super(id, name);
    }

    NetworkElementImpl(String id) {
        this(id, id);
    }

    /**
     * Check if network elements are equals. Network elements are considered equals when IDs are equals.
     *
     * @param o: If it's null or another object than NetworkElement it will return false.
     * @return A boolean true if objects are equals, otherwise false.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NetworkElementImpl networkElement = (NetworkElementImpl) o;
        return super.equals(networkElement);
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    /**
     * Returns the location of the network element, as a set of optional countries
     * @param network: the network object used to look for the network element
     * @return a set of optional countries containing the network element
     */
    @Override
    public Set<Optional<Country>> getLocation(Network network) {
        Identifiable<?> ne = network.getIdentifiable(this.getId());
        if (Objects.isNull(ne)) {
            throw new FaraoException("Network element " + this.getId() + " was not found in the network.");
        } else if (ne instanceof Branch) {
            Branch<?> branch = (Branch) ne;
            Optional<Country> country1 = getSubstationCountry(branch.getTerminal1().getVoltageLevel().getSubstation());
            Optional<Country> country2 = getSubstationCountry(branch.getTerminal2().getVoltageLevel().getSubstation());
            if (country1.equals(country2)) {
                return Set.of(country1);
            } else {
                return Set.of(country1, country2);
            }
        } else if (ne instanceof Switch) {
            return Set.of(getSubstationCountry(((Switch) ne).getVoltageLevel().getSubstation()));
        } else if (ne instanceof Injection) {
            return Set.of(getSubstationCountry(((Injection<?>) ne).getTerminal().getVoltageLevel().getSubstation()));
        } else if (ne instanceof  Bus) {
            return Set.of(getSubstationCountry(((Bus) ne).getVoltageLevel().getSubstation()));
        } else if (ne instanceof VoltageLevel) {
            return Set.of(getSubstationCountry(((VoltageLevel) ne).getSubstation()));
        } else if (ne instanceof Substation) {
            return Set.of(((Substation) ne).getCountry());
        } else if (ne instanceof HvdcLine) {
            return Set.of(getSubstationCountry(((HvdcLine) ne).getConverterStation1().getTerminal().getVoltageLevel().getSubstation()), getSubstationCountry(((HvdcLine) ne).getConverterStation2().getTerminal().getVoltageLevel().getSubstation()));
        }  else {
            throw new NotImplementedException("Don't know how to figure out the location of " + ne.getId() + " of type " + ne.getClass());
        }
    }

    private Optional<Country> getSubstationCountry(Optional<Substation> substation) {
        if (substation.isPresent()) {
            return substation.get().getCountry();
        } else {
            return Optional.empty();
        }
    }
}
