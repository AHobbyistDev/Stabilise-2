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


class TaskTesting {
    private TaskTesting() {}
    
    public static void main(String[] args) throws Exception {
        test4();
    }
    
    public static void test3() throws Exception {
        System.out.println(Tasks.exec(() -> "Hello, world!").get());
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
            .andThen(100, h -> {
                h.spawn(false, h2 -> {
                    h2.setStatus("Print \"Hi!\"");
                    say(h2, 100, "Hi!");
                });
                h.spawn(false, h2 -> {
                    h2.setStatus("Print \"Also hi!\"");
                    say(h2, 100, "Also hi!");
                });
                h.spawn(false, h2 -> {
                    h2.setStatus("Print \"Hi as well!\"");
                    say(h2, 100, "Hi as well!");
                });
                h.spawn(false, h2 -> {
                    h2.setStatus("Say \"lo, \"");
                    say(h2, 100, "Append \"lo, \"");
                    buf.append("lo, ");
                });
            })
                .onEvent(TaskEvent.STOP, (e) -> System.out.println("Group stopped!"))
                .onEvent(TaskEvent.COMPLETE, (e) -> System.out.println("Group completed!"))
                .onEvent(TaskEvent.FAIL, (e) -> System.out.println("Group failed!"))
            .andThen(h -> {
                h.setStatus("The almighty spawner");
                h.spawn(false, (h2) -> say(h2, 100, "Node 1"));
                h.spawn(true,  (h2) -> say(h2, 100, "Node 2"));
                h.spawn(true,  (h2) -> say(h2, 0, "Node 3"));
                h.spawn(false, (h2) -> {
                    say(h2, 100, "Node 4");
                    h2.spawn(false, h3 -> say(h3, 100, "Node 4.1"));
                    h2.spawn(false, h3 -> say(h3, 0, "Node 4.2"));
                    h2.spawn(true,  h3 -> say(h3, 0, "Node 4.3"));
                    h2.spawn(true,  h3 -> say(h3, 100, "Node 4.4"));
                    h2.spawn(false, h3 -> say(h3, 100, "Node 4.5"));
                });
                h.spawn(true, (h2) -> say(h2, 100, "Node 5"));
                h.beginFlatten();
                h.spawn(false, (h2) -> say(h2, 100, "Node 6"));
                h.spawn(false, (h2) -> say(h2, 1000, "Node 7"));
                h.spawn(false, (h2) -> say(h2, 100, "Node 8"));
                h.spawn(false, (h2) -> say(h2, 100, "Node 9"));
                h.spawn(false, (h2) -> say(h2, 0, "Node 10"));
                h.spawn(false, (h2) -> say(h2, 0, "Node 11"));
                h.spawn(false, (h2) -> say(h2, 0, "Node 12"));
                h.spawn(false, (h2) -> say(h2, 100, "Node 13"));
                h.spawn(false, (h2) -> say(h2, 100, "Node 14"));
                h.endFlatten();
                h.spawn(true, (h2) -> {
                    say(h2, 100,  "Node 15");
                    h2.spawn(false, h3 -> say(h3, 100, "Node 15.1"));
                    h2.spawn(true,  h3 -> say(h3, 0, "Node 15.2"));
                    h2.spawn(false, h3 -> say(h3, 100, "Node 15.3"));
                });
                h.spawn(false, (h2) -> say(h2, 0, "Node 16"));
                h.spawn(false, (h2) -> say(h2, 0, "Node 17"));
                h.spawn(false, (h2) -> say(h2, 100, "Node 18"));
                h.spawn(true,  (h2) -> say(h2, 0, "Node 19"));
                h.spawn(true,  (h2) -> say(h2, 100, "Node 20"));
                h.spawn(true,  (h2) -> say(h2, 0, "Node 21"));
            })
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
        Waiter waiter = task.waiter(5000, TimeUnit.MILLISECONDS);
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
                    //task.printStack();
                    break;
                case TIMEOUT:
                    System.out.println("Timeout!");
                    task.cancel();
                    boolean result = task.awaitUninterruptibly();
                    System.out.println("Awaited... " + result);
                    if(result) {
                        try {
                            System.out.println("Result: " + task.tryGet());
                        } catch(ExecutionException e1) {
                            e1.printStackTrace();
                        }
                    }
                    break loop;
                default:
                    throw new AssertionError();
            }
            try {
                Thread.sleep(250);
            } catch(InterruptedException e1) {
                e1.printStackTrace();
            }
        }
        exec.shutdown();
    }
    
    public static void say(TaskHandle h, long millis, String msg) throws InterruptedException {
        h.setStatus(msg);
        System.out.println(msg);
        Thread.sleep(millis);
    }
    
}
