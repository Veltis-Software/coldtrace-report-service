package com.acme.coldtrace.platform.shared.application.result;

import java.util.Optional;
import java.util.function.Function;

/**
 * Generic application-layer outcome wrapper.
 * @param <T> success value type
 * @param <E> failure value type
 */
public sealed interface Result<T, E> permits Result.Success, Result.Failure {
    /**
     * Successful result
     * @param value success value
     * @param <T> success value type
     * @param <E> failure value type
     */
    record Success<T, E>(T value) implements Result<T, E> {}

    /**
     * Failure result
     * @param error failure value
     * @param <T> success value type
     * @param <E> failure value type
     */
    record Failure<T, E>(E error) implements Result<T, E> {}

    /**
     * Creates a successful result.
     *
     * @param value success value
     * @param <T> success value type
     * @param <E> failure value type
     * @return success result
     */
    static <T, E> Result<T, E> success(T value) { return new Success<>(value); }

    /**
     * Creates a failed result.
     *
     * @param error failure value
     * @param <T> success value type
     * @param <E> failure value type
     * @return failure result
     */
    static <T, E> Result<T, E> failure(E error) { return new Failure<>(error); }

    /** @return {@code true} when this result is successful */
    default boolean isSuccess() { return this instanceof Success<?, ?>; }

    /** @return {@code true} when this result is failure */
    default boolean isFailure() { return this instanceof Failure<?, ?>; }

    /** @return success value when present */
    default Optional<T> success() {
        if (this instanceof Success<?, ?> success) {
            @SuppressWarnings("unchecked")
            T value = ((Success<T, E>) success).value();
            return Optional.of(value);
        }
        return Optional.empty();
    }

    /** @return failure value when present */
    default Optional<E> failure() {
        if (this instanceof Failure<?, ?> failure) {
            @SuppressWarnings("unchecked")
            E error = ((Failure<T, E>) failure).error();
            return Optional.of(error);
        }
        return Optional.empty();
    }

    /**
     * Folds this result into a single value.
     * @param onSuccess mapper for a successful value
     * @param onFailure mapper for a failure value
     * @param <R> folded return type
     * @return folded return type
     */
    default <R> R fold(Function<? super T, ? extends R> onSuccess, Function<? super E, ? extends R> onFailure) {
        if (this instanceof Success<?, ?> success) {
            @SuppressWarnings("unchecked")
            T value = ((Success<T, E>) success).value();
            return onSuccess.apply(value);
        }
        @SuppressWarnings("unchecked")
        E error = ((Failure<T, E>) this).error();
        return onFailure.apply(error);
    }
}
