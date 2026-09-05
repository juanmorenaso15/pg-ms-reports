package com.pulse_gym.ms_reports.repository;

import com.pulse_gym.lb_common.entity.reports.EventoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventoPagoRepository extends JpaRepository<EventoPago, Long> {

       /**
        * Busca eventos de pago por identificador de socio.
        *
        * @param socioIdentificador ID del socio
        * @return Lista de eventos de pago
        */
       List<EventoPago> findBySocioIdentificador(String socioIdentificador);

       /**
        * Busca eventos de pago en un rango de fechas.
        *
        * @param inicio Fecha inicio
        * @param fin    Fecha fin
        * @return Lista de eventos de pago
        */
       @Query("SELECT e FROM EventoPago e WHERE e.fechaPago BETWEEN :inicio AND :fin AND (e.anulado = false OR e.anulado IS NULL)")
       List<EventoPago> findByFechaPagoBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

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

       @Query("SELECT SUM(e.monto) FROM EventoPago e WHERE e.fechaPago BETWEEN :inicio AND :fin AND (e.anulado = false OR e.anulado IS NULL)")
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
                     "AND (e.anulado = false OR e.anulado IS NULL) " +
                     "GROUP BY e.tipoMembresia")
       List<Object[]> sumMontoByTipoMembresiaBetween(@Param("inicio") LocalDateTime inicio,
                     @Param("fin") LocalDateTime fin);

       @Query("SELECT COUNT(e) FROM EventoPago e WHERE DATE(e.fechaPago) = :fecha AND (e.anulado = false OR e.anulado IS NULL)")
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
                     "AND (e.anulado = false OR e.anulado IS NULL) " +
                     "GROUP BY DATE(e.fechaPago) " +
                     "ORDER BY DATE(e.fechaPago)")
       List<Object[]> countByDayBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

       /**
        * Elimina todos los eventos de pago con fecha de pago anterior a la fecha
        * especificada.
        * 
        * @param fecha Fecha límite
        * @return Número de registros eliminados
        */
       @Modifying
       @Query("DELETE FROM EventoPago e WHERE e.fechaPago < :fecha")
       long deleteAllByFechaPagoBefore(@Param("fecha") LocalDateTime fecha);

       Optional<EventoPago> findBySocioIdentificadorAndFechaPago(String socioIdentificador, LocalDateTime fechaPago);
}