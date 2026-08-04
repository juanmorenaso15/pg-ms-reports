package com.pulse_gym.ms_reports.repository;

import com.pulse_gym.lb_common.entity.reports.LogIncidencias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface LogIncidenciasRepository extends JpaRepository<LogIncidencias, Long> {
}