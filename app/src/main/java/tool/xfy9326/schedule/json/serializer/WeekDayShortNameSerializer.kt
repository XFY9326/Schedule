package tool.xfy9326.schedule.json.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import tool.xfy9326.schedule.beans.WeekDay

class WeekDayShortNameSerializer : KSerializer<WeekDay> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(javaClass.simpleName, PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder) = WeekDay.valueOfShortName(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: WeekDay) = encoder.encodeString(value.shortName)
}