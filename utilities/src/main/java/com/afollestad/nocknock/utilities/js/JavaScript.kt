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
package com.afollestad.nocknock.utilities.js

import org.mozilla.javascript.Context
import org.mozilla.javascript.EvaluatorException
import org.mozilla.javascript.Function

/** @author Aidan Follestad (@afollestad) */
object JavaScript {

  fun eval(
    code: String,
    response: String
  ): String? {
    try {
      val func = String.format(
          "function validate(response) { " +
              "try { " +
              "%s " +
              "} catch(e) { " +
              "return e; " +
              "} " +
              "}",
          code.replace("\n", " ")
      )

      // Every Rhino VM begins with the enter()
      // This Context is not Android's Context
      val rhino = Context.enter()

      // Turn off optimization to make Rhino Android compatible
      rhino.optimizationLevel = -1
      try {
        val scope = rhino.initStandardObjects()

        // Note the forth argument is 1, which means the JavaScript source has
        // been compressed to only one line using something like YUI
        rhino.evaluateString(scope, func, "JavaScript", 1, null)

        // Get the functionName defined in JavaScriptCode
        val jsFunction = scope.get("validate", scope) as Function

        // Call the function with params
        val jsResult = jsFunction.call(rhino, scope, scope, arrayOf<Any>(response))

        // Parse the jsResult object to a String
        val result = Context.toString(jsResult)

        val success = result != null && result == "true"
        var message = "The script returned a value other than true!"
        if (!success && result != null && result != "false") {
          message = if (result == "undefined") {
            "The script did not return or throw anything!"
          } else {
            result
          }
        }

        return if (!success) message else null
      } finally {
        Context.exit()
      }
    } catch (e: EvaluatorException) {
      return e.message
    }
  }
}
