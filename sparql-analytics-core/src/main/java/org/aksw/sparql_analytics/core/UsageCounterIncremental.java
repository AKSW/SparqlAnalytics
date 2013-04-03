package org.aksw.sparql_analytics.core;

import java.util.List;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class UsageCounterIncremental {
	private static final Logger logger = LoggerFactory
			.getLogger(UsageCounterIncremental.class);

	private final ApiBean apiBean;

	private Long lastSentEvent; // TODO Add time unit
	private int slotCount = 120;

	public UsageCounterIncremental(ApiBean api) {
		this.apiBean = api;

		DateTime dt = new DateTime().withSecondOfMinute(0).withMillisOfSecond(0);
		lastSentEvent = dt.minusMinutes(slotCount).getMillis();
	}
	
	public Long getStartTimestamp() {
		return lastSentEvent;
	}
	
	public int getTimeWindow() {
		return 60 * 1000; // 60 seconds
	}
	
	public List<RequestCount> next() {
		DateTime dt = new DateTime().withSecondOfMinute(0).withMillisOfSecond(0);

		// int minuteOfDay = dt.minuteOfDay().get();
		/*
		if (lastSentEvent == null) {
			lastSentEvent = dt.minusMinutes(slotCount).getMillis();
		}
		*/

		List<RequestCount> data;
		try {
			data = apiBean.createSummaryHistogram(lastSentEvent);
		} catch (Exception e) {
			//logger.error("Failed to retrieve data from backend", e);
			throw new RuntimeException(e);
		}

		lastSentEvent = dt.getMillis();

		return data;
	}
}
