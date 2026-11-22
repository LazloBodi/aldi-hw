package com.aldisued.iot.monitoring.service;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MeasurementCalculatorService {

  public List<Double> filterByAverageDeviation(List<Double> values, Double deviation) {
    if (deviation == null || !Double.isFinite(deviation) || deviation < 0.0 || deviation > 1.0) {
      throw new IllegalArgumentException("Deviation must be between 0.0 and 1.0");
    }

    if (values.isEmpty()) {
      return List.of();
    }

    double sum = values.stream().mapToDouble(Double::doubleValue).sum();
    double average = sum / values.size();
    double allowedDifference = Math.abs(average) * deviation;
    double minimum = average - allowedDifference;
    double maximum = average + allowedDifference;

    return values.stream()
        .filter(value -> value >= minimum && value <= maximum)
        .toList();
  }

}
