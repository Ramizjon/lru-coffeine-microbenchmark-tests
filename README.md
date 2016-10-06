LRU Caffeine Microbenchmark Tests
==================

Sample project showcasing the JMH gradle plugin. This investigation is intended to test performance(average time of execution) of these two caches in single threaded mode, depending on number of repeated elements.

Sample results of test execution:

**No repeated elements**

- Caffeine: 1.110 ± 0.014  ms/op
- LRU Cache: 0.326 ± 0.008  ms/op

**Every five repeated elements**

- Caffeine: 0.604 ± 0.011  ms/op
- LRU Cache: 0.314 ± 0.011  ms/op

**Half repeated elements**

- Caffeine: 0.223 ± 0.008  ms/op
- LRU Cache: 0.146 ± 0.003  ms/op
