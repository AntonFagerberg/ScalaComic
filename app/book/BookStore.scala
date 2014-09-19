package book

import play.api.Play.current
import play.api.db._
import anorm._
import anorm.SqlParser._
import user.{UserStore, User}

object BookStore {
  lazy val bookParser =
    get[Long]("book.id") ~
    get[String]("book.title") ~
    get[String]("book.filename") ~
    UserStore.userParser map {
      case id ~ title ~ filename ~ user => Book(id, title, filename, user)
    }

  def all(email: String): Seq[Book] = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          |SELECT
          | book.*,
          | user.*
          |FROM
          | book
          |INNER JOIN
          | user
          |ON
          | book.email = user.email
          |WHERE
          | book.email = {email}
          |ORDER BY
          | book.title ASC
        """.stripMargin
      ).on(
        'email -> email
      ).as(
        bookParser *
      )
    }
  }

  def find(bookId: Long): Option[Book] = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          |SELECT
          | book.*,
          | user.*
          |FROM
          | book
          |INNER JOIN
          | user
          |ON
          | book.email = user.email
          |WHERE
          | book.id = {bookId}
          |LIMIT 1
        """.stripMargin
      ).on(
        'bookId -> bookId
      ).as(
        bookParser singleOpt
      )
    }
  }

  def create(title: String, filename: String, user: User): Option[Long] = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          |INSERT INTO
          | book
          |SET
          | title = {title},
          | filename = {filename},
          | email = {email}
        """.stripMargin
      ).on(
        'title -> title,
        'filename -> filename,
        'email -> user.email
      ).executeInsert()
    }
  }

  def isOwner(bookId: Long)(email: String): Option[User] = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          |SELECT
          | user.email,
          | user.full_name
          |FROM
          | book
          |INNER JOIN
          | user
          |ON
          | book.email = user.email
          |WHERE
          | book.id = {bookId}
          |AND
          | book.email = {email}
          |LIMIT 1
        """.stripMargin
      ).on(
        'bookId -> bookId,
        'email -> email
      ).as(
        user.UserStore.userParser singleOpt
      )
    }
  }
}
