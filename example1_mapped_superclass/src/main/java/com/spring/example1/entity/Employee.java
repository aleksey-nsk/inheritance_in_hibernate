package com.spring.example1.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "employees")
@Data
@NoArgsConstructor
public class Employee extends Person {

    @Column(name = "months_in_company")
    private Integer monthsInCompany;
}
