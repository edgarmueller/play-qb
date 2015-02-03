package controllers

import play.api.mvc.{Action, Controller}

/**
 * Created by Edgar on 03.02.2015.
 */
object Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def serverIndex = Action {
    Ok(views.html.serverIndex())
  }
}
