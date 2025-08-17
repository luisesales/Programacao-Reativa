package com.bankai.mcpserver.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long bankId;
    private String name;
    private double balance;
    private boolean isActive;


    public Long getId() {
        return id;
    }

    public Long getbankId() {
        return bankId;
    }


    public String getName() {
        return name;
    }    

    public void setBankId(Long bankId){
        this.bankId = bankId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public void deposit(double value){
        this.balance += value;
    }

    public void draw(double value){
        this.balance -= value;
    }

    public boolean isActive(){
        return isActive;
    }

    public void activateDeactivate(){
        this.isActive  = !this.isActive;
    }

}
