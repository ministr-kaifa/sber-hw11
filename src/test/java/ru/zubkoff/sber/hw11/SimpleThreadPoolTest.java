package ru.zubkoff.sber.hw11;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

class SimpleThreadPoolTest {

  @RepeatedTest(100)
  void givenMoreThanCorePoolSizeTasks_whenExecutingTasks_thenAfterKeepAliveTimeAmountOfThreadsLessThanOrEqualsToCorePoolSize() throws Exception {
    //given
    var coreSize = 10;
    var maxSize = 100;
    var keepAlive = Duration.ZERO;
    try(SimpleThreadPool threadPool = new SimpleThreadPool(coreSize, maxSize, keepAlive, new ArrayBlockingQueue<>(10_000));) {
      
    //when
      CountDownLatch start = new CountDownLatch(1);
      for (int i = 0; i < maxSize; i++) {
        threadPool.execute(() -> {
          try {
            start.await();
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
        });
      }

    //then
      assertTrue(threadPool.threadsAmount() >= coreSize);
      start.countDown();
      try {
        Thread.sleep(50);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      assertTrue(threadPool.threadsAmount() - coreSize <= 1);  
    }
  }

  @Test
  void givenOneTaskCapacityThreadPool_whenExecuteMoreTasksThanCapacity_thenThrowsIllegalStateException() throws Exception {
    //given
    var coreSize = 0;
    var maxSize = 0;
    var keepAlive = Duration.ZERO;
    try(SimpleThreadPool threadPool = new SimpleThreadPool(coreSize, maxSize, keepAlive, new ArrayBlockingQueue<>(1));) {

    //when then
      threadPool.execute(()->{});
      assertThrows(IllegalStateException.class, () -> threadPool.execute(()->{}));
    }
  }
  
  
  

}
