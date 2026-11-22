package com.aldisued.iot.monitoring.repository;

import com.aldisued.iot.monitoring.entity.SensorReading;
import com.aldisued.iot.monitoring.entity.SensorType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SensorReadingRepository extends JpaRepository<SensorReading, String> {

  @Query("""
      SELECT reading.value
      FROM SensorReading reading
      WHERE reading.sensor.type = :sensorType
        AND reading.timestamp BETWEEN :from AND :to
      ORDER BY reading.timestamp
      """)
  List<Double> findValuesBySensorTypeAndTimestampBetweenOrderByTimestamp(
      @Param("sensorType") SensorType sensorType,
      @Param("from") LocalDateTime from,
      @Param("to") LocalDateTime to
  );

  @Query("""
      SELECT AVG(reading.value)
      FROM SensorReading reading
      WHERE reading.sensor.type = :sensorType
        AND reading.timestamp BETWEEN :from AND :to
      """)
  Optional<Double> findAverageValueBySensorTypeAndTimestampBetween(
      @Param("sensorType") SensorType sensorType,
      @Param("from") LocalDateTime from,
      @Param("to") LocalDateTime to
  );
}
