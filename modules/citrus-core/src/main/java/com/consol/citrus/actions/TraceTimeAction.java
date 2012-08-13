package com.consol.citrus.actions;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.context.TestContext;

public class TraceTimeAction extends AbstractTestAction {
	
	public static final String DEFAULT = "DEFAULT";
	/**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(SleepAction.class);
    private final Queue<String> watchedTimers = new LinkedList<String>();
	
	@Override
    public void doExecute(TestContext context) {
		String trackedTimer = watchedTimers.poll();
		if(context.getVariables().get(trackedTimer) == null) {
			Object timeStamp = System.currentTimeMillis();
			context.addVariables(Collections.singletonMap(trackedTimer, timeStamp));
			log.info("Starting TimeWatcher:" + formatDefaultTimer(trackedTimer));
		}
		else {
			String timerVariable = context.getVariable(trackedTimer);
			log.info("TimeWatcher" + formatDefaultTimer(trackedTimer) + " after " + getElapsedTime(timerVariable) + " seconds.");
		}
    }
	
	public void track(String timer) {
		watchedTimers.offer(timer);
	}
	
	private String formatDefaultTimer(String timer) {
		if(timer.equals(DEFAULT))
			return "";
		else 
			return " " + timer;
	}
	
	private double getElapsedTime(String timerVariable) {
		return Double.valueOf(System.currentTimeMillis() - Long.valueOf(timerVariable)) / 1000;
	}
}
