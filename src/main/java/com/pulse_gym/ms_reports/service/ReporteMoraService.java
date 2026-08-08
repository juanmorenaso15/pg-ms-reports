package com.pulse_gym.ms_reports.service;

import com.pulse_gym.lb_common.client.SocioMembresiaFeignClient;
import com.pulse_gym.lb_common.dto.ReporteMoraResponseDTO;
import com.pulse_gym.lb_common.dto.SocioMoraDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReporteMoraService {

    private final SocioMembresiaFeignClient socioMembresiaFeignClient;

    public ReporteMoraResponseDTO obtenerSociosEnMora(LocalDate fechaInicio, LocalDate fechaFin) {
        List<SocioMoraDTO> socios = socioMembresiaFeignClient.obtenerSociosEnMora(fechaInicio, fechaFin);
        ReporteMoraResponseDTO response = new ReporteMoraResponseDTO();
        response.setSociosEnMora(socios);
        response.setTotalMorosos(socios.size());
        if (socios.isEmpty()) {
            response.setMensaje("No hay socios en mora");
        }
        return response;
    }
}