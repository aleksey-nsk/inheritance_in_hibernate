package com.spring.example4.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

@Entity
@Data
@NoArgsConstructor
public class Customer extends Person {

    private Integer numberOfAccounts;
}
