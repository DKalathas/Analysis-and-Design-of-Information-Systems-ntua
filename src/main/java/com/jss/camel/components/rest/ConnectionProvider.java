package com.jss.camel.components.rest;

import com.jss.camel.dto.ConnectionDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ConnectionProvider {

    private static List<ConnectionDto> connectionData = new ArrayList<ConnectionDto>();
    private static ConnectionDto myConnection = new ConnectionDto();

    public ConnectionProvider() {
        ConnectionDto dto = new ConnectionDto();
    }

    public ConnectionDto getCurrentConnection(int id) {
        return connectionData.get(id);
    }

    public void setCurrentConnection(ConnectionDto dto) {
        myConnection = dto;
    }

    public ConnectionDto getCurrentConnection(ConnectionDto myConnection) {
        return myConnection;
    }
}
