package com.typesafe.tools.mima.core

import scala.util.Try

object ProblemReporting {
  private[mima] val versionOrdering: Ordering[String] = {
    // version string "x.y.z" is converted to an Int tuple (x, y, z) for comparison
    val VersionRegex = """(\d+)\.?(\d+)?\.?(.*)?""".r
    def int(versionPart: String) =
      Try(versionPart.replace("x", Short.MaxValue.toString).filter(_.isDigit).toInt).getOrElse(0)
    Ordering[(Int, Int, Int)].on[String] { case VersionRegex(x, y, z) => (int(x), int(y), int(z)) }
  }

  private[mima] def isReported(
      version: String,
      filters: Seq[ProblemFilter],
      versionedFilters: Map[String, Seq[ProblemFilter]]
  )(problem: Problem): Boolean = {
    val versionMatchingFilters = versionedFilters
      // get all filters that apply to given module version or any version after it
      .collect { case (version2, filters) if versionOrdering.gteq(version2, version) => filters }
      .flatten

    (versionMatchingFilters.iterator ++ filters).forall(filter => filter(problem))
  }
}
