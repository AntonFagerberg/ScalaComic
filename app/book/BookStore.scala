package book

import play.api.Play.current
import play.api.db._
import anorm._
import anorm.SqlParser._
import user.User

object BookStore {
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
}
