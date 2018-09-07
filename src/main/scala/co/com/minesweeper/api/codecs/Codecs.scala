package co.com.minesweeper.api.codecs

import co.com.minesweeper.api.codecs.exception.ExceptionCodecs
import co.com.minesweeper.model._
import co.com.minesweeper.model.error.GameOperationFailed
import co.com.minesweeper.model.messages.{GameHistory, GameState}
import co.com.minesweeper.model.request.NewGameRequest.GameSettings
import co.com.minesweeper.model.request.{MarkRequest, NewGameRequest, RevealRequest}
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}

/**  Mixin with all the codecs of messages and model classes for marshalling
  *
  *  @author david93111
  *
  *  Designed to avoid the need of companion object on each class to be encoded or decoded with circe
  *
  * */
trait Codecs extends ExceptionCodecs{

  implicit val fieldEncoder: Encoder[Field] = deriveEncoder[Field]
  implicit val fieldDecoder: Decoder[Field] = deriveDecoder[Field]
  implicit val minefieldDecoder: Decoder[Minefield] = deriveDecoder[Minefield]
  implicit val minefieldEncoder: Encoder[Minefield] = deriveEncoder[Minefield]
  implicit val gameDecoder: Decoder[GameState] = deriveDecoder[GameState]
  implicit val gameEncoder: Encoder[GameState] = deriveEncoder[GameState]
  implicit val gameNotFoundDecoder: Decoder[GameOperationFailed] = deriveDecoder[GameOperationFailed]
  implicit val gameNotFoundEncoder: Encoder[GameOperationFailed] = deriveEncoder[GameOperationFailed]
  implicit val gameHistoryDecoder: Decoder[GameHistory] = deriveDecoder[GameHistory]
  implicit val gameHistoryEncoder: Encoder[GameHistory] = deriveEncoder[GameHistory]

  // Request Decoders And Encoders
  implicit val markRequestDecoder: Decoder[MarkRequest] = deriveDecoder[MarkRequest]
  implicit val markRequestEncoder: Encoder[MarkRequest] = deriveEncoder[MarkRequest]
  implicit val revealRequestDecoder: Decoder[RevealRequest] = deriveDecoder[RevealRequest]
  implicit val revealRequestEncoder: Encoder[RevealRequest] = deriveEncoder[RevealRequest]
  implicit val newGameRequestDecoder: Decoder[NewGameRequest] = deriveDecoder[NewGameRequest]
  implicit val newGameRequestEncoder: Encoder[NewGameRequest] = deriveEncoder[NewGameRequest]
  implicit val gameSettingsDecoder: Decoder[GameSettings] = deriveDecoder[GameSettings]

}
