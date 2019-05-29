package io.sdkman

import java.util.concurrent.Executors

import scala.concurrent.ExecutionContext
import scala.language.implicitConversions

package object repos {
  implicit val mongoExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(10))
}

