package com.jss.camel.components.routes;

import com.jss.camel.dto.SensorDto;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.support.DefaultMessage;

import java.util.Date;

public class AddRoutesAtRuntimeTest {

    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                // here is an existing route
                //from("direct:start").to("mock:start");
            }
        };
    }

    /**
     * This route builder is a skeleton to add new routes at runtime
     */
    public static final class MyDynamcRouteBuilder extends RouteBuilder {
        private final String from;
        private final String to;

        public MyDynamcRouteBuilder(CamelContext context, String from, String to) {
            super(context);
            this.from = from;
            this.to = to;
        }

        @Override
        public void configure() throws Exception {
            from(from)
                //.log(LoggingLevel.ERROR, "Before Enrichment: ${body}")
                .unmarshal().json(JsonLibrary.Jackson, SensorDto.class)
                .process(this::enrichSensorDto)
                //.log(LoggingLevel.ERROR, "After Enrichment: ${body}")
                .marshal().json(JsonLibrary.Jackson, SensorDto.class)
                .to(to);
        }

        private void enrichSensorDto(Exchange exchange) {
            SensorDto dto = exchange.getMessage().getBody(SensorDto.class);
            dto.setReceivedTime(new Date().toString());

            Message message = new DefaultMessage(exchange);
            message.setBody(dto);
            exchange.setMessage(message);
        }
    }
}
