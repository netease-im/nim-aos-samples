package com.netease.nim.samples.utils;

import android.util.Log;

public class LogUtils {

    private static LogDelegate sLogDelegate = new LogDelegate() {
        @Override
        public int v(String tag, String msg) {
            return Log.v(tag, msg,null);
        }

        @Override
        public int v(String tag, String msg, Throwable tr) {
            return Log.v(tag, msg,tr);
        }

        @Override
        public int d(String tag, String msg) {
            return Log.d(tag, msg,null);
        }

        @Override
        public int d(String tag, String msg, Throwable tr) {
            return Log.d(tag, msg,tr);
        }

        @Override
        public int i(String tag, String msg) {
            return Log.i(tag, msg,null);
        }

        @Override
        public int i(String tag, String msg, Throwable tr) {
            return Log.i(tag, msg,tr);
        }

        @Override
        public int w(String tag, String msg) {
            return Log.w(tag, msg,null);
        }

        @Override
        public int w(String tag, String msg, Throwable tr) {
            return Log.w(tag, msg,tr);
        }

        @Override
        public int w(String tag, Throwable tr) {
            return Log.w(tag, null,tr);
        }

        @Override
        public int e(String tag, String msg) {
            return Log.e(tag, msg,null);
        }

        @Override
        public int e(String tag, String msg, Throwable tr) {
            return Log.e(tag, msg,tr);
        }
    };

    public static void setLogDelegate(LogDelegate delegate) {
        sLogDelegate = delegate;
    }
    /**
     * Send a {@link Log#VERBOSE} log message.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int v(String tag, String msg) {
        if(sLogDelegate != null) {
            return sLogDelegate.v(tag, msg);
        }
        return Log.d(tag, msg);
    }

    /**
     * Send a {@link Log#VERBOSE} log message and log the exception.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static int v(String tag, String msg, Throwable tr) {
        if(sLogDelegate != null) {
            return sLogDelegate.v(tag, msg, tr);
        }
        return Log.v(tag, msg, tr);
    }

    /**
     * Send a {@link Log#DEBUG} log message.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int d(String tag, String msg) {
        if (sLogDelegate != null) {
            return sLogDelegate.d(tag, msg);
        }
        return Log.d(tag, msg);
    }

    /**
     * Send a {@link Log#DEBUG} log message and log the exception.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static int d(String tag, String msg, Throwable tr) {
        if (sLogDelegate != null) {
            return sLogDelegate.d(tag, msg, tr);
        }
        return Log.d(tag, msg, tr);
    }

    /**
     * Send an {@link Log#INFO} log message.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int i(String tag, String msg) {
        if (sLogDelegate != null) {
            return sLogDelegate.i(tag, msg);
        }
        return Log.i(tag, msg);
    }

    /**
     * Send a {@link Log#INFO} log message and log the exception.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static int i(String tag, String msg, Throwable tr) {
        if (sLogDelegate != null) {
            return sLogDelegate.i(tag, msg, tr);
        }
        return Log.i(tag, msg, tr);
    }

    /**
     * Send a {@link Log#WARN} log message.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int w(String tag, String msg) {
        if (sLogDelegate != null) {
            return sLogDelegate.w(tag, msg);
        }
        return Log.w(tag, msg);
    }

    /**
     * Send a {@link Log#WARN} log message and log the exception.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static int w(String tag, String msg, Throwable tr) {
        if (sLogDelegate != null) {
            return sLogDelegate.w(tag, msg, tr);
        }
        return Log.w(tag, msg, tr);
    }

    /*
     * Send a {@link #WARN} log message and log the exception.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param tr An exception to log
     */
    public static int w(String tag, Throwable tr) {
        if (sLogDelegate != null) {
            return sLogDelegate.w(tag, tr);
        }
        return Log.w(tag, tr);
    }

    /**
     * Send an {@link Log#ERROR} log message.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static int e(String tag, String msg) {
        if (sLogDelegate != null) {
            return sLogDelegate.e(tag, msg);
        }
        return Log.e(tag, msg);
    }

    /**
     * Send a {@link Log#ERROR} log message and log the exception.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static int e(String tag, String msg, Throwable tr) {
        if (sLogDelegate != null) {
            return sLogDelegate.e(tag, msg, tr);
        }
        return Log.e(tag, msg, tr);
    }


    public interface LogDelegate {
        /**
         * Send a {@link android.util.Log#VERBOSE} log message.
         * @param tag Used to identify the source of a log message.  It usually identifies
         *        the class or activity where the log call occurs.
         * @param msg The message you would like logged.
         */
        int v(String tag, String msg);

        /**
         * Send a {@link android.util.Log#VERBOSE} log message and log the exception.
         * @param tag Used to identify the source of a log message.  It usually identifies
         *        the class or activity where the log call occurs.
         * @param msg The message you would like logged.
         * @param tr An exception to log
         */
        int v(String tag, String msg, Throwable tr);

        /**
         * Send a {@link android.util.Log#DEBUG} log message.
         * @param tag Used to identify the source of a log message.  It usually identifies
         *        the class or activity where the log call occurs.
         * @param msg The message you would like logged.
         */
        int d(String tag, String msg);

        /**
         * Send a {@link android.util.Log#DEBUG} log message and log the exception.
         * @param tag Used to identify the source of a log message.  It usually identifies
         *        the class or activity where the log call occurs.
         * @param msg The message you would like logged.
         * @param tr An exception to log
         */
        int d(String tag, String msg, Throwable tr);

        /**
         * Send an {@link android.util.Log#INFO} log message.
         * @param tag Used to identify the source of a log message.  It usually identifies
         *        the class or activity where the log call occurs.
         * @param msg The message you would like logged.
         */
        int i(String tag, String msg);

        /**
         * Send a {@link android.util.Log#INFO} log message and log the exception.
         * @param tag Used to identify the source of a log message.  It usually identifies
         *        the class or activity where the log call occurs.
         * @param msg The message you would like logged.
         * @param tr An exception to log
         */
        int i(String tag, String msg, Throwable tr);

        /**
         * Send a {@link android.util.Log#WARN} log message.
         * @param tag Used to identify the source of a log message.  It usually identifies
         *        the class or activity where the log call occurs.
         * @param msg The message you would like logged.
         */
        int w(String tag, String msg);

        /**
         * Send a {@link android.util.Log#WARN} log message and log the exception.
         * @param tag Used to identify the source of a log message.  It usually identifies
         *        the class or activity where the log call occurs.
         * @param msg The message you would like logged.
         * @param tr An exception to log
         */
        int w(String tag, String msg, Throwable tr);

        /*
         * Send a {@link android.util.Log#WARN} log message and log the exception.
         * @param tag Used to identify the source of a log message.  It usually identifies
         *        the class or activity where the log call occurs.
         * @param tr An exception to log
         */
        int w(String tag, Throwable tr);

        /**
         * Send an {@link android.util.Log#ERROR} log message.
         * @param tag Used to identify the source of a log message.  It usually identifies
         *        the class or activity where the log call occurs.
         * @param msg The message you would like logged.
         */
        int e(String tag, String msg);

        /**
         * Send a {@link android.util.Log#ERROR} log message and log the exception.
         * @param tag Used to identify the source of a log message.  It usually identifies
         *        the class or activity where the log call occurs.
         * @param msg The message you would like logged.
         * @param tr An exception to log
         */
        int e(String tag, String msg, Throwable tr);
    }

}
