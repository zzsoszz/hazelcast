/*
 * Copyright (c) 2008-2025, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.internal.util;

import com.hazelcast.core.HazelcastException;
import com.hazelcast.core.MemberLeftException;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import com.hazelcast.spi.annotation.PrivateApi;
import com.hazelcast.spi.impl.InternalCompletableFuture;
import com.hazelcast.transaction.TransactionTimedOutException;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

import static com.hazelcast.internal.util.ExceptionUtil.rethrow;

/**
 * This utility class contains convenience methods to work with multiple
 * futures at the same time, e.g.
 * {@link #waitWithDeadline(java.util.Collection, long, java.util.concurrent.TimeUnit, long, java.util.concurrent.TimeUnit)}
 */
@SuppressWarnings("checkstyle:methodcount")
public final class FutureUtil {

    /**
     * Just rethrows <b>all</b> exceptions
     */
    public static final ExceptionHandler RETHROW_EVERYTHING = throwable -> {
        throw rethrow(throwable);
    };

    /**
     * Ignores <b>all</b> exceptions
     */
    public static final ExceptionHandler IGNORE_ALL_EXCEPTIONS = throwable -> {
    };

    /**
     * Ignores all exceptions but still logs {@link com.hazelcast.core.MemberLeftException} per future and just tries
     * to finish all the given ones. This is the default behavior if nothing else is given.
     */
    public static final ExceptionHandler IGNORE_ALL_EXCEPT_LOG_MEMBER_LEFT = new ExceptionHandler() {
        @Override
        public void handleException(Throwable throwable) {
            if (throwable instanceof MemberLeftException) {
                if (LOGGER.isFinestEnabled()) {
                    LOGGER.finest("Member left while waiting for futures...", throwable);
                }
            }
        }
    };

    /**
     * This ExceptionHandler rethrows {@link java.util.concurrent.ExecutionException}s and logs
     * {@link com.hazelcast.core.MemberLeftException}s to the log.
     */
    public static final ExceptionHandler RETHROW_EXECUTION_EXCEPTION = new ExceptionHandler() {
        @Override
        public void handleException(Throwable throwable) {
            if (throwable instanceof MemberLeftException) {
                if (LOGGER.isFinestEnabled()) {
                    LOGGER.finest("Member left while waiting for futures...", throwable);
                }
            } else if (throwable instanceof ExecutionException) {
                throw new HazelcastException(throwable);
            }
        }
    };

    /**
     * This ExceptionHandler rethrows {@link java.util.concurrent.ExecutionException}s and logs
     * {@link com.hazelcast.core.MemberLeftException}s to the log.
     */
    public static final ExceptionHandler RETHROW_ALL_EXCEPT_MEMBER_LEFT = new ExceptionHandler() {
        @Override
        public void handleException(Throwable throwable) {
            if (throwable instanceof MemberLeftException) {
                if (LOGGER.isFinestEnabled()) {
                    LOGGER.finest("Member left while waiting for futures...", throwable);
                }
            } else {
                throw new HazelcastException(throwable);
            }
        }
    };

    private static final class CollectAllExceptionHandler implements ExceptionHandler {

        private List<Throwable> throwables;

        private CollectAllExceptionHandler(int count) {
            this.throwables = Collections.synchronizedList(new ArrayList<>(count));
        }

        @Override
        public void handleException(Throwable throwable) {
            throwables.add(throwable);
        }

        public List<Throwable> getThrowables() {
            return throwables;
        }
    }

    /**
     * Handler for transaction specific rethrown of exceptions.
     */
    public static final ExceptionHandler RETHROW_TRANSACTION_EXCEPTION = throwable -> {
        if (throwable instanceof TimeoutException) {
            throw new TransactionTimedOutException(throwable);
        }
        throw rethrow(throwable);
    };

    private static final ILogger LOGGER = Logger.getLogger(FutureUtil.class);

    private FutureUtil() {
    }

    /**
     * This ExceptionHandler rethrows {@link java.util.concurrent.ExecutionException}s and logs
     * {@link com.hazelcast.core.MemberLeftException}s to the log.
     *
     * @param logger  the ILogger instance to be used for logging
     * @param message the log message to appear in the logs before the stacktrace
     * @param level   the log level to be used for logging
     */
    @PrivateApi
    public static ExceptionHandler logAllExceptions(final ILogger logger, final String message, final Level level) {
        if (logger.isLoggable(level)) {
            return throwable -> logger.log(level, message, throwable);
        }
        return IGNORE_ALL_EXCEPTIONS;
    }

    /**
     * This ExceptionHandler rethrows {@link java.util.concurrent.ExecutionException}s and logs
     * {@link com.hazelcast.core.MemberLeftException}s to the log.
     *
     * @param message the log message to appear in the logs before the stacktrace
     * @param level   the log level to be used for logging
     */
    @PrivateApi
    public static ExceptionHandler logAllExceptions(final String message, final Level level) {
        if (LOGGER.isLoggable(level)) {
            return throwable -> LOGGER.log(level, message, throwable);
        }
        return IGNORE_ALL_EXCEPTIONS;
    }

    /**
     * This ExceptionHandler rethrows {@link java.util.concurrent.ExecutionException}s and logs
     * {@link com.hazelcast.core.MemberLeftException}s to the log.
     *
     * @param logger the ILogger instance to be used for logging
     * @param level  the log level to be used for logging
     */
    @PrivateApi
    public static ExceptionHandler logAllExceptions(final ILogger logger, final Level level) {
        if (logger.isLoggable(level)) {
            return throwable -> logger.log(level, "Exception occurred", throwable);
        }
        return IGNORE_ALL_EXCEPTIONS;
    }

    /**
     * This ExceptionHandler rethrows {@link java.util.concurrent.ExecutionException}s and logs
     * {@link com.hazelcast.core.MemberLeftException}s to the log.
     *
     * @param level the log level to be used for logging
     */
    @PrivateApi
    public static ExceptionHandler logAllExceptions(final Level level) {
        if (LOGGER.isLoggable(level)) {
            return throwable -> LOGGER.log(level, "Exception occurred", throwable);
        }
        return IGNORE_ALL_EXCEPTIONS;
    }

    @PrivateApi
    public static <V> Collection<V> returnWithDeadline(Collection<Future<V>> futures, long timeout, TimeUnit timeUnit) {
        return returnWithDeadline(futures, timeout, timeUnit, IGNORE_ALL_EXCEPT_LOG_MEMBER_LEFT);
    }

    @PrivateApi
    public static <V> Collection<V> returnWithDeadline(Collection<Future<V>> futures, long timeout, TimeUnit timeUnit,
                                                       ExceptionHandler exceptionHandler) {

        return returnWithDeadline(futures, timeout, timeUnit, timeout, timeUnit, exceptionHandler);
    }

    @PrivateApi
    public static <V> Collection<V> returnWithDeadline(Collection<Future<V>> futures,
                                                       long overallTimeout, TimeUnit overallTimeUnit,
                                                       long perFutureTimeout, TimeUnit perFutureTimeUnit) {

        return returnWithDeadline(futures, overallTimeout, overallTimeUnit, perFutureTimeout, perFutureTimeUnit,
                IGNORE_ALL_EXCEPT_LOG_MEMBER_LEFT);
    }

    @PrivateApi
    public static <V> Collection<V> returnWithDeadline(Collection<Future<V>> futures,
                                                       long overallTimeout, TimeUnit overallTimeUnit,
                                                       long perFutureTimeout, TimeUnit perFutureTimeUnit,
                                                       ExceptionHandler exceptionHandler) {

        // Calculate timeouts for whole operation and per future. If corresponding TimeUnits not set assume
        // the default of TimeUnit.SECONDS
        long overallTimeoutNanos = calculateTimeout(overallTimeout, overallTimeUnit);
        long perFutureTimeoutNanos = calculateTimeout(perFutureTimeout, perFutureTimeUnit);

        // Common deadline for all futures
        long deadline = System.nanoTime() + overallTimeoutNanos;

        List<V> results = new ArrayList<>(futures.size());
        for (Future<V> future : futures) {
            try {
                long timeoutNanos = calculateFutureTimeout(perFutureTimeoutNanos, deadline);
                V value = executeWithDeadline(future, timeoutNanos);
                if (value != null) {
                    results.add(value);
                }
            } catch (Exception e) {
                exceptionHandler.handleException(e);
            }
        }
        return results;
    }

    @PrivateApi
    public static void waitForever(Collection<? extends Future> futuresToWaitFor, ExceptionHandler exceptionHandler) {
        for (Future future : futuresToWaitFor) {
            do {
                try {
                    future.get();
                } catch (Exception e) {
                    exceptionHandler.handleException(e);
                }
                // future might not be done if get() call was interrupted and the handler ignored it
            } while (!future.isDone());
        }
    }

    @PrivateApi
    public static void waitForever(Collection<? extends Future> futures) {
        waitForever(futures, IGNORE_ALL_EXCEPT_LOG_MEMBER_LEFT);
    }

    @PrivateApi
    public static void waitWithDeadline(Collection<? extends Future> futures, long timeout, TimeUnit timeUnit) {
        waitWithDeadline(futures, timeout, timeUnit, IGNORE_ALL_EXCEPT_LOG_MEMBER_LEFT);
    }

    @PrivateApi
    public static void waitUntilAllRespondedWithDeadline(Collection<? extends Future> futures, long timeout, TimeUnit timeUnit,
                                                         ExceptionHandler exceptionHandler) {
        CollectAllExceptionHandler collector = new CollectAllExceptionHandler(futures.size());
        waitWithDeadline(futures, timeout, timeUnit, collector);
        final List<Throwable> throwables = collector.getThrowables();
        // synchronized list does not provide thread-safety guarantee for iteration, so we handle it ourselves.
        synchronized (throwables) {
            for (Throwable t : throwables) {
                exceptionHandler.handleException(t);
            }
        }
    }

    @PrivateApi
    public static List<Throwable> waitUntilAllResponded(Collection<? extends Future> futures) {
        CollectAllExceptionHandler collector = new CollectAllExceptionHandler(futures.size());
        waitForever(futures, collector);
        return collector.getThrowables();
    }

    @PrivateApi
    public static void waitWithDeadline(Collection<? extends Future> futures, long timeout, TimeUnit timeUnit,
                                        ExceptionHandler exceptionHandler) {

        waitWithDeadline(futures, timeout, timeUnit, timeout, timeUnit, exceptionHandler);
    }

    @PrivateApi
    public static void waitWithDeadline(Collection<? extends Future> futures, long overallTimeout, TimeUnit overallTimeUnit,
                                        long perFutureTimeout, TimeUnit perFutureTimeUnit) {

        waitWithDeadline(futures, overallTimeout, overallTimeUnit, perFutureTimeout, perFutureTimeUnit,
                IGNORE_ALL_EXCEPT_LOG_MEMBER_LEFT);
    }

    @PrivateApi
    public static void waitWithDeadline(Collection<? extends Future> futures, long overallTimeout, TimeUnit overallTimeUnit,
                                        long perFutureTimeout, TimeUnit perFutureTimeUnit, ExceptionHandler exceptionHandler) {

        // Calculate timeouts for whole operation and per future. If corresponding TimeUnits not set assume
        // the default of TimeUnit.SECONDS
        long overallTimeoutNanos = calculateTimeout(overallTimeout, overallTimeUnit);
        long perFutureTimeoutNanos = calculateTimeout(perFutureTimeout, perFutureTimeUnit);

        // Common deadline for all futures
        long deadline = System.nanoTime() + overallTimeoutNanos;

        for (Future future : futures) {
            try {
                long timeoutNanos = calculateFutureTimeout(perFutureTimeoutNanos, deadline);
                executeWithDeadline(future, timeoutNanos);
            } catch (Throwable e) {
                exceptionHandler.handleException(e);
            }
        }
    }

    @PrivateApi
    public static <V> V getValue(Future<V> future) {
        if (future instanceof InternalCompletableFuture) {
            return ((InternalCompletableFuture<V>) future).joinInternal();
        }

        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw rethrow(e);
        } catch (ExecutionException e) {
            throw rethrow(e);
        }

    }

    private static <V> V executeWithDeadline(Future<V> future, long timeoutNanos) throws Exception {
        if (timeoutNanos <= 0) {
            // Maybe we just finished in time
            if (future.isDone() || future.isCancelled()) {
                return retrieveValue(future);
            } else {
                throw new TimeoutException();
            }
        }
        return future.get(timeoutNanos, TimeUnit.NANOSECONDS);
    }

    private static <V> V retrieveValue(Future<V> future)
            throws ExecutionException, InterruptedException {

        if (future instanceof InternalCompletableFuture) {
            return ((InternalCompletableFuture<V>) future).joinInternal();
        }

        return future.get();
    }

    private static long calculateTimeout(long timeout, TimeUnit timeUnit) {
        timeUnit = timeUnit == null ? TimeUnit.SECONDS : timeUnit;
        return timeUnit.toNanos(timeout);
    }

    private static long calculateFutureTimeout(long perFutureTimeoutNanos, long deadline) {
        long remainingNanos = deadline - System.nanoTime();
        return Math.min(remainingNanos, perFutureTimeoutNanos);
    }

    /**
     * Internally used interface to define behavior of the FutureUtil methods when exceptions arise
     */
    public interface ExceptionHandler {
        void handleException(Throwable throwable);
    }

    /**
     * Check if all futures are done
     *
     * @param futures the list of futures
     * @return {@code true} if all futures are done
     */
    public static boolean allDone(Collection<Future<?>> futures) {
        for (Future<?> f : futures) {
            if (!f.isDone()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Rethrow exception of the fist future that completed with an exception
     *
     * @throws Exception
     */
    public static void checkAllDone(Collection<Future<?>> futures) throws Exception {
        for (Future<?> f : futures) {
            if (f.isDone()) {
                f.get();
            }
        }
    }

    /**
     * Get all futures that are done
     *
     * @param futures collection of futures which we will check for done futures.
     * @return list of completed futures
     */
    @Nonnull
    public static List<Future<?>> getAllDone(Collection<Future<?>> futures) {
        List<Future<?>> doneFutures = new ArrayList<>();
        for (Future<?> f : futures) {
            if (f.isDone()) {
                doneFutures.add(f);
            }
        }
        return doneFutures;
    }
}
