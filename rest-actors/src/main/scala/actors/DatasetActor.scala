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
    final case class ProcessRequest(operation: String, request: JsObject, replyTo: ActorRef[ResponseBody]) extends Command
    final case class ResponseBody(responseBody: JsObject)

    def apply(datasetService: DatasetService): Behavior[Command] = {
      Behaviors.setup(context => new DatasetActor(context, datasetService))
    }
  }

  class DatasetActor(context: ActorContext[DatasetActor.Command], datasetService: DatasetService)
    extends AbstractBehavior[DatasetActor.Command](context) {

    override def onMessage(msg: DatasetActor.Command): Behavior[DatasetActor.Command] = {
      msg match {
        case ProcessRequest(operation, request, replyTo) =>
          operation match {
            case "READ_DATASET" =>
              val responseBody: JsObject = getAllDatasets
              replyTo.tell(ResponseBody(responseBody))
              this

            case "READ_DATASET_BY_ID" =>
              val responseBody: JsObject = getDatasetById(request)
              replyTo.tell(ResponseBody(responseBody))
              this

            case "CREATE_DATASET" =>
              val responseBody: JsObject = create(request)
              replyTo.tell(ResponseBody(responseBody))
              this

            case "UPDATE_DATASET" =>
              val responseBody: JsObject = update(request)
              replyTo.tell(ResponseBody(responseBody))
              this

            case "DELETE_DATASET" =>
              val responseBody: JsObject = delete(request)
              replyTo.tell(ResponseBody(responseBody))
              this
          }
      }
    }

    def getAllDatasets: JsObject = {
      try{
        val datasets: Seq[Datasets] = datasetService.getAllDatasets.asScala.toSeq
        val results: Seq[JsObject] = Utils.convertDatasetsToJson(datasets)
        Utils.responseBody("api.read", "success", null, 200, results)
      }
      catch {
        case ex: Exception =>
          Utils.responseBody("api.error", "failure", ex.getMessage, 500, Seq.empty)
      }
    }

    def getDatasetById(request: JsObject): JsObject = {
      try{
        val id: String = (request \ "id").as[String]
        val dataset: Datasets = datasetService.getDatasetById(id)
        val results: JsObject = Utils.convertDatasetsToJson(dataset)
        Utils.responseBody("api.read", "success", null, 200, Seq(results))
      }
      catch {
        case ex: NullPointerException =>
        Utils.responseBody("api.error", "failure", ex.getMessage, 400, Seq.empty)
        case ex: Exception =>
        Utils.responseBody("api.error", "failure", ex.getMessage, 500, Seq.empty)
      }
    }

    def create(request: JsObject): JsObject = {
      try{
        val dataset: Datasets = Utils.convertJsonToDataset(request)
        datasetService.createDataset(dataset)
        Utils.responseBody("api.create", "success", null, 200, Seq(Json.obj("message" -> s"dataset with ${dataset.getId} created")))
      }
      catch {
        case ex: IllegalArgumentException =>
          Utils.responseBody("api.error", "failure", ex.getMessage, 400, Seq.empty)
        case ex: Exception =>
          Utils.responseBody("api.error", "failure", ex.getMessage, 500, Seq.empty)
      }
    }

    def update(request: JsObject): JsObject = {
      try{
        val id: String = (request \ "id").as[String]
        datasetService.getDatasetById(id)
        val updatedDataset: Datasets = Utils.convertJsonToDataset(request)
        datasetService.updateDataset(updatedDataset)
        val results: JsObject = Json.obj("message" -> s"dataset with id ${updatedDataset.getId} updated")
        Utils.responseBody("api.update", "success", null, 200, Seq(results))
      }catch {
        case ex: NullPointerException =>
          Utils.responseBody("api.error", "failure", ex.getMessage, 400, Seq.empty)
        case ex: Exception =>
          Utils.responseBody("api.error", "failure", ex.getMessage, 500, Seq.empty)
      }
    }

    def delete(request: JsObject): JsObject = {
      try{
        val id: String = (request \ "id").as[String]
        datasetService.deleteDataset(id)
        val results: JsObject = Json.obj("message" -> s"dataset with id ${id} deleted")
        Utils.responseBody("api.delete", "success", null, 200, Seq(results))
      }
      catch {
        case ex: NullPointerException =>
          Utils.responseBody("api.error", "failure", ex.getMessage, 400, Seq.empty)
        case ex: Exception =>
          Utils.responseBody("api.error", "failure", ex.getMessage, 500, Seq.empty)
      }
    }
  }
