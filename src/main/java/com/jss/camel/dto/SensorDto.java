package com.jss.camel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensorDto implements Serializable {
    static int counter = 1;
    private int id = counter++;

    private String temp;
    private String humidity;
    private String pressure;
    private String receivedTime;
}
