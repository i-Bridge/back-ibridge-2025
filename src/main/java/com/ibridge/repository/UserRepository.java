package com.ibridge.repository;

import com.ibridge.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    @Query("SELECT u FROM User u WHERE u.accountId = :accountId AND u.role = 'PARENT'")
    List<User> findParentsByAccountId(@Param("accountId") Long accountId);

    @Query("SELECT u FROM User u WHERE u.accountId = :accountId AND u.role = 'CHILD'")
    List<User> findChildrenByAccountId(@Param("accountId") Long accountId);
}
