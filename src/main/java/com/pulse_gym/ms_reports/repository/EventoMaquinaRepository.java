package com.pulse_gym.ms_reports.repository;

import com.pulse_gym.lb_common.entity.reports.EventoMaquina;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface EventoMaquinaRepository extends JpaRepository<EventoMaquina, Long> {

    List<EventoMaquina> findByNombreMaquina(String nombreMaquina);

    List<EventoMaquina> findByEstado(String estado);

    List<EventoMaquina> findByFechaReporteBetween(LocalDate inicio, LocalDate fin);
}