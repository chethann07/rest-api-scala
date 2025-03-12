package controllers

import org.apache.pekko.actor.typed.{ActorRef, ActorSystem}
import org.apache.pekko.util.Timeout
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents, Result}
import service.DatasetService
import actors.DatasetActor
import actors.DatasetActor._
import org.apache.pekko.actor.typed.Scheduler
import org.apache.pekko.actor.typed.scaladsl.AskPattern.Askable
import play.api.libs.json.{JsObject, JsValue, Json}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.DurationInt

class DatasetController @Inject() (cc: ControllerComponents, datasetService: DatasetService, actorSystem: ActorSystem[Void])
                                  (implicit ec: ExecutionContext) extends AbstractController(cc) {
  private implicit val timeout: Timeout = 5.seconds
  private implicit val scheduler: Scheduler = actorSystem.scheduler

  private val datasetActor: ActorRef[DatasetActor.Command] = {
    actorSystem.systemActorOf(DatasetActor(datasetService), "dataset-actor")
  }

  private def processRequest(operation: String, request: JsObject = Json.obj()): Future[Result] =
    datasetActor.ask(replyTo => ProcessRequest(operation, request, replyTo)).map {
      case ResponseBody(responseBody) => Ok(Json.toJson(responseBody))
    }.recover {
      case ex => InternalServerError(Json.obj("error" -> s"Error: ${ex.getMessage}"))
    }

  def getAllDatasets: Action[AnyContent] = Action.async { processRequest("READ_DATASET") }

  def getDatasetById(id: String): Action[AnyContent] = Action.async {
    val request = Json.obj("id" -> id)
    processRequest("READ_DATASET_BY_ID", request)
  }

  def createDataset: Action[JsValue] = Action.async(parse.json) { request =>
    val dataset: JsObject = request.body.as[JsObject]
    processRequest("CREATE_DATASET", dataset)
  }

  def updateDataset(): Action[JsValue] = Action.async(parse.json) { request =>
    val updateDataset: JsObject = request.body.as[JsObject]
    processRequest("UPDATE_DATASET", updateDataset)
  }

  def deleteDatasetById(id: String): Action[AnyContent] = Action.async {
    val request = Json.obj("id" -> id)
    processRequest("DELETE_DATASET", request)
  }
}
