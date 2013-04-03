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
	private Date date;
	private int requestCount;
	
	public RequestCount() {
		super();
	}

	public RequestCount(Date date, int requestCount) {
		super();
		this.date = date;
		this.requestCount = requestCount;
	}

	public Date getDate() {
		return date;
	}

	public int getRequestCount() {
		return requestCount;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public void setRequestCount(int requestCount) {
		this.requestCount = requestCount;
	}
}
