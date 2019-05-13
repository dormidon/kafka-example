package ru.mail.polis.channel.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;

import ru.mail.polis.channel.ChannelService;
import ru.mail.polis.channel.cache.ReadCache;
import ru.mail.polis.channel.search.SearchService;
import ru.mail.polis.channel.service.DualWriteChannelService;
import ru.mail.polis.channel.service.log.KafkaConfiguration;
import ru.mail.polis.channel.service.log.LogWriteChannelService;
import ru.mail.polis.channel.storage.StorageService;

/**
 * Application entry point.
 */
@SpringBootApplication
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class Application {

    public static void main(String[] args) {
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
    ChannelService logWriteChanneldService() {
        return new LogWriteChannelService(
                storageService(),
                searchService(),
                readCache(),
                kafka());
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

    private KafkaConfiguration kafka() {
        return new KafkaConfiguration(
                "localhost:9092",
                "messages",
                "read");
    }

}
