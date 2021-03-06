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

import org.gradle.api.tasks.testing.Test

abstract class JavaStatusPlugin : AbsStatusPlugin() {
  override fun registerTasks() {
    val testProvider = target.tasks.named("test", Test::class.java)

    val triggerProvider = target.tasks.register("withTestGithubStatuses")
    topLevelTriggerProvider.configure { it.dependsOn(triggerProvider) }

    registerStatusTasks(triggerProvider, testProvider) { testTask, _, doneProvider ->
      configureResultsExt(
        testTask,
        testTask.map { it.reports.junitXml.destination },
        doneProvider
      )
    }
  }
}