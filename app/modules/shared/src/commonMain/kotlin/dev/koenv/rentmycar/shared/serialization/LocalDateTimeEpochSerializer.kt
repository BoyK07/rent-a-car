package dev.koenv.rentmycar.shared.serialization

import kotlinx.datetime.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Serializes LocalDateTime to/from UNIX epoch milliseconds in UTC.
 */
object LocalDateTimeEpochSerializer : KSerializer<LocalDateTime> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LocalDateTimeEpochMillis", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        val instant = value.toInstant(TimeZone.UTC)
        encoder.encodeLong(instant.toEpochMilliseconds())
    }

    override fun deserialize(decoder: Decoder): LocalDateTime {
        val millis = decoder.decodeLong()
        return Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC)
    }
}
