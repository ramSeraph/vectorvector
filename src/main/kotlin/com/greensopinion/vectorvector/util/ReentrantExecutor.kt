package com.greensopinion.vectorvector.util

import java.util.concurrent.Executor

class ReentrantExecutor(
    private val executor: Executor
): Executor {
    private val isTask = ThreadLocal<Boolean?>()

    override fun execute(task: Runnable) {
        if (isTask.get() == true) {
            task.run()
        } else {
            executor.execute {
                isTask.set(true)
                try {
                    task.run()
                } finally {
                    isTask.set(false)
                }
            }
        }
    }
}