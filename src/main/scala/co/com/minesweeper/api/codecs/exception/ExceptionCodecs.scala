package co.com.minesweeper.api.codecs.exception

import co.com.minesweeper.model.error.ServiceException
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.deriveEncoder
import io.circe.generic.semiauto._

/**  Mixin for Exception classes that need be marshaled
  *
  *  @author david93111
  *
  *  Designed to share encoders and decoders of custom exception classes used both on api or handlers
  *
  * */
trait ExceptionCodecs {

  // Exception Response Encoder
  implicit val serviceExceptionEncoder: Encoder[ServiceException] = deriveEncoder[ServiceException]
  implicit val serviceExceptionDecoder: Decoder[ServiceException] = deriveDecoder[ServiceException]

}
