package org.aksw.sparql_analytics.web.live;

import java.util.List;

import org.aksw.sparql_analytics.core.ApiBean;
import org.aksw.sparql_analytics.core.RequestCount;
import org.atmosphere.cpr.Broadcaster;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class EventGenerator {

	private static final Logger logger = LoggerFactory.getLogger(EventGenerator.class);
	
	private static final String GENERATOR_THREAD_NAME = "GeneratorThread";

	private volatile Thread generatorThread;

	/**
	 * Starts the generation of random events. Each event is broadcasted using
	 * the provided Broadcaster. This method does nothing if a Generator is
	 * already running.
	 */
	public synchronized void start(Broadcaster broadcaster, ApiBean apiBean, Object queryNotifier) {
		if (generatorThread == null) {
			Generator generator = new Generator(broadcaster, apiBean, queryNotifier);
			generatorThread = new Thread(generator, GENERATOR_THREAD_NAME);
			generatorThread.start();
		}
	}

	/**
	 * Stops the generation of random events. This method does nothing if no
	 * Generator was started before.
	 */
	public synchronized void stop() {
		if (generatorThread != null) {
			Thread tmpReference = generatorThread;
			generatorThread = null;
			tmpReference.interrupt();
		}
	}
	
	/**
	 * Notify the thread that an event has happened and it may have to send new data
	 * 
	 */
	public synchronized void ping() {
		generatorThread.notifyAll();
	}
	

	private class Generator implements Runnable {

		private final Broadcaster broadcaster;
		private final ApiBean apiBean;
		private final Object queryNotifier;
		
		//private final int interval;
		
		private DateTime lastSentEvent; // TODO Add time unit
		private int slotCount = 20;
		
		
		//Timestamp 
		

		public Generator(Broadcaster broadcaster, ApiBean apiBean, Object queryNotifier) {
			this.broadcaster = broadcaster;
			this.apiBean = apiBean;
			this.queryNotifier = queryNotifier;
			
			//this.interval = interval;
		}

		@Override
		public void run() {
			logger.info("Starting Event Generator Thread");
			Thread currentThread = Thread.currentThread();
			while (currentThread == generatorThread) {
				
				try {
					logger.info("Broadcaster thread going to wait for events");
					
					synchronized(queryNotifier) {
						queryNotifier.wait();
					}
					logger.info("Broadcaster thread woke up");
					
					// Once we were notified, we wait for another second before we actually begin our work
					Thread.sleep(1000);
					
					DateTime dt = new DateTime();
					//int minuteOfDay = dt.minuteOfDay().get();
					
					if(lastSentEvent == null) {
						lastSentEvent = dt.minus(slotCount);
					}

					List<RequestCount> data;
					try {
						data = apiBean.createSummaryHistogram(lastSentEvent.getMillis());
					} catch (Exception e) {
						logger.error("Failed to retrieve data from backend", e);
						continue;
					}
					
					Gson gson = new Gson();
					String json = gson.toJson(data);
					
					broadcaster.broadcast(json);
					//Thread.sleep(interval);
				} catch (InterruptedException e) {
					break;
				}
			}
		}

	}

}
