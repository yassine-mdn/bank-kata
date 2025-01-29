package com.skypay;

import lombok.Getter;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AccountServiceImp implements AccountService{

    private final Clock clock;
    @Getter
    private int balance;
    @Getter
    private final List<Transaction> transactions;

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
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive.");
        }
        if (amount > balance) {
            throw new IllegalArgumentException("Insufficient funds for withdrawal.");
        }
        balance -= amount;
        transactions.add(new Transaction(LocalDateTime.now(clock), -amount, balance));
    }

    @Override
    public void printStatement() {
        if (transactions.isEmpty()) {
            System.out.print("No transactions to display\n");
            return;
        }
        System.out.print("date       || Amount || balance\n");
        transactions.stream()
                .sorted(Comparator.comparing(Transaction::timeStamp).reversed())
                .forEach(this::printTransaction);
    }

    private void printTransaction(Transaction transaction) {
        System.out.printf("%s || %d || %d\n",
                formatDate(transaction.timeStamp()),
                transaction.amount(),
                transaction.balance());
    }

    private String formatDate(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

}
