package com.spring.example2.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "persons")
@Data
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "my_generator")
    @SequenceGenerator(name = "my_generator", sequenceName = "my_seq", allocationSize = 20)
    @Column(name = "id")
    private Long id;

    @Column(name = "firstname")
    private String firstname;
}
