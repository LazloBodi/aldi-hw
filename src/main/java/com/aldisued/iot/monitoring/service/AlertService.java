package com.aldisued.iot.monitoring.service;

import com.aldisued.iot.monitoring.dto.AlertDto;
import com.aldisued.iot.monitoring.entity.Alert;
import com.aldisued.iot.monitoring.repository.AlertRepository;
import com.aldisued.iot.monitoring.repository.SensorRepository;
import java.util.UUID;
import com.aldisued.iot.monitoring.service.exception.EntityNotFoundException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class AlertService {
  public static final String ALERTS_TOPIC_NAME = "alerts";

  private final AlertRepository alertRepository;
  private final SensorRepository sensorRepository;
  private final KafkaTemplate<String, AlertDto> kafkaTemplate;

  public AlertService(AlertRepository alertRepository, SensorRepository sensorRepository,
      KafkaTemplate<String, AlertDto> kafkaTemplate) {
    this.alertRepository = alertRepository;
    this.sensorRepository = sensorRepository;
    this.kafkaTemplate = kafkaTemplate;
  }

  public Alert saveAlert(AlertDto alertDto) {
    var sensor = sensorRepository.findById(alertDto.sensorId())
        .orElseThrow(() -> new EntityNotFoundException(
            "Sensor was not found with id: " + alertDto.sensorId()));
    var alert = new Alert(alertDto.message(), alertDto.timestamp(), sensor);
    var savedAlert = alertRepository.save(alert);
    kafkaTemplate.send(ALERTS_TOPIC_NAME, alertDto);
    return savedAlert;
  }

  public AlertDto findLastAlertBySensorId(UUID sensorId) {
    return alertRepository.findFirstBySensor_IdOrderByTimestampDesc(sensorId)
            .map(alert -> new AlertDto(
                    alert.getSensor().getId(),
                    alert.getMessage(),
                    alert.getTimestamp()
            ))
            .orElseThrow(() -> new EntityNotFoundException("Alert not found for sensor id: " + sensorId));
  }
}
