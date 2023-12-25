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
val jacksonVersion = "2.16.1"
val log4jVersion = "2.22.0"
var cloudEventKafkaVersion = "2.5.0"
val jdbcPostgresVersion = "42.7.1"
val flinkJdbcVersion = "3.1.1-1.17"

dependencies {
    // Flink dependencies
    implementation("org.apache.flink:flink-java:$flinkVersion")
    implementation("org.apache.flink:flink-streaming-java:$flinkVersion")
    implementation("org.apache.flink:flink-clients:$flinkVersion")
    implementation("org.apache.flink:flink-json:$flinkVersion")
    implementation("org.apache.flink:flink-connector-base:$flinkVersion")
    implementation("org.apache.flink:flink-connector-jdbc:$flinkJdbcVersion")
    implementation("org.apache.flink:flink-connector-kafka:$flinkKafkaVersion")

    // JDBC driver for Postgres
    implementation("org.postgresql:postgresql:$jdbcPostgresVersion")


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
