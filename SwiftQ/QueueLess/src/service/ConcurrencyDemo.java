package service;
import java.util.concurrent.*; import java.util.*;
/** Small teaching demo: wait/notify producer-consumer, thread pool, Callable/Future and scheduled task. */
public class ConcurrencyDemo {
 private static class TokenBuffer {
  private final Queue<String> items=new LinkedList<>();
  public synchronized void produce(String item)throws InterruptedException{items.add(item);System.out.println("Producer added: "+item);notify();notifyAll();}
  public synchronized String consume()throws InterruptedException{while(items.isEmpty()){System.out.println("Consumer waiting for a token...");wait();}String item=items.remove();System.out.println("Consumer processed: "+item);return item;}
 }
 public static void run(){
  System.out.println("\n--- SIMPLE MULTITHREADING DEMONSTRATION ---");
  // Thread and Runnable: two producers add sample token requests.
  TokenBuffer buffer=new TokenBuffer();
  Thread producer1=new Thread(()->{try{buffer.produce("Demo token A");}catch(InterruptedException e){Thread.currentThread().interrupt();}},"Producer-1");
  Thread producer2=new Thread(()->{try{buffer.produce("Demo token B");}catch(InterruptedException e){Thread.currentThread().interrupt();}},"Producer-2");
  Thread consumer=new Thread(()->{try{buffer.consume();buffer.consume();}catch(InterruptedException e){Thread.currentThread().interrupt();}},"Consumer");
  System.out.println("Producer initial state: "+producer1.getState());
  consumer.setDaemon(false);consumer.start();
  try{Thread.sleep(150);}catch(InterruptedException e){Thread.currentThread().interrupt();}
  producer1.start();producer2.start();
  try{producer1.join();producer2.join();consumer.join();}catch(InterruptedException e){Thread.currentThread().interrupt();}
  // Daemon thread: a background monitor does not keep the application alive.
  Thread daemonMonitor=new Thread(()->System.out.println("Daemon monitor: background check started."),"QueueLess-Daemon-Monitor");
  daemonMonitor.setDaemon(true);daemonMonitor.start();
  System.out.println("Daemon thread? "+daemonMonitor.isDaemon());
  // ExecutorService + Callable + Future: get a simple count result.
  ExecutorService pool=Executors.newFixedThreadPool(2);
  try{Callable<Integer> task=()->{int count; synchronized(DataStore.tokenHistory){count=DataStore.tokenHistory.size();} System.out.println("Background task counted saved tokens: "+count);return count;};Future<Integer> result=pool.submit(task);System.out.println("Callable result received through Future: "+result.get());}
  catch(InterruptedException e){Thread.currentThread().interrupt();}catch(ExecutionException e){System.out.println("Task error: "+e.getMessage());}
  finally{pool.shutdown();}
  // ScheduledExecutorService: one simple delayed report, then shut down.
  ScheduledExecutorService scheduler=Executors.newSingleThreadScheduledExecutor();
  scheduler.schedule(()->System.out.println("Scheduled task: QueueLess demo report generated."),1,TimeUnit.SECONDS);
  scheduler.shutdown();try{scheduler.awaitTermination(2,TimeUnit.SECONDS);}catch(InterruptedException e){Thread.currentThread().interrupt();}
  System.out.println("Demo complete. Real application data was not changed.");
 }
}
