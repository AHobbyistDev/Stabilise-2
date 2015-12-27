package com.stabilise.tests;

import java.util.concurrent.ExecutionException;

import com.stabilise.util.AppDriver;
import com.stabilise.util.box.Box;
import com.stabilise.util.box.Boxes;
import com.stabilise.util.concurrent.Tasks;
import com.stabilise.util.concurrent.task.ReturnTask;
import com.stabilise.util.concurrent.task.Task;


public class TaskTesting2 {
    
    public static void test1() {
        ReturnTask<String> task = Task.builder(Tasks.newThreadExecutor())
                .beginReturn(String.class)
                .andThenReturn(() -> "Hello, world!")
                .build();
        try {
            System.out.println(task.start().get());
        } catch(InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
    
    public static void test2() {
        Task task = Task.builder(Tasks.newThreadExecutor())
                .name("Building stuff")
                .begin()
                .andThen(h -> {
                    h.setStatus("Diddily");
                    saySleep(500, "Node 1");
                 })
                .andThen(h -> {
                    h.setStatus("Doing subtask thingies");
                    h.beginFlatten();
                    for(int i = 0; i < 10; i++) {
                        final int j = i;
                        h.spawn(false, h2 -> {
                            h2.setStatus("Doing thing " + j);
                            saySleep(500, "Doing the thing " + j);
                        });
                    }
                    h.endFlatten();
                })
                .andThen(h -> saySleep(500, "Node 2"))
                .andThen(h -> saySleep(500, "Node 3"))
                .build();
        Box<AppDriver> driver = Boxes.emptyMut();
        Runnable updater = () -> {
            /*
            for(int i = 0; i < 20; i++)
                System.out.println();
            task.printStack();
            //*/
            if(task.stopped())
                driver.get().stop();
        };
        driver.set(new AppDriver(3, updater, null));
        task.start();
        new Thread(() -> {
            try {
                Thread.sleep(1200L);
            } catch(Exception e) {}
            task.cancel();
        }).start();
        driver.get().run();
        System.out.println("End test");
    }
    
    public static void saySleep(long millis, String msg) throws InterruptedException {
        //System.out.println(msg);
        Thread.sleep(millis);
    }
    
    public static void main(String[] args) {
        //test1();
        test2();
    }
    
}
