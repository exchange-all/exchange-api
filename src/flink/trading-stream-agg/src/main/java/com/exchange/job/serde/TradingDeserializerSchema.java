package com.exchange.job.serde;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.util.Collector;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author uuhnaut69
 */
public class TradingDeserializerSchema implements KafkaRecordDeserializationSchema<JsonNode> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public void deserialize(
            ConsumerRecord<byte[], byte[]> consumerRecord,
            Collector<JsonNode> collector
    ) throws IOException {
        collector.collect(OBJECT_MAPPER.readValue(new String(consumerRecord.value()), JsonNode.class));
    }

    @Override
    public TypeInformation<JsonNode> getProducedType() {
        return TypeInformation.of(JsonNode.class);
    }
}
