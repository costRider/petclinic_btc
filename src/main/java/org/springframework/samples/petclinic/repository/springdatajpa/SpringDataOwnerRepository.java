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
package org.springframework.samples.petclinic.repository.springdatajpa;

import java.util.Collection;
import java.util.Collections;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.OwnerSearchResults;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Spring Data JPA specialization of the {@link OwnerRepository} interface
 *
 * @author Michael Isvy
 * @since 15.1.2013
 */
public interface SpringDataOwnerRepository extends OwnerRepository, Repository<Owner, Integer> {

    @Override
    @Query("SELECT DISTINCT owner FROM Owner owner left join fetch owner.pets WHERE owner.lastName LIKE :lastName%")
    public Collection<Owner> findByLastName(@Param("lastName") String lastName);

    @EntityGraph(attributePaths = {"pets"})
    @Query(value = "SELECT DISTINCT owner FROM Owner owner WHERE owner.lastName LIKE :lastName%",
        countQuery = "SELECT COUNT(DISTINCT owner.id) FROM Owner owner WHERE owner.lastName LIKE :lastName%")
    Page<Owner> findOwnersPageByLastName(@Param("lastName") String lastName, Pageable pageable);

    @Override
    default OwnerSearchResults findByLastName(String lastName, int page, int pageSize) {
        int sanitizedPageSize = Math.max(pageSize, 1);
        int sanitizedPage = Math.max(page, 1);

        Sort sort = Sort.by("lastName").ascending()
            .and(Sort.by("firstName").ascending())
            .and(Sort.by("id").ascending());

        Pageable pageable = PageRequest.of(sanitizedPage - 1, sanitizedPageSize, sort);
        Page<Owner> ownerPage = findOwnersPageByLastName(lastName, pageable);

        long totalElements = ownerPage.getTotalElements();
        if (totalElements == 0) {
            return new OwnerSearchResults(Collections.emptyList(), 0, 1, sanitizedPageSize, lastName);
        }

        int totalPages = ownerPage.getTotalPages();
        if (sanitizedPage > totalPages) {
            sanitizedPage = totalPages;
            pageable = PageRequest.of(sanitizedPage - 1, sanitizedPageSize, sort);
            ownerPage = findOwnersPageByLastName(lastName, pageable);
        }

        return new OwnerSearchResults(ownerPage.getContent(), (int) totalElements, sanitizedPage, sanitizedPageSize, lastName);
    }

    @Override
    @Query("SELECT owner FROM Owner owner left join fetch owner.pets WHERE owner.id =:id")
    public Owner findById(@Param("id") int id);
}
