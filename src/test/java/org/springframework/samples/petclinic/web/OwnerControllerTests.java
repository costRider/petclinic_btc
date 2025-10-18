package org.springframework.samples.petclinic.web;

import java.util.Collections;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.OwnerSearchResults;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for {@link OwnerController}
 *
 * @author Colin But
 */

@SpringJUnitWebConfig(locations = {"classpath:spring/mvc-test-config.xml", "classpath:spring/mvc-core-config.xml"})
class OwnerControllerTests {

    private static final int TEST_OWNER_ID = 1;

    @Autowired
    private OwnerController ownerController;

    @Autowired
    private ClinicService clinicService;

    private MockMvc mockMvc;

    private Owner george;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(ownerController).build();

        george = new Owner();
        george.setId(TEST_OWNER_ID);
        george.setFirstName("George");
        george.setLastName("Franklin");
        george.setAddress("110 W. Liberty St.");
        george.setCity("Madison");
        george.setTelephone("6085551023");
        given(this.clinicService.findOwnerById(TEST_OWNER_ID)).willReturn(george);
        given(this.clinicService.findOwnerByLastName(anyString(), anyInt(), anyInt()))
            .willReturn(new OwnerSearchResults(Collections.emptyList(), 0, 1, 10, ""));

    }

    @Test
    void testInitCreationForm() throws Exception {
        mockMvc.perform(get("/owners/new"))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("owner"))
            .andExpect(view().name("owners/createOrUpdateOwnerForm"));
    }

    @Test
    void testProcessCreationFormSuccess() throws Exception {
        mockMvc.perform(post("/owners/new")
            .param("firstName", "Joe")
            .param("lastName", "Bloggs")
            .param("address", "123 Caramel Street")
            .param("city", "London")
            .param("telephone", "01316761638")
        )
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void testProcessCreationFormHasErrors() throws Exception {
        mockMvc.perform(post("/owners/new")
            .param("firstName", "Joe")
            .param("lastName", "Bloggs")
            .param("city", "London")
        )
            .andExpect(status().isOk())
            .andExpect(model().attributeHasErrors("owner"))
            .andExpect(model().attributeHasFieldErrors("owner", "address"))
            .andExpect(model().attributeHasFieldErrors("owner", "telephone"))
            .andExpect(view().name("owners/createOrUpdateOwnerForm"));
    }

    @Test
    void testInitFindForm() throws Exception {
        mockMvc.perform(get("/owners/find"))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("owner"))
            .andExpect(model().attributeExists("pageSizeOptions"))
            .andExpect(model().attribute("pageSize", is(10)))
            .andExpect(view().name("owners/findOwners"));
    }

    @Test
    void testProcessFindFormSuccess() throws Exception {
        given(this.clinicService.findOwnerByLastName("", 1, 10))
            .willReturn(new OwnerSearchResults(Lists.newArrayList(george, new Owner()), 2, 1, 10, ""));

        mockMvc.perform(get("/owners"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("totalCount", is(2)))
            .andExpect(model().attribute("page", is(1)))
            .andExpect(model().attribute("pageSize", is(10)))
            .andExpect(model().attribute("totalPages", is(1)))
            .andExpect(model().attributeExists("pageSizeOptions"))
            .andExpect(model().attribute("searchLastName", is("")))
            .andExpect(view().name("owners/ownersList"));
    }

    @Test
    void testProcessFindFormByLastName() throws Exception {
        given(this.clinicService.findOwnerByLastName(george.getLastName(), 1, 10))
            .willReturn(new OwnerSearchResults(Lists.newArrayList(george), 1, 1, 10, george.getLastName()));

        mockMvc.perform(get("/owners")
            .param("lastName", "Franklin")
        )
            .andExpect(status().isOk())
            .andExpect(model().attribute("totalCount", is(1)))
            .andExpect(model().attribute("page", is(1)))
            .andExpect(model().attribute("pageSize", is(10)))
            .andExpect(model().attribute("totalPages", is(1)))
            .andExpect(model().attributeExists("pageSizeOptions"))
            .andExpect(model().attribute("searchLastName", is(george.getLastName())))
            .andExpect(model().attribute("selections", hasSize(1)))
            .andExpect(view().name("owners/ownersList"));
    }

    @Test
    void testProcessFindFormNoOwnersFound() throws Exception {
        given(this.clinicService.findOwnerByLastName("Unknown Surname", 1, 10))
            .willReturn(new OwnerSearchResults(Collections.emptyList(), 0, 1, 10, "Unknown Surname"));

        mockMvc.perform(get("/owners")
            .param("lastName", "Unknown Surname")
        )
            .andExpect(status().isOk())
            .andExpect(model().attributeHasFieldErrors("owner", "lastName"))
            .andExpect(model().attributeHasFieldErrorCode("owner", "lastName", "notFound"))
            .andExpect(model().attributeExists("pageSizeOptions"))
            .andExpect(model().attribute("pageSize", is(10)))
            .andExpect(view().name("owners/findOwners"));
    }

    @Test
    void testInitUpdateOwnerForm() throws Exception {
        mockMvc.perform(get("/owners/{ownerId}/edit", TEST_OWNER_ID))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("owner"))
            .andExpect(model().attribute("owner", hasProperty("lastName", is("Franklin"))))
            .andExpect(model().attribute("owner", hasProperty("firstName", is("George"))))
            .andExpect(model().attribute("owner", hasProperty("address", is("110 W. Liberty St."))))
            .andExpect(model().attribute("owner", hasProperty("city", is("Madison"))))
            .andExpect(model().attribute("owner", hasProperty("telephone", is("6085551023"))))
            .andExpect(view().name("owners/createOrUpdateOwnerForm"));
    }

    @Test
    void testProcessUpdateOwnerFormSuccess() throws Exception {
        mockMvc.perform(post("/owners/{ownerId}/edit", TEST_OWNER_ID)
            .param("firstName", "Joe")
            .param("lastName", "Bloggs")
            .param("address", "123 Caramel Street")
            .param("city", "London")
            .param("telephone", "01616291589")
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name("redirect:/owners/{ownerId}"));
    }

    @Test
    void testProcessUpdateOwnerFormHasErrors() throws Exception {
        mockMvc.perform(post("/owners/{ownerId}/edit", TEST_OWNER_ID)
            .param("firstName", "Joe")
            .param("lastName", "Bloggs")
            .param("city", "London")
        )
            .andExpect(status().isOk())
            .andExpect(model().attributeHasErrors("owner"))
            .andExpect(model().attributeHasFieldErrors("owner", "address"))
            .andExpect(model().attributeHasFieldErrors("owner", "telephone"))
            .andExpect(view().name("owners/createOrUpdateOwnerForm"));
    }

    @Test
    void testShowOwner() throws Exception {
        mockMvc.perform(get("/owners/{ownerId}", TEST_OWNER_ID))
            .andExpect(status().isOk())
            .andExpect(model().attribute("owner", hasProperty("lastName", is("Franklin"))))
            .andExpect(model().attribute("owner", hasProperty("firstName", is("George"))))
            .andExpect(model().attribute("owner", hasProperty("address", is("110 W. Liberty St."))))
            .andExpect(model().attribute("owner", hasProperty("city", is("Madison"))))
            .andExpect(model().attribute("owner", hasProperty("telephone", is("6085551023"))))
            .andExpect(view().name("owners/ownerDetails"));
    }

}
