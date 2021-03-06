// Copyright (c) 2019 The DAML Authors. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package com.daml.ledger.api.testtool.infrastructure

import java.io.PrintStream

import scala.util.Try

trait Reporter[A] {
  def report(results: Vector[LedgerTestSummary]): A
}

object Reporter {

  object ColorizedPrintStreamReporter {

    private val reset = "\u001b[0m"

    private def red(s: String): String = s"\u001b[31m$s$reset"
    private def green(s: String): String = s"\u001b[32m$s$reset"
    private def yellow(s: String): String = s"\u001b[33m$s$reset"
    private def blue(s: String): String = s"\u001b[34m$s$reset"
    private def cyan(s: String): String = s"\u001b[36m$s$reset"

    private def render(configuration: LedgerSessionConfiguration): String =
      s"address: ${configuration.host}:${configuration.port}, ssl: ${configuration.ssl.isDefined}"

    private def render(t: Throwable): Seq[String] =
      s"${t.getClass.getName}: ${t.getMessage}" +: t.getStackTrace.map(render)

    private def render(e: StackTraceElement): String =
      s"\tat ${e.getClassName}.${e.getMethodName}(${e.getFileName}:${e.getLineNumber})"

    private def extractRelevantLineNumberFromAssertionError(
        assertionError: AssertionError): Option[Int] =
      assertionError.getStackTrace
        .find(
          stackTraceElement =>
            Try(Class.forName(stackTraceElement.getClassName))
              .filter(classOf[LedgerTestSuite].isAssignableFrom)
              .isSuccess)
        .map(_.getLineNumber)

  }

  final class ColorizedPrintStreamReporter(s: PrintStream, printStackTraces: Boolean)
      extends Reporter[Unit] {

    import ColorizedPrintStreamReporter._

    private def indented(n: Int, msg: String) = {
      val indent = " " * n
      msg.lines.map(l => s"$indent$l").mkString("\n")
    }

    override def report(results: Vector[LedgerTestSummary]): Unit = {

      s.println()
      s.println(blue("#" * 80))
      s.println(blue("#"))
      s.println(blue("# TEST REPORT"))
      s.println(blue("#"))
      s.println(blue("#" * 80))

      results.groupBy(_.suite).foreach {
        case (suite, summaries) =>
          s.println()
          s.println(cyan(suite))

          for (LedgerTestSummary(_, test, _, result) <- summaries) {

            s.print(cyan(s"- $test ... "))
            result match {
              case Result.Succeeded => s.println(green(s"Success"))
              case Result.TimedOut => s.println(red(s"Timeout"))
              case Result.Skipped(reason) =>
                s.println(yellow(s"Skipped (reason: $reason)"))
              case Result.Failed(cause) =>
                val message =
                  extractRelevantLineNumberFromAssertionError(cause).fold(s"Failed") { lineHint =>
                    s"Failed at line $lineHint"
                  }
                s.println(red(message))
                s.println(red(indented(2, cause.getMessage)))
                if (printStackTraces) {
                  for (renderedStackTraceLine <- render(cause))
                    s.println(red(indented(2, renderedStackTraceLine)))
                }
              case Result.FailedUnexpectedly(cause) =>
                s.println(red("Failed due to an unexpected exception"))
                s.println(red(indented(2, cause.getMessage)))
                if (printStackTraces) {
                  for (renderedStackTraceLine <- render(cause))
                    s.println(red(indented(2, renderedStackTraceLine)))
                }
            }

          }
      }
    }
  }

}
