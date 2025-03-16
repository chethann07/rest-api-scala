name := "rest-api-scala"
scalaVersion := "2.13.14"

lazy val root = (project in file(".")).aggregate(restActors, restService)

lazy val restActors = (project in file("rest-actors")).dependsOn(restCommon)

lazy val restService = (project in file("rest-service")).enablePlugins(PlayScala).dependsOn(restCommon, restActors)

lazy val restCommon = (project in file("rest-common"))