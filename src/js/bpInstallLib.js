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
    // The java checker object, responsibile for silently checking for the existence
    // of java.
    var JavaChecker = function(cb) {
        // XXX
        cb(false);
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
