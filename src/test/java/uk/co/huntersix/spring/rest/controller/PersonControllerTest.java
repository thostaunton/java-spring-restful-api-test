package uk.co.huntersix.spring.rest.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.co.huntersix.spring.rest.model.Person;
import uk.co.huntersix.spring.rest.referencedata.PersonDataService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RunWith(SpringRunner.class)
@WebMvcTest(PersonController.class)
public class PersonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PersonDataService personDataService;

    @Autowired
    private ObjectMapper objectMapper;

    List<Person> peopleWithDifferentLastNames = Arrays.asList(
            new Person("Thomas", "Staunton"),
            new Person("Mary", "Smith"));

    List<Person> peopleWithTheSameLastNames = Arrays.asList(
            new Person("Mary", "Smith"),
            new Person("John", "Smith")
    );


    @Test
    public void shouldReturnPersonFromService() throws Exception {
        when(personDataService.findPerson(any(), any())).thenReturn(new Person("Mary", "Smith"));
        this.mockMvc.perform(get("/person/smith/mary"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("id").exists())
            .andExpect(jsonPath("firstName").value("Mary"))
            .andExpect(jsonPath("lastName").value("Smith"));
    }

    // Exercise 2
    // I have updated the person endpoint to return a not found status if there is no person found.
    // This tests that scenario.
    @Test
    public void shouldNotReturnPersonFromService() throws Exception {
        mockMvc.perform(get("/person/Notexist/Does"))
                .andExpect(status().isNotFound());
    }

    // Exercise 3
    // Created a findPersonByLastName service and endpoint
    // Here I test multiple matches, a single match and no match for the surname

    @Test
    public void shouldReturnPersonListWhenOnePersonMatchSurname() throws Exception {
        when(personDataService.findPersonByLastName(anyString())).thenReturn(peopleWithDifferentLastNames);
        mockMvc.perform(get("/person/Staunton"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].lastName", is("Staunton")));
    }

    @Test
    public void shouldReturnPersonListWhenMultiplePeopleMatchSurname() throws Exception {
        when(personDataService.findPersonByLastName(anyString())).thenReturn(peopleWithTheSameLastNames);
        mockMvc.perform(get("/person/Smith"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].lastName", is("Smith")))
                .andExpect(jsonPath("$[1].lastName", is("Smith")));
    }

    @Test
    public void shouldReturnNotFoundWhenNoPersonMatchSurname() throws Exception {
        mockMvc.perform(get("/person/DoesNotExist"))
                .andExpect(status().isNotFound());
    }

    // Exercise 4
    // Created a addPerson service and endpoint
    // Here I test adding a new person and adding an existing entry which I handle by using my
    // findPersonByLastName method to test for a match before returning a created HTTP status
    // if the person does not already exist

    @Test
    public void shouldAddNewPerson() throws Exception {
        Person newPerson = new Person("Thomas", "Staunton");
        when(personDataService.addPerson(any(Person.class))).thenReturn(newPerson);

        mockMvc.perform(MockMvcRequestBuilders.post("/person")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(newPerson)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName", is(newPerson.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(newPerson.getLastName())));
    }

    @Test
    public void shouldNotAddExistingPerson() throws Exception {
        Person existingPerson = new Person("Mary", "Smith");
        List<Person> existingPeople = Collections.singletonList(existingPerson);
        when(personDataService.findPersonByLastName(existingPerson.getLastName())).thenReturn(existingPeople);

        mockMvc.perform(MockMvcRequestBuilders.post("/person")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(existingPerson)))
                .andExpect(status().isConflict());
    }

}