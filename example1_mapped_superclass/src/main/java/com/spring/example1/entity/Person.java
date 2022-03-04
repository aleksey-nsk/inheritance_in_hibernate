package com.spring.example1.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@MappedSuperclass
@Data
@NoArgsConstructor
public abstract class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "my_generator")
    @SequenceGenerator(name = "my_generator", sequenceName = "my_seq", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(name = "firstname")
    private String firstname;
}
