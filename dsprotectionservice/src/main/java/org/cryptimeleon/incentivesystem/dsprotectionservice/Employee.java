package org.cryptimeleon.incentivesystem.dsprotectionservice;

import lombok.Value;

import javax.persistence.*;

@Entity
@Table(name="employees")
@Value // lombok decorator
public class Employee {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private long id;
    private String name;
}
