package com.afollestad.nocknock.ui

/** @author Aidan Follestad (@afollestad) */
enum class NightMode {
  /** Night mode is on at the system level. */
  ENABLED,
  /** Night mode is off at the system level. */
  DISABLED,
  /** We don't know about night mode, fallback to custom impl. */
  UNKNOWN
}