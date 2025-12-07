package com.gtn.bank.transactional;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
public class AccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal balance;

    @Column(nullable = false, unique = true)
    private String ownerName;

    protected AccountEntity() {
        // JPA only
    }

    public AccountEntity(String ownerName, BigDecimal initialBalance) {
        this.ownerName = ownerName;
        this.balance = initialBalance;
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}