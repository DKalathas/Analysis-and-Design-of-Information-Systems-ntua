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
public class ConnectionDto implements Serializable {
    static int counter = 1;
    private int id = counter++;

    // Route id
    private String routeId;

    // Rabbit config
    private String rabbitHost;
    private String rabbitPort;

    private String rabbitExchange;
    private String queue;
    private String routing_key;

    private String username;
    private String password;

    // Mosquitto config
    private String mosquittoHost;
    private String mosquittoPort;
    private String topic;
}
