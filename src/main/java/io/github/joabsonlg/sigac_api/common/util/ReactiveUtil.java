package io.github.joabsonlg.sigac_api.common.util;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

/**
 * Utility class for reactive operations.
 * Provides helper methods for working with Mono and Flux.
 */
public class ReactiveUtil {
    
    /**
     * Converts a List to a Flux.
     */
    public static <T> Flux<T> fromList(List<T> list) {
        return Flux.fromIterable(list);
    }
    
    /**
     * Converts a Flux to a Mono<List>.
     */
    public static <T> Mono<List<T>> toList(Flux<T> flux) {
        return flux.collectList();
    }
    
    /**
     * Maps a Mono to another type, handling null values.
     */
    public static <T, R> Mono<R> mapIfPresent(Mono<T> mono, Function<T, R> mapper) {
        return mono.map(mapper).switchIfEmpty(Mono.empty());
    }
    
    /**
     * Returns a default value if the Mono is empty.
     */
    public static <T> Mono<T> defaultIfEmpty(Mono<T> mono, T defaultValue) {
        return mono.defaultIfEmpty(defaultValue);
    }
    
    /**
     * Executes an action if the Mono is empty.
     */
    public static <T> Mono<T> doOnEmpty(Mono<T> mono, Runnable action) {
        return mono.switchIfEmpty(Mono.fromRunnable(action).then(Mono.empty()));
    }
    
    /**
     * Safely handles exceptions in reactive chains.
     */
    public static <T> Mono<T> handleError(Mono<T> mono, Function<Throwable, Mono<T>> errorHandler) {
        return mono.onErrorResume(errorHandler);
    }
    
    /**
     * Safely handles exceptions in reactive chains with default value.
     */
    public static <T> Mono<T> handleErrorWithDefault(Mono<T> mono, T defaultValue) {
        return mono.onErrorReturn(defaultValue);
    }
    
    /**
     * Combines two Monos into a tuple-like result.
     */
    public static <T1, T2> Mono<CombinedResult<T1, T2>> combine(Mono<T1> mono1, Mono<T2> mono2) {
        return Mono.zip(mono1, mono2, CombinedResult::new);
    }
    
    /**
     * Simple record to hold two combined results.
     */
    public record CombinedResult<T1, T2>(T1 first, T2 second) {}
}
