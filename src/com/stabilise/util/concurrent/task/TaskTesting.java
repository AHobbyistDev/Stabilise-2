package com.stabilise.util.concurrent.task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


class TaskTesting {
    private TaskTesting() {}
    
    public static void main(String[] args) {
        test2();
    }
    
    public static void test1() {
        Task task = new TaskBuilder(Executors.newCachedThreadPool(), "Doing stuff")
            .andThen(t -> blah(t))
            .andThen(t -> blah(t))
                .onEvent(TaskEvent.STOP, (e) -> System.out.println("stop detected"))
                .onEvent(TaskEvent.COMPLETE, (e) -> System.out.println("complete detected"))
                .onEvent(TaskEvent.CANCEL, (e) -> System.out.println("cancel detected"))
            .andThenGroup()
                .subtask(t -> blah(t))
                    .onEvent(TaskEvent.START, (e) -> System.out.println("start detected"))
                    .onEvent(TaskEvent.FAIL, (e) -> System.out.println("fail detected"))
                .subtask(t -> blah(t))
                    .andThen(t -> blah(t))
            .endGroup()
            .build();
        task.awaitUninterruptibly();
    }
    
    public static void blah(TaskHandle h) {}
    public static void blah2() {}
    
    public static void test2() {
        ExecutorService exec = Executors.newCachedThreadPool();
        Task task = new TaskBuilder(exec, "Task stuff")
            .andThen(t -> {})
            .andThen(t -> Thread.sleep(1000))
                .onEvent(TaskEvent.COMPLETE, e -> System.out.println("Sleep ended: " + e))
            .build();
        while(true) {
            System.out.println(task);
            if(task.stopped()) {
                System.out.println("Completed!");
                break;
            }
            try {
                Thread.sleep(100);
            } catch(InterruptedException e1) {
                e1.printStackTrace();
            }
        }
        exec.shutdown();
    }
    
}
