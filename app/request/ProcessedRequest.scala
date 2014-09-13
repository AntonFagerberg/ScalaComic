package request

import play.api.mvc._
import user.User

case class ProcessedRequest(
  user: User,
  request: Request[AnyContent]
) extends WrappedRequest(request)