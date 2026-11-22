package com.aldisued.iot.monitoring.service;

import com.aldisued.iot.monitoring.dto.SensorReadingDto;
import com.aldisued.iot.monitoring.entity.SensorReading;
import com.aldisued.iot.monitoring.repository.SensorReadingRepository;
import com.aldisued.iot.monitoring.repository.SensorRepository;
import com.aldisued.iot.monitoring.service.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class SensorReadingService {

  private final SensorReadingRepository sensorReadingRepository;
  private final SensorRepository sensorRepository;
  private final AlertService alertService;

  public SensorReadingService(
      SensorReadingRepository sensorReadingRepository,
      SensorRepository sensorRepository,
      AlertService alertService) {
    this.sensorReadingRepository = sensorReadingRepository;
    this.sensorRepository = sensorRepository;
    this.alertService = alertService;
  }

  public SensorReading saveSensorReading(SensorReadingDto sensorReadingDto) {
    var sensor = sensorRepository.findById(sensorReadingDto.sensorId())
        .orElseThrow(() -> new EntityNotFoundException(
            "Sensor was not found with id: " + sensorReadingDto.sensorId()));
    var savedSensorReading = sensorReadingRepository.save(new SensorReading(
            sensorReadingDto.value(),
            sensorReadingDto.timestamp(),
            sensor
    ));
    alertService.checkReadingAndSaveAlert(savedSensorReading);
    return savedSensorReading;
  }

}
