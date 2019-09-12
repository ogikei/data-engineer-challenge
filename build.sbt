name := "data-engineer-challenge"

version := "0.1"

mainClass := Some("Answer")

version := "2.11.12"

libraryDependencies ++= Seq(
  "org.apache.spark" % "spark-core_2.12" % "2.4.3",
  "org.apache.spark" % "spark-sql_2.12" % "2.4.3",
  "org.apache.spark" % "spark-hive_2.12" % "2.4.3"
)
