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
package org.springframework.samples.petclinic.web;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.OwnerSearchResults;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 */
@Controller
public class OwnerController {

    private static final String VIEWS_OWNER_CREATE_OR_UPDATE_FORM = "owners/createOrUpdateOwnerForm";
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final List<Integer> PAGE_SIZE_OPTIONS = Arrays.asList(10, 20, 30, 40, 50);
    private final ClinicService clinicService;


    @Autowired
    public OwnerController(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    @InitBinder
    public void setAllowedFields(WebDataBinder dataBinder) {
        dataBinder.setDisallowedFields("id");
    }

    @GetMapping(value = "/owners/new")
    public String initCreationForm(Map<String, Object> model) {
        Owner owner = new Owner();
        model.put("owner", owner);
        return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
    }

    @PostMapping(value = "/owners/new")
    public String processCreationForm(@Valid Owner owner, BindingResult result) {
        if (result.hasErrors()) {
            return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
        } else {
            this.clinicService.saveOwner(owner);
            return "redirect:/owners/" + owner.getId();
        }
    }

    @GetMapping(value = "/owners/find")
    public String initFindForm(Map<String, Object> model) {
        model.put("owner", new Owner());
        addPaginationOptions(model, DEFAULT_PAGE_SIZE);
        return "owners/findOwners";
    }

    @GetMapping(value = "/owners")
    public String processFindForm(Owner owner, BindingResult result, Map<String, Object> model,
        @RequestParam(value = "page", defaultValue = "1") int page,
        @RequestParam(value = "size", defaultValue = "10") int size) {

        // allow parameterless GET request for /owners to return all records
        if (owner.getLastName() == null) {
            owner.setLastName(""); // empty string signifies broadest possible search
        }

        int pageSize = normalizePageSize(size);
        addPaginationOptions(model, pageSize);

        OwnerSearchResults results = this.clinicService.findOwnerByLastName(owner.getLastName(), page, pageSize);

        if (results.getTotalCount() == 0) {
            // no owners found
            result.rejectValue("lastName", "notFound", "not found");
            return "owners/findOwners";
        }

        // owners found (one or more)
        model.put("owner", owner);
        model.put("selections", results.getOwners());
        model.put("totalCount", results.getTotalCount());
        model.put("page", results.getPage());
        model.put("pageSize", results.getPageSize());
        model.put("totalPages", results.getTotalPages());
        model.put("searchLastName", results.getLastName());
        return "owners/ownersList";
    }

    private void addPaginationOptions(Map<String, Object> model, int pageSize) {
        model.put("pageSizeOptions", PAGE_SIZE_OPTIONS);
        model.put("pageSize", pageSize);
    }

    private int normalizePageSize(int requestedSize) {
        if (PAGE_SIZE_OPTIONS.contains(requestedSize)) {
            return requestedSize;
        }
        if (requestedSize <= PAGE_SIZE_OPTIONS.get(0)) {
            return PAGE_SIZE_OPTIONS.get(0);
        }
        if (requestedSize >= PAGE_SIZE_OPTIONS.get(PAGE_SIZE_OPTIONS.size() - 1)) {
            return PAGE_SIZE_OPTIONS.get(PAGE_SIZE_OPTIONS.size() - 1);
        }
        for (int i = PAGE_SIZE_OPTIONS.size() - 1; i >= 0; i--) {
            int option = PAGE_SIZE_OPTIONS.get(i);
            if (requestedSize >= option) {
                return option;
            }
        }
        return PAGE_SIZE_OPTIONS.get(0);
    }

    @GetMapping(value = "/owners/{ownerId}/edit")
    public String initUpdateOwnerForm(@PathVariable("ownerId") int ownerId, Model model) {
        Owner owner = this.clinicService.findOwnerById(ownerId);
        model.addAttribute(owner);
        return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
    }

    @PostMapping(value = "/owners/{ownerId}/edit")
    public String processUpdateOwnerForm(@Valid Owner owner, BindingResult result, @PathVariable("ownerId") int ownerId) {
        if (result.hasErrors()) {
            return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
        } else {
            owner.setId(ownerId);
            this.clinicService.saveOwner(owner);
            return "redirect:/owners/{ownerId}";
        }
    }

    /**
     * Custom handler for displaying an owner.
     *
     * @param ownerId the ID of the owner to display
     * @return a ModelMap with the model attributes for the view
     */
    @GetMapping("/owners/{ownerId}")
    public ModelAndView showOwner(@PathVariable("ownerId") int ownerId) {
        ModelAndView mav = new ModelAndView("owners/ownerDetails");
        mav.addObject(this.clinicService.findOwnerById(ownerId));
        return mav;
    }

}
