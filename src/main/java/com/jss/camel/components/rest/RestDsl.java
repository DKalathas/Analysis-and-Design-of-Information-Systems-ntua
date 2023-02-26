package com.jss.camel.components.rest;

import com.jss.camel.components.routes.AddRoutesAtRuntimeTest;
import com.jss.camel.dto.ConnectionDto;
import com.jss.camel.dto.RouteDto;
import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.tomcat.util.json.JSONParser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Objects;

import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Component
public class RestDsl extends RouteBuilder {

    private final ConnectionProvider connectionProvider;
    public static ConnectionDto myConnection = new ConnectionDto();


    public RestDsl(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public void configure() throws Exception {

        restConfiguration()
                .component("servlet")
                        .bindingMode(RestBindingMode.auto);

        rest("/api")
                .consumes("application/json").produces("application/json")
                .post("/connection").type(ConnectionDto.class).to("direct:make-connection")
                .delete("/delete").type(RouteDto.class).to("direct:delete-connection")
                .get("/allconnections").to("direct:get-connections");


        from("direct:make-connection")
                .process(this::makeConnection);


        from("direct:delete-connection")
                //.setExchangePattern(ExchangePattern.InOnly)
                .toD("controlbus:route?routeId=${body.routeId}&action=stop&async=true")
                .process(this::deleteConnection);


        from("direct:get-connections")
                .marshal().json(JsonLibrary.Jackson)
                .setHeader("Content-Type", constant("application/json"))
                .setHeader("Accept", constant("application/json"))
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                .removeHeader(Exchange.HTTP_PATH)
                .recipientList(simple("http://localhost:15672/api/channels?bridgeEndpoint=true"))
                .unmarshal().json(JsonLibrary.Jackson);
                //.process(this::getConnections);
                //.to("file:///home/jimk/Documents/NTUA/semester9/pliroforiaka/camel/src/main/other?fileName=conns.txt&fileExist=Append");
                //.log(LoggingLevel.ERROR, "${body[0].name}");

    }

    private void getConnections(Exchange exchange) {
        // FIX THIS
        JSONParser jsonParser = new JSONParser(exchange.getMessage().getBody().toString());
    }


    public void makeConnection(Exchange exchange) throws Exception {
        ConnectionDto dto = exchange.getMessage().getBody(ConnectionDto.class);
        connectionProvider.setCurrentConnection(dto);

        if(Objects.nonNull(dto)) {

            String rabbit_uri = String.format("rabbitmq:%s?hostname=%s&portNumber=%s&username=%s&password=%s&queue=%s&routingKey=%s&autoDelete=false&AutomaticRecoveryEnabled=false&topologyRecoveryEnabled=false",
                                        dto.getRabbitExchange(), dto.getRabbitHost(), dto.getRabbitPort(), dto.getUsername(), dto.getPassword(), dto.getQueue(), dto.getRouting_key());

            String mosquitto_uri = String.format("paho:%s?brokerUrl=tcp://%s:%s",
                                        dto.getTopic(), dto.getMosquittoHost(), dto.getMosquittoPort());

            String routeId = dto.getRouteId();

            CamelContext context = getContext();
            context.addRoutes(new AddRoutesAtRuntimeTest.MyDynamcRouteBuilder(context, rabbit_uri, mosquitto_uri, routeId));

        } else {
            exchange.getMessage().setHeader(HTTP_RESPONSE_CODE, NOT_FOUND.value());
        }
    }

    private void deleteConnection(Exchange exchange) throws Exception {
        RouteDto rout = exchange.getMessage().getBody(RouteDto.class);

        if(Objects.nonNull(rout)) {
            CamelContext context = getContext();
            System.out.println(context.getRoutes());
            context.removeRoute(rout.getRouteId());
            System.out.println(context.getRoutes());

        } else {
            exchange.getMessage().setHeader(HTTP_RESPONSE_CODE, NOT_FOUND.value());
        }
    }
}


