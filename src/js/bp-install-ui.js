// create BPTool object if required
if (typeof BPTool == "undefined" || !BPTool) {
	var BPTool = {};
}

BPTool.UIInstaller = typeof BPTool.UIInstaller != "undefined" && BPTool.UIInstaller ? BPTool.UIInstaller : function() {

	var strings = {
		title: "Yahoo! BrowserPlus",
		bd_image: '<img src="logo.png" hspace=5 vspace=5>',
		bd_text: "To continue using all of the features of this website, you need to update your system " + 
				"with the BrowserPlus plug-in.<br><br>" +
				"The installation will take less than a minute and you won't even need to restart your browser.",
		bd_tos: 'I agree to the <a href="#">terms of service</a> and automatic <a href="#">feature updates</a>.',
		bd_continue: "Continue",
		bd_notnow: "Not Now",
		bd_tosnotchecked: "In order to continue, you need to accept the terms and conditions.",
		java_title: "Installing Yahoo! BrowserPlus...",
		fallback_title: "Installing Yahoo! BrowserPlus...",
		fallback_head: "Installing is Easy!",
		fallback_text: "During installation, click Run or Allow if prompted by dialog boxes.",
		done_title: "Yahoo! BrowserPlus Setup - Complete",
		done_head: "You have successfully installed Yahoo BrowserPlus",
		done_text: 'Yahoo! BrowserPlus updates will automatically be downloaded to provide you with the ' + 
			'latest features and security improvements. To change this, see ' + 
			'<a target="blank" href="http://browserplus.yahoo.com/autoupdate">'	 +
			'http://browserplus.yahoo.com/autoupdate</a>.',
		done_button: "Close"
	},
	
	installer,
	
	// Page "2" of dialog for those without Java - shows "Click Allow or Run" text
	fallbackTmpl = 
		'<table border=0 width="100%" height="auto">' +
			'<tbody><tr>' +
				'<td valign=top>{bd_image}</td>' +
				'<td valign=top><h2>{fallback_head}</h2>{fallback_text}</td>' +
			'</tr></tbody>' +
		'</table>',


	// Page "2" of dialog for those with Java - shows progress bar
	javaTmpl = 
		'<div class="ybp_wi_progress_container">' +
			'<div>{bd_image}</div>' +
			'<div id="ybp_wi_progress" style="margin:0 auto;">' +
				'<div id="ybp_wi_progress_bar"></div>' +
				'<div id="ybp_wi_progress_text">0%</div>' +
			'</div>' +
		'</div>',
		
	// Page "3" of dialog, All Done!
	doneTmpl =
		'<table border=0 width="100%" height="auto">' +
			'<tbody><tr>' +
				'<td valign=top>{bd_image}</td>' +
				'<td valign=top><h2>{done_head}</h2>{done_text}</td>' +
			'</tr></tbody>' +
			'<tfoot><tr>' +
				'<td align=right colspan=2>' +
					'<button id="ybp_wi_bt3">{done_button}</button>' +
				'</td>' +
			'</tr></tfoot>' +
		'</table>',
	

	// Page "1" of dialog
	dialogTmpl = 
		'<div class="hd">' +
			'<div id="ybp_wi_title">{title}</div><div id="ybp_wi_close">x</div>' + 
		'</div>' +
		'<div id="ybp_wi_bd">' +
			'<table border=0 width="100%" height="auto">' +
				'<tbody>' + 
					'<tr>' +
						'<td valign=top>{bd_image}</td>' +
						'<td>{bd_text}</td>' +
					'</tr>' +
					'<tr>' +
						'<td align="center" colspan=2>' +
							'<div><input id="ybp_wi_cb" type="checkbox">{bd_tos}</div>' +
							'<p id="ybp_wi_tos_not" style="display:none">{bd_tosnotchecked}</p>' +
						'</td>' +								
					'</tr>' +
				'</tbody>' +
				'<tfoot><tr>'+
					'<td align=right colspan=2>' +
						'<button id="ybp_wi_bt1">{bd_continue}</button> <button id="ybp_wi_bt2">{bd_notnow}</button>' +
					'</td>' +
				'</tr></tfoot>' +
			'</table>' +
		'</div>',
		
		Dialog,
		Overlay,

		ua = function() {
			var o={ie:0, opera:0, gecko:0, webkit: 0};
			var ua=navigator.userAgent, m;

			// Modern KHTML browsers should qualify as Safari X-Grade
			if ((/KHTML/).test(ua)) {
				o.webkit=1;
			}

			// Modern WebKit browsers are at least X-Grade
			m=ua.match(/AppleWebKit\/([^\s]*)/);
			if (m&&m[1]) {
				o.webkit=parseFloat(m[1]);
			}

			if (!o.webkit) { // not webkit
				m=ua.match(/Opera[\s\/]([^\s]*)/);
				if (m&&m[1]) {
					o.opera=parseFloat(m[1]);
				} else { // not opera or webkit
					m=ua.match(/MSIE\s([^;]*)/);
					if (m&&m[1]) {
						o.ie=parseFloat(m[1]);
					} else { // not opera, webkit, or ie
						m=ua.match(/Gecko\/([^\s]*)/);
						if (m) {
							o.gecko=1; // Gecko detected, look for revision
							m=ua.match(/rv:([^\s\)]*)/);
							if (m&&m[1]) {
								o.gecko=parseFloat(m[1]);
							}
						}
					}
				}
			}
			return o;
		}();

	function get(el) {
		return (el && el.nodeType) ? el : document.getElementById(el);
	}
	
	function isString(o) {
		return typeof o === 'string';
	}

	function substitute(s, o) {
		var i, j, k, key, v, meta, saved=[], token, SPACE=' ', LBRACE='{', RBRACE='}';

		for (;;) {
			i = s.lastIndexOf(LBRACE);
			if (i < 0) { break;}
			j = s.indexOf(RBRACE, i);
			if (i + 1 >= j) { break; }

			//Extract key and meta info 
			token = s.substring(i + 1, j);
			key = token;
			meta = null;
			k = key.indexOf(SPACE);
			if (k > -1) {
				meta = key.substring(k + 1);
				key = key.substring(0, k);
			}

			// lookup the value
			v = o[key];
			s = s.substring(0, i) + v + s.substring(j + 1);
		}

		// restore saved {block}s
		for (i=saved.length-1; i>=0; i=i-1) {
			s = s.replace(new RegExp("~-" + i + "-~"), "{"	+ saved[i] + "}", "g");
		}

		return s;
	}


	function addListener(el, type, fn) {
		if (isString(el)) { el = get(el); }

		if (el.addEventListener){
			el.addEventListener(type, fn, false);
		} else if (el.attachEvent) {
			el.attachEvent("on"+type, fn);
		}
	}

	function removeListener(el, type, fn) {
		if (isString(el)) { el = get(el); }

		if (!el) { return; }
		if (el.removeEventListener){
			el.removeEventListener(type, fn, false);
		} else if (el.detachEvent) {
			el.detachEvent("on"+type, fn);
		}
	}
	
	function getViewportSize() {
		var width = self.innerWidth;   // Safari, Operaa
		var height = self.innerHeight; // Safari, Opera
		var mode = document.compatMode;

		if ( (mode || ua.ie) && !ua.opera ) { // IE, Gecko
			if (mode === 'CSS1Compat') {
				// Standards
				height = document.documentElement.clientHeight;
				width  = document.documentElement.clientWidth;
			} else {
				// Quirks
				height = document.body.clientHeight; 
				width  = document.body.clientWidth; 
			}
		}

		return [width, height];
	}

	function resizeCB() {
		// we only need to move the dialog based on scroll position if
		//	 we're using a browser that doesn't support position: fixed, like < IE 7
		var view = getViewportSize();

		var left = window.XMLHttpRequest === null ? document.documentElement.scrollLeft : 0;
		var top = window.XMLHttpRequest === null ? document.documentElement.scrollTop : 0;

		var h = Dialog.offsetHeight;
		var w = Dialog.offsetWidth;

		Dialog.style.left = Math.floor(Math.max((left + (view[0] - w) / 2), 0)) + 'px';
		Dialog.style.top  = Math.max(0, Math.floor(Math.max((top + (view[1] - h) / 2), 0)) - 50) + 'px';
	}

	function goAway() {
		// remove all traces
		removeListener("ybp_wi_bt1", "click", buttonCB);
		removeListener("ybp_wi_bt2", "click", goAway);
		removeListener("ybp_wi_bt3", "click", goAway);
		removeListener("ybp_wi_close", "click", goAway);
		removeListener(window, "resize", resizeCB);
		Dialog.parentNode.removeChild(Dialog);
		Overlay.parentNode.removeChild(Overlay);			
	}

	function buttonCB(e) {
		var tos = get("ybp_wi_cb");
		if (tos.checked) {
			installer.resume();
			// control back to installCB
		} else {
			get("ybp_wi_tos_not").style.display="block";
		}
	}

	function showOverlay() {
		Overlay = document.createElement("div");
		Overlay.id = "ybp_wi_overlay";
		document.body.appendChild(Overlay);
		/*
		if (ua.ie) {
			//overlay.style.top = Math.max(document.body.scrollTop,document.documentElement.scrollTop) + 'px';
			//overlay.left = Math.max(document.body.scrollLeft,document.documentElement.scrollLeft) + 'px';
			var size = getViewportSize();
			overlay.style.width = size[0] + 'px';
			overlay.style.height = size[1] + 'px';		
		}
		*/
	}

	function showDialog() {
		Dialog = document.createElement("div");
		Dialog.id = "ybp_wi_dialog";
		document.body.appendChild(Dialog);

		Dialog.innerHTML = substitute(dialogTmpl, strings);
		resizeCB();
		addListener(window, "resize", resizeCB);
		addListener("ybp_wi_bt1", "click", buttonCB);
		addListener("ybp_wi_bt2", "click", goAway);
		addListener("ybp_wi_close", "click", goAway);
	}
	
	function installCB(e, ii) {
		var bd, title, percent, type = e.type, pbar, ptext, dialog;

		dialog =  get("ybp_wi_dialog");

		bd = get("ybp_wi_bd");
		title = get("ybp_wi_title"),
		percent = 0;

		if (type === "javaCheck") {
			e.pause();
			showOverlay();
			showDialog();
		} else if (type === "startFallbackInstall") {
			removeListener("ybp_wi_bt1", "click", buttonCB);
			removeListener("ybp_wi_bt2", "click", goAway);
			bd.innerHTML = substitute(fallbackTmpl, strings);
			title.innerHTML = strings.fallback_title;
			// show instructions
		} else if (type === "startJavaInstall") {
			removeListener("ybp_wi_bt1", "click", buttonCB);
			removeListener("ybp_wi_bt2", "click", goAway);

			bd.innerHTML = substitute(javaTmpl, strings);
			title.innerHTML = strings.java_title;
			// show java install progress
		} else if (type === "running") {
			if (e.hasOwnProperty('percent')) {
				pbar = get("ybp_wi_progress_bar");
				ptext = get("ybp_wi_progress_text");
				if (pbar && ptext) {
					percent = ""+parseInt(e.percent, 10) + "%";
					get("ybp_wi_progress_bar").style.width = percent;
					get("ybp_wi_progress_text").innerHTML = percent;
				}
			}
		} else if (type === "complete" && dialog) {
			bd.innerHTML = substitute(doneTmpl, strings);
			title.innerHTML = strings.done_title;
			addListener("ybp_wi_bt3", "click", goAway);
			// need to register Done button
		}
	}


	installer = BPInstaller({
		pathToJar: ".",
		installJarName: "bp_installer_signed.jar",
		checkJarName: "bp_java_check.jar",
		installURL: "http://browserplus.yahoo.com/dist/v2/installer/",
		eventHandler: installCB});

	return {
		start: function(cb) {
			installer.start(function(r) {
				cb(r);
			});
		}
	};

}();