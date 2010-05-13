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

    private static boolean DEBUG = true;
    
    public static void LOG(String msg){
        if (DEBUG) {
            System.out.println( "BrowserPlus Loader: " + msg );
        }
 
        // If you don't have a Java Console, this sends all debug messages to 
        // http://localhost/logger.php which is simply:
        //
        //   if (isset($_POST['msg'])) error_log($_POST['msg'] . "\n", 3, "/tmp/logger.dbg");
        //
        // then you can monitor output with
        //
        //   tail -f /tmp/logger.dbg
        //
        
        /* UNCOMMENT THIS OUT IF YOU NEED TO DEBUG
        try {
            // Construct data
            String data = java.net.URLEncoder.encode("msg", "UTF-8") + "=" + java.net.URLEncoder.encode(msg, "UTF-8");

            // Send data
            URL url = new URL("http://localhost/logger.php");
            URLConnection conn = url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            java.io.OutputStreamWriter wr = new java.io.OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.flush();

            // Get the response
            java.io.BufferedReader rd = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                // Process line...
            }

            rd.close();

            wr.close();

        } catch (Exception e) {
            // ignore
        }
        */
    }
    
    public void init() {//steve - whole function
        m_state = "initialized";
        LOG("**STATE("+m_state+")**");
        m_percent = 0;
        LOG("Initialize \"BrowserPlus loader\" Java Applet");
    }
    
    
    public void start() {
        LOG("VVV start VVV");
        try {
            String debug = this.getParameter("debug");

            if (debug != null && debug.equalsIgnoreCase("on") ){  DEBUG = true; }

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
        so.status = m_state;
        so.percent = m_percent;
        return so;
    }

    private void download(BootstrapLoader loader) {
        LOG("Preparing to download BrowserPlus installer");
        String address = loader.getInstallerUrl();
        String localFileName = loader.getDestination();
        
        OutputStream out = null;
        URLConnection conn = null;
        InputStream in = null;

        m_state = "downloading";                
        LOG("**STATE("+m_state+")**");

        try {
            // Get the URL
            URL url = new URL(address);
            // Open an output stream to the destination file on our local filesystem
            out = new BufferedOutputStream(new FileOutputStream(localFileName));
            conn = url.openConnection();
            conn.setUseCaches(false);
            
            // Read the data
            int length = 0;
            try {
                length = conn.getContentLength();
            } catch(Exception e) {
                LOG( "Failed to get content length!!  assuming 1.5mb" );
                length = 1024 * 1500;
            }

            LOG( "Reading " + length + " bytes" );

            in = conn.getInputStream();
            
            byte[] buffer = new byte[1024];
            int numRead;
            int totalRead = 0;
            int lastPercent = -1;

            while ((numRead = in.read(buffer)) != -1) {
                totalRead += numRead;
                m_percent = (100 * totalRead) / length;
                if (m_percent != lastPercent && m_percent % 5 == 0) {
                    lastPercent = m_percent;
                    LOG( "Percent Done: " + m_percent );
                }
                out.write(buffer, 0, numRead);
            } 
            m_percent = 100;

            LOG( "File Downloaded Succesfully!" );

            m_state = "downloaded";                
            LOG("**STATE("+m_state+")**");
            
            if (in != null) { in.close(); }
            if (out != null) { out.close(); }

            LOG( "Loading the installer" );

            m_state = "launching";                
            LOG("**STATE("+m_state+")**");
            loader.loadInstaller();
            m_state = "complete";                
            LOG("**STATE("+m_state+")**");
            // Done! Just clean up and get out
            
        } catch (java.lang.SecurityException exception) {
            LOG( "INSTALL FAILED!  Security Exception - Permissions maybe?.." );
            exception.printStackTrace();

            m_state = "SecurityException";
            LOG("**STATE("+m_state+")**");
            
        } catch (Exception exception) {
            LOG( "INSTALL FAILED!  Couldn't fetch BrowserPlus installer" );
            exception.printStackTrace();

            // we must now let javascript know!
            m_state = "error";
            LOG("**STATE("+m_state+")**");
        } finally {
            conn = null;
        }

        LOG("@@ download @@");
    }
}
