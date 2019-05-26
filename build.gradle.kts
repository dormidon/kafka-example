plugins {
    java
    application
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

repositories {
    jcenter()
}

dependencies {
    // Logging
    compile("org.slf4j:slf4j-api:1.7.26")
    compile("ch.qos.logback:logback-classic:1.2.3")

    // Annotations for better code documentation
    compile("com.intellij:annotations:12.0")

    // Guava primitives
    compile("com.google.guava:guava:27.0.1-jre")
    
    // Elastic
    compile("org.elasticsearch.client:elasticsearch-rest-high-level-client:7.0.1")
    testRuntime("org.apache.logging.log4j:log4j-core:2.11.2")
    
    // Redis
    compile("redis.clients:jedis:3.0.1")

    // Postgres
    compile("org.postgresql:postgresql:42.2.5.jre7")

    // Kafka
    compile("org.apache.kafka:kafka-clients:0.10.2.0")

    // Connection pool
    compile("com.zaxxer:HikariCP:3.3.1")
    
    // Spring
    compile("org.springframework.boot:spring-boot-starter-web:2.1.4.RELEASE")
    testCompile("org.springframework.boot:spring-boot-starter-test:2.1.4.RELEASE")
    compile("org.springframework:spring-jdbc:5.1.7.RELEASE")
    
    // Swagger
    compile("io.springfox:springfox-swagger2:2.9.2")
    compile("io.springfox:springfox-swagger-ui:2.9.2")

    // JUnit Jupiter test framework
    testCompile("org.junit.jupiter:junit-jupiter-api:5.4.0")
    testRuntime("org.junit.jupiter:junit-jupiter-engine:5.4.0")
}

val run by tasks.getting(JavaExec::class) {
    standardInput = System.`in`
}

tasks {
    test {
        useJUnitPlatform()
    }
}

application {
    // Define the main class for the application
    mainClassName = "ru.mail.polis.channel.api.Application"
}
