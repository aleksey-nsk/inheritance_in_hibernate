package com.spring.example4.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

@Entity
@Data
@NoArgsConstructor
public class Employee extends Person {

    private Integer monthsInCompany;
}
