package uk.co.huntersix.spring.rest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.co.huntersix.spring.rest.model.Person;
import uk.co.huntersix.spring.rest.referencedata.PersonDataService;

import java.util.List;

@RestController
public class PersonController {
    private PersonDataService personDataService;

    public PersonController(@Autowired PersonDataService personDataService) {
        this.personDataService = personDataService;
    }

    @GetMapping("/person/{lastName}")
    public ResponseEntity<List<Person>> personByLastName(@PathVariable String lastName) {
        List<Person> people = personDataService.findPersonByLastName(lastName);
        if (people.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(people);
    }

    @GetMapping("/person/{lastName}/{firstName}")
    public ResponseEntity<Person> person(@PathVariable String lastName, @PathVariable String firstName) {
        Person person = personDataService.findPerson(firstName, lastName);
        if (person == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(person);
    }

    @PostMapping("/person")
    public ResponseEntity<?> addPerson(@RequestBody Person person) {
        List<Person> existingPeople = personDataService.findPersonByLastName(person.getLastName());

        if (existingPeople.stream().anyMatch(p -> p.getFirstName().equalsIgnoreCase(person.getFirstName()))) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Person already exists");
        }

        Person newPerson = personDataService.addPerson(person);
        return ResponseEntity.status(HttpStatus.CREATED).body(newPerson);
    }

}