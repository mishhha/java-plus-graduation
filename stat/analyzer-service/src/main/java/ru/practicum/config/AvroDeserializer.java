package ru.practicum.config;

import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

public class AvroDeserializer<T extends SpecificRecordBase> implements Deserializer<T> {

    private final Class<T> targetType;

    public AvroDeserializer(Class<T> targetType) {
        this.targetType = targetType;
    }

    public AvroDeserializer() {
        this.targetType = null;
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // Настройки не требуются
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }
        if (targetType == null) {
            throw new SerializationException("Target type is not configured");
        }
        try {
            T instance = targetType.getDeclaredConstructor().newInstance();

            SpecificDatumReader<T> reader = new SpecificDatumReader<>(instance.getSchema());

            BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(data, null);

            return reader.read(null, decoder);
        } catch (IOException | ReflectiveOperationException e) {
            throw new SerializationException("Ошибка десериализации Avro сообщения", e);
        }
    }

    @Override
    public void close() {
        // Ресурсы не требуют освобождения
    }
}