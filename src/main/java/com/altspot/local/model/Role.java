package com.altspot.local.model;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false)
    private AppRole name;

    public Role() {}

    public Role(AppRole appRole){
        this.name = appRole;
    }

    public AppRole getRole(){
        return this.name;
    }
    @Override
    public String toString(){
        return this.name.name();
    }



}
