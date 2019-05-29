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

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.File
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

@RunWith(JUnit4::class)
class DelegatingStatusPluginJavaLibTest {

  private val javaLib by systemProperty()

  @get:Rule
  val testProjectDir = TemporaryFolder()

  private fun prepProjectDir() = testProjectDir.newFolder().apply {
    javaLib.copyRecursively(this, true)
  }

  private fun gradleRunner(projectDir: File, vararg args: String): GradleRunner {
    val argList = args.toMutableList().apply {
      add("--stacktrace")
      add("--scan")
    }

    return GradleRunner.create()
      .withProjectDir(projectDir)
      .withPluginClasspath()
      .withArguments(argList)
      .forwardOutput()
  }

  @Test
  fun `pending and done task automatically added to java project`() {
    val projectDir = prepProjectDir()
    val result = gradleRunner(projectDir, "tasks").build()

    assertThat(result.output).contains(
      """
        Github Api tasks
        ----------------
        githubStatusDoneTest - Set github status of success or failure
        githubStatusPendingTest - Set github status as pending
        """.trimIndent()
    )
  }

  @Test
  fun `test task does not require pending task be run in the same invocation`() {
    val projectDir = prepProjectDir()
    val result = gradleRunner(projectDir, "test").buildAndFail()

    assertThat(result.tasks)
      .extracting { it.path }
      .doesNotContain(":githubStatusPendingTest")
  }

  @Test
  fun `pending task does not require done tas be run in the same invocation`() {
    val projectDir = prepProjectDir()
    val result = gradleRunner(projectDir, "githubStatusPendingTest")
      .build()

    assertThat(result.tasks)
      .extracting { it.path }
      .doesNotContain(":githubStatusDoneTest")
  }

  @Test
  fun `done task does not require pending task be run in the same invocation`() {
    val projectDir = prepProjectDir()
    val result = gradleRunner(projectDir, "githubStatusDoneTest")
      .build()

    assertThat(result.tasks)
      .extracting { it.path }
      .doesNotContain(":githubStatusPendingTest")
  }

  @Test
  fun `done task skipped if not explicitly invoked`() {
    val projectDir = prepProjectDir()
    val result = gradleRunner(projectDir, "test")
      .buildAndFail()

    assertThat(result.task(":githubStatusDoneTest")!!.outcome)
      .isEqualTo(TaskOutcome.SKIPPED)
  }

  private fun systemProperty(): ReadOnlyProperty<Any, File> {
    return object : ReadOnlyProperty<Any, File> {
      override fun getValue(thisRef: Any, property: KProperty<*>): File {
        return File(System.getProperty(property.name))
      }
    }
  }
}