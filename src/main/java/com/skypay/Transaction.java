package com.skypay;

import java.time.LocalDateTime;

/**
 * This class represents a transaction.
 */
public record Transaction(
        LocalDateTime timeStamp,
        int amount,
        int balance
) {
}
