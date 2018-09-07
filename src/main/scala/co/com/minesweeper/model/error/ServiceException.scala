package co.com.minesweeper.model.error

/** Used for Technical exceptions in the API layer or unexpected error that go up until Http Api layer */
case class ServiceException(cause: String, message: String)
