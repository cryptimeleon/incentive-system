package org.cryptimeleon.incentivesystem.dsprotectionservice.dummy;

import lombok.Value;

import javax.persistence.*;

@Entity
@Table(name="employees")
public class Employee {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private long id;
    private String name;

    public Employee(){

    }

    public Employee(long id, String name){
        this.id = id;
        this.name = name;
    }

    public String toString()
    {
        return id + " " + this.name;
    }
}
