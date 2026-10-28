package com.juliadrake.studiomanager.repository;

import com.juliadrake.studiomanager.model.SessionPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SessionPackageRepository extends JpaRepository<SessionPackage, Long> {
    List<SessionPackage> findByActiveTrueOrderByBasePriceAsc();
}
