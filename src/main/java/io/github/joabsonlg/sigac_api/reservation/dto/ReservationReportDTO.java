package io.github.joabsonlg.sigac_api.reservation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ReservationReportDTO(
    Long totalReservations,
    Long confirmedReservations,
    Long completedReservations,
    Long cancelledReservations,
    Double totalRevenue,
    List<ReservationDTO> latestReservations,
    Map<String, Double> statusPercentages
) {}
