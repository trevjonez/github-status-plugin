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
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.tasks.TaskState
import org.gradle.api.tasks.testing.AbstractTestTask
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestListener
import org.gradle.api.tasks.testing.TestResult

class CountingTestListener(testTask: AbstractTestTask) :
  TestListener {
  private var didRun = false

  private var skipped = 0L
  private var failed = 0L
  private var successful = 0L

  private val total: Long
    get() = skipped + failed + successful

  init {
    testTask.doFirst(ACTION_NAME) {
      didRun = true
    }
    testTask.addTestListener(this)
    testTask.project.gradle.addListener(object : TaskExecutionListener {
      override fun beforeExecute(task: Task) {
        if (task === testTask) task.logger.info(this@CountingTestListener.toString())
      }

      override fun afterExecute(task: Task, state: TaskState) {
        if (task === testTask) task.logger.info(this@CountingTestListener.toString())
      }
    })
  }

  fun toState(): Status.State {
    return when {
      !didRun -> Status.State.ERROR
      failed != 0L -> Status.State.FAILURE
      else -> Status.State.SUCCESS
    }
  }

  fun toDescription(): String {
    return when {
      !didRun -> "No tests ran."
      total == 0L -> "No tests found."
      else -> "$successful/$total Passed." +
          (if (failed > 0) " $failed Failed." else "") +
          (if (skipped > 0) " $skipped Skipped." else "")
    }
  }


  override fun beforeSuite(suite: TestDescriptor) {}
  override fun afterSuite(suite: TestDescriptor, result: TestResult) {}
  override fun beforeTest(testDescriptor: TestDescriptor) {}
  override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {
    skipped += result.skippedTestCount
    failed += result.failedTestCount
    successful += result.successfulTestCount
  }

  override fun toString(): String {
    return "CountingTestListener(didRun=$didRun, skipped=$skipped, failed=$failed, successful=$successful) -> ${toState()}: ${toDescription()}"
  }

  companion object {
    const val ACTION_NAME = "GithubStatusTaskResultRunWatcher"
  }
}