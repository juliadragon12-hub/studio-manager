package com.juliadrake.studiomanager.repository;

import com.juliadrake.studiomanager.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {
}
