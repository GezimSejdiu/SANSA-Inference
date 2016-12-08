package net.sansa_stack.inference.spark.data

import java.io.File

import org.apache.spark.SparkContext
import org.apache.spark.sql.SparkSession
import net.sansa_stack.inference.data.RDFTriple
import org.apache.spark.rdd.RDD
import org.slf4j.LoggerFactory

/**
  * Loads an RDF graph from disk or a set of triples.
  *
  * @author Lorenz Buehmann
  *
  */
object RDFGraphLoader {

  private val logger = com.typesafe.scalalogging.Logger(LoggerFactory.getLogger(this.getClass.getName))

  def loadFromFile(path: String, sc: SparkContext, minPartitions: Int = 2): RDFGraph = {
    logger.info("loading triples from disk...")
    val startTime  = System.currentTimeMillis()

    val triples =
      sc.textFile(path, minPartitions)
        .map(line => line.replace(">", "").replace("<", "").split("\\s+")) // line to tokens
        .map(tokens => RDFTriple(tokens(0), tokens(1), tokens(2))) // tokens to triple

//    logger.info("finished loading " + triples.count() + " triples in " + (System.currentTimeMillis()-startTime) + "ms.")
    new RDFGraph(triples)
  }

  def loadFromDisk(paths: Seq[File], sc: SparkContext, minPartitions: Int = 2): RDFGraph = {
    logger.info("loading triples from disk...")
    val startTime  = System.currentTimeMillis()

    val triples = sc.textFile(paths.map(p => p.getAbsolutePath).mkString(","))
      .map(line => line.replace(">", "").replace("<", "").split("\\s+")) // line to tokens
       .map(tokens => RDFTriple(tokens(0), tokens(1), tokens(2))).cache() // tokens to triple

    //    logger.info("finished loading " + triples.count() + " triples in " + (System.currentTimeMillis()-startTime) + "ms.")
    new RDFGraph(triples)
  }

  def loadGraphFromFile(path: String, session: SparkSession, minPartitions: Int = 2): RDFGraphNative = {
    logger.info("loading triples from disk...")
    val startTime  = System.currentTimeMillis()

    val triples =
      session.sparkContext.textFile(path, minPartitions)
        .map(line => line.replace(">", "").replace("<", "").split("\\s+")) // line to tokens
        .map(tokens => RDFTriple(tokens(0), tokens(1), tokens(2))) // tokens to triple

//    logger.info("finished loading " + triples.count() + " triples in " + (System.currentTimeMillis()-startTime) + "ms.")
    new RDFGraphNative(triples)
  }

  def loadGraphDataFrameFromFile(path: String, session: SparkSession, minPartitions: Int = 2): RDFGraphDataFrame = {
    new RDFGraphDataFrame(loadGraphFromFile(path, session, minPartitions).toDataFrame(session))
  }
}
