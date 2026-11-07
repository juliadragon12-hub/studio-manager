package com.juliadrake.studiomanager.repository;

import com.juliadrake.studiomanager.model.MiniSessionEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface MiniSessionEventRepository extends JpaRepository<MiniSessionEvent, Long> {
    List<MiniSessionEvent> findAllByOrderByDateAscStartTimeAsc();
    List<MiniSessionEvent> findByDateGreaterThanEqualOrderByDateAscStartTimeAsc(LocalDate date);
}
