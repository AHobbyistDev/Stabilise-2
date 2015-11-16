package com.stabilise.tests;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.stabilise.util.concurrent.Tasks;
import com.stabilise.util.concurrent.Waiter;
import com.stabilise.util.concurrent.task.ReturnTask;
import com.stabilise.util.concurrent.task.Task;
import com.stabilise.util.concurrent.task.TaskEvent;
import com.stabilise.util.concurrent.task.TaskHandle;
import com.stabilise.util.concurrent.task.TaskRunnable;


class TaskTesting {
    private TaskTesting() {}
    
    public static void main(String[] args) {
        test4();
    }
    
    public static void test3() {
        try {
            System.out.println(Tasks.exec(() -> "Hello, world!").get());
        } catch(InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
    
    public static void test4() {
        ExecutorService exec = Executors.newCachedThreadPool();
        StringBuffer buf = new StringBuffer();
        ReturnTask<String> task = Task.builder(exec).name("Doing stuff").beginReturn(String.class)
            .andThen(100, h -> {
                h.setStatus("Prepping");
                for(int i = 0; i < 100; i++) {
                    h.increment();
                    Thread.sleep(5);
                }
            })
            .andThen(() -> buf.append("Hel"))
            .andThenGroup()
                .subtask(20, (h) -> {
                    h.setStatus("Print \"Hi!\"");
                    sayWithSleep(100, "Hi!");
                })
                    .onEvent(TaskEvent.COMPLETE, (e) -> System.out.println("Hi, guy!"))
                .subtask(20, (h) -> {
                    h.setStatus("Print \"Also hi!\"");
                    sayWithSleep(100, "Also hi!");
                })
                .subtask(new TaskRunnable() {
                    public void run(TaskHandle h) throws Exception {
                        h.setStatus("Print \"Hi as well!\"");
                        sayWithSleep(100, "Hi as well!");
                     }
                    public long getParts() { return 20; }
                })
                .subtask(20, (h) -> {
                    h.setStatus("Say \"lo, \"");
                    buf.append("lo, ");
                })
                .subtask(20, (h) -> {
                    h.setStatus("Print \"Hi from Hawaii!\"");
                    sayWithSleep(100, "Hi from Hawaii!");
                })
            .endGroup()
                .onEvent(TaskEvent.STOP, (e) -> System.out.println("Group stopped!"))
                .onEvent(TaskEvent.COMPLETE, (e) -> System.out.println("Group completed!"))
                .onEvent(TaskEvent.FAIL, (e) -> System.out.println("Group failed!"))
            .andThen(100, (h) -> {
                h.setStatus("Say \"world\"");
                buf.append("w");
                h.increment(20);
                buf.append("o");
                h.increment(20);
                buf.append("r");
                h.increment(20);
                buf.append("l");
                h.increment(20);
                buf.append("d");
                h.increment(200000);
            })
            .andThenReturn(100, (h) -> {
                h.setStatus("Say \"!\"");
                buf.append("!");
                return buf.toString();
            })
            .build().start();
        Waiter waiter = task.waiter(2500, TimeUnit.MILLISECONDS);
        loop: while(true) {
            switch(waiter.poll()) {
                case COMPLETE:
                    System.out.println(task);
                    try {
                        System.out.println("Result: " + task.get());
                    } catch(InterruptedException | ExecutionException e1) {
                        e1.printStackTrace();
                    }
                    break loop;
                case INCOMPLETE:
                    System.out.println(task);
                    break;
                case TIMEOUT:
                    System.out.println("Timeout!");
                    task.cancel();
                    System.out.println("Awaited... " + task.awaitUninterruptibly());
                    break loop;
                default:
                    throw new AssertionError();
            }
            try {
                Thread.sleep(100);
            } catch(InterruptedException e1) {
                e1.printStackTrace();
            }
        }
        exec.shutdown();
    }
    
    public static void sayWithSleep(long millis, String msg) throws InterruptedException {
        System.out.println(msg);
        Thread.sleep(millis);
    }
    
}
