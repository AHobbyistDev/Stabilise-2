package com.stabilise.util.concurrent.task;



public interface TaskRunnable {
    
    void run(TaskHandle handle) throws Exception;
    
}
