package com.skypay;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AccountServiceImpTest {

    private AccountServiceImp accountServiceImp;
    private Clock clock;
    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    private static final String PRINT_STATEMENT_HEADER = "date       || Amount || balance\n";

    @BeforeEach
    void setUp() {
        clock = mock(Clock.class);
        accountServiceImp = new AccountServiceImp(clock);
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    /**
     * @param date This method sets the clock to a specific date.
     */
    private void setClockTo(String date) {
        Instant instant = LocalDate.parse(date).atStartOfDay(ZoneId.systemDefault()).toInstant();
        when(clock.instant()).thenReturn(instant);
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
    }

    @Test
    void transaction_ShouldBeEmptyList_WhenAccountIsCreated() {
        assertEquals(0, accountServiceImp.getTransactions().size());
    }

    @Test
    void deposit_ShouldIncreaseBalanceToSameValueAsDeposit_WhenBalanceIsEmpty() {
        accountServiceImp.deposit(100);
        assertEquals(100, accountServiceImp.getBalance());
    }

    @Test
    void deposit_ShouldIncreaseBalance_WhenGivenPositiveValues() {
        accountServiceImp.deposit(100);
        accountServiceImp.deposit(200);
        assertEquals(300, accountServiceImp.getBalance());
    }

    @Test
    void deposit_ShouldThrowException_WhenGivenNegativeValues() {
        assertThrows(IllegalArgumentException.class, () -> accountServiceImp.deposit(-200));
    }

    @Test
    void deposit_ShouldAddTransactionToStatement_WhenDepositIsMade() {
        setClockTo("2020-01-01");
        accountServiceImp.deposit(100);
        accountServiceImp.printStatement();
        String expected =  PRINT_STATEMENT_HEADER+
                          "01/01/2020 || 100.00 || 100.00";
        assertEquals(expected,  outputStreamCaptor.toString()
                .trim());
    }

    @Test
    void deposit_ShouldTransactionsHaveDifferentTimeStamps_WhenMadeInTheSameDay(){
        setClockTo("2020-01-01");
        accountServiceImp.deposit(100);
        accountServiceImp.deposit(200);
        assertEquals(2, accountServiceImp.getTransactions().size());
        assertNotEquals(accountServiceImp.getTransactions().get(0).getTimestamp(), accountServiceImp.getTransactions().get(1).getTimestamp());
    }
}