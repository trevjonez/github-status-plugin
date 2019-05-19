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

import com.trevjonez.github.gradle.GithubApiTask
import com.trevjonez.github.statuses.Status
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import retrofit2.HttpException

abstract class StatusApiTask : GithubApiTask() {

  @get:Input
  val sha: Property<String> = property()

  @get:Input
  val context: Property<String> = property()

  @get:Input
  val state: Property<Status.State> = property()

  @get:Input
  @get:Optional
  val targetUrl: Property<String> = property()

  @get:Input
  @get:Optional
  val description: Property<String> = property()

  private val statusApi by githubApi<Status.Api>()

  @TaskAction
  fun run() {
    val response = statusApi.create(
      owner.get(),
      repo.get(),
      sha.get(),
      "token ${authToken.get()}",
      Status.Request(
        state.get(),
        targetUrl.orNull,
        description.orNull,
        context.get()
      )
    ).execute()

    if (!response.isSuccessful) {
      throw HttpException(response)
    }
  }

  private inline fun <reified T : Any> property() =
    project.objects.property(T::class.java)
}