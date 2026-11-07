package com.juliadrake.studiomanager.repository;

import com.juliadrake.studiomanager.model.Project;
import com.juliadrake.studiomanager.model.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByStatusOrderBySessionDateTimeAsc(ProjectStatus status);
    List<Project> findAllByOrderBySessionDateTimeAsc();

    /** Bookings holding a given mini-session slot (cancelled bookings don't hold a seat). */
    long countByMiniEventIdAndSessionDateTimeAndStatusNot(Long miniEventId, LocalDateTime sessionDateTime,
                                                          ProjectStatus status);
}
