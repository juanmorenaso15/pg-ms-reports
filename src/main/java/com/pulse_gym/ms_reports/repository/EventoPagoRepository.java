package com.pulse_gym.ms_reports.repository;

import com.pulse_gym.lb_common.entity.reports.EventoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface EventoPagoRepository extends JpaRepository<EventoPago, Long> {

    /**
     * Busca eventos de pago por identificador de socio.
     *
     * @param socioIdentificador ID del socio
     * @return Lista de eventos de pago
     */
    List<EventoPago> findBySocioIdentificador(String socioIdentificador);

    /**
     * Busca eventos de pago por rango de fechas (fechaPago).
     *
     * @param inicio Fecha inicio del rango
     * @param fin    Fecha fin del rango
     * @return Lista de eventos de pago
     */
    List<EventoPago> findByFechaPagoBetween(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Busca eventos de pago por tipo de membresía.
     *
     * @param tipoMembresia Tipo de membresía
     * @return Lista de eventos de pago
     */
    List<EventoPago> findByTipoMembresia(String tipoMembresia);

    /**
     * Busca eventos de pago por método de pago.
     *
     * @param metodoPago Método de pago
     * @return Lista de eventos de pago
     */
    List<EventoPago> findByMetodoPago(String metodoPago);

    /**
     * Calcula el total de ingresos (suma de montos) en un rango de fechas.
     *
     * @param inicio Fecha inicio
     * @param fin    Fecha fin
     * @return Suma total de montos
     */
    @Query("SELECT SUM(e.monto) FROM EventoPago e WHERE e.fechaPago BETWEEN :inicio AND :fin")
    BigDecimal sumMontoByFechaPagoBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    /**
     * Agrupa los ingresos por tipo de membresía en un rango de fechas.
     *
     * @param inicio Fecha inicio
     * @param fin    Fecha fin
     * @return Lista de objetos con tipoMembresia y suma de montos
     */
    @Query("SELECT e.tipoMembresia, SUM(e.monto) " +
           "FROM EventoPago e " +
           "WHERE e.fechaPago BETWEEN :inicio AND :fin " +
           "GROUP BY e.tipoMembresia")
    List<Object[]> sumMontoByTipoMembresiaBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    /**
     * Cuenta la cantidad de pagos en una fecha específica.
     *
     * @param fecha Fecha a consultar (se usa la parte de fecha de fechaPago)
     * @return Número de pagos en esa fecha
     */
    @Query("SELECT COUNT(e) FROM EventoPago e WHERE DATE(e.fechaPago) = :fecha")
    Long countByFechaPago(@Param("fecha") LocalDate fecha);

    /**
     * Obtiene los pagos agrupados por día para un rango de fechas.
     *
     * @param inicio Fecha inicio
     * @param fin    Fecha fin
     * @return Lista de objetos con fecha y conteo
     */
    @Query("SELECT DATE(e.fechaPago) as fecha, COUNT(e) as total " +
           "FROM EventoPago e " +
           "WHERE e.fechaPago BETWEEN :inicio AND :fin " +
           "GROUP BY DATE(e.fechaPago) " +
           "ORDER BY DATE(e.fechaPago)")
    List<Object[]> countByDayBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);
}