//var apiUrl = "" + SparqlAnalytics.getDocumentLocationPath().concat(SparqlAnalytics.Path.create('../sparql-analytics/api/live'));
var apiUrl = "http://localhost:5522/sparql-analytics/api/live"

/*
$.ajax({
	type: 'GET',
	url: "http://localhost:5522/sparql-analytics/api/data/min",
	crossDomain: true
}).done(function(data) {
	console.log("Got: " + data);
}).fail(function(data) {
	console.log("Failed to access API: ", data);
});
*/



ns.WebSocketAgent = function(handler) {
	this.socket = null;
	this.handler = handler;
}

ns.WebSocketAgent.prototype = {
		'connect': function() {
				var self = this;
			
				var request = {
						url: apiUrl,
						logLevel : 'trace',
						transport: 'websocket', /* websocket, jsonp, long-polling, polling, streaming */
						fallbackTransport: 'streaming',
						attachHeadersAsQueryString: true,
						/*
						headers: {
							'X-Map-Bounds': {}//mapAgent.getBoundsHeader()
						}
						*/
						/* CORS */
						enableXDR: true,
						readResponsesHeaders: false,
						
						/* Callbacks */
						onOpen: function(response) {
							console.log('Connected to realtime endpoint using ' + response.transport);
						},
						onReconnect: function(response) {
							console.log('Reconnecting to realtime endpoint');
						},
						onClose: function(response) {
							console.log('Disconnected from realtime endpoint');
						},
						onMessage: function(response) {
							var data = response.responseBody;
							if (data.length > 0) {
								// Shouldn't 'data' be in JSON format in the first place?
								var json = JSON.parse(data);
								
								console.log('Message Received using ' + response.transport + ': ', data);
								
								var handler = self.handler;								
								if(handler) {
									handler.onMessage(json);
								}
							
							}
						}
				};
				
				this.socket = $.atmosphere.subscribe(request);
		},
		'disconnect': function() {
			$.atmosphere.unsubscribe();
			this.socket = null;
		},
//		'update': function(bounds) {
//			if (!this.socket) return;
//			this.socket.push(JSON.stringify(bounds));
//		},
		'trigger': function(data) {
			if (!this.socket) {
				console.log("No socket open - data not send: " + data);
				return;
			}
			$.ajax({
				type: 'POST',
				url: apiUrl + '/event',
				contentType: 'application/json',
				data: JSON.stringify(data)
			});
		}
};

ns.SeriesApi = function(series) {
	this.series = series; 
};

ns.SeriesApi.prototype = {
	getIndexForX: function(x) {
		var result = null;
		var points = this.series.points;

		for(var i = 0; i < points.length; i++) {
			var point = points[i];
			//console.log("x scan - value: ", point);
			var pX = point.x;
			if(x == pX) {
				result = i;
				break;
			}
		}

		return result;
	},
	
	/*
	setX: function(x, value) {
		
	},
	*/
	
	getIndexForMaxX: function() {
		var maxI = null;
		var maxValue = null;
		var points = this.series.points;

		for(var i = 0; i < points.length; i++) {
			var point = points[i];
			var x = point.x;
			if(!maxValue || x > maxValue) {
				maxValue = x;
				maxI = i;
			}
		}

		return maxI;
	},
	
	getMaxX: function() {
		var i = this.getIndexForMaxX();
		
		var result;
		if(i) {
			result = this.series.points[i].x;
		} else {
			result = null;
		}
		
		return result;
	},
	
	getIndexForMinX: function() {
		var minI = null;
		var minValue = null;
		var points = this.series.points;

		for(var i = 0; i < points.length; i++) {
			var point = points[i];
			var x = point.x;
			if(!minValue || x < minValue) {
				minValue = x;
				minI = i;
			}
		}

		return minI;
	},
	
	getPointForIndex: function(i) {
		return this.series.points[i];
	},
	
	getPointForX: function(x) {
		var result = null;
		
		var i = this.getIndexForX(x);
		if(i) {
			result = this.getPointForIndex(i);
		}
		
		return result;
	},
	
	addPoint: function() {
		this.series.addPoint.apply(this.series, arguments);
	},
	
	setData: function() {
		this.series.setData.apply(this.series, arguments);
	},	
	
	getData: function() {
		return this.series.data;
	}
};



ns.ChartController = function(chart) {
	this.chart = chart;
	this.series = new SeriesApi(this.chart.series[0]);
	
	this.clientN = 120;
	
	var self = this;
	
	// Generate update events on the client side in case the server is idle
	// TODO: Factor this component out
	this.clientUpdate = null;
}


ns.ChartController.prototype = {
		/**
		 * Required attributes:
		 * 
		 * startTimestamp
		 * timeWindow
		 */
		reset: function(options) {
			console.log("Chart reset with ", options);
			
			var now = new Date().getTime();
			
			var start = options.startTimestamp;
			var window = options.timeWindow;

			//console.log("Start: " + start);
			//console.log("Now: " + now);
			
			// Number of slots as responded by the server
			var serverN = Math.floor(((now - start) / window)) + 1; 

			// No matter what the server tells us, we are only going to use this number of slots
			var clientN = this.clientN;
			
			// Server = 100 slots, client 10 -> skip 90 slots 
			var delta = serverN - clientN;
			console.log("serverN, clientN, delta: ", serverN, clientN, delta);
			
			var data = new Array(clientN); //SparqlAnalytics.fillArray(n);
			var t = start + delta * window; // Start time of the first client slot
			for(var i = 0; i < clientN; ++i) {
				
				//console.log("Init Date: " + new Date(t));
				
				data[i] = [t, 0];
				t += window;
			}
			
			
			// Note: Highcharts adapts the scale to the current date, but not the data
			
			this.series.setData(data);
			
			var self = this;
			this.setupClientUpdate(t, window, function(timestamp, window) {
				//console.log("Performing self update.");
				self.update([{timestamp: timestamp, requestCount: 0}]); 
			});
		},
		

		setupClientUpdate: function(start, window, fnWork) {

			var now = new Date().getTime();
			if(start < now) {
				// The start lies in the past
				//start = 10, now = 100, window = 5 -> we have to catch up 18 windows

				// Catch-up loop
				do {
					fnWork(start, window);
					
					start += window;
					now = new Date().getTime();

					factor = Math.floor(((now - start) / window));
				} while(factor > 0)				
			}
			// start = 100, now = 10, window = 5 ->  we can wait for 90/5=18 windows
				
				
			var delta = start - now;
			console.log("Scheduling self update in " + (delta / 1000.0) + " seconds.");
			
			if(this.clientUpdate) {
				clearTimeout(this.clientUpdate);
			}
			
			var self = this;
			this.clientUpdate = setTimeout(function() {
				fnWork(start, window);
				
				var nextStart = start + window;
				self.setupClientUpdate(nextStart, window, fnWork);

			}, delta);
			
			
		},

		
		/**
		 * Array of datapoints.
		 * 
		 * Datapoint:
		 * 
		 */
		update: function(rawData) {
			
			var dataPoints = [];
			
			for(var i = 0; i < rawData.length; ++i) {
				var raw = rawData[i];
				var dataPoint = [raw.timestamp, raw.requestCount];
				dataPoints.push(dataPoint);
			}
			
			var series = this.series;
			
			console.log("Chart update with: ", dataPoints);
			for(var i = 0; i < dataPoints.length; ++i) {
				var dataPoint = dataPoints[i];
				var x = dataPoint[0];
				var y = dataPoint[1];
				
				var p = series.getPointForX(x);
				
				//console.log("point, x, y: ", p, x , y);
				if(p) {
					p.update(y, true);
					//var data = this.series.getData();
					//data[j] = y;
					
					//this.series.setData(data);
				} else {
					var maxX = series.getMaxX();
					if(x > maxX) {
						series.addPoint(dataPoint, true, true);
					}
					else {
						console.log("[WARN] Discarded data point because couldn't decide what to do with: ", dataPoint);
					}
				}
			}
		}
};

var chartController = new ChartController(activeChart);


var messageHandler = {
	onMessage: function(data) {
		//console.log("Got data:", data);
		
		if(data.reset) {
			chartController.reset(data.reset);
		}
		
		if(data.update) {
			chartController.update(data.update);
		}
		
		
	}
}

var webSocketAgent = new WebSocketAgent(messageHandler);

webSocketAgent.connect();

$.ajax({type: 'GET', url: apiUrl + '/start'});


/*
var statsAgent = {
		frequency: 1000,
		totalEvent: 0,
		numEvent: 0,
		running: false,
		timer: null,
		totalCallback: null,
		epsCallback: null,
		'process': function() {
			var eps = (this.numEvent / this.frequency) * 1000;
			this.numEvent = 0;
			if (this.epsCallback) this.epsCallback(eps);
		},
		'start': function() {
			this.stop();
			this.timer = setInterval(function() {statsAgent.process();}, this.frequency);
			this.running = true;
			this.numEvent = 0;
		},
		'stop': function() {
			clearInterval(this.timer);
			this.running = false;
		},
		'notify': function() {
			this.numEvent++;
			this.totalEvent++;
			if (this.totalCallback) this.totalCallback(this.totalEvent);
		}
};
*/
/*
var mapAgent = {
		map: null,
		'init': function(target, center, zoom) {
			var map = L.map(target).setView(center, zoom);
			L.tileLayer('http://{s}.mqcdn.com/tiles/1.0.0/osm/{z}/{x}/{y}.png', {
				subdomains: ['otile1','otile2','otile3','otile4'],
				maxZoom: 18,
			}).addTo(map);
			this.map = map;
			// Add listeners
			map.on('click', function(event) {
				var latLng = event.latlng;
				console.log('Trigger event:', JSON.stringify(latLng));
				webSocketAgent.trigger(latLng);
			});
			map.on('moveend', function(event) {
				var bounds = mapAgent.getBounds();
				console.log('Map bounds changed:', JSON.stringify(bounds));
				webSocketAgent.update(bounds);
			});
		},
		'getBounds': function() {
			var bounds = this.map.getBounds();
			return {
				'southLat': bounds.getSouthWest().lat,
				'northLat': bounds.getNorthEast().lat,
				'westLng': bounds.getSouthWest().lng,
				'eastLng': bounds.getNorthEast().lng
			};
		},
		'getBoundsHeader': function() {
			var bounds = this.getBounds();
			return bounds.southLat + ',' + bounds.northLat + ',' + bounds.westLng + ',' + bounds.eastLng;
		},
		'drawEvent': function(json) {
			var marker = L.marker([json.lat, json.lng]).addTo(mapAgent.map);
			setTimeout(function() {
				mapAgent.map.removeLayer(marker)
			}, 2000);
		}
};
*/
