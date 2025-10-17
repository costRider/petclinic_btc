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
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.orm.hibernate5.support.OpenSessionInViewFilter;
import org.springframework.samples.petclinic.model.Owner;
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

    @SuppressWarnings("unchecked")
    @Override
    public List<Owner> findByLastNameWithCursor(String lastName, Integer cursor, int limit, boolean forward) {
        StringBuilder jpql = new StringBuilder(
            "SELECT DISTINCT owner FROM Owner owner left join fetch owner.pets WHERE owner.lastName LIKE :lastName"
        );
        if (cursor != null) {
            jpql.append(forward ? " AND owner.id > :cursor" : " AND owner.id < :cursor");
        }
        jpql.append(" ORDER BY owner.id ").append(forward ? "ASC" : "DESC");

        Query query = this.em.createQuery(jpql.toString());
        query.setParameter("lastName", lastName + "%");
        if (cursor != null) {
            query.setParameter("cursor", cursor);
        }
        query.setMaxResults(limit);
        return query.getResultList();
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

    @Override
    public boolean existsByLastNameBefore(String lastName, int cursor) {
        Long count = this.em.createQuery(
                "SELECT COUNT(owner.id) FROM Owner owner WHERE owner.lastName LIKE :lastName AND owner.id < :cursor",
                Long.class)
            .setParameter("lastName", lastName + "%")
            .setParameter("cursor", cursor)
            .getSingleResult();
        return count != null && count > 0;
    }

    @Override
    public boolean existsByLastNameAfter(String lastName, int cursor) {
        Long count = this.em.createQuery(
                "SELECT COUNT(owner.id) FROM Owner owner WHERE owner.lastName LIKE :lastName AND owner.id > :cursor",
                Long.class)
            .setParameter("lastName", lastName + "%")
            .setParameter("cursor", cursor)
            .getSingleResult();
        return count != null && count > 0;
    }

    @Override
    public int countByLastName(String lastName) {
        Long count = this.em.createQuery(
                "SELECT COUNT(owner.id) FROM Owner owner WHERE owner.lastName LIKE :lastName",
                Long.class)
            .setParameter("lastName", lastName + "%")
            .getSingleResult();
        return count == null ? 0 : count.intValue();
    }

}
