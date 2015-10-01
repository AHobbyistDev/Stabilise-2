package com.stabilise.util.concurrent.task;

import java.util.concurrent.Executors;


class TaskTesting {
    private TaskTesting() {}
    
    public static void blah(TaskHandle h) {
        
    }
    
    public static void blah2() {
        
    }
    
    public static void main(String[] args) {
        Task task = new TaskBuilder(Executors.newCachedThreadPool(), "Doing stuff")
            .andThen(t -> blah(t))
            .andThen(t -> blah(t))
                .onStop(() -> System.out.println("stop detected"))
                .onComplete(() -> System.out.println("complete detected"))
                .onCancel(() -> System.out.println("cancel detected"))
            .andThenGroup()
                .subtask(t -> blah(t))
                    .onFail(() -> System.out.println("fail detected"))
                .subtask(t -> blah(t))
                    .andThen(t -> blah(t))
            .endGroup()
            .build();
        task.waitUninterruptibly();
    }
    
}
