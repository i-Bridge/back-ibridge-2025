package com.ibridge.repository;

import com.ibridge.domain.entity.Child;
import com.ibridge.domain.entity.Parent;
import com.ibridge.domain.entity.Notice;
import com.ibridge.domain.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    @Query("SELECT p FROM Notice p WHERE p.receiver = :parent")
    List<Notice> findAllByParent(@Param("parent") Parent parent);

    @Query("SELECT p FROM Notice p WHERE p.receiver = :receiver and p.sender = :sender and p.type = 2")
    Notice findByReceiverandSendertoJoinFamily(@Param("receiver")Parent receiver,@Param("sender") Parent sender);

    @Query("SELECT p FROM Notice p WHERE p.sender = :sender")
    List<Notice> findAllReceiverBySender(@Param("sender") Parent sender);

    List<Notice> findAllBySender(Parent sender);

    @Query("SELECT p from Notice p where p.sender = :sender and p.type = 2")
    List<Notice> findBySenderAndType(Parent sender);
    List<Notice> findAllByReceiver(Parent receiver);

    Optional<Notice> findBySubjectAndReceiver(Subject subject, Parent receiver);

    @Query("SELECT n FROM Notice n WHERE n.receiver = :parent AND n.child = :child AND FUNCTION('YEAR', n.send_at) = :year AND FUNCTION('MONTH', n.send_at) = :month AND n.isRead = false ORDER BY n.send_at")
    List<Notice> findAllByReceiverAndChild(Parent parent, Long year, Long month, Child child);
}
