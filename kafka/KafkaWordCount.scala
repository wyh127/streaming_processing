package org.apache.spark.examples.streaming
import org.apache.spark._
import org.apache.spark.SparkConf
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka._
import org.apache.spark.streaming.StreamingContext._
import org.apache.spark.streaming.kafka.KafkaUtils

object KafkaWordCount{
  def main(args:Array[String]){
    StreamingExamples.setStreamingLogLevels()
    val sc = new SparkConf().setAppName("KafkaWordCount").setMaster("local[2]")
    val ssc = new StreamingContext(sc,Seconds(10))

    ssc.checkpoint("file:///Users/apple/spark-2.1.0/mycode/kafka/checkpoint")
    
    val zkQuorum = "localhost:2181" 
    val group = "1"  //topic group
    val topics = "wordsender"           
    val numThreads = 1  //topic分区数
    val topicMap =topics.split(",").map((_,numThreads.toInt)).toMap
    val lineMap = KafkaUtils.createStream(ssc,zkQuorum,group,topicMap)
    val lines = lineMap.map(_._2)
    val words = lines.flatMap(_.split(" "))
    val pair = words.map(x => (x,1))
    val wordCounts = pair.reduceByKeyAndWindow(_ + _,_ - _,Minutes(2),Seconds(10),2)
    
    wordCounts.print
    ssc.start
    ssc.awaitTermination
  }
}      
