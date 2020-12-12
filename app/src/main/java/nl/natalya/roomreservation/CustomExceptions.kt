package nl.natalya.roomreservation

class BackEndException(message: String): Exception(message)
class BackEndExceptionWithCause(message: String, cause: Exception): Exception(message, cause)
class AuthenticationException (message: String): Exception(message)
