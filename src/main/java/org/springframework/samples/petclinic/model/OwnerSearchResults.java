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
package org.springframework.samples.petclinic.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Container for paginated owner search results.
 */
public class OwnerSearchResults {

    private final List<Owner> owners;
    private final int totalCount;
    private final int page;
    private final int pageSize;
    private final String lastName;

    public OwnerSearchResults(Collection<Owner> owners, int totalCount, int page, int pageSize, String lastName) {
        this.owners = Collections.unmodifiableList(new ArrayList<>(owners));
        this.totalCount = Math.max(totalCount, 0);
        this.page = Math.max(page, 1);
        this.pageSize = Math.max(pageSize, 1);
        this.lastName = lastName == null ? "" : lastName;
    }

    public List<Owner> getOwners() {
        return owners;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public String getLastName() {
        return lastName;
    }

    public int getTotalPages() {
        if (totalCount == 0) {
            return 0;
        }
        return (int) Math.ceil(totalCount / (double) pageSize);
    }
}
