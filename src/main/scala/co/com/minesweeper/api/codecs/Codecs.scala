package co.com.minesweeper.api.codecs

import co.com.minesweeper.model._
import io.circe.{ Decoder, Encoder }
import io.circe.generic.semiauto._

trait Codecs {

  implicit val fieldEncoder: Encoder[Field] = deriveEncoder[Field]
  implicit val fieldDecoder: Decoder[Field] = deriveDecoder[Field]

}
