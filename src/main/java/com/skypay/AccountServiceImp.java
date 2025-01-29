package com.skypay;

import lombok.Getter;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AccountServiceImp implements AccountService{

    private final Clock clock;
    @Getter
    private int balance;
    @Getter
    private List<Transaction> transactions;

    public AccountServiceImp(Clock clock) {
        this.clock = clock;
        this.transactions = new ArrayList<>();
    }

    @Override
    public void deposit(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive.");
        }
        balance += amount;
        transactions.add(new Transaction(LocalDateTime.now(clock), amount, balance));
    }

    @Override
    public void withdraw(int amount) {

    }

    @Override
    public void printStatement() {

    }

}
