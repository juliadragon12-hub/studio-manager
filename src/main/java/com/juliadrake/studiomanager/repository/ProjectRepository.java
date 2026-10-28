package com.juliadrake.studiomanager.repository;

import com.juliadrake.studiomanager.model.Project;
import com.juliadrake.studiomanager.model.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByStatusOrderBySessionDateTimeAsc(ProjectStatus status);
    List<Project> findAllByOrderBySessionDateTimeAsc();
}
