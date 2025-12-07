package com.gtn.bank.deadlock;

public class BankAccount {

    private final long id;
    private int balance;

    public BankAccount(long id, int initialBalance) {
        this.id = id;
        this.balance = initialBalance;
    }

    public long getId() {
        return id;
    }

    public int getBalance() {
        return balance;
    }

    public void deposit(int amount) {
        this.balance += amount;
    }

    public void withdraw(int amount) {
        this.balance -= amount;
    }

    @Override
    public String toString() {
        return "BankAccount{" +
                "id=" + id +
                ", balance=" + balance +
                '}';
    }
}