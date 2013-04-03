var SparqlAnalytics;

(function(ns) {
	
	ns.Path = function(steps) {
		this.steps = steps;
	};
	
	ns.Path.create = function(str) {
		var steps = str.split("/");
		
		var result = new ns.Path(steps);
		return result;
	};
	
	ns.Path.prototype = {
		toString: function() {
			return this.steps.join("/");
		},
		
		pop: function() {
			this.steps.pop();
		},
		
		getLastStep: function() {
			var n = this.steps.length;
			if(n === 0) {
				return null;
				//throw "Cannot get last element because there are no steps in the path";
			}
			
			return this.steps[n - 1];
		},
		
		concat: function(that) {
			//console.log("Concat invoked - arg is: ", that);
			
			for(var i = 0; i < that.steps.length; ++i) {
				var s = that.steps[i];
				
				if(s === "..") {
					this.pop();
				} else {
					this.steps.push(s);
				}
			}
			
			return this;
		}
	};

	ns.getDocumentLocationPath = function() {
		var base = document.location.toString();

		var result = ns.Path.create(base);
		
		var lastStep = result.getLastStep();
		if(!lastStep) {
			result.pop();
			lastStep = result.getLastStep();
		} 

		
		var isFile = (lastStep.indexOf(".") > 0);			
		if(isFile) {
			result.pop();
		}
		
		return result;
	};

	ns.fillArray = function(len, value) {
		var result = Array.apply(null, new Array(len)).map(Number.prototype.valueOf, value);
		return result;
	};
	
})(SparqlAnalytics || (SparqlAnalytics = {}));
