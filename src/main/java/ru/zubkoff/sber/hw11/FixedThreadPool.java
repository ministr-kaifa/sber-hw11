package ru.zubkoff.sber.hw11;

import java.time.Duration;
import java.util.concurrent.BlockingQueue;

public class FixedThreadPool extends ScalableThreadPool {

  public FixedThreadPool(int threads, Duration keepAliveTime,
      BlockingQueue<Runnable> workQueue) {
    super(threads, threads, keepAliveTime, workQueue);
  }
  
}
