package com.spring.example1;

import com.spring.example1.entity.Customer;
import com.spring.example1.entity.Employee;
import com.spring.example1.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@SpringBootApplication
public class Example1Application {

    private final PersonRepository personRepository;

    @Autowired
    public Example1Application(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(Example1Application.class, args);
    }

    @PostConstruct
    public void init() {
        System.out.println("I am init() method, using @PostConstruct");

        Employee employee = new Employee();
        employee.setMonthsInCompany(12);
        employee.setFirstname("Петя");
        personRepository.save(employee);

        Customer customer = new Customer();
        customer.setNumberOfAccounts(4);
        customer.setFirstname("Вася");
        personRepository.save(customer);
    }

    @PreDestroy
    public void destroy() {
        System.out.println("I am destroy() method, using @PreDestroy");
    }
}
