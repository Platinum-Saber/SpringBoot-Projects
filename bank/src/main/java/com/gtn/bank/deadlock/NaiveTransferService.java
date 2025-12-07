package com.gtn.bank.deadlock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NaiveTransferService {

    private static final Logger log = LoggerFactory.getLogger(NaiveTransferService.class);

    /**
     * Naive transfer method that can cause a deadlock.
     *
     * It synchronizes first on the "from" account, then on the "to" account.
     * If two threads call this with inverted parameters, e.g.
     *   T1: transfer(A, B)
     *   T2: transfer(B, A)
     * they can each grab the first lock and wait forever on the second.
     */
    public void transfer(BankAccount from, BankAccount to, int amount) {
        log.debug("Thread {} attempting transfer {} -> {} amount {}",
                Thread.currentThread().getName(), from.getId(), to.getId(), amount);

        synchronized (from) {
            log.debug("Thread {} locked FROM account {}",
                    Thread.currentThread().getName(), from.getId());

            // Simulate some processing time to increase chance of deadlock
            sleep(100);

            synchronized (to) {
                log.debug("Thread {} locked TO account {}",
                        Thread.currentThread().getName(), to.getId());

                if (from.getBalance() < amount) {
                    throw new IllegalStateException("Insufficient funds in account " + from.getId());
                }

                from.withdraw(amount);
                to.deposit(amount);

                log.info("Thread {} completed transfer {} -> {} amount {}. Balances: from={}, to={}",
                        Thread.currentThread().getName(), from.getId(), to.getId(), amount,
                        from.getBalance(), to.getBalance());
            }
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}