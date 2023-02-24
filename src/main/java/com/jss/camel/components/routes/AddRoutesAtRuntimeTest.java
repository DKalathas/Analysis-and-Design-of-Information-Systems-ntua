package com.jss.camel.components.routes;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;

public class AddRoutesAtRuntimeTest {

    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                // here is an existing route
                from("direct:start").to("mock:start");
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
            from(from).to(to);
        }
    }
}
