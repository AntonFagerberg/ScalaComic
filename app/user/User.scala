package user

case class User(
  email: String,
  fullName: Option[String]
)