package com.seagazer.ble

import android.util.Log

/**
 * Author: Seagazer
 * Date: 2020/4/25
 */
class Logger {

    companion object {
        private const val TAG = "Seagazer"
        private var DEBUG = true

        fun i(msg: String) {
            if (DEBUG) {
                val stackTrace = Thread.currentThread().stackTrace
                if (stackTrace.size > 4) {
                    val e = stackTrace[3]
                    Log.i(TAG, "${e.className} # ${e.methodName}[Line:${e.lineNumber}]: $msg")
                }
            }
        }

        fun d(msg: String) {
            if (DEBUG) {
                val stackTrace = Thread.currentThread().stackTrace
                if (stackTrace.size > 4) {
                    val e = stackTrace[3]
                    Log.d(TAG, "${e.className} # ${e.methodName}[Line:${e.lineNumber}]: $msg")
                }
            }
        }

        fun w(msg: String) {
            if (DEBUG) {
                val stackTrace = Thread.currentThread().stackTrace
                if (stackTrace.size > 4) {
                    val e = stackTrace[3]
                    Log.w(TAG, "${e.className} # ${e.methodName}[Line:${e.lineNumber}]: $msg")
                }
            }
        }

        fun e(msg: String) {
            if (DEBUG) {
                val stackTrace = Thread.currentThread().stackTrace
                if (stackTrace.size > 4) {
                    val e = stackTrace[3]
                    Log.e(TAG, "${e.className} # ${e.methodName}[Line:${e.lineNumber}]: $msg")
                }
            }
        }

        fun i(tag: String, msg: String) {
            if (DEBUG) {
                val stackTrace = Thread.currentThread().stackTrace
                if (stackTrace.size > 4) {
                    val e = stackTrace[3]
                    Log.i(tag, "${e.className} # ${e.methodName}[Line:${e.lineNumber}]: $msg")
                }
            }
        }

        fun d(tag: String, msg: String) {
            if (DEBUG) {
                val stackTrace = Thread.currentThread().stackTrace
                if (stackTrace.size > 4) {
                    val e = stackTrace[3]
                    Log.d(tag, "${e.className} # ${e.methodName}[Line:${e.lineNumber}]: $msg")
                }
            }
        }

        fun w(tag: String, msg: String) {
            if (DEBUG) {
                val stackTrace = Thread.currentThread().stackTrace
                if (stackTrace.size > 4) {
                    val e = stackTrace[3]
                    Log.w(tag, "${e.className} # ${e.methodName}[Line:${e.lineNumber}]: $msg")
                }
            }
        }

        fun e(tag: String, msg: String) {
            if (DEBUG) {
                val stackTrace = Thread.currentThread().stackTrace
                if (stackTrace.size > 4) {
                    val e = stackTrace[3]
                    Log.e(tag, "${e.className} # ${e.methodName}[Line:${e.lineNumber}]: $msg")
                }
            }
        }

    }


}