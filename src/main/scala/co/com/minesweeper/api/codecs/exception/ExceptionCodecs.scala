package co.com.minesweeper.api.codecs.exception

import co.com.minesweeper.model.error.ServiceException
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.deriveEncoder
import io.circe.generic.semiauto._

trait ExceptionCodecs {

  // Exception Response Encoder
  implicit val serviceExceptionEncoder: Encoder[ServiceException] = deriveEncoder[ServiceException]
  implicit val serviceExceptionDecoder: Decoder[ServiceException] = deriveDecoder[ServiceException]

}
