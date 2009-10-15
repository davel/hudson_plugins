package com.ikokoon.toolkit;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

import org.apache.log4j.Logger;

/**
 * During the testing with object databases for persistence it looked like the threads were getting locked, and in fact even this thread stopped so
 * finally oodb couldn't be used. Still no resolution on that.
 * 
 * This thread iterates over the threads that can be locked and prints out the info on them, if there are any of course.
 * 
 * @author Michael Couck
 * @since 15.10.09
 * @version 01.00
 */
public class ThreadDeadLockDetector implements Runnable {

	private static final long SLEEP = 10000;
	private Logger logger = Logger.getLogger(ThreadDeadLockDetector.class);

	/**
	 * {@inheritDoc}
	 */
	public void run() {
		while (true) {
			try {
				Thread.sleep(SLEEP);
			} catch (InterruptedException e) {
				logger.error("Dead lock detector interrupted", e);
			}
			ThreadMXBean bean = ManagementFactory.getThreadMXBean();
			logger.error("Thread deadlock bean : " + bean);
			long[] threadIds = bean.findDeadlockedThreads(); // Returns null if no threads are deadlocked.
			if (threadIds != null) {
				ThreadInfo[] infos = bean.getThreadInfo(threadIds);
				for (ThreadInfo info : infos) {
					StackTraceElement[] stackTraceElements = info.getStackTrace();
					logger.error("Thread locked : " + info.getLockOwnerName());
					for (StackTraceElement stackTraceElement : stackTraceElements) {
						logger.error(stackTraceElement.getClassName() + ":" + stackTraceElement.getMethodName() + ":"
								+ stackTraceElement.getLineNumber());
					}
				}
			}
		}

	}

}
