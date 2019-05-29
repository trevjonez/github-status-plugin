/*
 *    Copyright 2019 Trevor Jones
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.trevjonez.github

import com.trevjonez.github.statuses.Status
import groovy.util.XmlSlurper
import groovy.util.slurpersupport.GPathResult
import groovy.util.slurpersupport.NodeChild
import org.gradle.api.provider.Provider
import java.io.File

class TestResultParser(reportDir: Provider<File>) {

  private val results: Provider<TotalResults> =
    reportDir.map { dir ->
      val fileResults =
        dir.listFiles()
          ?.filter { it.name.endsWith(".xml") }
          ?.map {
            val testSuite = XmlSlurper().parse(it)
            FileResults(
              testSuite["tests"].toLong(),
              testSuite["skipped"].toLong(),
              testSuite["failures"].toLong(),
              testSuite["errors"].toLong()
            )
          }
      TotalResults(fileResults ?: emptyList())
    }

  private interface Results {
    val tests: Long
    val skipped: Long
    val failures: Long
    val errors: Long

    val passed: Long
      get() = tests - skipped - failures - errors
  }

  private data class FileResults(
    override val tests: Long,
    override val skipped: Long,
    override val failures: Long,
    override val errors: Long
  ) : Results

  private class TotalResults(val files: List<FileResults>) : Results {
    override val tests: Long by sumOf { tests }
    override val skipped: Long by sumOf { skipped }
    override val failures: Long by sumOf { failures }
    override val errors: Long by sumOf { errors }

    private inline fun sumOf(crossinline getter: FileResults.() -> Long) = lazy {
      files.fold(0L) { acc, file -> acc + file.getter() }
    }
  }

  fun stateProvider(): Provider<Status.State> {
    return results.map {
      when {
        it.failures > 0 -> Status.State.FAILURE
        it.errors > 0 -> Status.State.ERROR
        else -> Status.State.SUCCESS
      }
    }
  }

  fun descriptionProvider(): Provider<String> {
    return results.map {
      it.run {
        when (tests) {
          0L -> "No test results found."
          else -> "$passed/$tests Passed." +
              (if (failures > 0) " $failures Failed." else "") +
              (if (errors > 0) " $errors Errored." else "") +
              (if (skipped > 0) " $skipped Skipped." else "")
        }
      }
    }
  }
}

private operator fun GPathResult.get(property: String): String {
  this as NodeChild
  return attributes()[property] as String
}
