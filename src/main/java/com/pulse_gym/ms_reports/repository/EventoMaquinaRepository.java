package com.pulse_gym.ms_reports.repository;

import com.pulse_gym.lb_common.entity.reports.EventoMaquina;

import feign.Param;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface EventoMaquinaRepository extends JpaRepository<EventoMaquina, Long> {

    /**
     * Busca eventos de máquina por nombre de máquina.
     * @param nombreMaquina
     * @return
     */
    List<EventoMaquina> findByNombreMaquina(String nombreMaquina);

    /**
     * Busca eventos de máquina por estado.
     * @param estado
     * @return
     */
    List<EventoMaquina> findByEstado(String estado);

    /**
     * Busca eventos de máquina por rango de fechas (fechaReporte).
     * @param inicio
     * @param fin
     * @return
     */
    List<EventoMaquina> findByFechaReporteBetween(LocalDate inicio, LocalDate fin);

    /**
     * Elimina todos los eventos de máquina con fecha de reporte anterior a la fecha
     * especificada.
     * 
     * @param fecha Fecha límite
     * @return Número de registros eliminados
     */
    @Modifying
    @Query("DELETE FROM EventoMaquina e WHERE e.fechaReporte < :fecha")
    long deleteAllByFechaReporteBefore(@Param("fecha") LocalDateTime fecha);
}