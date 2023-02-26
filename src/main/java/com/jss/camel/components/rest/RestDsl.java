package com.jss.camel.components.rest;

import com.jss.camel.components.routes.AddRoutesAtRuntimeTest;
import com.jss.camel.dto.ConnectionDto;
import com.jss.camel.dto.RouteDto;
import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.stereotype.Component;

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
                .delete("/delete").type(RouteDto.class).to("direct:delete-connection");


        from("direct:make-connection")
                .process(this::makeConnection);


        from("direct:delete-connection")
                .setExchangePattern(ExchangePattern.InOnly)
                .log("Stopping route")
                .to("controlbus:route?routeId=myRoute&action=stop&async=true")
                .log("Signalled to stop route")
                .process(this::deleteConnection);
    }


    public void makeConnection(Exchange exchange) throws Exception {
        ConnectionDto dto = exchange.getMessage().getBody(ConnectionDto.class);
        connectionProvider.setCurrentConnection(dto);

        if(Objects.nonNull(dto)) {
            myConnection = dto;
            System.out.println(myConnection.toString());

            String rabbit_uri = String.format("rabbitmq:%s?hostname=%s&portNumber=%s&username=%s&password=%s&queue=%s&routingKey=%s&autoDelete=false&AutomaticRecoveryEnabled=false&topologyRecoveryEnabled=false",
                                        dto.getRabbitExchange(), dto.getRabbitHost(), dto.getRabbitPort(), dto.getUsername(), dto.getPassword(), dto.getQueue(), dto.getRouting_key());

            String mosquitto_uri = String.format("paho:%s?brokerUrl=tcp://%s:%s",
                                        dto.getTopic(), dto.getMosquittoHost(), dto.getMosquittoPort());

            CamelContext context = getContext();
            context.addRoutes(new AddRoutesAtRuntimeTest.MyDynamcRouteBuilder(context, rabbit_uri, mosquitto_uri));

        } else {
            exchange.getMessage().setHeader(HTTP_RESPONSE_CODE, NOT_FOUND.value());
        }
    }

    private void deleteConnection(Exchange exchange) throws Exception {
        RouteDto rout = exchange.getMessage().getBody(RouteDto.class);

        if(Objects.nonNull(rout)) {
            CamelContext context = getContext();
            System.out.println(rout.getRouteId());
            System.out.println(context.getRoutes());
            context.removeRoute(rout.getRouteId());
            System.out.println(context.getRoutes());
            //System.out.println(context.getRoute("myRoute"));
            //context.stop();

        } else {
            exchange.getMessage().setHeader(HTTP_RESPONSE_CODE, NOT_FOUND.value());
        }
    }
}


