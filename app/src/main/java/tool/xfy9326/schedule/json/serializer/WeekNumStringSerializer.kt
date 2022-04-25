package tool.xfy9326.schedule.json.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import tool.xfy9326.schedule.utils.deserializeToBooleanArray
import tool.xfy9326.schedule.utils.serializeToString

class WeekNumStringSerializer : KSerializer<BooleanArray> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(javaClass.simpleName, PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder) = decoder.decodeString().deserializeToBooleanArray()

    override fun serialize(encoder: Encoder, value: BooleanArray) = encoder.encodeString(value.serializeToString())
}