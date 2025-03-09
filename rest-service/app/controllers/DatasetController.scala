package controllers

import org.apache.pekko.actor.typed.{ActorRef, ActorSystem}
import org.apache.pekko.util.Timeout
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import service.DatasetService
import actors.DatasetActor
import actors.DatasetActor._
import org.apache.pekko.actor.typed.Scheduler
import org.apache.pekko.actor.typed.scaladsl.AskPattern.Askable
import play.api.libs.json.{JsObject, JsValue, Json}
import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

class DatasetController @Inject() (cc: ControllerComponents, datasetService: DatasetService, actorSystem: ActorSystem[Void])
                                  (implicit ec: ExecutionContext) extends AbstractController(cc) {
  private implicit val timeout: Timeout = 5.seconds
  private implicit val scheduler: Scheduler = actorSystem.scheduler

  private val datasetActor: ActorRef[DatasetActor.Command] = {
    actorSystem.systemActorOf(DatasetActor(datasetService), "dataset-actor")
  }

  def getAllDatasets: Action[AnyContent] =   Action.async {
    datasetActor.ask(replyTo => GetAllDatasets(replyTo)).map{
      case ResponseBody(responseBody) =>
        Ok(Json.toJson(responseBody))
    }.recover{
      case ex =>
        InternalServerError("Error" + ex.printStackTrace())
    }
  }

  def getDatasetById(id: String): Action[AnyContent] = Action.async{
    datasetActor.ask(replyTo => GetDatasetById(id, replyTo)).map {
      case ResponseBody(responseBody) =>
        Ok(Json.toJson(responseBody))
    }.recover{
      case ex =>
        InternalServerError("Error" + ex.printStackTrace())
    }
  }

  def createDataset: Action[JsValue] = Action.async(parse.json) { request =>
    val jsonDataset: JsObject = request.body.as[JsObject]
    datasetActor.ask(replyTo => CreateDataset(jsonDataset, replyTo)).map {
      case ResponseBody(responseBody) =>
        Ok(Json.toJson(responseBody))
    }.recover {
      case ex =>
        InternalServerError(Json.obj("error" -> s"Error: ${ex.getMessage}"))
    }
  }

  def updateDataset(id: String): Action[JsValue] = Action.async(parse.json) { request =>
    val jsonDataset: JsObject = request.body.as[JsObject]
    datasetActor.ask(replyTo => UpdateDataset(id, jsonDataset, replyTo)).map{
      case ResponseBody(responseBody) =>
        Ok(Json.toJson(responseBody))
    }.recover {
      case ex =>
        InternalServerError(Json.obj("error" -> s"Error: ${ex.getMessage}"))
    }
  }

  def deleteDatasetById(id: String): Action[AnyContent] = Action.async{
    datasetActor.ask(replyTo => DeleteDatasetById(id, replyTo)).map {
      case ResponseBody(responseBody) =>
        Ok(Json.toJson(responseBody))
    }.recover{
      case ex =>
        InternalServerError("Error" + ex.printStackTrace())
    }
  }
}
