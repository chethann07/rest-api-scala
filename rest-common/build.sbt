name := "rest-common"
scalaVersion := "2.13.14"

libraryDependencies ++= Seq(
  guice,

  "org.hibernate.orm" % "hibernate-core" % "6.6.1.Final",
  "jakarta.persistence" % "jakarta.persistence-api" % "3.1.0",
  "org.postgresql" % "postgresql" % "42.7.1",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.14.3"
)