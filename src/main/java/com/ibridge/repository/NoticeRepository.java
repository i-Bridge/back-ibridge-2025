package com.ibridge.repository;

import com.ibridge.domain.entity.Notice;
import com.ibridge.domain.entity.Parent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    @Query("SELECT n FROM Notice n WHERE n.receivers = :receiver ORDER BY n.id asc")
    List<Notice> findByReceiver(@Param("receiver")Parent receiver);
}
