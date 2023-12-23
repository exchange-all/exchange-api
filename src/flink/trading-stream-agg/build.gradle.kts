plugins {
    id("java")
}

group = "com.exchange"
version = "1.0-RELEASE"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

repositories {
    mavenCentral()
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

val flinkVersion = "1.18.0"
val flinkKafkaVersion = "3.0.2-1.18"
val lombokVersion = "1.18.30"
val jacksonVersion = "2.16.0"
val log4jVersion = "2.22.0"
var cloudEventKafkaVersion = "2.5.0"

dependencies {
    // Flink java API
    implementation("org.apache.flink:flink-java:$flinkVersion")
    // Flink Kafka client
    implementation("org.apache.flink:flink-clients:$flinkVersion")
    // Flink Kafka web UI for local development
    implementation("org.apache.flink:flink-runtime-web:$flinkVersion")
    // Flink Kafka connector
    implementation("org.apache.flink:flink-connector-kafka:$flinkKafkaVersion")

    // Logging
    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")

    // Code generation
    compileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")

    // Jackson
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")

    // CloudEvents
    implementation("io.cloudevents:cloudevents-kafka:$cloudEventKafkaVersion")
    implementation("io.cloudevents:cloudevents-json-jackson:$cloudEventKafkaVersion")

    // Test dependencies
    testImplementation(platform("org.junit:junit-bom:5.10.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.apache.logging.log4j:log4j-slf4j2-impl:$log4jVersion")
}

tasks.test {
    useJUnitPlatform()
}
