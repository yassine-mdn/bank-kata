package com.skypay;

import org.junit.jupiter.api.AfterEach;
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

    @AfterEach
    public void tearDown() {
        System.setOut(standardOut);
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
        assertEquals(LocalDateTime.now(clock).format(formater), accountServiceImp.getTransactions().getFirst().timeStamp().format(formater));
        assertEquals(100, accountServiceImp.getTransactions().getFirst().amount());
        assertEquals(100, accountServiceImp.getTransactions().getFirst().balance());
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

    @Test
    void withdraw_ShouldThrowException_WhenGivenNegativeValues() {
        assertThrows(IllegalArgumentException.class, () -> accountServiceImp.withdraw(-200));
    }

    @Test
    void withdraw_ShouldThrowException_WhenGivenZero() {
        assertThrows(IllegalArgumentException.class, () -> accountServiceImp.withdraw(0));
    }

    @Test
    void withdraw_ShouldThrowException_WhenGivenValueGreaterThanBalance() {
        assertThrows(IllegalArgumentException.class, () -> accountServiceImp.withdraw(200));
    }

    @Test
    void withdraw_ShouldDecreaseBalance_WhenGivenPositiveValuesThatIsLowerThanBalance() {
        accountServiceImp.deposit(100);
        accountServiceImp.withdraw(50);
        assertEquals(50, accountServiceImp.getBalance());
    }

    @Test
    void withdraw_ShouldAddTransactionToStatement_WhenWithdrawIsMade() {
        accountServiceImp.deposit(100);
        accountServiceImp.withdraw(50);
        assertEquals(2, accountServiceImp.getTransactions().size());
        assertEquals(LocalDateTime.now(clock).format(formater), accountServiceImp.getTransactions().get(1).timeStamp().format(formater));
        assertEquals(-50, accountServiceImp.getTransactions().get(1).amount());
        assertEquals(50, accountServiceImp.getTransactions().get(1).balance());
    }

    @Test
    void withdraw_ShouldTransactionsHaveDifferentTimeStamps_WhenMadeInTheSameDay(){
        setClockTo("2020-01-01T00:00");
        accountServiceImp.deposit(100);
        accountServiceImp.deposit(200);
        setClockTo("2020-01-01T01:00");
        accountServiceImp.withdraw(50);
        setClockTo("2020-01-01T05:00");
        accountServiceImp.withdraw(150);
        assertEquals(4, accountServiceImp.getTransactions().size());
        assertNotEquals(accountServiceImp.getTransactions().get(2).timeStamp().toString(), accountServiceImp.getTransactions().get(3).timeStamp().toString());
    }

    @Test
    void printStatement_ShouldPrintNoTransactionMessage_WhenNoTransactions() {
        accountServiceImp.printStatement();
        assertEquals(PRINT_STATEMENT_EMPTY, outputStreamCaptor.toString());
    }

    @Test
    void printStatement_ShouldIncludePrintHeader_WhenTransactionsExist() {
        accountServiceImp.deposit(100);
        accountServiceImp.printStatement();
        assertTrue(outputStreamCaptor.toString().trim().contains(PRINT_STATEMENT_HEADER));
    }

    @Test
    void PrintStatement_ShouldDisplayTransactionsInReverseChronologicalOrder_WhenTransactionsExist() {
        setClockTo("2020-01-01T00:00");
        accountServiceImp.deposit(100);
        setClockTo("2020-01-01T01:00");
        accountServiceImp.deposit(200);
        setClockTo("2020-01-02T00:00");
        accountServiceImp.withdraw(50);
        setClockTo("2020-01-02T01:00");
        accountServiceImp.withdraw(150);

        accountServiceImp.printStatement();

        String expected = PRINT_STATEMENT_HEADER+
                "02/01/2020 || -150 || 100\n"+
                "02/01/2020 || -50 || 250\n"+
                "01/01/2020 || 200 || 300\n"+
                "01/01/2020 || 100 || 100\n";

        assertEquals(expected.trim(), outputStreamCaptor.toString().trim());
    }

}