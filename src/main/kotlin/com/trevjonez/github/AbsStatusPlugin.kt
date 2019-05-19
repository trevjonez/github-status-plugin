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

import com.trevjonez.github.gradle.GithubApiPlugin
import com.trevjonez.github.statuses.Status
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider

abstract class AbsStatusPlugin : GithubApiPlugin() {

  val triggerTaskProvider: TaskProvider<*> by lazy {
    target.tasks.register("withGithubStatusReporting")
  }

  fun <T : Task> registerStatusTasks(
    subjectProvider: TaskProvider<T>,
    configureTaskResultsProperty: (T) -> Unit
  ) {
    subjectProvider.configure(configureTaskResultsProperty)

    val pendingProvider = target.tasks.register(
      "githubStatusPending${subjectProvider.name.capitalize()}",
      StatusApiTask::class.java
    ) {
      val subjectTask = subjectProvider.get()

      it.group = TASK_GROUP
      it.setDescription("Set github status as pending")
      it.context.set(subjectTask.path)
      it.state.set(Status.State.PENDING)
      it.description.set("${subjectTask.name} is running")
      //TODO targetUrl
      it.onlyIf { task -> task.project.gradle.taskGraph.hasTask(triggerTaskProvider.get()) }

      it.finalizedBy(subjectTask)
      subjectTask.dependsOn(it)
    }

    val doneProvider = target.tasks.register(
      "githubStatusDone${subjectProvider.name.capitalize()}",
      StatusApiTask::class.java
    ) {
      val subjectTask = subjectProvider.get()
      it.group = TASK_GROUP
      it.setDescription("Set github status of success or failure")
      it.context.set(subjectTask.path)
      it.state.set(subjectProvider.results.state)
      it.description.set(subjectProvider.results.description)
      //TODO targetUrl
      it.onlyIf { task -> task.project.gradle.taskGraph.hasTask(triggerTaskProvider.get()) }
      subjectTask.finalizedBy(it)
    }

    pendingProvider.configure {
      it.finalizedBy(doneProvider)
    }
    doneProvider.configure {
      it.dependsOn(pendingProvider)
    }
  }
}