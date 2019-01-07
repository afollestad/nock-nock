/**
 * Designed and developed by Aidan Follestad (@afollestad)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.afollestad.nocknock.data

import com.afollestad.nocknock.data.model.Site
import com.afollestad.nocknock.data.model.SiteSettings
import com.afollestad.nocknock.data.model.Status
import com.afollestad.nocknock.data.model.Status.OK
import com.afollestad.nocknock.data.model.ValidationMode
import com.afollestad.nocknock.data.model.ValidationMode.STATUS_CODE
import com.afollestad.nocknock.data.model.ValidationResult
import java.lang.System.currentTimeMillis

fun fakeSettingsModel(
  id: Long,
  validationMode: ValidationMode = STATUS_CODE
) = SiteSettings(
    siteId = id,
    validationIntervalMs = 600000,
    validationMode = validationMode,
    validationArgs = null,
    disabled = false,
    networkTimeout = 10000
)

fun fakeResultModel(
  id: Long,
  status: Status = OK,
  reason: String? = null
) = ValidationResult(
    siteId = id,
    status = status,
    reason = reason,
    timestampMs = currentTimeMillis()
)

fun fakeRetryPolicy(
  id: Long,
  count: Int = 3,
  minutes: Int = 6
) = RetryPolicy(
    siteId = id,
    count = count,
    minutes = minutes
)

fun fakeModel(id: Long) = Site(
    id = id,
    name = "Test",
    url = "https://test.com",
    settings = fakeSettingsModel(id),
    lastResult = fakeResultModel(id),
    retryPolicy = fakeRetryPolicy(id)
)

val MOCK_MODEL_1 = fakeModel(1)
val MOCK_MODEL_2 = fakeModel(2)
val MOCK_MODEL_3 = fakeModel(3)
