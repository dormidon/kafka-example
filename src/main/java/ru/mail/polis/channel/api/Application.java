package ru.mail.polis.channel.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;

import ru.mail.polis.channel.ChannelService;
import ru.mail.polis.channel.cache.ReadCache;
import ru.mail.polis.channel.search.SearchService;
import ru.mail.polis.channel.service.classic.DualWriteChannelService;
import ru.mail.polis.channel.service.log.KafkaConfig;
import ru.mail.polis.channel.service.log.LogWriteChannelService;
import ru.mail.polis.channel.storage.StorageService;

/**
 * Application entry point.
 */
@SpringBootApplication
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class Application {

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }

    //@Bean(destroyMethod = "close")
    ChannelService dualWritesChannelService() {
        return new DualWriteChannelService(
                storageService(),
                searchService(),
                readCache());
    }

    @Bean(destroyMethod = "close")
    ChannelService logWriteChannelService() {
        return new LogWriteChannelService(
                storageService(),
                searchService(),
                readCache(),
                kafkaConfig());
    }

    private SearchService searchService() {
        return new SearchService(
                "awesome_channel",
                "localhost", 9200);
    }

    private ReadCache readCache() {
        return new ReadCache("localhost", 6379);
    }

    private StorageService storageService() {
        return new StorageService("localhost", 5432,
                "channel", "password",
                "channel");
    }

    private KafkaConfig kafkaConfig() {
        return new KafkaConfig(
                "localhost:9092",
                "messages",
                "read");
    }
}
