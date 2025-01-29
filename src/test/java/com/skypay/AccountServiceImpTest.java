package com.skypay;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.*;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AccountServiceImpTest {

    private AccountServiceImp accountServiceImp;
    private Clock clock;
    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    private static final String PRINT_STATEMENT_HEADER = "date       || Amount || balance\n";
    private static final String PRINT_STATEMENT_EMPTY = "No transactions to display\n";
    private static final DateTimeFormatter formater = DateTimeFormatter.ofPattern("yyyy/M/dd");

    @BeforeEach
    void setUp() {
        clock = mock(Clock.class);
        setClockTo("2025-01-29T00:00");
        accountServiceImp = new AccountServiceImp(clock);
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    /**
     * @param dateTime This method sets the clock to a specific date.
     */
    private void setClockTo(String dateTime) {
        Instant instant = LocalDateTime.parse(dateTime)
                .atZone(ZoneId.systemDefault())
                .toInstant();
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
        accountServiceImp.deposit(100);
        assertEquals(1, accountServiceImp.getTransactions().size());
        assertEquals(LocalDateTime.now().format(formater), accountServiceImp.getTransactions().get(0).timeStamp().format(formater));
        assertEquals(100, accountServiceImp.getTransactions().get(0).amount());
        assertEquals(100, accountServiceImp.getTransactions().get(0).balance());
    }

    @Test
    void deposit_ShouldTransactionsHaveDifferentTimeStamps_WhenMadeInTheSameDay(){
        setClockTo("2020-01-01T00:00");
        accountServiceImp.deposit(100);
        setClockTo("2020-01-01T01:00");
        accountServiceImp.deposit(200);
        assertEquals(2, accountServiceImp.getTransactions().size());
        assertNotEquals(accountServiceImp.getTransactions().get(0).timeStamp().toString(), accountServiceImp.getTransactions().get(1).timeStamp().toString());
    }
}