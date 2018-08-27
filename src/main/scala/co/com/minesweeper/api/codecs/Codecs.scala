package co.com.minesweeper.api.codecs

import co.com.minesweeper.model._
import co.com.minesweeper.model.error.GameOperationFailed
import co.com.minesweeper.model.request.{MarkRequest, RevealRequest}
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._

trait Codecs {

  implicit val fieldEncoder: Encoder[Field] = deriveEncoder[Field]
  implicit val fieldDecoder: Decoder[Field] = deriveDecoder[Field]
  implicit val minefieldDecoder: Decoder[Minefield] = deriveDecoder[Minefield]
  implicit val minefieldEncoder: Encoder[Minefield] = deriveEncoder[Minefield]
  implicit val gameDecoder: Decoder[Game] = deriveDecoder[Game]
  implicit val gameEncoder: Encoder[Game] = deriveEncoder[Game]
  implicit val gameNotFoundDecoder: Decoder[GameOperationFailed] = deriveDecoder[GameOperationFailed]
  implicit val gameNotFoundEncoder: Encoder[GameOperationFailed] = deriveEncoder[GameOperationFailed]

  implicit val markRequestDecoder: Decoder[MarkRequest] = deriveDecoder[MarkRequest]
  implicit val revealRequestDecoder: Decoder[RevealRequest] = deriveDecoder[RevealRequest]

}
