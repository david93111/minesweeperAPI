package co.com.minesweeper.api.codecs

import co.com.minesweeper.api.codecs.exception.ExceptionCodecs
import co.com.minesweeper.model._
import co.com.minesweeper.model.error.GameOperationFailed
import co.com.minesweeper.model.request.NewGameRequest.GameSettings
import co.com.minesweeper.model.request.{MarkRequest, NewGameRequest, RevealRequest}
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}

trait Codecs extends ExceptionCodecs{

  implicit val fieldEncoder: Encoder[Field] = deriveEncoder[Field]
  implicit val fieldDecoder: Decoder[Field] = deriveDecoder[Field]
  implicit val minefieldDecoder: Decoder[Minefield] = deriveDecoder[Minefield]
  implicit val minefieldEncoder: Encoder[Minefield] = deriveEncoder[Minefield]
  implicit val gameDecoder: Decoder[Game] = deriveDecoder[Game]
  implicit val gameEncoder: Encoder[Game] = deriveEncoder[Game]
  implicit val gameNotFoundDecoder: Decoder[GameOperationFailed] = deriveDecoder[GameOperationFailed]
  implicit val gameNotFoundEncoder: Encoder[GameOperationFailed] = deriveEncoder[GameOperationFailed]

  // Request Decoders And Encoders
  implicit val markRequestDecoder: Decoder[MarkRequest] = deriveDecoder[MarkRequest]
  implicit val markRequestEncoder: Encoder[MarkRequest] = deriveEncoder[MarkRequest]
  implicit val revealRequestDecoder: Decoder[RevealRequest] = deriveDecoder[RevealRequest]
  implicit val revealRequestEncoder: Encoder[RevealRequest] = deriveEncoder[RevealRequest]
  implicit val newGameRequestDecoder: Decoder[NewGameRequest] = deriveDecoder[NewGameRequest]
  implicit val newGameRequestEncoder: Encoder[NewGameRequest] = deriveEncoder[NewGameRequest]
  implicit val gameSettingsDecoder: Decoder[GameSettings] = deriveDecoder[GameSettings]

}
