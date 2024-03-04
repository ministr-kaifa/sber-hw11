package ru.zubkoff.sber.hw11;

import java.time.Duration;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class ScalableThreadPool implements ThreadPool, AutoCloseable {

  private final int corePoolSize;
  private final int maximumPoolSize;
  private final Duration keepAliveTime;
  
  private final ReentrantLock workersLock;
  private final Set<Worker> workers;
  private final BlockingQueue<Runnable> taskQueue;
  private volatile boolean isRunning;
  private volatile long removeCandidatesAmount;

  public ScalableThreadPool(int corePoolSize, int maximumPoolSize, Duration keepAliveTime,
      BlockingQueue<Runnable> workQueue) {
    this.corePoolSize = corePoolSize;
    this.maximumPoolSize = maximumPoolSize;
    this.keepAliveTime = keepAliveTime;
    this.taskQueue = workQueue;
    this.workers = new HashSet<>();
    removeCandidatesAmount = 0;
    isRunning = true;
    workersLock = new ReentrantLock();
  }

  @Override
  public void close() throws Exception {
    isRunning = false;
  }

  @Override
  public void start() {
    workersLock.lock();
    while (workers.size() < corePoolSize) {
      Worker worker = new Worker();
      workers.add(worker);
      worker.start();
    }
    workersLock.unlock();
  }

  @Override
  public void execute(Runnable task) {
    if (workers.size() < maximumPoolSize) {
      workersLock.lock();
      Worker worker = new Worker();
      workers.add(worker);
      worker.start();
      workersLock.unlock();
    }
    var taskAdded = taskQueue.offer(task);
    if(!taskAdded) {
      throw new IllegalStateException("Task queue is full");
    }
  }

  /**
   * @return текущее количество потоков в пуле.
   */
  public long threadsAmount() {
    return workers.size();
  }

  private final class Worker extends Thread {
    
    private boolean terminated = false;

    @Override
    public void run() {
      while (isRunning && !terminated) {
        nextTask()
          .ifPresentOrElse(Runnable::run, () -> terminated = true);
      }
      workersLock.lock();
      workers.remove(this);
      workersLock.unlock();
    }

    private Optional<Runnable> nextTask() {
      workersLock.lock();
      var isRemoveCandidate = workers.size() - removeCandidatesAmount > corePoolSize;
      if(isRemoveCandidate) {
        removeCandidatesAmount++;
      }
      workersLock.unlock();
      try {
        if(isRemoveCandidate) {
          var task = Optional.ofNullable(taskQueue.poll(keepAliveTime.toNanos(), TimeUnit.NANOSECONDS));
          workersLock.lock();
          removeCandidatesAmount--;
          workersLock.unlock();
          return task;
        } else {
          return Optional.of(taskQueue.take());
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException(e);
      }
    }

  }

}
