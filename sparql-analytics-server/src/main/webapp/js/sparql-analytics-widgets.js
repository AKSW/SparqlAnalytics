/*
 * Ready-to-use widgets for sparql-analytics
 * 
 */

Highcharts.setOptions({
    global: {
        useUTC: false
    }
});


var SparqlAnalytics;

(function(ns) {

	var charts = Namespace("org.aksw.sparql-analytics.charts");

	
	/**
	 * Options must include:
	 * 
	 * options = {
	 *     el: 'element where to attach the chart',
	 *     apiUrl: '' (connect, start and stop is API convention),
	 * }
	 * 
	 */
	ns.WidgetChartQueryLoad = function(options) {

		this.options = options;
		
		this.chart = null;

		this.init();
		this.connect();
		this.start();
	};
	
	ns.WidgetChartQueryLoad.prototype = {
			
			init: function() {
				var options = this.options;
				
				var chartDiv = $(options.el);
				var elChartDiv = chartDiv.get(0);
			
				var chartSpec = charts.createHistogramChartSpec({
					name: "Recent Query Load"
				});
			
				chartSpec.chart.renderTo = elChartDiv;
				
				this.chart = new Highcharts.Chart(chartSpec);
				this.chartController = new ns.ChartController(this.chart);
				
				var self = this;
				var messageHandler = {
					url: this.options.apiUrl,
						
					onMessage: function(response) {
						var str = response.responseBody;
						if(str.length > 0) {
							var data = JSON.parse(str);
							//console.log("Got data:", data);
							
							if(data.reset) {
								self.chartController.reset(data.reset);
							}
							
							if(data.update) {
								self.chartController.update(data.update);
							}
						}
					}
				}
	
				this.webSocketAgent = new ns.WebSocketAgent(messageHandler);	

			},
			
			connect: function() {
				this.webSocketAgent.connect();
			},
			
			start: function() {
				$.ajax({
					type: 'GET',
					url: this.options.apiUrl + '/start'
				});
			}
	};
	
})(SparqlAnalytics || (SparqlAnalytics = {}));
