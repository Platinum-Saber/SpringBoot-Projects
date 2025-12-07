package com.gtn.bank.controller;

import com.gtn.bank.transactional.AccountEntity;
import com.gtn.bank.transactional.AccountRepository;
import com.gtn.bank.transactional.TransactionalTransferService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/accounts")
public class AccountController {

    private static final Logger log = LoggerFactory.getLogger(AccountController.class);

    private final AccountRepository accountRepository;
    private final TransactionalTransferService transferService;

    public AccountController(AccountRepository accountRepository,
                             TransactionalTransferService transferService) {
        this.accountRepository = accountRepository;
        this.transferService = transferService;
    }

    // DTOs

    public record CreateAccountRequest(String ownerName, BigDecimal initialBalance) {}
    public record CreateAccountResponse(Long id, String ownerName, BigDecimal balance) {}
    public record TransferRequest(Long fromAccountId, Long toAccountId, BigDecimal amount) {}

    // POST /v1/accounts
    @PostMapping
    public ResponseEntity<CreateAccountResponse> createAccount(@RequestBody CreateAccountRequest request) {
        if (request.ownerName() == null || request.ownerName().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if (request.initialBalance() == null || request.initialBalance().compareTo(BigDecimal.ZERO) < 0) {
            return ResponseEntity.badRequest().build();
        }

        AccountEntity entity = new AccountEntity(request.ownerName(), request.initialBalance());
        AccountEntity saved = accountRepository.save(entity);

        CreateAccountResponse response = new CreateAccountResponse(
                saved.getId(),
                saved.getOwnerName(),
                saved.getBalance()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /v1/accounts
    @GetMapping
    public List<AccountEntity> listAccounts() {
        return accountRepository.findAll();
    }

    // POST /v1/accounts:transfer
    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestBody TransferRequest request) {
        try {
            transferService.transfer(
                    request.fromAccountId(),
                    request.toAccountId(),
                    request.amount()
            );
            return ResponseEntity.ok(Map.of(
                    "status", "OK",
                    "message", "Transfer completed"
            ));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            log.warn("Business error during transfer", ex);
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "ERROR",
                    "message", ex.getMessage()
            ));
        } catch (Exception ex) {
            log.error("Unexpected error during transfer", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "ERROR",
                    "message", "Internal server error"
            ));
        }
    }
}