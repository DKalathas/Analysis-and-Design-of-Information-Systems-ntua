package com.jss.camel.components.rest;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jss.camel.components.routes.AddRoutesAtRuntimeTest;
import com.jss.camel.dto.ChannelDto;
import com.jss.camel.dto.Conn.Root;
import com.jss.camel.dto.ConnectionDto;
import com.jss.camel.dto.RouteDto;
import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.support.DefaultMessage;
import org.apache.camel.util.json.JsonArray;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
                .get("/allchannels").to("direct:get-channels")
                .get("/allchannels/details").to("direct:get-channels-details")
                .post("/channelrate").type(ChannelDto.class).to("direct:get-channel-rates");


        from("direct:make-connection")
                .process(this::makeConnection);


        from("direct:delete-connection")
                .choice()
                    .when(simple("${body.check} == 'channelName'"))
                        .log("We are here")
                        .process(exchange -> {
                            RouteDto dto = exchange.getMessage().getBody(RouteDto.class);
                            String chann = URLEncoder.encode(dto.getChannelName(), StandardCharsets.UTF_8);
                            chann = chann.replace("+","%20");

                            // set exchange message to chanel names
                            Message message = new DefaultMessage(exchange.getContext());
                            message.setBody(chann);
                            exchange.setMessage(message);
                        })
                        .setHeader("Content-Type", constant("application/json"))
                        .setHeader("Accept", constant("application/json"))
                        .setHeader(Exchange.HTTP_METHOD, constant("DELETE"))
                        .setHeader("Authorization", constant("Basic Z3Vlc3Q6Z3Vlc3Q="))
                        .removeHeader(Exchange.HTTP_PATH)
                        .recipientList(simple("http://localhost:15672/api/connections/${body}?bridgeEndpoint=true"))
                        .end()

                    .otherwise()
                        .toD("controlbus:route?routeId=${body.routeId}&action=stop&async=true")
                        .process(this::deleteConnection);


        from("direct:get-channels")
                .marshal().json(JsonLibrary.Jackson)
                .setHeader("Content-Type", constant("application/json"))
                .setHeader("Accept", constant("application/json"))
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                .removeHeader(Exchange.HTTP_PATH)
                .recipientList(simple("http://localhost:15672/api/channels?bridgeEndpoint=true"))
                //.unmarshal().json(JsonLibrary.Jackson)
                .to("file:///home/jimk/Documents/NTUA/semester9/pliroforiaka/camel/src/main/other/?fileName=connsList.json&fileExist=Override")
                .process(this::getChannels);
                //.to("file:///home/jimk/Documents/NTUA/semester9/pliroforiaka/camel/src/main/other?fileName=conns.txt&fileExist=Append");
                //.log(LoggingLevel.ERROR, "${body[0].name}");


        from("direct:get-channels-details")
                .marshal().json(JsonLibrary.Jackson)
                .setHeader("Content-Type", constant("application/json"))
                .setHeader("Accept", constant("application/json"))
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                .removeHeader(Exchange.HTTP_PATH)
                .recipientList(simple("http://localhost:15672/api/channels?bridgeEndpoint=true"))
                .unmarshal().json(JsonLibrary.Jackson);


        from("direct:get-channel-rates")
                .process(exchange -> {
                    ChannelDto dto = exchange.getMessage().getBody(ChannelDto.class);
                    String chann = URLEncoder.encode(dto.getName(), StandardCharsets.UTF_8);
                    chann = chann.replace("+","%20");
                    //System.out.println(chann)
                    //dto.setName(chann);

                    // set exchange message to chanel names
                    Message message = new DefaultMessage(exchange.getContext());
                    message.setBody(chann);
                    exchange.setMessage(message);
                })
                .log(LoggingLevel.ERROR, "http://localhost:15672/api/channels/${body}?bridgeEndpoint=true")
                //.to("http://localhost:15672/api/channels/127.0.0.1%3A35180%20-%3E%20127.0.0.1%3A5672%20%281%29?bridgeEndpoint=true");
                //.marshal().json(JsonLibrary.Jackson);
                .setHeader("Content-Type", constant("application/json"))
                .setHeader("Accept", constant("application/json"))
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                .setHeader("Authorization", constant("Basic Z3Vlc3Q6Z3Vlc3Q="))
                .removeHeader(Exchange.HTTP_PATH)
                //.toD("http://localhost:15672/api/channels/${body}?bridgeEndpoint=true");
                .recipientList(simple("http://localhost:15672/api/channels/${body}?bridgeEndpoint=true"))
                //.unmarshal().json(JsonLibrary.Jackson)
                .to("file:///home/jimk/Documents/NTUA/semester9/pliroforiaka/camel/src/main/other/?fileName=conns.json&fileExist=Override")
                .process(this::getRates);
    }

    private void getRates(Exchange exchange) throws IOException {
        String channelrates = null;

        // creates ObjectMapper object
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        Root root = mapper.readValue(new File("src/main/other/conns.json"), Root.class);

        if(!Objects.equals(root.message_stats, null)) {
            if (!Objects.equals(root.message_stats.publish, null)) {
                channelrates = "Connection: " + root.name + " -- published messages: " + root.message_stats.publish + " -- with rate: " + root.message_stats.publish_details.rate + " messages per second";
            } else {
                channelrates = "Connection: " + root.name + " -- published messages: " + root.message_stats.deliver_get + " -- with rate: " + root.message_stats.deliver_get_details.rate + " messages per second";
            }
        } else {
            channelrates = "Connection: " + root.name + " -- has no messages";
        }

        System.out.println(channelrates);

        // set exchange message to chanel names
        Message message = new DefaultMessage(exchange.getContext());
        message.setBody(channelrates);
        exchange.setMessage(message);
    }

    private void getChannels(Exchange exchange) throws IOException {
        ArrayList<String> allchannels = new ArrayList<>();

        // creates ObjectMapper object
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

//        Root root = mapper.readValue(new File("src/main/other/conns.json"), Root.class);
//        System.out.println("root object name -> "+root.name);

        //List<Root> rootList = mapper.readValue((JsonParser) exchange.getMessage().getBody(), new TypeReference<List<Root>>() {});
        List<Root> rootList = mapper.readValue(new File("src/main/other/connsList.json"), new TypeReference<List<Root>>() {});
        for (Root root : rootList) {
            allchannels.add(root.name);
        }

        // set exchange message to chanel names
        Message message = new DefaultMessage(exchange.getContext());
        message.setBody(allchannels);
        exchange.setMessage(message);
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


