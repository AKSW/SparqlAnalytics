(function() {
	var ns = Namespace("org.aksw.utils");
	
	ns.roundNumber = function(number, decimals) {
		var result = Math.round(number * Math.pow(10, decimals)) / Math.pow(10, decimals);
		
		return result;
	};
	
})();
