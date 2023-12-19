plugins {
    id("java")
}

group = "com.exchange"
version = "1.0-RELEASE"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
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
val flinkKafkaVersion = "1.17.2"
val lombokVersion = "1.18.30"
val jacksonVersion = "2.16.0"
val log4jVersion = "2.22.0"

dependencies {
    // Flink dependencies
    implementation("org.apache.flink:flink-java:$flinkVersion")
    implementation("org.apache.flink:flink-clients:$flinkVersion")
    implementation("org.apache.flink:flink-connector-kafka:$flinkKafkaVersion")

    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")

    compileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")

    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.apache.logging.log4j:log4j-slf4j2-impl:$log4jVersion")
}

tasks.test {
    useJUnitPlatform()
}
