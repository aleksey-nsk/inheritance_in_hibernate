package com.spring.example1.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
public class Customer extends Person {

    @Column(name = "number_of_accounts")
    private Integer numberOfAccounts;
}
