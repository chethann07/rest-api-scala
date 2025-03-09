package actors

import actors.DatasetActor._
import model.Datasets
import org.apache.pekko.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import org.apache.pekko.actor.typed.{ActorRef, Behavior}
import play.api.libs.json.{JsObject, Json}
import service.DatasetService
import helpers.Utils
import scala.jdk.CollectionConverters._

object DatasetActor {

  sealed trait Command

  final case class GetAllDatasets(replyTo: ActorRef[ResponseBody]) extends Command
  final case class GetDatasetById(id: String, replyTo: ActorRef[ResponseBody]) extends Command
  final case class CreateDataset(jsonDataset: JsObject, replyTo: ActorRef[ResponseBody]) extends Command
  final case class UpdateDataset(id: String, jsonDataset: JsObject, replyTo: ActorRef[ResponseBody]) extends Command
  final case class DeleteDatasetById(id: String, replyTo: ActorRef[ResponseBody]) extends Command

  final case class ResponseBody(responseBody: JsObject)

  def apply(datasetService: DatasetService): Behavior[Command] = {
    Behaviors.setup(context => new DatasetActor(context, datasetService))
  }
}

class DatasetActor(context: ActorContext[DatasetActor.Command], datasetService: DatasetService)
  extends AbstractBehavior[DatasetActor.Command](context) {

  override def onMessage(msg: DatasetActor.Command): Behavior[DatasetActor.Command] = {
    msg match {
      case GetAllDatasets(replyTo) =>
        try{
          val datasets: Seq[Datasets] = datasetService.getAllDatasets.asScala.toSeq
          val results: Seq[JsObject] = Utils.convertDatasetsToJson(datasets)
          val responseBody: JsObject = Utils.responseBody("api.read", "success", null, 200, results)
          replyTo.tell(ResponseBody(responseBody))
        }
        catch {
          case ex: Exception =>
            val responseBody: JsObject = Utils.responseBody("api.error", "failure", ex.getMessage, 500, Seq.empty)
            replyTo.tell(ResponseBody(responseBody))
        }
        this

      case GetDatasetById(id, replyTo) =>
        try{
          val dataset: Datasets = datasetService.getDatasetById(id)
          val results: JsObject = Utils.convertDatasetsToJson(dataset)
          val responseBody: JsObject = Utils.responseBody("api.read", "success", null, 200, Seq(results))
          replyTo.tell(ResponseBody(responseBody))
        }
        catch {
          case ex: NullPointerException =>
            val responseBody: JsObject = Utils.responseBody("api.error", "failure", ex.getMessage, 400, Seq.empty)
            replyTo.tell(ResponseBody(responseBody))
          case ex: Exception =>
            val responseBody: JsObject = Utils.responseBody("api.error", "failure", ex.getMessage, 500, Seq.empty)
            replyTo.tell(ResponseBody(responseBody))
        }
        this

      case CreateDataset(jsonDataset, replyTo) =>
        try{
          val dataset: Datasets = Utils.convertJsonToDataset(jsonDataset)
          datasetService.createDataset(dataset)
          val responseBody: JsObject = Utils.responseBody("api.create", "success", null, 200, Seq(jsonDataset))
          replyTo.tell(ResponseBody(responseBody))
        }
        catch {
          case ex: IllegalArgumentException =>
            val responseBody: JsObject = Utils.responseBody("api.error", "failure", ex.getMessage, 400, Seq.empty)
            replyTo.tell(ResponseBody(responseBody))
          case ex: Exception =>
            val responseBody: JsObject = Utils.responseBody("api.error", "failure", ex.getMessage, 500, Seq.empty)
            replyTo.tell(ResponseBody(responseBody))
        }
        this

      case UpdateDataset(id,jsonDataset, replyTo) =>
        val updatedDataset: Datasets = Utils.convertJsonToDataset(jsonDataset)
        datasetService.updateDataset(updatedDataset)
        val responseBody: JsObject = Utils.responseBody("api.update", "success", null, 200, Seq(jsonDataset))
        replyTo.tell(ResponseBody(responseBody))
        this

      case DeleteDatasetById(id, replyTo) =>
        try{
          val dataset: Datasets = datasetService.getDatasetById(id)
          datasetService.deleteDataset(id)
          val results: JsObject = Json.obj("message" -> s"dataset with id ${id} deleted")
          val responseBody: JsObject = Utils.responseBody("api.delete", "success", null, 200, Seq(results))
          replyTo.tell(ResponseBody(responseBody))
        }
        catch {
          case ex: NullPointerException =>
            val responseBody: JsObject = Utils.responseBody("api.error", "failure", ex.getMessage, 400, Seq.empty)
            replyTo.tell(ResponseBody(responseBody))
          case ex: Exception =>
            val responseBody: JsObject = Utils.responseBody("api.error", "failure", ex.getMessage, 500, Seq.empty)
            replyTo.tell(ResponseBody(responseBody))
        }
        this
    }
  }
}
