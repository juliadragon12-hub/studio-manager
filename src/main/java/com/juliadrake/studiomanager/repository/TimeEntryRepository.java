package com.juliadrake.studiomanager.repository;

import com.juliadrake.studiomanager.model.TimeEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TimeEntryRepository extends JpaRepository<TimeEntry, Long> {
    List<TimeEntry> findByProjectIdOrderByStartTimeAsc(Long projectId);
}
