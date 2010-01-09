/*
 * Copyright 2006-2009 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 *  Citrus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Citrus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Citrus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.samples.flightbooking.persistence.impl;

import java.util.List;

import javax.persistence.*;

import org.springframework.stereotype.Repository;

import com.consol.citrus.samples.flightbooking.model.Flight;
import com.consol.citrus.samples.flightbooking.persistence.FlightDao;

/**
 * @author deppisch Christoph Deppisch ConSol* Software GmbH
 */
@Repository
public class FlightDaoImpl implements FlightDao {

    @PersistenceContext
    private EntityManager em;
    
    public Flight find(String flightId) {
        return em.find(Flight.class, flightId);
    }

    @SuppressWarnings("unchecked")
    public List<Flight> findAll() {
        Query query = em.createQuery("from Flight f");
        
        return query.getResultList();
    }

    public void merge(Flight flight) {
        em.merge(flight);
    }

    public void persist(Flight flight) {
        em.persist(flight);
    }

    public void remove(Flight flight) {
        em.remove(flight);
    }

}
