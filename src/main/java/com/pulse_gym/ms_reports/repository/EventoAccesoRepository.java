package com.pulse_gym.ms_reports.repository;

import com.pulse_gym.lb_common.entity.reports.EventoAcceso;
import com.pulse_gym.lb_common.enums.EnumTipoAcceso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface EventoAccesoRepository extends JpaRepository<EventoAcceso, Long> {

    /**
     * Busca eventos de acceso por identificador de socio.
     *
     * @param socioIdentificacion ID del socio
     * @return Lista de eventos de acceso
     */
    List<EventoAcceso> findBySocioIdentificacion(Long socioIdentificacion);

    /**
     * Busca eventos de acceso por rango de fechas (fechaRegistro).
     *
     * @param inicio Fecha inicio del rango
     * @param fin    Fecha fin del rango
     * @return Lista de eventos de acceso
     */
    List<EventoAcceso> findByFechaRegistroBetween(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Busca eventos de acceso por tipo de acceso.
     *
     * @param tipoAcceso Tipo de acceso (WEB, APP, BIOMETRICO)
     * @return Lista de eventos de acceso
     */
    List<EventoAcceso> findByTipoAcceso(EnumTipoAcceso tipoAcceso);

    /**
     * Cuenta la cantidad de eventos de acceso para una fecha específica.
     *
     * @param fecha Fecha a consultar (se usa la parte de fecha de fechaRegistro)
     * @return Número de eventos en esa fecha
     */
    @Query("SELECT COUNT(e) FROM EventoAcceso e WHERE DATE(e.fechaRegistro) = :fecha")
    Long countByFechaRegistro(@Param("fecha") LocalDate fecha);

    /**
     * Cuenta la cantidad de eventos de acceso en un rango de fechas.
     *
     * @param inicio Fecha inicio
     * @param fin    Fecha fin
     * @return Número de eventos en el rango
     */
    @Query("SELECT COUNT(e) FROM EventoAcceso e WHERE e.fechaRegistro BETWEEN :inicio AND :fin")
    Long countByFechaRegistroBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    /**
     * Obtiene los eventos de acceso agrupados por día para un rango de fechas.
     * (Útil para tendencias semanales/mensuales)
     *
     * @param inicio Fecha inicio
     * @param fin    Fecha fin
     * @return Lista de objetos con fecha y conteo (se mapea con proyección)
     */
    @Query("SELECT DATE(e.fechaRegistro) as fecha, COUNT(e) as total " +
            "FROM EventoAcceso e " +
            "WHERE e.fechaRegistro BETWEEN :inicio AND :fin " +
            "GROUP BY DATE(e.fechaRegistro) " +
            "ORDER BY DATE(e.fechaRegistro)")
    List<Object[]> countByDayBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    /**
     * Elimina todos los eventos de acceso con fecha de registro anterior a la fecha
     * especificada.
     * 
     * @param fecha Fecha límite
     * @return Número de registros eliminados
     */
    @Modifying
    @Query("DELETE FROM EventoAcceso e WHERE e.fechaRegistro < :fecha")
    long deleteAllByFechaRegistroBefore(@Param("fecha") LocalDateTime fecha);

    /**
     * Obtiene todos los eventos de acceso de una fecha específica (sin distinción
     * de hora).
     *
     * @param inicio Fecha inicio (00:00)
     * @param fin    Fecha fin (23:59)
     * @return Lista de eventos de acceso
     */
    @Query("SELECT e FROM EventoAcceso e WHERE e.fechaRegistro BETWEEN :inicio AND :fin")
    List<EventoAcceso> findByFechaRegistroBetweenOrderByFechaRegistroAsc(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);
}