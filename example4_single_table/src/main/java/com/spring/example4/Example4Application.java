package com.spring.example4;

import com.spring.example4.entity.Customer;
import com.spring.example4.entity.Employee;
import com.spring.example4.entity.Person;
import com.spring.example4.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import java.util.List;

@SpringBootApplication
public class Example4Application {

    private final PersonRepository personRepository;

    @Autowired
    public Example4Application(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(Example4Application.class, args);
    }

    @PostConstruct
    public void init() {
        System.out.println("I am init() method, using @PostConstruct");

        Employee employee = new Employee();
        employee.setMonthsInCompany(11);
        employee.setFirstname("Егорка");
        personRepository.save(employee);

        Customer customer = new Customer();
        customer.setNumberOfAccounts(12);
        customer.setFirstname("Славик");
        personRepository.save(customer);

        System.out.println("Select all persons");
        List<Person> persons = personRepository.selectAll();
        System.out.println("persons: " + persons);
    }
}
