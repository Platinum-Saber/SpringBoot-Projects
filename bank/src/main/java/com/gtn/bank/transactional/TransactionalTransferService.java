package com.gtn.bank.transactional;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class TransactionalTransferService {

    private static final Logger log = LoggerFactory.getLogger(TransactionalTransferService.class);

    private final AccountRepository accountRepository;

    public TransactionalTransferService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * Transfer money from one account to another atomically.
     *
     * This method is transactional:
     * - Either both updates (debit + credit) are committed
     * - Or none are committed (rollback on error)
     *
     * To avoid deadlocks:
     * - We always lock the accounts in a consistent order (by id).
     * - This removes the circular-wait pattern of the naive solution.
     */
    @Transactional
    public void transfer(long fromId, long toId, BigDecimal amount) {
        if (fromId == toId) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        log.debug("Starting transactional transfer from {} to {} amount {}", fromId, toId, amount);

        // Enforce a consistent ordering of DB row access
        long firstId = Math.min(fromId, toId);
        long secondId = Math.max(fromId, toId);

        AccountEntity firstAccount = accountRepository.findById(firstId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + firstId));

        AccountEntity secondAccount = accountRepository.findById(secondId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + secondId));

        // Map back to from/to logically
        AccountEntity from;
        AccountEntity to;

        if (fromId == firstId) {
            from = firstAccount;
            to = secondAccount;
        } else {
            from = secondAccount;
            to = firstAccount;
        }

        if (from.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds in account " + from.getId());
        }

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

        log.info("Transactional transfer complete: {} -> {} amount {}. New balances: from={}, to={}",
                fromId, toId, amount, from.getBalance(), to.getBalance());
        // No explicit save() needed if JPA is tracking these entities; changes flush on transaction commit
    }
}