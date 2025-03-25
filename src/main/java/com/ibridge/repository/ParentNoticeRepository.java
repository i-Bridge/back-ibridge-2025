package com.ibridge.repository;

import com.ibridge.domain.entity.Notice;
import com.ibridge.domain.entity.Parent;
import com.ibridge.domain.entity.ParentNotice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ParentNoticeRepository extends JpaRepository<ParentNotice, Long> {
    @Query("SELECT p FROM ParentNotice p WHERE p.receiver = :parent")
    List<ParentNotice> findAllByParent(@Param("parent") Parent parent);

    @Query("SELECT p FROM ParentNotice p WHERE p.receiver = :receiver and p.sender = :sender and p.notice.type == 2")
    ParentNotice findByReceiverandSendertoJoinFamily(@Param("receiver")Parent receiver,@Param("sender") Parent sender);

    @Query("SELECT p FROM ParentNotice p WHERE p.receiver = :receiver and p.notice = :notice")
    ParentNotice findByNoticeandReceiver(@Param("notice") Notice notice, @Param("receiver") Parent receiver);

    @Query("SELECT p FROM ParentNotice p WHERE p.notice = :notice")
    List<ParentNotice> findAllReceiverByNotice(@Param("notice") Notice notice);

    @Query("SELECT COUNT(p) FROM ParentNotice p WHERE p.notice = :notice")
    int CountByNotice(@Param("notice") Notice notice);
}
