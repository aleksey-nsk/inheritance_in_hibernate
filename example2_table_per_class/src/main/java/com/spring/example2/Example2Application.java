package com.spring.example2;

import com.spring.example2.entity.Customer;
import com.spring.example2.entity.Employee;
import com.spring.example2.entity.Person;
import com.spring.example2.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import java.util.List;

@SpringBootApplication
public class Example2Application {

    private final PersonRepository personRepository;

    @Autowired
    public Example2Application(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(Example2Application.class, args);
    }

    @PostConstruct
    public void init() {
        System.out.println("I am init() method, using @PostConstruct");

        Employee employee = new Employee();
        employee.setMonthsInCompany(4);
        employee.setFirstname("Николай");
        personRepository.save(employee);

        Customer customer = new Customer();
        customer.setNumberOfAccounts(5);
        customer.setFirstname("Дмитрий");
        personRepository.save(customer);

        Person person = new Person();
        person.setFirstname("Человек");
        personRepository.save(person);

        System.out.println("Select all persons");
        List<Person> persons = personRepository.selectAll();
        System.out.println("persons: " + persons);
    }
}
