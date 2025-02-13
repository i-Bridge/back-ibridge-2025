package com.ibridge.repository;

import com.ibridge.domain.entity.Account;
import com.ibridge.domain.entity.Parent;
import com.ibridge.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Account findById(long id);
    @Query("SELECT a FROM Account a WHERE a.id = :accountId")
    Account findAccountById(@Param("Id") Long Id);
}
