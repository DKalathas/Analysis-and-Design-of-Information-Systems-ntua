package com.jss.camel.components.rest;

import com.jss.camel.components.routes.AddRoutesAtRuntimeTest;
import com.jss.camel.dto.ConnectionDto;
import com.jss.camel.dto.WeatherDto;
import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.support.DefaultMessage;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.jss.config.CamelConfiguration.RABBIT_URI;
import static com.sun.activation.registries.LogSupport.log;
import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Component
public class RestDsl extends RouteBuilder {

    private final WeatherDataProvider weatherDataProvider;
    private final ConnectionProvider connectionProvider;
    public static String conn_queue;
    public static String conn_routing_key;
    public static ConnectionDto myConnection = new ConnectionDto();


    public RestDsl(WeatherDataProvider weatherDataProvider, ConnectionProvider connectionProvider) {
        this.weatherDataProvider = weatherDataProvider;
        this.connectionProvider = connectionProvider;
    }

    @Override
    public void configure() throws Exception {

        restConfiguration()
                .component("servlet")
                        .bindingMode(RestBindingMode.auto);

        rest("/api")
                .consumes("application/json").produces("application/json")
                .get("/weather/{city}").outType(WeatherDto.class).to("direct:get-weather-data")
                .post("/weather").type(WeatherDto.class).to("direct:save-weather-data")
                .post("/connection").type(ConnectionDto.class).to("direct:make-connection");

        from("direct:get-weather-data")
                .process(this::getWeatherData);

        from("direct:save-weather-data")
                .process(this::saveWeatherDataAndSetToExchange)
                .to(ExchangePattern.InOnly, "direct:write-to-rabbit");

        from("direct:write-to-rabbit")
                .marshal().json(JsonLibrary.Jackson, WeatherDto.class)
                .toF(RABBIT_URI, "weather-event", "weather-event");

        fromF(RABBIT_URI, "weather-event", "weather-event")
                .to("paho:test?brokerUrl=tcp://localhost:1883");

        from("direct:make-connection")
                .process(this::makeConnection)
                //.log(LoggingLevel.ERROR, connectionProvider.getCurrentConnection(1).toString())
                .log(LoggingLevel.ERROR, "[API} Connection details posted: ${body}")
                //.marshal().json(JsonLibrary.Jackson, ConnectionDto.class)
                //.log(LoggingLevel.ERROR, "[API} After marshall: ${body}")
                //.to(ExchangePattern.InOnly, String.format(RABBIT_URI, conn_queue, conn_routing_key));
                .setExchangePattern(ExchangePattern.InOnly);
                //.log(this.makeConnection());
                //.log(LoggingLevel.ERROR, Example.conn_queue)
                //.toD();
                //.log(body());
                //.log(body().toString().replace("simple{", ""));
                //.toD(String.format(RABBIT_URI, myConnection.getQueue(), myConnection.getRouting_key()));
                //.toD("rabbitmq:amq.direct?queue=${body.queue}&routingKey=sender3&autoDelete=false");
                //.toF("rabbitmq:%s?queue=%s&routingKey=%s&autoDelete=false", "${body.rabbitExchange}", "${body.queue}", "${body.routing_key}");
    }

    private void saveWeatherDataAndSetToExchange(Exchange exchange) {
        WeatherDto dto = exchange.getMessage().getBody(WeatherDto.class);
        weatherDataProvider.setCurrentWeather(dto);
    }

    private void getWeatherData(Exchange exchange) {

        String city = exchange.getMessage().getHeader("city", String.class);
        WeatherDto currentWeather = weatherDataProvider.getCurrentWeather(city);

        if(Objects.nonNull(currentWeather)) {
            Message message = new DefaultMessage(exchange.getContext());
            message.setBody(currentWeather);
            exchange.setMessage(message);
        } else {
            exchange.getMessage().setHeader(HTTP_RESPONSE_CODE, NOT_FOUND.value());
        }
    }

    public void makeConnection(Exchange exchange) throws Exception {
        ConnectionDto dto = exchange.getMessage().getBody(ConnectionDto.class);
        connectionProvider.setCurrentConnection(dto);

        if(Objects.nonNull(dto)) {
            myConnection = dto;
            System.out.println(myConnection.toString());

//            Example.conn_queue = dto.getQueue();
//            System.out.println(Example.conn_queue);
//            Example.conn_routing_key = dto.getRouting_key();

            String uri = String.format("rabbitmq:%s?queue=%s&routingKey=%s&autoDelete=false",
                                        dto.getRabbitExchange(), dto.getQueue(), dto.getRouting_key());
//            Message message = new DefaultMessage(exchange.getContext());
//            message.setBody(uri);
//            exchange.setMessage(message);

            CamelContext context = getContext();
            context.addRoutes(new AddRoutesAtRuntimeTest.MyDynamcRouteBuilder(context, uri, "paho:test?brokerUrl=tcp://localhost:1883"));

        } else {
            exchange.getMessage().setHeader(HTTP_RESPONSE_CODE, NOT_FOUND.value());
        }

    }
}


