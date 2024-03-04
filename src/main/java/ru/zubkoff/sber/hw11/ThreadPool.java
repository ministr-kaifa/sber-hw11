package ru.zubkoff.sber.hw11;

public interface ThreadPool {

  /**
   * запускает потоки. Потоки бездействуют, до тех пор пока не появится новое задание в очереди (см. execute)
   */
  void start(); 

  /**
   * складывает taskв очередь. Освободившийся поток должен выполнить это задание. Каждое задание должны быть выполнено ровно 1 раз
   * @param task
   */
  void execute(Runnable task);
  
}
