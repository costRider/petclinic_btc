/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.repository.jpa;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.springframework.orm.hibernate5.support.OpenSessionInViewFilter;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.OwnerSearchResults;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA implementation of the {@link OwnerRepository} interface.
 *
 * @author Mike Keith
 * @author Rod Johnson
 * @author Sam Brannen
 * @author Michael Isvy
 * @since 22.4.2006
 */
@Repository
public class JpaOwnerRepositoryImpl implements OwnerRepository {

    @PersistenceContext
    private EntityManager em;


    /**
     * Important: in the current version of this method, we load Owners with all their Pets and Visits while
     * we do not need Visits at all and we only need one property from the Pet objects (the 'name' property).
     * There are some ways to improve it such as:
     * - creating a Ligtweight class (example here: https://community.jboss.org/wiki/LightweightClass)
     * - Turning on lazy-loading and using {@link OpenSessionInViewFilter}
     */
    @SuppressWarnings("unchecked")
    public Collection<Owner> findByLastName(String lastName) {
        // using 'join fetch' because a single query should load both owners and pets
        // using 'left join fetch' because it might happen that an owner does not have pets yet
        Query query = this.em.createQuery("SELECT DISTINCT owner FROM Owner owner left join fetch owner.pets WHERE owner.lastName LIKE :lastName");
        query.setParameter("lastName", lastName + "%");
        return query.getResultList();
    }

    @Override
    public OwnerSearchResults findByLastName(String lastName, int page, int pageSize) {
        int sanitizedPageSize = Math.max(pageSize, 1);
        int sanitizedPage = Math.max(page, 1);

        Long totalCount = this.em.createQuery(
                "SELECT COUNT(DISTINCT owner.id) FROM Owner owner WHERE owner.lastName LIKE :lastName",
                Long.class)
            .setParameter("lastName", lastName + "%")
            .getSingleResult();

        int total = totalCount == null ? 0 : totalCount.intValue();
        if (total == 0) {
            return new OwnerSearchResults(Collections.emptyList(), 0, 1, sanitizedPageSize, lastName);
        }

        int totalPages = (int) Math.ceil(total / (double) sanitizedPageSize);
        if (sanitizedPage > totalPages) {
            sanitizedPage = totalPages;
        }

        TypedQuery<Owner> query = this.em.createQuery(
            "SELECT DISTINCT owner FROM Owner owner left join fetch owner.pets WHERE owner.lastName LIKE :lastName ORDER BY owner.lastName, owner.firstName, owner.id",
            Owner.class
        );
        query.setParameter("lastName", lastName + "%");
        query.setFirstResult((sanitizedPage - 1) * sanitizedPageSize);
        query.setMaxResults(sanitizedPageSize);

        List<Owner> owners = query.getResultList();

        return new OwnerSearchResults(owners, total, sanitizedPage, sanitizedPageSize, lastName);
    }

    @Override
    public Owner findById(int id) {
        // using 'join fetch' because a single query should load both owners and pets
        // using 'left join fetch' because it might happen that an owner does not have pets yet
        Query query = this.em.createQuery("SELECT owner FROM Owner owner left join fetch owner.pets WHERE owner.id =:id");
        query.setParameter("id", id);
        return (Owner) query.getSingleResult();
    }


    @Override
    public void save(Owner owner) {
        if (owner.getId() == null) {
            this.em.persist(owner);
        } else {
            this.em.merge(owner);
        }

    }

}
