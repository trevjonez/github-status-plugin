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

  lateinit var statusConfigExt: StatusConfigurationExt

  val topLevelTriggerProvider by lazy {
    target.tasks.register("withGithubStatuses")
  }

  override fun createConfigExt() {
    statusConfigExt =
      target.extensions.create("GithubStatus", StatusConfigurationExt::class.java)
  }

  fun <T : Task> registerStatusTasks(
    triggerProvider: TaskProvider<*>,
    subjectProvider: TaskProvider<T>,
    configureTaskResultsProperty: (TaskProvider<T>, TaskProvider<StatusApiTask>, TaskProvider<StatusApiTask>) -> Unit
  ) {
    val pendingProvider = target.tasks.register(
      "githubStatusPending${subjectProvider.name.capitalize()}",
      StatusApiTask::class.java
    ) {
      it.group = TASK_GROUP
      it.setDescription("Set github status as pending")
      it.context.set(subjectProvider.map { subject -> subject.path })
      it.state.set(Status.State.PENDING)
      it.description.set(subjectProvider.map { subject -> "${subject.name} is running" })
      //TODO targetUrl
      it.sha.set(statusConfigExt.sha)
      it.setApiConfig(configExtension)
      it.onlyIf {
        target.gradle.startParameter.taskNames.any { requested ->
          requested == it.name ||
              requested == triggerProvider.name ||
              requested == topLevelTriggerProvider.name
        }
      }
    }

    val doneProvider = target.tasks.register(
      "githubStatusDone${subjectProvider.name.capitalize()}",
      StatusApiTask::class.java
    ) {
      it.group = TASK_GROUP
      it.setDescription("Set github status of success or failure")
      it.context.set(subjectProvider.map { subject -> subject.path })
      it.state.set(subjectProvider.results.flatMap { results -> results.state })
      it.description.set(subjectProvider.results.flatMap { results -> results.description })
      //TODO targetUrl
      it.sha.set(statusConfigExt.sha)
      it.setApiConfig(configExtension)

      it.mustRunAfter(pendingProvider)
      it.mustRunAfter(subjectProvider)
      it.onlyIf {
        target.gradle.startParameter.taskNames.any { requested ->
          requested == it.name ||
              requested == triggerProvider.name ||
              requested == topLevelTriggerProvider.name
        }
      }
    }

    subjectProvider.configure { subject ->
      subject.mustRunAfter(pendingProvider)
      subject.finalizedBy(doneProvider)
    }

    triggerProvider.configure {
      it.dependsOn(pendingProvider)
      it.dependsOn(doneProvider)
    }

    configureTaskResultsProperty(subjectProvider, pendingProvider, doneProvider)
  }
}