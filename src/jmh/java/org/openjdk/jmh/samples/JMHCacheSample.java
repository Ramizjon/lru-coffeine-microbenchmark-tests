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

@State(Scope.Thread)
public class JMHCacheSample {

    private static final int STRING_ARRAY_SIZE = 5000;
    private static final int STRING_SIZE = 20;
    private static final int RAND_SEED = 0;
    int maxEntries = 80;

    LoadingCache<String, String> caffeineCache;
    LRUCache<String, String> lruCache;
    CacheLoader<String, String> loader;

    String[] stringArrayHalfRepeated = generateStringsWithInit(2500);
    String[] stringArrayEveryFiveRepeated = generateStringsWithInit(5);
    String[] stringArrayNoRepeated = generateStringsWithInit(1);

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public List<String> measureLRUCacheHalfRepeated() throws InterruptedException {
        Random rand = new Random(RAND_SEED);
        List<String> resultList = new LinkedList<>();

        for (int i = 0; i < stringArrayHalfRepeated.length; i++) {
            int index = rand.nextInt((stringArrayHalfRepeated.length - 0) + 1);
            String keyValue = stringArrayHalfRepeated[index];
            if (!lruCache.containsKey(keyValue)) {
                lruCache.put(keyValue, keyValue);
            }
            resultList.add(lruCache.get(keyValue));
        }
        return resultList;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public List<String> measureCaffeineCacheHalfRepeated() throws InterruptedException {
        Random rand = new Random(RAND_SEED);
        List<String> resultList = new LinkedList<>();

        for (int i = 0; i < stringArrayHalfRepeated.length; i++) {
            int index = rand.nextInt((stringArrayHalfRepeated.length - 0) + 1);
            resultList.add(caffeineCache.get(stringArrayHalfRepeated[index]));
        }
        return resultList;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public List<String> measureLRUCacheEveryFiveRepeated() throws InterruptedException {
        Random rand = new Random(RAND_SEED);
        List<String> resultList = new LinkedList<>();

        for (int i = 0; i < stringArrayEveryFiveRepeated.length; i++) {
            int index = rand.nextInt((stringArrayEveryFiveRepeated.length - 0) + 1);
            String keyValue = stringArrayEveryFiveRepeated[index];
            if (!lruCache.containsKey(keyValue)) {
                lruCache.put(keyValue, keyValue);
            }
            resultList.add(lruCache.get(keyValue));
        }
        return resultList;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public List<String> measureCaffeineCacheEveryFiveRepeated() throws InterruptedException {
        Random rand = new Random(RAND_SEED);
        List<String> resultList = new LinkedList<>();

        for (int i = 0; i < stringArrayEveryFiveRepeated.length; i++) {
            int index = rand.nextInt((stringArrayEveryFiveRepeated.length - 0) + 1);
            resultList.add(caffeineCache.get(stringArrayEveryFiveRepeated[index]));
        }
        return resultList;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public List<String> measureLRUCacheNoRepeated() throws InterruptedException {
        Random rand = new Random(RAND_SEED);
        List<String> resultList = new LinkedList<>();

        for (int i = 0; i < stringArrayNoRepeated.length; i++) {
            int index = rand.nextInt((stringArrayNoRepeated.length - 0) + 1);
            String keyValue = stringArrayNoRepeated[index];
            if (!lruCache.containsKey(keyValue)) {
                lruCache.put(keyValue, keyValue);
            }
            resultList.add(lruCache.get(keyValue));
        }
        return resultList;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public List<String> measureCaffeineCacheNoRepeated() throws InterruptedException {
        Random rand = new Random(RAND_SEED);
        List<String> resultList = new LinkedList<>();

        for (int i = 0; i < stringArrayNoRepeated.length; i++) {
            int index = rand.nextInt((stringArrayNoRepeated.length - 0) + 1);
            resultList.add(caffeineCache.get(stringArrayNoRepeated[index]));
        }
        return resultList;
    }

    @Setup
    public void prepare() {
        lruCache = new LRUCache<String, String>(maxEntries);

        loader = new CacheLoader<String, String>() {
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
        caffeineCache = Caffeine
                .newBuilder()
                .maximumSize(Long.valueOf(maxEntries))
                .initialCapacity(maxEntries)
                .build(loader);
    }

    private static String[] generateStringsWithInit(int repeatedStrings) {
        List<String> stringList = new ArrayList<String>();

        for (int i = 0; i < STRING_ARRAY_SIZE / repeatedStrings; i++) {
            String generated = RandomStringUtils.random(STRING_SIZE);
            for (int k = 0; k < repeatedStrings; k++) {
                stringList.add(generated);
            }
        }
        Collections.shuffle(stringList, new Random(RAND_SEED));
        return stringList.toArray(new String[STRING_ARRAY_SIZE]);
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



