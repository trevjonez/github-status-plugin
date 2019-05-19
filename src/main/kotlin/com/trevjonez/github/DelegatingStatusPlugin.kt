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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin

abstract class DelegatingStatusPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    //TODO add plugin interfaces for CI provider auto config helpers
    // IE: apply plugin: `com.trevjonez.github-status.bitrise`
    // would automatically configure properties for URL's and credentials

    target.plugins.withId("com.android.application") {
      target.pluginManager.apply(AndroidAppStatusPlugin::class.java)
    }

    target.plugins.withType(JavaBasePlugin::class.java) {
      target.pluginManager.apply(JavaStatusPlugin::class.java)
    }
  }
}
