package edu.cmu.lti.nlp.amr.GraphDecoder
import edu.cmu.lti.nlp.amr._

import java.io.File
import java.io.FileOutputStream
import java.io.PrintStream
import java.io.BufferedOutputStream
import java.io.OutputStreamWriter
import java.lang.Math.abs
import java.lang.Math.log
import java.lang.Math.exp
import java.lang.Math.random
import java.lang.Math.floor
import java.lang.Math.min
import java.lang.Math.max
import scala.io.Source
import scala.util.matching.Regex
import scala.collection.mutable.Map
import scala.collection.mutable.Set
import scala.collection.mutable.ArrayBuffer
import Double.{NegativeInfinity => minusInfty}

class DualDecomposition(featureNames: List[String], labelSet: Array[(String, Int)], stepsize: Double)
    extends Decoder(List("edgeId")) {
    // Base class has defined:
    // val features: Features

    val alg1 = new Alg1(featureNames, labelSet)
    val alg2 = new Alg2(featureNames, labelSet)

    val multipliers = FeatureVector()

    val regex = "Id1=.*".r
    def IdFeature(feat: String) : Boolean = {
        if (feat.matches("Id1=.*")) {
            true
        } else {
            false
        }
    }

    def decode(input: Input) : DecoderResult = {
        alg1.features.weights = features.weights    // Weights shared across the decoders
        alg2.features.weights = features.weights

        var result = DecoderResult(Graph.empty(), FeatureVector(), 0.0)
        var delta = FeatureVector()
        do {
            logger(1, "weights: \n"+features.weights)
            logger(1, "multipliers: \n"+multipliers.toString)
            features.weights += multipliers
            logger(1, "weights1: \n"+features.weights)
            result = alg1.decode(input)
            logger(1, "features1: \n"+result.features)
            features.weights -= 2.0 * multipliers
            logger(1, "weights2: \n"+features.weights)
            val result2 = alg2.decode(input)
            logger(1, "features2: \n"+result2.features)
            features.weights += multipliers

            delta = result.features.slice(x => IdFeature(x))
            delta -= result2.features.slice(x => IdFeature(x))
            logger(1, "delta: \n"+delta.toString)
            multipliers -= stepsize * delta
        } while (delta.nonzero)

        val feats = result.features.slice(x => !IdFeature(x))
        return DecoderResult(result.graph, feats, features.weights.dot(feats))
    }
}

