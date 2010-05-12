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
    // if the client didn't include browserplus.js, then we cannot run
    if (typeof BrowserPlus == "undefined" || !BrowserPlus) {
        throw "bpInstallLib.js requires browserplus.js to have been included";
    }
    var $BP = BrowserPlus;

    return {
        start: function(args, fn) {
            // allow the client to omit argument specification
            if (fn == null) {
                fn = args;
                args = {};
            }

            // now we'll route through BrowserPlus's init call()
            $BP.init(args, function(r) {
                if (!r.success && r.error === 'bp.notInstalled') {
                    // BrowserPlus is *not* installed!  now it's time to
                    // start the upsell dance.
                    // XXX
                } else {
                    // in all other cases we'll just pass control back to the client
                    // provided callback and let them deal with it.
                    fn(r);
                }
            });
        } 
    };
};
