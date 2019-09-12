package lib

import org.apache.spark.sql.types.TimestampType
import org.apache.spark.sql.{SQLContext, SparkSession}

case class ELBLog(timestamp: java.sql.Timestamp,
                  elb: String,
                  clientPort: String,
                  backendPort: String,
                  requestProcessingTime: String,
                  backendProcessingTime: String,
                  responseProcessingTime: String,
                  elbStatusCode: String,
                  backendStatusCode: String,
                  receivedBytes: String,
                  sentBytes: String,
                  request: String,
                  userAgent: String,
                  ssl_cipher: String,
                  ssl_protocol: String)

case class ClientRequest(timestamp: java.sql.Timestamp,
                         clientIp: String,
                         request: String)

case class UserRequest(timestamp: java.sql.Timestamp,
                       clientIp: String,
                       request: String,
                       timeDiff: Long,
                       sessionId: Long)

trait SparkApp {
  val spark: SparkSession = SparkSession
    .builder()
    .appName("data-engineer-challenge")
    .master("local[*]")
    .getOrCreate()

  val sc = spark.sparkContext

  val sqlContext: SQLContext = spark.sqlContext
}
