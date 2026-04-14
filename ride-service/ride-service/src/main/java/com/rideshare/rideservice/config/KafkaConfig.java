package com.rideshare.rideservice.config;


import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    //topic ride service publish ride request
    //matching service subcriber to this topic


    @Bean
    public NewTopic rideRequestedTopic(){
        return TopicBuilder.name("ride.requested")
                .partitions(3).build();
    }

    //topic where matching serivces publish match result
    //ride serivce subscribers to this topic

    public NewTopic rideMatchTopic(){
        return TopicBuilder.name("ride.match").partitions(3).replicas(1).build();


    }

}
