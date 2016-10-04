package org.openjdk.jmh.samples;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.apache.commons.lang3.RandomStringUtils;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;


public class JMHCacheSample {

    @Benchmark
    @BenchmarkMode({Mode.Throughput, Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureLRUCache(BenchmarkState state) throws InterruptedException {
        LRUCache<String, String> cacheObject = new LRUCache<String, String>(state.maxEntries);
        Random rand = new Random(state.RAND_SEED);

        for (int i = 0; i < state.stringArray.length; i++) {
            int index = rand.nextInt((state.stringArray.length - 0) + 1);
            String keyValue = state.stringArray[index];
            if (!cacheObject.containsKey(keyValue)) {
                cacheObject.put(keyValue, keyValue);
            }
            cacheObject.get(keyValue);
        }
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput, Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureCaffeineCache(BenchmarkState state) throws InterruptedException {
        Random rand = new Random(state.RAND_SEED);

        LoadingCache<String, String> caffeineCache = Caffeine
                .newBuilder()
                .maximumSize(Long.valueOf(state.maxEntries))
                .initialCapacity(state.maxEntries)
                .build(state.loader);

        for (int i = 0; i < state.stringArray.length; i++) {
            int index = rand.nextInt((state.stringArray.length - 0) + 1);
            caffeineCache.get(state.stringArray[index]);
        }
    }

    @State(Scope.Benchmark)
    public static class BenchmarkState {
        private static final int REPEATED_STRINGS = 5;
        private static final int STRING_ARRAY_SIZE = 1000;
        private static final int STRING_SIZE = 20;
        private static final int RAND_SEED = 0;
        int maxEntries = 100;
        String[] stringArray = generateStringsWithInit();

        CacheLoader<String, String> loader = new CacheLoader<String, String>() {
            @Override
            public String load(String key) throws Exception {
                return key;
            }

            @Override
            public CompletableFuture<String> asyncLoad(String key, Executor executor) {
                return CompletableFuture.supplyAsync(() -> {
                    try {
                        return load(key);
                    } catch (RuntimeException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new CompletionException(e);
                    }
                }, executor);
            }
        };

        private static String[] generateStringsWithInit() {
            List<String> stringList = new ArrayList<String>();

            for (int i = 0; i < STRING_ARRAY_SIZE / REPEATED_STRINGS; i++) {
                String generated = RandomStringUtils.random(STRING_SIZE);
                for (int k = 0; k < REPEATED_STRINGS; k++) {
                    stringList.add(generated);
                }
            }
            Collections.shuffle(stringList, new Random(RAND_SEED));
            return stringList.toArray(new String[STRING_ARRAY_SIZE]);
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(".*" + JMHCacheSample.class.getSimpleName() + ".*")
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}

class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private int maxNumberEntries;

    public LRUCache(int maxEntries) {
        super(maxEntries + 1, 1.0f, true);
        maxNumberEntries = maxEntries;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxNumberEntries;
    }
}



