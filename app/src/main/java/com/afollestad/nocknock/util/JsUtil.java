package com.afollestad.nocknock.util;

import android.util.Log;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

/** @author Aidan Follestad (afollestad) */
public class JsUtil {

  public static String exec(String code, String response) {
    try {
      final String func =
          String.format(
              "function validate(response) { "
                  + "try { "
                  + "%s "
                  + "} catch(e) { "
                  + "return e; "
                  + "} "
                  + "}",
              code.replace("\n", " "));

      // Every Rhino VM begins with the enter()
      // This Context is not Android's Context
      Context rhino = Context.enter();

      // Turn off optimization to make Rhino Android compatible
      rhino.setOptimizationLevel(-1);
      try {
        Scriptable scope = rhino.initStandardObjects();

        // Note the forth argument is 1, which means the JavaScript source has
        // been compressed to only one line using something like YUI
        rhino.evaluateString(scope, func, "JavaScript", 1, null);

        // Get the functionName defined in JavaScriptCode
        Function jsFunction = (Function) scope.get("validate", scope);

        // Call the function with params
        Object jsResult = jsFunction.call(rhino, scope, scope, new Object[] {response});

        // Parse the jsResult object to a String
        String result = Context.toString(jsResult);

        boolean success = result != null && result.equals("true");
        String message = "The script returned a value other than true!";
        if (!success && result != null && !result.equals("false")) {
          if (result.equals("undefined")) {
            message = "The script did not return or throw anything!";
          } else {
            message = result;
          }
        }

        Log.d("JsUtil", "Evaluated to " + message + " (" + success + "): " + code);
        return !success ? message : null;
      } finally {
        Context.exit();
      }
    } catch (EvaluatorException e) {
      return e.getMessage();
    }
  }

  private JsUtil() {}
}
