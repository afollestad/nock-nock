/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.nocknock.utilities.js

import android.util.Log
import com.afollestad.nocknock.utilities.BuildConfig
import org.mozilla.javascript.Context
import org.mozilla.javascript.EvaluatorException
import org.mozilla.javascript.Function

/** @author Aidan Follestad (afollestad) */
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

        log(
            "Evaluated to $message ($success): $code"
        )
        return if (!success) message else null
      } finally {
        Context.exit()
      }
    } catch (e: EvaluatorException) {
      return e.message
    }
  }

  private fun log(message: String) {
    if (BuildConfig.DEBUG) {
      Log.d("JavaScript", message)
    }
  }
}
