package user

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

import play.api.Play.current
import play.api.db._
import anorm._
import anorm.SqlParser._

object UserStore {
  def create(email: String, password: String, fullName: Option[String]): Boolean = {
    val salt = generateSalt()
    val passwordHash = hashPassword(password, salt)

    DB.withConnection { implicit connection =>
      SQL(
        """
          |INSERT INTO
          | user
          |SET
          | email = {email},
          | password = {password},
          | salt = {salt},
          | full_name = {fullName}
        """.stripMargin
      ).on(
        'email -> email,
        'password -> anorm.Object(passwordHash),
        'salt -> anorm.Object(salt),
        'fullName -> fullName
      ).executeUpdate() == 1
    }
  }

  def validate(email: String, password: String): Boolean = {
    implicit def rowToByteArray: Column[Array[Byte]] = {
      Column.nonNull[Array[Byte]] { (value, meta) =>
        val MetaDataItem(qualified, nullable, clazz) = meta
        value match {
          case bytes: Array[Byte] => Right(bytes)
          case _ => Left(TypeDoesNotMatch("..."))
        }
      }
    }

    case class Secret(
      passwordHash: Array[Byte],
      salt: Array[Byte]
   )

    val secretParser =
      get[Array[Byte]]("user.password") ~
      get[Array[Byte]]("user.salt") map {
        case pwd ~ salt => Secret(pwd, salt)
      }

    DB.withConnection { implicit connection =>
      SQL(
        """
          |SELECT
          | password,
          | salt
          |FROM
          | user
          |WHERE
          | email = {email}
          |LIMIT 1
        """.stripMargin
      ).on(
        'email -> email
      ).as(
        secretParser singleOpt
      ).exists(
        secret => secret.passwordHash.sameElements(hashPassword(password, secret.salt).deep)
      )
    }
  }

  def requestValidation(email: String): Option[User] = {
    val userParser =
      get[String]("user.email") ~
      get[Option[String]]("user.full_name") map {
        case rowEmail ~ fullName => User(rowEmail, fullName)
      }

    DB.withConnection { implicit connection =>
      SQL(
        """
          |SELECT
          | email,
          | full_name
          |FROM
          | user
          |WHERE
          | email = {email}
          |LIMIT 1
        """.stripMargin
      ).on(
        'email -> email
      ).as(
        userParser singleOpt
      )
    }
  }

  private def hashPassword(password: String, salt: Array[Byte]): Array[Byte] = {
    val spec = new PBEKeySpec(password.toCharArray, salt, 20480, 160)
    val keyFactory: SecretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
    keyFactory.generateSecret(spec).getEncoded
  }

  private def generateSalt(): Array[Byte] = {
    val random = SecureRandom.getInstance("SHA1PRNG")
    val salt = new Array[Byte](20)
    random.nextBytes(salt)
    salt
  }
}
