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
    // class global data
    var UNIQUE_ID_FRAGMENT = 'bp75717a74cec54ae480567a4a7b07b60d';

    // instance data
    var STATE = "allocated";
    var CANCELED = false; // gets flipped when the client cancels us
    var PAUSED = false; // gets flipped when the client pauses us at a given event
    var $BP = BrowserPlus;
    var clientCallback = null;
    var initArgs = null;
    var javaVersion = null;
    var errorReturn = null;

    // this whole thing is a big state machine
    var TheMachine = {
        /* entries are <state_name> => [ <emit_event?>, <pausable?>, <state function>,
                                         [ allowable source states ], <description> ] */
        start: [
            false, false, start_StateFunction,
            "immediately occurs immediately allocation and argument validation"
        ],
        bpCheck: [
            true, true, bpCheck_StateFunction,
            "after the client calls start(), immediately before we call BrowserPlusInit"
        ],
        javaCheck: [
            true, true, javaCheck_StateFunction,
            "immediately before the check for Java on the machine"
        ],
        startJavaInstall: [
            true, true, startJavaInstall_StateFunction,
            "after we've confirmed that java is available on the machine but before we begin " +
            "the installation in earnest"
        ],
        startFallbackInstall: [
            true, true, startFallbackInstall_StateFunction,
            "after we've confirmed that java is available on the machine but before we begin " +
            "the installation in earnest"
        ],
        downloading: [
            true, false, downloading_StateFunction,
            "In the process of downloading the BrowserPlus installer"
        ],
        complete: [
            true, false, complete_StateFunction,
            "We expect that the installation has completed successfully and should be able to immediately " +
            "invoke the client's callback"
        ],
        error: [
            true, false, error_StateFunction,
            "an unrecoverable error was encountered during the installation attempt"
        ]
    };

    var debug = function(msg) {
        if (cfg && cfg.debug) {
            cfg.debug(msg);
        }
    }

    var emitEvent = function(e, pausable, extra) {
        if (cfg && cfg.eventHandler) {
            var ev = {
                event: e,
                desc: TheMachine[e][3],
                pausable: pausable,
                pause: pausable ? function() { PAUSED = true; } : null,
            };
            if (extra === null) extra = {};
            for (k in extra) {
                if (extra.hasOwnProperty(k)) {
                    ev[k] = extra[k];
                }
            }
            cfg.eventHandler(self, ev);
        }
    }

    function raiseError(error, verboseError) {
        if (error === null) { error = "bp.installationError"; }
        if (verboseError === null) { verboseError = "" };
        errorReturn = {
            success: false,
            error: error,
            verboseError: verboseError
        };
        stateTransition('error');
    }

    var stateTransition = function (state, extra) {
        debug("Attempt to transition from '"+STATE+"' to '"+state+"'");
        if (!TheMachine[state]) {
            throw "attempt to transition to non-existent state: " + state;
        }
        STATE = state;
        var s = TheMachine[STATE];
        // if this is a state where we emit, then emit
        if (s[0]) { emitEvent(state, s[1], extra); }

        // if we are not paused, then move into the state
        if (!PAUSED && !CANCELED) { s[2](); } 
    };

    function getAppletContainer(divId, appletName, jarName, javaClass, params) {
        var t =
            '<applet codebase="' + cfg.pathToJar + '"' +
            ' code="'+javaClass+'"' +
            ' archive="' + jarName + '"' +
            ' width="0" height="0" name="' + appletName + '" mayscript="true">';

        if (!params.codebase_lookup) {
            params["codebase_lookup"] = false;
        }

        for (var param in params) {
            if (params.hasOwnProperty(param)) {
                t += '<param name="'+param+'" value="'+params[param]+'"></param>';
            }
        }
        t += '</applet>';

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
        div.innerHTML = t;
        return div;
    }

    // The java checker object, responsibile for silently checking for the existence
    // of java.
    function javaCheck_StateFunction() {
        debug("building java check DOM node"); 
        var divId = UNIQUE_ID_FRAGMENT + "_check_id";
        var appletName = UNIQUE_ID_FRAGMENT + "_check_name";
        var div = getAppletContainer(divId, appletName, cfg.checkJarName,
            "com.yahoo.browserplus.installer.javatest.class", {});
        debug("appending java check DOM node to DOM"); 
        document.body.appendChild(div);

        // an async break to allow the applet to become ready.
        debug("async break to allow for applet readiness"); 
        setTimeout(function() {
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
            if (javaVersion !== null) {
                stateTransition("startJavaInstall");
            } else {
                stateTransition("startFallbackInstall");                
            }
        }, 0);                   
    };

    function startJavaInstall_StateFunction() {
        debug("building java check DOM node"); 
        var divId = UNIQUE_ID_FRAGMENT + "_install_id";
        var appletName = UNIQUE_ID_FRAGMENT + "_install_name";
        var div = getAppletContainer(divId, appletName, cfg.installJarName,
            "com.yahoo.browserplus.installer.bplusloader.class",
            {
                installerBaseURL: cfg.installURL                
            });
        debug("appending java check DOM node to DOM"); 
        document.body.appendChild(div);

        // an async break to allow the applet to become ready.
        debug("async break to allow for applet readiness"); 
        var pollerId = setInterval(function() {
            try {
		        var applet = document[appletName];
                var status = applet.status().status;
                debug("applet status: " + status);
                if (status === 'error') {
                    clearInterval(pollerId);
                    debug("java installer encountered an error"); 
                    raiseError("bp.installerJavaError", "java installer encountered an error");
                } else if (status === 'complete') {
                    
                } else if (status === 'downloading') {
                    stateTransition('downloading', {percent: applet.status().percent}); 
                } else {
                    debug("UNEXPECTED STATUS: " + status);
                }
            } catch (e) {
                clearInterval(pollerId);
                debug("that was exceptional: " + e.name + ": " + e.message); 
                raiseError("bp.installerJavascriptError", e.name + ": " + e.message);
            }
        }, 250);
    }

    function startFallbackInstall_StateFunction() {
        raiseError("bp.notImplemented", "not yet implemented");
    }


    function start_StateFunction()  {
        debug("validating inclusion of browserplus.js"); 
        // if the client didn't include browserplus.js, then we cannot run
        if (typeof BrowserPlus == "undefined" || !BrowserPlus) {
            throw "bpInstallLib.js requires browserplus.js to have been included";
        }
        debug("validated!"); 

        // verify required arguments
        debug("validating required arguments"); 
        {
            var requiredArgs = [ "pathToJar", "installJarName", "checkJarName", "installURL" ];
            for (a in requiredArgs) {
                if (!cfg || !cfg[requiredArgs[a]]) {
                    throw "BPInstaller missing required '"+ requiredArgs[a] +"' config parameter";
                }
            }
        }
        debug("validated!"); 
    }


    function downloading_StateFunction() {
        // noop
    }

    function complete_StateFunction() {
        // XXX: now we just need to init() browserplus and return!
        raiseError("bp.notImplemented", "not yet implemented");
    }

    function bpCheck_StateFunction() {
        // now we'll route through BrowserPlus's init call()
        $BP.init(initArgs, function(r) {
            if (!r.success && r.error === 'bp.notInstalled') {
                // BrowserPlus is *not* installed!  now it's time to
                // start the upsell dance.
                debug("BrowserPlus not installed, checking for presence of java"); 
                stateTransition("javaCheck");
            } else {
                debug("error returned from init, aborting installation: " + r.error); 
                errorReturn = r;
                stateTransition("error");
            }
        });
    }

    function error_StateFunction() {
        CANCELED = true;
        clientCallback(errorReturn);
    }
        
    stateTransition("start");

    var self = {
        start: function(args, fn) {
            // allow the client to omit argument specification
            if (fn == null) {
                fn = args;
                args = {};
            }
            clientCallback = fn;
            initArgs = args;
            stateTransition("bpCheck");
        },
        cancel: function() {
            CANCELED = true;              
        },
        "continue": function() {
            debug("client invokes continue when in the '"+STATE+"' state");
            if (PAUSED) {
                PAUSED = false;
                if (TheMachine[STATE] && TheMachine[STATE][2]) {
                    debug("continuing from '"+STATE+"'");
                    TheMachine[STATE][2]();
                } else {
                    debug("no work to be done to continue from this state");
                }
            }
        }
    };

    return self;
};
