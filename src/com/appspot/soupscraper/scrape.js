
function scrape (s,d){
	if (s == 'HTML') return d.innerHTML;
	if (s == 'TEXT') return d.innerText || d.textContent;
	if (typeof s == 'string') {
		if (s.match(scrape.REGEX_ATTRIB))
			return d.getAttribute(s.replace(scrape.REGEX_ATTRIB,''));
		if (typeof d[s] != 'undefined')
			return d[s];
		return "[unsupported scrape: '"+s+"'] maybe you're looking for '@"+s+"' ?";
	}
	var r={};
	for (var sk in s) {
		if (s.prototype && s[sk] == s.prototype[sk]) continue;
	    var selector = s[sk][0];
	    var ss = s[sk][1];
	    var many = sk.match(scrape.REGEX_MANY);
	    var res = scrape.SELECTOR_ENGINE(selector, d);
	    if (many) {
	        sk = sk.replace(/\[\]$/,'');
	        r[sk] = [];
	        for(var i=0; i<res.length; i++) {
	            r[sk][i] = scrape(ss,res[i]);
	        }
	    }
	    else {
	        r[sk] = res.length ? scrape(ss,res[0]) : null;
	    }
	}
	return r;
}
scrape.REGEX_MANY = /\[\]$/;
scrape.REGEX_ATTRIB = /^@/;
scrape.SELECTOR_ENGINE = Sizzle;

/* short hand to call from JAVA */
scrape.JSONrequest = function(json, xml, indent) {
	return JSON.stringify(scrape(JSON.parse(json), document.loadXML(xml)), null, indent || 3);
}