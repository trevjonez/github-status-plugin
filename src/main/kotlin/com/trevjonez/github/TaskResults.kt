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

import com.trevjonez.github.GithubStatusTaskResults.Companion.TASK_RESULTS
import com.trevjonez.github.GithubStatusTaskResults.Companion.TASK_RESULTS_PROPERTY
import com.trevjonez.github.statuses.Status
import org.gradle.api.Task
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import java.io.File


abstract class GithubStatusTaskResults(
  val task: Task
) {
  val state: Property<Status.State> =
    task.project.objects.property(Status.State::class.java)

  val description: Property<String> =
    task.project.objects.property(String::class.java)

  companion object {
    const val TASK_RESULTS = "GithubStatusTaskResults"
    const val TASK_RESULTS_PROPERTY = "GithubStatusTaskResultsProperty"
  }
}

fun configureResultsExt(
  testTaskProvider: TaskProvider<*>,
  reportDir: Provider<File>,
  doneProvider: TaskProvider<StatusApiTask>
) {

  testTaskProvider.configure {
    it.configureResultsExt {
      val resultParser = TestResultParser(reportDir)
      state.set(resultParser.stateProvider())
      description.set(resultParser.descriptionProvider())
    }
  }
}

inline fun <T : Task> T.configureResultsExt(
  crossinline action: GithubStatusTaskResults.() -> Unit
) {
  val value = extensions.create(TASK_RESULTS, GithubStatusTaskResults::class.java, this)
  val property = project.objects.property(GithubStatusTaskResults::class.java)
  extensions.add(TASK_RESULTS_PROPERTY, property)
  property.set(value)
  property.finalizeValue()
  value.action()
}

val TaskProvider<out Task>.results: Provider<GithubStatusTaskResults>
  get() = map { it.extensions.getByName(TASK_RESULTS) as GithubStatusTaskResults }