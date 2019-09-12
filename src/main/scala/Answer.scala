import lib.{ClientRequest, ELBLog, SparkApp, UserRequest}

import org.apache.spark.sql.{Encoders, SaveMode}
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._

object Answer extends SparkApp {

  def main(args: Array[String]): Unit = {
    import spark.implicits._

    val ds = spark
      .read
      .schema(Encoders.product[ELBLog].schema)
      .option("delimiter", " ")
      .csv(this.getClass.getResource("./2015_07_22_mktplace_shop_web_log_sample.log").getPath)
      .as[ELBLog]
    ds.show()
    //  +--------------------+----------------+--------------------+-------------+---------------------+---------------------+----------------------+-------------+-----------------+-------------+---------+--------------------+--------------------+--------------------+------------+
    //  |           timestamp|             elb|          clientPort|  backendPort|requestProcessingTime|backendProcessingTime|responseProcessingTime|elbStatusCode|backendStatusCode|receivedBytes|sentBytes|             request|           userAgent|          ssl_cipher|ssl_protocol|
    //  +--------------------+----------------+--------------------+-------------+---------------------+---------------------+----------------------+-------------+-----------------+-------------+---------+--------------------+--------------------+--------------------+------------+
    //  |2015-07-22T09:00:...|marketpalce-shop|123.242.248.130:5...|10.0.6.158:80|             0.000022|             0.026109|               0.00002|          200|              200|            0|      699|GET https://paytm...|Mozilla/5.0 (Wind...|ECDHE-RSA-AES128-...|     TLSv1.2|
    //  |2015-07-22T09:00:...|marketpalce-shop| 203.91.211.44:51402|10.0.4.150:80|             0.000024|              0.15334|              0.000026|          200|              200|            0|     1497|GET https://paytm...|Mozilla/5.0 (Wind...|ECDHE-RSA-AES128-...|     TLSv1.2|
    //  |2015-07-22T09:00:...|marketpalce-shop|   1.39.32.179:56419|10.0.4.244:80|             0.000024|             0.164958|              0.000017|          200|              200|            0|      157|GET https://paytm...|Mozilla/5.0 (Wind...|ECDHE-RSA-AES128-...|     TLSv1.2|
    //  |2015-07-22T09:00:...|marketpalce-shop|180.179.213.94:48725|10.0.6.108:80|              0.00002|             0.002333|              0.000021|          200|              200|            0|    35734|GET https://paytm...|                   -|ECDHE-RSA-AES128-...|     TLSv1.2|
    //  |2015-07-22T09:00:...|marketpalce-shop|120.59.192.208:13527|10.0.4.217:80|             0.000024|             0.015091|              0.000016|          200|              200|           68|      640|POST https://payt...|Mozilla/5.0 (Wind...|ECDHE-RSA-AES128-...|     TLSv1.2|
    //  |2015-07-22T09:00:...|marketpalce-shop|117.239.195.66:50524|10.0.6.195:80|             0.000024|              0.02157|              0.000021|          200|              200|            0|       60|GET https://paytm...|Mozilla/5.0 (Wind...|ECDHE-RSA-AES128-...|     TLSv1.2|
    //  |2015-07-22T09:00:...|marketpalce-shop| 101.60.186.26:33177|10.0.4.244:80|              0.00002|             0.001098|              0.000022|          200|              200|            0|     1150|GET https://paytm...|Mozilla/5.0 (Wind...|ECDHE-RSA-AES128-...|     TLSv1.2|
    //  |2015-07-22T09:00:...|marketpalce-shop|  59.183.41.47:62014|10.0.4.227:80|             0.000021|             0.008161|              0.000021|          200|              200|            0|       72|GET https://paytm...|Mozilla/5.0 (Wind...|ECDHE-RSA-AES128-...|     TLSv1.2|
    //  |2015-07-22T09:00:...|marketpalce-shop|117.239.195.66:50538|10.0.4.227:80|             0.000019|             0.001035|              0.000021|          200|              200|            0|      396|GET https://paytm...|Mozilla/5.0 (Wind...|ECDHE-RSA-AES128-...|     TLSv1.2|

    val clientRequest = ds
      .select('timestamp, 'clientPort, 'request)
      .map(row => ClientRequest(row.getTimestamp(0), row.getString(1).split(":")(0), row.getString(2).split(" ")(1)))
    clientRequest.show()
    // +--------------------+---------------+--------------------+
    // |           timestamp|       clientIp|             request|
    // +--------------------+---------------+--------------------+
    // |2015-07-22 18:00:...|123.242.248.130|https://paytm.com...|
    // |2015-07-22 18:15:...|  203.91.211.44|https://paytm.com...|
    // |2015-07-22 18:15:...|    1.39.32.179|https://paytm.com...|
    // |2015-07-22 18:01:...| 180.179.213.94|https://paytm.com...|
    // |2015-07-22 18:01:...| 120.59.192.208|https://paytm.com...|
    // |2015-07-22 18:01:...| 117.239.195.66|https://paytm.com...|
    // |2015-07-22 18:01:...|  101.60.186.26|https://paytm.com...|
    // |2015-07-22 18:01:...|   59.183.41.47|https://paytm.com...|
    // |2015-07-22 18:01:...| 117.239.195.66|https://paytm.com...|
    // |2015-07-22 18:01:...|  183.83.237.83|https://paytm.com...|
    // |2015-07-22 18:01:...|  117.195.91.36|https://paytm.com...|
    // |2015-07-22 18:01:...|122.180.245.251|https://paytm.com...|
    // |2015-07-22 18:01:...| 117.198.215.20|https://paytm.com...|
    // |2015-07-22 18:01:...| 223.176.154.91|https://paytm.com...|
    // |2015-07-22 18:00:...|223.225.236.110|https://paytm.com...|
    // |2015-07-22 18:01:...| 117.241.97.140|https://paytm.com...|
    // |2015-07-22 18:01:...|117.205.247.140|https://paytm.com...|
    // |2015-07-22 18:01:...|   14.102.53.58|https://paytm.com...|
    // |2015-07-22 18:01:...|  203.200.99.67|https://paytm.com...|
    // |2015-07-22 18:00:...|107.167.109.204|https://paytm.com...|
    // +--------------------+---------------+--------------------+

    // セッションIDを0で初期化し、同一IPのtimestampとその前のtimestampを引き、前にアクセスした時間とどれくらい時間が空いているかを出す
    val userClient = clientRequest
      //      .withColumn("nextAccessTime", lead('timestamp, 1).over(Window.partitionBy('clientIp)
      //        .orderBy('timestamp)))
      .withColumn("previousAccessTime", lag('timestamp, 1).over(Window.partitionBy('clientIp)
        .orderBy('timestamp)))
      .withColumn("timeDiff", (unix_timestamp('timestamp) - unix_timestamp('previousAccessTime)))
      .withColumn("sessionId", lit(0))
      //      .drop("nextAccessTime")
      .drop("previousAccessTime")
      .na
      .fill(0, Seq("timeDiff"))
      .as[UserRequest]
    userClient.show()
    // +--------------------+------------+--------------------+--------+---------+
    // |           timestamp|    clientIp|             request|timeDiff|sessionId|
    // +--------------------+------------+--------------------+--------+---------+
    // |2015-07-23 01:19:...|1.186.143.37|https://paytm.com...|       0|        0|
    // |2015-07-23 01:28:...|1.186.143.37|https://paytm.com...|     546|        0|
    // |2015-07-22 11:45:...|1.187.164.29|https://paytm.com...|       0|        0|
    // |2015-07-22 11:45:...|1.187.164.29|https://paytm.com...|      10|        0|
    // |2015-07-22 11:45:...|1.187.164.29|https://paytm.com...|       2|        0|
    // |2015-07-22 11:48:...|1.187.164.29|https://paytm.com...|     171|        0|
    // |2015-07-22 11:48:...|1.187.164.29|https://paytm.com...|       8|        0|
    // |2015-07-22 11:49:...|1.187.164.29|https://paytm.com...|      33|        0|
    // |2015-07-22 11:55:...|1.187.164.29|https://paytm.com...|     354|        0|
    // |2015-07-22 11:55:...|1.187.164.29|https://paytm.com...|      45|        0|
    // |2015-07-22 12:00:...|1.187.164.29|https://paytm.com...|     299|        0|
    // |2015-07-23 01:43:...|  1.22.41.76|https://paytm.com...|       0|        0|
    // |2015-07-23 01:45:...|  1.22.41.76|https://paytm.com...|     149|        0|
    // |2015-07-23 01:46:...|  1.22.41.76|https://paytm.com...|      21|        0|
    // |2015-07-23 01:49:...|  1.22.41.76|https://paytm.com...|     200|        0|
    // |2015-07-23 01:50:...|  1.22.41.76|https://paytm.com...|      51|        0|
    // |2015-07-23 01:51:...|  1.22.41.76|https://paytm.com...|      98|        0|
    // |2015-07-23 01:55:...|  1.22.41.76|https://paytm.com...|     210|        0|
    // |2015-07-23 01:57:...|  1.22.41.76|https://paytm.com...|     135|        0|
    // |2015-07-23 02:45:...| 1.23.208.26|https://paytm.com...|       0|        0|
    // +--------------------+------------+--------------------+--------+---------+

    userClient.where('clientIp === "103.16.71.9").show(100)
    // +--------------------+-----------+--------------------+--------+---------+
    // |           timestamp|   clientIp|             request|timeDiff|sessionId|
    // +--------------------+-----------+--------------------+--------+---------+
    // |2015-07-22 16:05:...|103.16.71.9|https://paytm.com...|       0|        0|
    // |2015-07-22 16:06:...|103.16.71.9|https://paytm.com...|      50|        0|
    // |2015-07-22 16:12:...|103.16.71.9|https://paytm.com...|     341|        0|
    // |2015-07-23 01:12:...|103.16.71.9|https://paytm.com...|   32439|        0|
    // |2015-07-23 01:12:...|103.16.71.9|https://paytm.com...|       0|        0|
    // |2015-07-23 01:12:...|103.16.71.9|https://paytm.com...|       0|        0|
    // |2015-07-23 01:13:...|103.16.71.9|https://paytm.com...|      33|        0|
    // |2015-07-23 01:13:...|103.16.71.9|https://paytm.com...|       0|        0|
    // |2015-07-23 01:13:...|103.16.71.9|https://paytm.com...|      12|        0|
    // |2015-07-23 01:13:...|103.16.71.9|https://paytm.com...|       1|        0|
    // |2015-07-23 01:13:...|103.16.71.9|https://paytm.com...|      31|        0|
    // |2015-07-23 01:14:...|103.16.71.9|https://paytm.com...|      14|        0|
    // |2015-07-23 01:14:...|103.16.71.9|https://paytm.com...|      10|        0|
    // |2015-07-23 01:14:...|103.16.71.9|https://paytm.com...|       0|        0|
    // |2015-07-23 01:14:...|103.16.71.9|https://paytm.com...|       0|        0|
    // |2015-07-23 01:14:...|103.16.71.9|https://paytm.com...|       1|        0|
    // |2015-07-23 01:14:...|103.16.71.9|https://paytm.com...|       4|        0|
    // |2015-07-23 01:15:...|103.16.71.9|https://paytm.com...|      68|        0|
    // |2015-07-23 01:15:...|103.16.71.9|https://paytm.com...|       2|        0|
    // |2015-07-23 01:15:...|103.16.71.9|https://paytm.com...|       0|        0|
    // |2015-07-23 01:15:...|103.16.71.9|https://paytm.com...|      23|        0|
    // |2015-07-23 01:17:...|103.16.71.9|https://paytm.com...|      74|        0|
    // |2015-07-23 01:17:...|103.16.71.9|https://paytm.com...|       2|        0|
    // |2015-07-23 01:17:...|103.16.71.9|https://paytm.com...|       1|        0|
    // |2015-07-23 01:17:...|103.16.71.9|https://paytm.com...|       2|        0|
    // |2015-07-23 01:17:...|103.16.71.9|https://paytm.com...|      25|        0|
    // |2015-07-23 01:18:...|103.16.71.9|https://paytm.com...|      38|        0|
    // |2015-07-23 01:18:...|103.16.71.9|https://paytm.com...|      36|        0|
    // |2015-07-23 01:19:...|103.16.71.9|https://paytm.com...|       6|        0|
    // |2015-07-23 01:19:...|103.16.71.9|https://paytm.com...|       1|        0|
    // |2015-07-23 01:19:...|103.16.71.9|https://paytm.com...|       7|        0|
    // |2015-07-23 01:19:...|103.16.71.9|https://paytm.com...|      35|        0|
    // |2015-07-23 01:19:...|103.16.71.9|https://paytm.com...|       0|        0|
    // |2015-07-23 01:20:...|103.16.71.9|https://paytm.com...|      23|        0|
    // |2015-07-23 01:20:...|103.16.71.9|https://paytm.com...|       5|        0|
    // |2015-07-23 01:20:...|103.16.71.9|https://paytm.com...|      22|        0|
    // |2015-07-23 01:20:...|103.16.71.9|https://paytm.com...|       3|        0|
    // |2015-07-23 01:20:...|103.16.71.9|https://paytm.com...|      11|        0|
    // |2015-07-23 01:20:...|103.16.71.9|https://paytm.com...|       3|        0|
    // |2015-07-23 01:20:...|103.16.71.9|https://paytm.com...|       1|        0|
    // |2015-07-23 01:20:...|103.16.71.9|https://paytm.com...|       3|        0|
    // |2015-07-23 01:20:...|103.16.71.9|https://paytm.com...|       0|        0|
    // |2015-07-23 01:20:...|103.16.71.9|https://paytm.com...|       1|        0|
    // |2015-07-23 01:20:...|103.16.71.9|https://paytm.com...|       1|        0|
    // |2015-07-23 01:21:...|103.16.71.9|https://paytm.com...|      21|        0|
    // |2015-07-23 01:21:...|103.16.71.9|https://paytm.com...|       5|        0|
    // |2015-07-23 01:21:...|103.16.71.9|https://paytm.com...|       3|        0|
    // |2015-07-23 01:21:...|103.16.71.9|https://paytm.com...|       4|        0|
    // |2015-07-23 01:21:...|103.16.71.9|https://paytm.com...|       5|        0|
    // |2015-07-23 01:21:...|103.16.71.9|https://paytm.com...|      10|        0|
    // |2015-07-23 01:21:...|103.16.71.9|https://paytm.com...|       1|        0|
    // |2015-07-23 01:22:...|103.16.71.9|https://paytm.com...|      14|        0|
    // |2015-07-23 01:22:...|103.16.71.9|https://paytm.com...|       5|        0|
    // |2015-07-23 01:22:...|103.16.71.9|https://paytm.com...|       1|        0|
    // |2015-07-23 01:22:...|103.16.71.9|https://paytm.com...|       5|        0|
    // |2015-07-23 01:22:...|103.16.71.9|https://paytm.com...|      11|        0|
    // |2015-07-23 01:22:...|103.16.71.9|https://paytm.com...|       4|        0|
    // |2015-07-23 01:22:...|103.16.71.9|https://paytm.com...|       9|        0|
    // |2015-07-23 01:22:...|103.16.71.9|https://paytm.com...|      14|        0|
    // |2015-07-23 01:23:...|103.16.71.9|https://paytm.com...|      10|        0|
    // |2015-07-23 01:23:...|103.16.71.9|https://paytm.com...|      12|        0|
    // |2015-07-23 01:23:...|103.16.71.9|https://paytm.com...|       9|        0|
    // |2015-07-23 01:23:...|103.16.71.9|https://paytm.com...|      13|        0|
    // |2015-07-23 01:23:...|103.16.71.9|https://paytm.com...|       3|        0|
    // |2015-07-23 01:23:...|103.16.71.9|https://paytm.com...|       1|        0|
    // |2015-07-23 01:23:...|103.16.71.9|https://paytm.com...|      13|        0|
    // |2015-07-23 01:23:...|103.16.71.9|https://paytm.com...|       4|        0|
    // |2015-07-23 01:23:...|103.16.71.9|https://paytm.com...|       3|        0|
    // |2015-07-23 01:24:...|103.16.71.9|https://paytm.com...|       9|        0|
    // |2015-07-23 01:24:...|103.16.71.9|https://paytm.com...|      10|        0|
    // |2015-07-23 01:24:...|103.16.71.9|https://paytm.com...|       3|        0|
    // |2015-07-23 01:24:...|103.16.71.9|https://paytm.com...|       2|        0|
    // |2015-07-23 01:24:...|103.16.71.9|https://paytm.com...|      17|        0|
    // |2015-07-23 01:24:...|103.16.71.9|https://paytm.com...|      15|        0|
    // |2015-07-23 01:25:...|103.16.71.9|https://paytm.com...|       9|        0|
    // |2015-07-23 01:25:...|103.16.71.9|https://paytm.com...|       4|        0|
    // |2015-07-23 01:25:...|103.16.71.9|https://paytm.com...|       1|        0|
    // |2015-07-23 01:25:...|103.16.71.9|https://paytm.com...|      26|        0|
    // |2015-07-23 01:25:...|103.16.71.9|https://paytm.com...|       5|        0|
    // |2015-07-23 01:25:...|103.16.71.9|https://paytm.com...|      19|        0|
    // |2015-07-23 01:26:...|103.16.71.9|https://paytm.com...|       3|        0|
    // |2015-07-23 01:26:...|103.16.71.9|https://paytm.com...|      16|        0|
    // |2015-07-23 01:26:...|103.16.71.9|https://paytm.com...|      21|        0|
    // |2015-07-23 01:26:...|103.16.71.9|https://paytm.com...|       9|        0|
    // |2015-07-23 01:26:...|103.16.71.9|https://paytm.com...|      11|        0|
    // |2015-07-23 01:27:...|103.16.71.9|https://paytm.com...|      15|        0|
    // |2015-07-23 01:27:...|103.16.71.9|https://paytm.com...|       2|        0|
    // |2015-07-23 01:28:...|103.16.71.9|https://paytm.com...|      45|        0|
    // |2015-07-23 01:28:...|103.16.71.9|https://paytm.com...|       0|        0|
    // |2015-07-23 01:28:...|103.16.71.9|https://paytm.com...|       1|        0|
    // |2015-07-23 01:28:...|103.16.71.9|https://paytm.com...|       0|        0|
    // |2015-07-23 01:28:...|103.16.71.9|https://paytm.com...|       2|        0|
    // |2015-07-23 01:28:...|103.16.71.9|https://paytm.com...|       6|        0|
    // |2015-07-23 01:28:...|103.16.71.9|https://paytm.com...|       6|        0|
    // |2015-07-23 01:29:...|103.16.71.9|https://paytm.com...|      52|        0|
    // |2015-07-23 01:44:...|103.16.71.9|https://paytm.com...|     935|        0|
    // |2015-07-23 01:48:...|103.16.71.9|https://paytm.com...|     250|        0|
    // |2015-07-23 01:51:...|103.16.71.9|https://paytm.com...|     166|        0|
    // |2015-07-23 01:52:...|103.16.71.9|https://paytm.com...|      55|        0|
    // |2015-07-23 01:55:...|103.16.71.9|http://www.paytm....|     167|        0|
    // +--------------------+-----------+--------------------+--------+---------+

    val sessionCounter = sc.longAccumulator

    // 各IP毎にsessionIDを付与する
    val userRequestWithSessionId = userClient
      .groupByKey(row => row.clientIp)
      .flatMapGroups((i, it) =>
        it
          .toSeq
          .map { ur =>
            if (ur.timeDiff > 15 * 60) {
              sessionCounter.add(1)
              UserRequest(ur.timestamp, ur.clientIp, ur.request, ur.timeDiff, ur.sessionId + sessionCounter.value)
            } else UserRequest(ur.timestamp, ur.clientIp, ur.request, ur.timeDiff, sessionCounter.value)
          })
    userRequestWithSessionId.where('clientIp === "103.16.71.9").show(100)
    // +--------------------+-----------+--------------------+--------+---------+
    // |           timestamp|   clientIp|             request|timeDiff|sessionId|
    // +--------------------+-----------+--------------------+--------+---------+
    // |2015-07-22 16:05:...|103.16.71.9|https://paytm.com...|       0|        6|
    // |2015-07-22 16:06:...|103.16.71.9|https://paytm.com...|      50|        6|
    // |2015-07-22 16:12:...|103.16.71.9|https://paytm.com...|     341|        6|
    // |2015-07-23 01:12:...|103.16.71.9|https://paytm.com...|   32439|        7|
    // |2015-07-23 01:12:...|103.16.71.9|https://paytm.com...|       0|        7|
    // |2015-07-23 01:12:...|103.16.71.9|https://paytm.com...|       0|        7|
    // |2015-07-23 01:13:...|103.16.71.9|https://paytm.com...|      33|        7|
    // |2015-07-23 01:13:...|103.16.71.9|https://paytm.com...|       0|        7|
    // |2015-07-23 01:13:...|103.16.71.9|https://paytm.com...|      12|        7|
    // |2015-07-23 01:13:...|103.16.71.9|https://paytm.com...|       1|        7|
    // |2015-07-23 01:13:...|103.16.71.9|https://paytm.com...|      31|        7|
    // |2015-07-23 01:14:...|103.16.71.9|https://paytm.com...|      14|        7|
    // |2015-07-23 01:14:...|103.16.71.9|https://paytm.com...|      10|        7|
    // |2015-07-23 01:14:...|103.16.71.9|https://paytm.com...|       0|        7|
    // |2015-07-23 01:14:...|103.16.71.9|https://paytm.com...|       0|        7|
    // |2015-07-23 01:14:...|103.16.71.9|https://paytm.com...|       1|        7|
    // |2015-07-23 01:14:...|103.16.71.9|https://paytm.com...|       4|        7|
    // |2015-07-23 01:15:...|103.16.71.9|https://paytm.com...|      68|        7|
    // |2015-07-23 01:15:...|103.16.71.9|https://paytm.com...|       2|        7|
    // |2015-07-23 01:15:...|103.16.71.9|https://paytm.com...|       0|        7|
    // |2015-07-23 01:15:...|103.16.71.9|https://paytm.com...|      23|        7|
    // |2015-07-23 01:17:...|103.16.71.9|https://paytm.com...|      74|        7|
    // |2015-07-23 01:17:...|103.16.71.9|https://paytm.com...|       2|        7|
    // |2015-07-23 01:17:...|103.16.71.9|https://paytm.com...|       1|        7|
    // |2015-07-23 01:17:...|103.16.71.9|https://paytm.com...|       2|        7|
    // |2015-07-23 01:17:...|103.16.71.9|https://paytm.com...|      25|        7|
    // |2015-07-23 01:18:...|103.16.71.9|https://paytm.com...|      38|        7|
    // |2015-07-23 01:18:...|103.16.71.9|https://paytm.com...|      36|        7|
    // |2015-07-23 01:19:...|103.16.71.9|https://paytm.com...|       6|        7|
    // |2015-07-23 01:19:...|103.16.71.9|https://paytm.com...|       1|        7|
    // |2015-07-23 01:19:...|103.16.71.9|https://paytm.com...|       7|        7|
    // |2015-07-23 01:19:...|103.16.71.9|https://paytm.com...|      35|        7|
    // |2015-07-23 01:19:...|103.16.71.9|https://paytm.com...|       0|        7|
    // |2015-07-23 01:20:...|103.16.71.9|https://paytm.com...|      23|        7|
    // |2015-07-23 01:20:...|103.16.71.9|https://paytm.com...|       5|        7|
    // |2015-07-23 01:20:...|103.16.71.9|https://paytm.com...|      22|        7|
    // |2015-07-23 01:20:...|103.16.71.9|https://paytm.com...|       3|        7|
    // |2015-07-23 01:20:...|103.16.71.9|https://paytm.com...|      11|        7|
    // |2015-07-23 01:20:...|103.16.71.9|https://paytm.com...|       3|        7|
    // |2015-07-23 01:20:...|103.16.71.9|https://paytm.com...|       1|        7|
    // |2015-07-23 01:20:...|103.16.71.9|https://paytm.com...|       3|        7|
    // |2015-07-23 01:20:...|103.16.71.9|https://paytm.com...|       0|        7|
    // |2015-07-23 01:20:...|103.16.71.9|https://paytm.com...|       1|        7|
    // |2015-07-23 01:20:...|103.16.71.9|https://paytm.com...|       1|        7|
    // |2015-07-23 01:21:...|103.16.71.9|https://paytm.com...|      21|        7|
    // |2015-07-23 01:21:...|103.16.71.9|https://paytm.com...|       5|        7|
    // |2015-07-23 01:21:...|103.16.71.9|https://paytm.com...|       3|        7|
    // |2015-07-23 01:21:...|103.16.71.9|https://paytm.com...|       4|        7|
    // |2015-07-23 01:21:...|103.16.71.9|https://paytm.com...|       5|        7|
    // |2015-07-23 01:21:...|103.16.71.9|https://paytm.com...|      10|        7|
    // |2015-07-23 01:21:...|103.16.71.9|https://paytm.com...|       1|        7|
    // |2015-07-23 01:22:...|103.16.71.9|https://paytm.com...|      14|        7|
    // |2015-07-23 01:22:...|103.16.71.9|https://paytm.com...|       5|        7|
    // |2015-07-23 01:22:...|103.16.71.9|https://paytm.com...|       1|        7|
    // |2015-07-23 01:22:...|103.16.71.9|https://paytm.com...|       5|        7|
    // |2015-07-23 01:22:...|103.16.71.9|https://paytm.com...|      11|        7|
    // |2015-07-23 01:22:...|103.16.71.9|https://paytm.com...|       4|        7|
    // |2015-07-23 01:22:...|103.16.71.9|https://paytm.com...|       9|        7|
    // |2015-07-23 01:22:...|103.16.71.9|https://paytm.com...|      14|        7|
    // |2015-07-23 01:23:...|103.16.71.9|https://paytm.com...|      10|        7|
    // |2015-07-23 01:23:...|103.16.71.9|https://paytm.com...|      12|        7|
    // |2015-07-23 01:23:...|103.16.71.9|https://paytm.com...|       9|        7|
    // |2015-07-23 01:23:...|103.16.71.9|https://paytm.com...|      13|        7|
    // |2015-07-23 01:23:...|103.16.71.9|https://paytm.com...|       3|        7|
    // |2015-07-23 01:23:...|103.16.71.9|https://paytm.com...|       1|        7|
    // |2015-07-23 01:23:...|103.16.71.9|https://paytm.com...|      13|        7|
    // |2015-07-23 01:23:...|103.16.71.9|https://paytm.com...|       4|        7|
    // |2015-07-23 01:23:...|103.16.71.9|https://paytm.com...|       3|        7|
    // |2015-07-23 01:24:...|103.16.71.9|https://paytm.com...|       9|        7|
    // |2015-07-23 01:24:...|103.16.71.9|https://paytm.com...|      10|        7|
    // |2015-07-23 01:24:...|103.16.71.9|https://paytm.com...|       3|        7|
    // |2015-07-23 01:24:...|103.16.71.9|https://paytm.com...|       2|        7|
    // |2015-07-23 01:24:...|103.16.71.9|https://paytm.com...|      17|        7|
    // |2015-07-23 01:24:...|103.16.71.9|https://paytm.com...|      15|        7|
    // |2015-07-23 01:25:...|103.16.71.9|https://paytm.com...|       9|        7|
    // |2015-07-23 01:25:...|103.16.71.9|https://paytm.com...|       4|        7|
    // |2015-07-23 01:25:...|103.16.71.9|https://paytm.com...|       1|        7|
    // |2015-07-23 01:25:...|103.16.71.9|https://paytm.com...|      26|        7|
    // |2015-07-23 01:25:...|103.16.71.9|https://paytm.com...|       5|        7|
    // |2015-07-23 01:25:...|103.16.71.9|https://paytm.com...|      19|        7|
    // |2015-07-23 01:26:...|103.16.71.9|https://paytm.com...|       3|        7|
    // |2015-07-23 01:26:...|103.16.71.9|https://paytm.com...|      16|        7|
    // |2015-07-23 01:26:...|103.16.71.9|https://paytm.com...|      21|        7|
    // |2015-07-23 01:26:...|103.16.71.9|https://paytm.com...|       9|        7|
    // |2015-07-23 01:26:...|103.16.71.9|https://paytm.com...|      11|        7|
    // |2015-07-23 01:27:...|103.16.71.9|https://paytm.com...|      15|        7|
    // |2015-07-23 01:27:...|103.16.71.9|https://paytm.com...|       2|        7|
    // |2015-07-23 01:28:...|103.16.71.9|https://paytm.com...|      45|        7|
    // |2015-07-23 01:28:...|103.16.71.9|https://paytm.com...|       0|        7|
    // |2015-07-23 01:28:...|103.16.71.9|https://paytm.com...|       1|        7|
    // |2015-07-23 01:28:...|103.16.71.9|https://paytm.com...|       0|        7|
    // |2015-07-23 01:28:...|103.16.71.9|https://paytm.com...|       2|        7|
    // |2015-07-23 01:28:...|103.16.71.9|https://paytm.com...|       6|        7|
    // |2015-07-23 01:28:...|103.16.71.9|https://paytm.com...|       6|        7|
    // |2015-07-23 01:29:...|103.16.71.9|https://paytm.com...|      52|        7|
    // |2015-07-23 01:44:...|103.16.71.9|https://paytm.com...|     935|        8|
    // |2015-07-23 01:48:...|103.16.71.9|https://paytm.com...|     250|        8|
    // |2015-07-23 01:51:...|103.16.71.9|https://paytm.com...|     166|        8|
    // |2015-07-23 01:52:...|103.16.71.9|https://paytm.com...|      55|        8|
    // |2015-07-23 01:55:...|103.16.71.9|http://www.paytm....|     167|        8|
    // +--------------------+-----------+--------------------+--------+---------+

    userRequestWithSessionId.show()
    // +--------------------+------------+--------------------+--------+---------+
    // |           timestamp|    clientIp|             request|timeDiff|sessionId|
    // +--------------------+------------+--------------------+--------+---------+
    // |2015-07-23 01:19:...|1.186.143.37|https://paytm.com...|       0|        0|
    // |2015-07-23 01:28:...|1.186.143.37|https://paytm.com...|      10|        0|
    // |2015-07-22 11:45:...|1.187.164.29|https://paytm.com...|       0|        0|
    // |2015-07-22 11:45:...|1.187.164.29|https://paytm.com...|       1|        0|
    // |2015-07-22 11:45:...|1.187.164.29|https://paytm.com...|       1|        0|
    // |2015-07-22 11:48:...|1.187.164.29|https://paytm.com...|       3|        0|
    // |2015-07-22 11:48:...|1.187.164.29|https://paytm.com...|       1|        0|
    // |2015-07-22 11:49:...|1.187.164.29|https://paytm.com...|       1|        0|
    // |2015-07-22 11:55:...|1.187.164.29|https://paytm.com...|       6|        0|
    // |2015-07-22 11:55:...|1.187.164.29|https://paytm.com...|       1|        0|
    // |2015-07-22 12:00:...|1.187.164.29|https://paytm.com...|       5|        0|
    // |2015-07-23 01:43:...|  1.22.41.76|https://paytm.com...|       0|        0|
    // |2015-07-23 01:45:...|  1.22.41.76|https://paytm.com...|       3|        0|
    // |2015-07-23 01:46:...|  1.22.41.76|https://paytm.com...|       1|        0|
    // |2015-07-23 01:49:...|  1.22.41.76|https://paytm.com...|       4|        0|
    // |2015-07-23 01:50:...|  1.22.41.76|https://paytm.com...|       1|        0|
    // |2015-07-23 01:51:...|  1.22.41.76|https://paytm.com...|       2|        0|
    // |2015-07-23 01:55:...|  1.22.41.76|https://paytm.com...|       4|        0|
    // |2015-07-23 01:57:...|  1.22.41.76|https://paytm.com...|       3|        0|
    // |2015-07-23 02:45:...| 1.23.208.26|https://paytm.com...|       0|        0|
    // +--------------------+------------+--------------------+--------+---------+

    // 結果の出力
    userRequestWithSessionId
      .write
      .option("encoding", "UTF-8")
      .option("header", "true")
      .mode(SaveMode.Overwrite)
      .csv("./1_sessionize_the_web_log_by_ip")

    val userRequestWithSessionTime = userRequestWithSessionId
      .groupBy('clientIp, 'sessionId)
      .agg(
        max(unix_timestamp('timestamp)) - min(unix_timestamp('timestamp)) as "sessionTime"
      )
      .orderBy('sessionId)
    userRequestWithSessionTime.show()
    userRequestWithSessionTime.where('clientIp === "103.16.71.9").show()
    // +-----------+---------+-----------+
    // |   clientIp|sessionId|sessionTime|
    // +-----------+---------+-----------+
    // |103.16.71.9|        6|        391|
    // |103.16.71.9|        7|        988|
    // |103.16.71.9|        8|        736|
    // |103.16.71.9|        9|          0|
    // |103.16.71.9|       10|          0|
    // |103.16.71.9|       11|          0|
    // +-----------+---------+-----------+

    userRequestWithSessionTime
      .groupBy('clientIp)
      .agg(
        avg('sessionTime)
      )
      .write
      .option("encoding", "UTF-8")
      .option("header", "true")
      .mode(SaveMode.Overwrite)
      .csv("./2_average_session_time")

    userRequestWithSessionId
      .groupBy('clientIp, 'sessionId, 'request)
      .agg(
        count('request)
      )
      .write
      .option("encoding", "UTF-8")
      .option("header", "true")
      .mode(SaveMode.Overwrite)
      .csv("./3_determine_unique_url")

    userRequestWithSessionTime
      .groupBy('clientIp)
      .agg(
        sum('sessionTime) as "sessionTimeSum"
      )
      .write
      .option("encoding", "UTF-8")
      .option("header", "true")
      .mode(SaveMode.Overwrite)
      .csv("./4_most_engaged_users")
  }

}
