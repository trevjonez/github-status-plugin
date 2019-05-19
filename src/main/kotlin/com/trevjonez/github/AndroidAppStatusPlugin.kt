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

import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.tasks.DeviceProviderInstrumentTestTask
import com.android.build.gradle.tasks.LintGlobalTask
import com.android.build.gradle.tasks.LintPerVariantTask
import com.android.build.gradle.tasks.factory.AndroidUnitTest

abstract class AndroidAppStatusPlugin : AbsStatusPlugin() {
  override fun registerTasks() {
    target.extensions.getByType(AppExtension::class.java)
      .applicationVariants.configureEach { variant ->

      val unitTestProvider = target.tasks.named(
        "test${variant.name.capitalize()}UnitTest",
        AndroidUnitTest::class.java
      )

      registerStatusTasks(unitTestProvider) { unitTest ->
        configureResultsExt(unitTest)
      }

      val androidTestProvider = target.tasks.named(
        "connected${variant.name.capitalize()}AndroidTest",
        DeviceProviderInstrumentTestTask::class.java
      )

      registerStatusTasks(androidTestProvider) { instrumentationTest ->
        instrumentationTest.configureResultsExt {

        }
      }

      val lintTaskProvider = target.tasks.named(
        "lint${variant.name.capitalize()}",
        LintPerVariantTask::class.java
      )

      registerStatusTasks(lintTaskProvider) { lint ->
        lint.configureResultsExt {

        }
      }
    }

    val globalLintTaskProvider =
      target.tasks.named("lint", LintGlobalTask::class.java)

    registerStatusTasks(globalLintTaskProvider) { lint ->
      lint.configureResultsExt {

      }
    }
  }
}