package helpers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import model.Datasets
import play.api.libs.json.{JsObject, Json}

object Utils {

  private val objectMapper = new ObjectMapper().registerModule(DefaultScalaModule)

  def convertDatasetsToJson(datasets: Seq[Datasets]): Seq[JsObject] = {
    datasets.map { dataset =>
      Json.obj(
        "id" -> dataset.getId,
        "dataSchema" -> Json.parse(objectMapper.writeValueAsString(dataset.getDataSchema)),
        "routeConfig" -> Json.parse(objectMapper.writeValueAsString(dataset.getRouteConfig)),
        "status" -> dataset.getStatus.toString,
        "updatedBy" -> dataset.getUpdatedBy,
        "createdBy" -> dataset.getCreatedBy,
        "createdAt" -> dataset.getCreatedAt.toString,
        "updatedAt" -> dataset.getUpdatedAt.toString
      )
    }
  }

  def convertDatasetsToJson(dataset: Datasets): JsObject = {
    Json.obj(
      "id" -> dataset.getId,
      "dataSchema" -> Json.parse(objectMapper.writeValueAsString(dataset.getDataSchema)),
      "routeConfig" -> Json.parse(objectMapper.writeValueAsString(dataset.getRouteConfig)),
      "status" -> dataset.getStatus.toString,
      "updatedBy" -> dataset.getUpdatedBy,
      "createdBy" -> dataset.getCreatedBy,
      "createdAt" -> dataset.getCreatedAt.toString,
      "updatedAt" -> dataset.getUpdatedAt.toString
    )
  }

  def convertJsonToDataset(json: JsObject): Datasets = {
    objectMapper.readValue(json.toString(), classOf[Datasets])
  }

  def responseBody(id: String, status: String, errorMsg: String, responseCode: Int, result: Seq[JsObject]): JsObject = {
    val responseJson: JsObject = Json.obj(
      "id" -> id,
      "ver" -> "1.0",
      "ts" -> java.time.Instant.now.toString,
      "params" -> Json.obj(
        "resmsgid" -> java.util.UUID.randomUUID(),
        "status" -> status,
        "errmsg" -> errorMsg
      ),
      "responseCode" -> responseCode,
      "result" -> result
    )
    responseJson
  }
}
