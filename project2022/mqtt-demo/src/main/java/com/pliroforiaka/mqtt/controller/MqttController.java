package com.pliroforiaka.mqtt.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.pliroforiaka.mqtt.MqttGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MqttController {

    @Autowired
    MqttGateway mqttGateway;
    @PostMapping("/sendMessage")
    public ResponseEntity<?> publish (@RequestBody String mqttMessage) {        //ResponseEntity represents the whole HTTP response: status code, headers, and body

        try {
            JsonObject convertObject = new Gson().fromJson(mqttMessage, JsonObject.class);
            mqttGateway.sentToMqtt(convertObject.get("message").toString(), convertObject.get("topic").toString());
            return ResponseEntity.ok("Success");
        }catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.ok("fail");
        }
    }

}
