package tool.xfy9326.schedule.json.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import tool.xfy9326.schedule.content.utils.CourseAdapterUtils
import java.util.Date

class
DateFormatStringSerializer : KSerializer<Date> {
    private val termDateFormat = CourseAdapterUtils.newDateFormat()

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(javaClass.simpleName, PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Date = termDateFormat.parse(decoder.decodeString())!!

    override fun serialize(encoder: Encoder, value: Date) = encoder.encodeString(termDateFormat.format(value))
}