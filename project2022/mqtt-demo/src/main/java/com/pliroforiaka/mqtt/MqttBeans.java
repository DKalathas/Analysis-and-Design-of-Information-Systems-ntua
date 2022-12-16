package com.pliroforiaka.mqtt;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

@Configuration
public class MqttBeans {

    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();      //Create a default MqttClient and a set of options as configured
        MqttConnectOptions options = new MqttConnectOptions();                          //Holds the set of options that control how the client connects to a server

        options.setServerURIs(new String[] {"tcp://localhost:1883"});                   //Return a list of serverURIs the client may connect to
        options.setUserName("admin");                                                   //Sets the username to use for the connection
        String pass = "admin";
        options.setPassword(pass.toCharArray());                                        //Sets the password to use for the connection
        options.setCleanSession(true);                                                  //client and server should remember state across restarts and reconnects

        factory.setConnectionOptions(options);                                          //Set the MqttConnectOptions

        return factory;
    }

    @Bean
    public MessageChannel mqttInputChannel() {                                          //Defines methods for sending messages
        return new DirectChannel();             //A channel that invokes a single subscriber for each sent Message. The invocation will occur in the sender's thread.
    }

    @Bean
    public MessageProducer inbound() {          //Base interface for any component that is capable of sending messages to a MessageChannel

        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter("serverIn",
                mqttClientFactory(), "#");

        adapter.setCompletionTimeout(5000);                                             //Set the completion timeout for operations
        adapter.setConverter(new DefaultPahoMessageConverter());                        //A converter to turn the payload of a Message from serialized form to a typed Object and vice versa
        adapter.setQos(2);                                                              //Set the QoS for each topic; a single value will apply to all topics otherwise the correct number of qos values must be provided
        adapter.setOutputChannel(mqttInputChannel());                                   //Specify the MessageChannel to which produced Messages should be sent
        return adapter;
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")        //The service activator is the endpoint type for connecting any Spring-managed object to an input channel so that it may play the role of a service
    public MessageHandler handler() {
        return new MessageHandler() {

            @Override
            public void handleMessage(Message<?> message) throws MessagingException {
                String topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC).toString();
                if (topic.equals("myTopic")) {
                    System.out.println("This is our topic");
                }
                System.out.println(message.getPayload());
            }
        };
    }

    @Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MessageHandler mqttOutbound() {
        MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler("serverOut", mqttClientFactory());

        messageHandler.setAsync(true);                                                  //Set to true if you don't want to block when sending messages
        messageHandler.setDefaultTopic("#");                                            //Set the topic to which the message will be published if the topicExpression evaluates to `null`

        return messageHandler;
    }

}
