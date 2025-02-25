package com.ibridge.repository;

import com.ibridge.domain.entity.Family;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FamliyRepository extends JpaRepository<Family, Long> {
}
