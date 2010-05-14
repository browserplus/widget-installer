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
import java.awt.Color;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class bplusloader extends Applet {

    private String m_state;
    private int m_percent;
    private DownloadThread m_dlThread;

    public static void LOG(String msg)
    {
        System.out.println( "BP Loader: " + msg );
    }
    
    public void init()
    {
        m_state = "initialized";
        LOG("**STATE("+m_state+")**");
        m_percent = 0;
        LOG("Initialize \"BrowserPlus loader\" Java Applet");
    }
    
    
    public void start() {
        LOG("VVV start VVV");
        try {
            String osname = System.getProperty("os.name").toLowerCase();
            LOG( "Operating System is \"" + osname + "\"");
        
            String value = this.getParameter("backgroundColor");
            if (value != null) {
                LOG( "Setting background color to: " + value );
                this.setBackground( new Color(Integer.parseInt(value, 16) ) );
            }

            m_state = "started";        
            LOG("**STATE("+m_state+")**");

            if (osname.startsWith("mac os x")){
                this.download( new MacLoader( this ) );
            } else { 
                this.download( new WinLoader( this ) );
            } 
            LOG("@@ start @@");
        } catch(java.lang.SecurityException ace){
        	 ace.printStackTrace();
             m_state = "SecurityException";
             LOG("**STATE("+m_state+")**");
             
        } catch (Exception e) {
            e.printStackTrace();
            m_state = "error";
            LOG("**STATE("+m_state+")**");
        }
        
        LOG("^^^ start ^^^");
    }

    public StatusObject status() 
    {
        StatusObject so = new StatusObject();
        if (m_state == "threadSpawned") {
            String phase = m_dlThread.getState();
            so.status = (phase == "complete" ? "complete" : "running");
            so.percent = m_dlThread.getTotalPercent();
            so.localPercent = m_dlThread.getLocalPercent();
            so.phase = phase;
        } else {
            so.status = m_state;
            so.percent = m_percent;
            so.localPercent = 0;
            so.phase = null;
        }
        return so;
    }

    private void download(BootstrapLoader loader) {
        String address = loader.getInstallerUrl();
        String localFileName = loader.getDestination();
        LOG("Spinning a thread to download BrowserPlus installer from '" + address + "' to '" + localFileName);
        m_state = "threadSpawned";
        // at this point we'll set state to "threadSpawned" which indicates
        // that state should be extracted from the running thread
        m_dlThread = new DownloadThread();
        m_dlThread.start(loader, address, localFileName);

        LOG("@@ download @@");
    }
}
