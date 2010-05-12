/*!
 *  bpInstallLib.js - A little javascript library which makes it easy
 *               to integrate in-page browserplus installation.
 *
 *  Usage:
 *
 *    installer = BPInstaller({
 *      config: "params"
 *    });
 *
 *    installer.start(callback);
 *
 *  (c) Yahoo! Inc 2010 All rights reserved
 */

// handle multiple inclusions of this file
BPInstaller = typeof BPInstaller != "undefined" && BPInstaller ? BPInstaller : function(cfg) {
    // verify required arguments
    var requiredArgs = [ "pathToJar", "jarName" ];
    for (a in requiredArgs) {
        if (!cfg || !cfg[requiredArgs[a]]) {
            throw "BPInstaller missing required '"+ requiredArgs[a] +"' config parameter";
        }
    }
    if (!cfg.pathToJar) {
        throw "BPInstaller missing required 'pathToJar' config parameter";
    }
 
    var UNIQUE_ID_FRAGMENT = 'bp75717a74cec54ae480567a4a7b07b60d';

    // The java checker object, responsibile for silently checking for the existence
    // of java.
    var JavaChecker = function(cb) {
        debug("building java check DOM node"); 
        var divId = UNIQUE_ID_FRAGMENT + "_check_id";
        var appletName = UNIQUE_ID_FRAGMENT + "_check_name";

	    var div = document.createElement("div");
        div.id = divId;
        div.style.visibility = "hidden";
        try { div.style.borderStyle = "hidden"; } catch (e) { }
        div.style.width = 0;
        div.style.height = 0;
        div.style.border = 0;
        div.style.position = "absolute";
        div.style.top = 0;
        div.style.left = 0;
        div.innerHTML =
            '<applet codebase="' + cfg.pathToJar + '"' +
            ' code="com.yahoo.browserplus.installer.javatest.class"' +
            ' archive="' + cfg.jarName + '"' +
            ' width="0" height="0" name="' + appletName + '" mayscript="true">' +
            '<param name="codebase_lookup" value="false"></param>' +
            '</applet>';
        debug("appending java check DOM node to DOM"); 
        document.body.appendChild(div);

        // an async break to allow the applet to become ready.
        debug("async break to allow for applet readiness"); 
        setTimeout(function() {
            var javaVersion = null;
            try {
                javaVersion = document[appletName].getJavaVersion();
            } catch (e) {
            }
            
            // remove pollutant from DOM
            try {
                document.body.removeChild(document.getElementById(divId));
            }
            catch(e) {
                try { debug("couldn't remove applet tag: " + e); } catch(e) {}
            }

            // XXX: parse javaVersion to ensure correct handling of older versions.
            cb(javaVersion !== null);
        }, 0);                   
    };

    var debug = function(msg) {
        if (cfg && cfg.debug) {
            cfg.debug(msg);
        }
    }

    debug("validating inclusion of browserplus.js"); 
    // if the client didn't include browserplus.js, then we cannot run
    if (typeof BrowserPlus == "undefined" || !BrowserPlus) {
        throw "bpInstallLib.js requires browserplus.js to have been included";
    }
    debug("validated!"); 

    debug("validating required arguments"); 
    debug("validated!"); 

    var $BP = BrowserPlus;
    var clientCallback = null;

    var javaCheckComplete = function(r) {
        debug("got java? - " + r); 
    };

    var self = {
        start: function(args, fn) {
            // allow the client to omit argument specification
            if (fn == null) {
                fn = args;
                args = {};
            }
            clientCallback = fn;

            debug("invoking BrowserPlus init to check installed status"); 
            
            // now we'll route through BrowserPlus's init call()
            $BP.init(args, function(r) {
                // XXX: remove this "true"
                if (true) { //(!r.success && r.error === 'bp.notInstalled') {
                    // BrowserPlus is *not* installed!  now it's time to
                    // start the upsell dance.
                    debug("BrowserPlus not installed, checking for presence of java"); 
                    JavaChecker(javaCheckComplete);
                } else {
                    debug("error returned from init, aborting installation: " + r.error); 
                    
                    // in all other cases we'll just pass control back to the client
                    // provided callback and let them deal with it.
                    fn(r);
                }
            });
        } 
    };

    return self;
};
