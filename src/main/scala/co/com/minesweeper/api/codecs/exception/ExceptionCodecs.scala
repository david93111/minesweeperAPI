package co.com.minesweeper.api.codecs.exception

import co.com.minesweeper.model.error.ServiceException
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

trait ExceptionCodecs {

  // Exception Response Encoder
  implicit val serviceExceptionEncoder: Encoder[ServiceException] = deriveEncoder[ServiceException]

}
