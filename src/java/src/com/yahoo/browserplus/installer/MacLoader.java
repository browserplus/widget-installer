/*
 * Copyright 2010, Yahoo!
 *  
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are
 *  met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in
 *     the documentation and/or other materials provided with the
 *     distribution.
 *  3. Neither the name of Yahoo! nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT,
 *  INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *   SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 *  HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 *  STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
 *  IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE.
 */

package com.yahoo.browserplus.installer;

import java.applet.Applet;
import java.io.File;

public class MacLoader extends BootstrapLoader {
            
    public MacLoader(Applet applet){
        super();
        
        config = new MacInstallerConfig( applet );
                
        bplusloader.LOG( "MAC Loader created" );
    }
    
    public String getInstallerUrl(){
        String url = config.getInstaller();
        bplusloader.LOG( "Installer Url: " +  url );
        
        return url;
    }
    
    public String getDestination(){
        String destination = System.getProperty("java.io.tmpdir") +
            File.separator + config.getDestination();
        return destination;
    }
    
    public void loadInstaller()
        throws java.io.IOException, java.lang.InterruptedException
    {
        
        bplusloader.LOG("Begin extraction and execution of BrowserPlus installer");
        Runtime rt = Runtime.getRuntime();
            
        String tmpdir = System.getProperty("java.io.tmpdir") +
            File.separator;

        String dmgPath = getDestination();
        String mountPoint = dmgPath.substring(0, dmgPath.indexOf(".dmg"));
            
        try {            
            bplusloader.LOG( "Toggling quanrantine bit on downloaded .app" );
            String[] quarantineCommand = {
                "xattr", "-d", "com.apple.quarantine", dmgPath
            };
            Process quanrantineProc = rt.exec(quarantineCommand);
            quanrantineProc.waitFor();
        } catch (Throwable t) {
            bplusloader.LOG( "Can't toggle Quarantine bit, perhaps this is "
                             + "< OSX leopard?  no worries." );
        }
            
        String[] mountCommand = {
            "hdiutil",
            "mount",
            "-private", dmgPath,
            "-mountpoint", mountPoint};

        bplusloader.LOG( "Mounting DMG: " + dmgPath + " in:" + mountPoint );
        Process mount = rt.exec(mountCommand);
            
        bplusloader.LOG( "Waiting for DMG to be mounted" );
        mount.waitFor();

        String installerPath = mountPoint +
            "/BrowserPlus Installer.app/Contents/MacOS/BrowserPlusInstaller";
            
        bplusloader.LOG( "Opening installer: " + installerPath );
        // open -W would work great if it weren't for the fact that
        // 10.4 doesn't support the -W flag (wait for completion before
        // exiting).  For this reason we specify the full path to the
        // binary withing the installer.
        String[] openCommand = {
            installerPath,
            "-nogui=true",
            "-verbose=true"
        };
        Process runInstaller = rt.exec(openCommand);
            
        // wait for the installation to complete so we can properly
        // clean up
        runInstaller.waitFor();

        bplusloader.LOG( "Installer complete, now unmounting " + mountPoint );

        // now unmount the image
        String[] unmountCommand = {
            "hdiutil",
            "unmount",
            mountPoint
        };
        Process unmountProc = rt.exec(unmountCommand);
        unmountProc.waitFor();            

        bplusloader.LOG( "Finally, deleting dmg at  " + dmgPath );
        String[] rmCommand = { "rm", dmgPath };
        Process rmProc = rt.exec(rmCommand);
        rmProc.waitFor();            
            
        bplusloader.LOG( "All done!" );
            
        bplusloader.LOG("@@ loadInstaller @@");
    }

    class MacInstallerConfig extends BootstrapLoader.InstallerConfig{

        private String installerMac = "osx";
    
        public MacInstallerConfig(Applet applet){
            super(applet);
            
            if (!this.destinationFileName.toLowerCase().endsWith(".dmg")){
                this.destinationFileName += ".dmg";
            }
            
            String installerName = applet.getParameter("installerMac");
            this.setInstallerName(installerName);
        }
        
        public String getInstaller(){ return getBaseUrl() + installerMac; };
        
        public void setInstallerName( String macFileName ){
            if (macFileName != null)
                this.installerMac = macFileName;
        }
    }
}
