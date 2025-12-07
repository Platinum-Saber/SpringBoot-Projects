package com.gtn.bank.deadlock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * This runner will start automatically on application startup
 * (when 'deadlock-demo' profile is active) and demonstrate
 * a classic deadlock scenario using two bank accounts.
 */
@Component
@Profile("deadlock-demo")
public class DeadlockRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DeadlockRunner.class);

    private final NaiveTransferService transferService;

    public DeadlockRunner(NaiveTransferService transferService) {
        this.transferService = transferService;
    }

    @Override
    public void run(String... args) {
        log.info("Starting deadlock demo...");

        BankAccount accountA = new BankAccount(1L, 1000);
        BankAccount accountB = new BankAccount(2L, 1000);

        Thread t1 = new Thread(() -> {
            while (true) {
                try {
                    transferService.transfer(accountA, accountB, 10);
                    Thread.sleep(50);
                } catch (Exception e) {
                    log.error("Error in thread T1", e);
                }
            }
        }, "T1");

        Thread t2 = new Thread(() -> {
            while (true) {
                try {
                    transferService.transfer(accountB, accountA, 10);
                    Thread.sleep(50);
                } catch (Exception e) {
                    log.error("Error in thread T2", e);
                }
            }
        }, "T2");

        t1.start();
        t2.start();

        log.info("DeadlockRunner started threads T1 and T2. Watch logs for deadlock.");
    }
}