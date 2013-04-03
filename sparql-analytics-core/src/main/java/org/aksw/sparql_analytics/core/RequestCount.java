package org.aksw.sparql_analytics.core;

import java.util.Date;

/**
 * Event for notification of a query count within a
 * (most recent) time span.
 * 
 * The date field marks the beginning of that time span.
 * 
 * A client may receive multiple events with the timestamp span,
 * which is to be interpreted as updates for that timestamp.
 * 
 * @author Claus Stadler
 *
 */
public class RequestCount {
	private long timestamp;
	private int requestCount;
	
	public RequestCount() {
		super();
	}

	public RequestCount(long timestamp, int requestCount) {
		super();
		this.timestamp = timestamp;
		this.requestCount = requestCount;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public int getRequestCount() {
		return requestCount;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public void setRequestCount(int requestCount) {
		this.requestCount = requestCount;
	}
}
