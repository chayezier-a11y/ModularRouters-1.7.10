package me.desht.modularrouters.util;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import com.google.common.primitives.Ints;
import com.google.common.util.concurrent.AbstractListeningExecutorService;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import me.desht.modularrouters.ModularRouters;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkArgument;

public final class Scheduler extends AbstractListeningExecutorService {

    private static final Scheduler server;
    private static final Scheduler client;

    public static Scheduler server() { return server; }
    public static Scheduler client() { return client; }
    public static Scheduler forSide(Side side) { return side == Side.CLIENT ? client : server; }

    public void schedule(Runnable r, long tickDelay) {
        checkArgument(tickDelay >= 0);
        execute(new WaitingTask(r, tickDelay));
    }

    @Override
    public void execute(Runnable task) { execute(new WrappedRunnable(task)); }

    public void execute(Task task) { inputQueue.offer(task); }

    static {
        client = FMLCommonHandler.instance().getSide() == Side.CLIENT ? new Scheduler() : null;
        server = new Scheduler();
    }

    private final ConcurrentLinkedQueue<Task> inputQueue = new ConcurrentLinkedQueue<>();
    private Task[] activeTasks = new Task[5];
    private int size = 0;

    public void tick() {
        Task[] activeTasks = this.activeTasks;
        int size = this.size;
        {
            int idx = 0, free = -1;
            while (idx < size) {
                Task t = activeTasks[idx];
                if (!checkedExecute(t)) {
                    activeTasks[idx] = null;
                    if (free == -1) free = idx;
                } else if (free != -1) {
                    activeTasks[free++] = t;
                    activeTasks[idx] = null;
                }
                idx++;
            }
            if (free != -1) this.size = free;
        }
        {
            Task task;
            while ((task = inputQueue.poll()) != null) {
                if (checkedExecute(task)) {
                    if (size == activeTasks.length) {
                        Task[] newArr = new Task[size << 1];
                        System.arraycopy(activeTasks, 0, newArr, 0, size);
                        activeTasks = this.activeTasks = newArr;
                    }
                    activeTasks[size] = task;
                    this.size++;
                }
            }
        }
    }

    private static boolean checkedExecute(Task task) {
        try { return task.execute(); }
        catch (Throwable x) {
            ModularRouters.logger.error("Scheduler error: " + task, x);
            return false;
        }
    }

    public interface Task { boolean execute(); }

    private static final class WaitingTask implements Task {
        private final Runnable r;
        private long ticks;
        WaitingTask(Runnable r, long ticks) { this.r = r; this.ticks = ticks; }
        @Override
        public boolean execute() { if (--ticks == 0) { r.run(); return false; } return true; }
        @Override
        public String toString() { return String.format("Scheduled(task=%s,ticks=%s)", r, ticks); }
    }

    private static class WrappedRunnable implements Task {
        private final Runnable task;
        WrappedRunnable(Runnable task) { this.task = task; }
        @Override
        public boolean execute() { task.run(); return false; }
        @Override
        public String toString() { return task.toString(); }
    }

    @Override @Deprecated public boolean isShutdown() { return false; }
    @Override @Deprecated public boolean isTerminated() { return false; }
    @Override @Deprecated public void shutdown() {}
    @Nonnull @Override @Deprecated public List<Runnable> shutdownNow() { return Collections.emptyList(); }
    @Override @Deprecated
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        long millis = unit.toMillis(timeout);
        Thread.sleep(millis, Ints.saturatedCast(unit.toNanos(timeout) - TimeUnit.MILLISECONDS.toNanos(millis)));
        return false;
    }

    private Scheduler() {}
}
