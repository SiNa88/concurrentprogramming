package concurrentprogramming;

import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Hooman
 */
public class NeatAndNastyConflict {

  public static final int DEFAULT_NEAT_NUM = 2;
  public static final int DEFAULT_NASTY_NUM = 10;

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    int neats = DEFAULT_NEAT_NUM;
    int nasties = DEFAULT_NASTY_NUM;

    if (args.length == 2) {
      neats = Integer.valueOf(args[0]);
      nasties = Integer.valueOf(args[1]);
    } else {
      System.out.println(String.format("Invalid input arguments. Using the default values: neats=%d, nasties=%d", DEFAULT_NEAT_NUM, DEFAULT_NASTY_NUM));
    }
    NeatAndNastyConflict t = new NeatAndNastyConflict();
    t.runTest(neats, nasties);
  }

  public void runTest(int neats, int nasties) {
    Room room = new Room();

    Thread[] threads = new Thread[nasties + neats];
    for (int i = 0; i < neats; i++) {
      threads[i] = new Thread(new Neat(room));
      threads[i].setName(String.format("Neat%d", i + 1));
    }
    for (int i = neats; i < neats + nasties; i++) {
      threads[i] = new Thread(new Nasty(room));
      threads[i].setName(String.format("Nasty%d", i - neats + 1));
    }

    for (Thread t : threads) {
      t.start();
    }

  }

  class Nasty implements Runnable {

    private final Room room;
    private Random r;

    public Nasty(Room room) {
      this.room = room;
      r = new Random();
    }

    @Override
    public void run() {
      while (true) {
        try {
          room.makeDirty();
          Thread.sleep(r.nextInt(2000));
        } catch (InterruptedException ex) {
          Logger.getLogger(NeatAndNastyConflict.class.getName()).log(Level.SEVERE, null, ex);
        }

      }
    }
  }

  class Neat implements Runnable {

    private final Room room;
    private Random r;

    public Neat(Room room) {
      this.room = room;
      r = new Random();
    }

    @Override
    public void run() {
      while (true) {
        try {
          room.clean();
          Thread.sleep(r.nextInt(2000));
        } catch (InterruptedException ex) {
          Logger.getLogger(NeatAndNastyConflict.class.getName()).log(Level.SEVERE, null, ex);
        }

      }
    }
  }

  class Room {

    final ReentrantLock lock = new ReentrantLock();
    final Condition isDirty = lock.newCondition();
    final Condition isClean = lock.newCondition();

    private boolean dirty = false;

    public void clean() throws InterruptedException {
      lock.lock();

      while (!dirty) {
        isDirty.await();
      }

      dirty = false;
      System.out.println(String.format("%s cleaned the room.", Thread.currentThread().getName()));
      isClean.signalAll();
      lock.unlock();
    }

    public void makeDirty() throws InterruptedException {
      lock.lock();

      while (dirty) {
        isClean.await();
      }

      dirty = true;
      System.out.println(String.format("%s made the room dirty.", Thread.currentThread().getName()));

      isDirty.signalAll();
      lock.unlock();
    }
  }

}
