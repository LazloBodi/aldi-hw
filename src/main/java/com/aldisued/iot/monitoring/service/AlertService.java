package com.aldisued.iot.monitoring.service;

import com.aldisued.iot.monitoring.dto.AlertDto;
import com.aldisued.iot.monitoring.entity.Alert;
import com.aldisued.iot.monitoring.entity.SensorReading;
import com.aldisued.iot.monitoring.entity.SensorType;
import com.aldisued.iot.monitoring.repository.AlertRepository;
import com.aldisued.iot.monitoring.repository.SensorRepository;
import com.aldisued.iot.monitoring.service.exception.EntityNotFoundException;

import java.util.Optional;
import java.util.UUID;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class AlertService {
  public static final String ALERTS_TOPIC_NAME = "alerts";

  public static final double MAX_TEMPERATURE_THRESHOLD = 40.0;
  public static final double MIN_TEMPERATURE_THRESHOLD = -20.0;
  public static final double MAX_HUMIDITY_THRESHOLD = 90.0;
  public static final double MIN_PRESSURE_THRESHOLD = 950.0;
  public static final double MAX_PRESSURE_THRESHOLD = 1050.0;

  private final AlertRepository alertRepository;
  private final SensorRepository sensorRepository;
  private final KafkaTemplate<String, AlertDto> kafkaTemplate;

  public AlertService(AlertRepository alertRepository, SensorRepository sensorRepository,
      KafkaTemplate<String, AlertDto> kafkaTemplate) {
    this.alertRepository = alertRepository;
    this.sensorRepository = sensorRepository;
    this.kafkaTemplate = kafkaTemplate;
  }

  public Alert saveAndPublishAlert(AlertDto alertDto) {
    var sensor = sensorRepository.findById(alertDto.sensorId())
        .orElseThrow(() -> new EntityNotFoundException(
            "Sensor was not found with id: " + alertDto.sensorId()));
    var alert = new Alert(alertDto.message(), alertDto.timestamp(), sensor);
    var savedAlert = alertRepository.save(alert);
    kafkaTemplate.send(ALERTS_TOPIC_NAME, alertDto);
    return savedAlert;
  }

  public void checkReadingAndSaveAlert(SensorReading reading) {
    shouldAlertWithMessage(reading.getSensor().getType(), reading.getValue())
            .ifPresent(message ->
                    alertRepository.save(new Alert(message, reading.getTimestamp(), reading.getSensor())));
  }

  private static Optional<String> shouldAlertWithMessage(SensorType sensorType, Double value) {
    return switch (sensorType) {
      case TEMPERATURE -> value > MAX_TEMPERATURE_THRESHOLD || value < MIN_TEMPERATURE_THRESHOLD
          ? Optional.of(String.format("Temperature out of range: %s", value))
          : Optional.empty();
      case HUMIDITY -> value > MAX_HUMIDITY_THRESHOLD
          ? Optional.of(String.format("Humidity too high: %s", value))
          : Optional.empty();
      case ATMOSPHERIC_PRESSURE -> value < MIN_PRESSURE_THRESHOLD || value > MAX_PRESSURE_THRESHOLD
          ? Optional.of(String.format("Pressure out of range: %s", value))
          : Optional.empty();
    };
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
