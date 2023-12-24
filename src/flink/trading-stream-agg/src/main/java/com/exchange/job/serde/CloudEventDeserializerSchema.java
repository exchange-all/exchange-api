package com.exchange.job.serde;

import io.cloudevents.CloudEvent;
import io.cloudevents.kafka.CloudEventDeserializer;
import org.apache.flink.annotation.PublicEvolving;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.connectors.kafka.KafkaDeserializationSchema;
import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * @author uuhnaut69
 */
@PublicEvolving
public class CloudEventDeserializerSchema implements KafkaDeserializationSchema<Tuple2<CloudEvent, Long>> {

    private transient CloudEventDeserializer cloudEventDeserializer;

    @Override
    public void open(DeserializationSchema.InitializationContext context) throws Exception {
        cloudEventDeserializer = new CloudEventDeserializer();
    }

    @Override
    public boolean isEndOfStream(Tuple2<CloudEvent, Long> nextElement) {
        return false;
    }

    @Override
    public Tuple2<CloudEvent, Long> deserialize(ConsumerRecord<byte[], byte[]> consumerRecord) throws Exception {
        return Tuple2.of(
                cloudEventDeserializer.deserialize(
                        consumerRecord.topic(),
                        consumerRecord.headers(),
                        consumerRecord.value()
                ),
                consumerRecord.timestamp()
        );
    }


    @Override
    public TypeInformation<Tuple2<CloudEvent, Long>> getProducedType() {
        return TypeInformation.of(new TypeHint<>() {
        });
    }
}
