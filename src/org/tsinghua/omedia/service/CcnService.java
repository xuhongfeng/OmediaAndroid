package org.tsinghua.omedia.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.ccnx.android.ccnlib.CCNxConfiguration;
import org.ccnx.android.ccnlib.CCNxServiceCallback;
import org.ccnx.android.ccnlib.CCNxServiceControl;
import org.ccnx.android.ccnlib.CCNxServiceStatus.SERVICE_STATUS;
import org.ccnx.ccn.CCNHandle;
import org.ccnx.ccn.impl.CCNNetworkManager.NetworkProtocol;
import org.ccnx.ccn.io.CCNInputStream;
import org.ccnx.ccn.profiles.ccnd.FaceManager;
import org.ccnx.ccn.profiles.ccnd.PrefixRegistrationManager;
import org.ccnx.ccn.protocol.ContentName;
import org.tsinghua.omedia.OmediaApplication;
import org.tsinghua.omedia.datasource.DataSource;
import org.tsinghua.omedia.datasource.sdcard.CcnFileDatasource;
import org.tsinghua.omedia.tool.Logger;

import android.os.AsyncTask;

/**
 * 
 * @author xuhongfeng
 *
 */
public class CcnService implements CCNxServiceCallback {
    private static final Logger logger = Logger.getLogger(CcnService.class);
    
    private static CcnService me;
    
    private CcnService(){
    }
    
    public static CcnService getInstance() {
        if(me != null) return me;
        synchronized (CcnService.class) {
            if(me == null) {
                me = new CcnService();
            }
        }
        return me;
    }
    
    private CCNxServiceControl ccnd;
    
    public void ccnGetFile(final String ccnFile) throws IOException {
        AsyncTask<Void, Void, Throwable> task = new AsyncTask<Void, Void, Throwable>() {

            @Override
            protected Throwable doInBackground(Void... params) {
                File file = null;
                try {
                    if(!isCcnRunning()) {
                        throw new Exception("ccnd is not running");
                    }
                    String uri = DataSource.getInstance().getCcnUrl();
                    ContentName name = ContentName.fromURI(uri+"/"+ccnFile);
                    CCNHandle handle = CCNHandle.open();
                    String filePath = CcnFileDatasource.getInstance()
                            .getAbsolutePath(ccnFile);
                    file = new File(filePath);
                    FileOutputStream fos = null;
                    CCNInputStream input = null;
                    try {
                        fos = new FileOutputStream(file);
                        input = new CCNInputStream(name, handle);
                        byte [] buffer = new byte[1024];
                        int readed;
                        while((readed=input.read(buffer)) != -1) {
                            fos.write(buffer, 0, readed);
                            fos.flush();
                        }
                    } finally {
                        try {
                            if(input != null) input.close();
                        } finally {
                            if(fos != null) fos.close();
                        }
                    }
                } catch (Throwable e) {
                    if(file != null) {
                        file.delete();
                    }
                    return e;
                }
                return null;
            }
            
        };
        task.execute();
        Throwable e;
        try {
            e = task.get();
        } catch (Throwable e1) {
            e = e1;
        }
        if(e != null) {
            throw new IOException(e);
        }
    }
    
    private boolean isCcnRunning() {
        boolean running = false;
        try {
            running = getCcnd().isCcndRunning();
        } catch (Throwable e) {
            logger.error(e);
            ccnd = null;
        }
        return running;
    }
    
    private boolean ccndc(String uri, String host) {
        try {
            CCNHandle ccnHandle = CCNHandle.open();
            FaceManager fHandle = new FaceManager(ccnHandle);
            int faceID = fHandle.createFace(NetworkProtocol.UDP, host, 9695);
            PrefixRegistrationManager pre = new PrefixRegistrationManager(ccnHandle);
            pre.registerPrefix(uri, faceID, null);
            faceID = fHandle.createFace(NetworkProtocol.TCP, host, 9695);
            pre.registerPrefix(uri, faceID, null);
            ccnHandle.close();
            return true;
        } catch (Throwable e) {
            logger.error(e);
            ccnd = null;
            return false;
        }
    }
    
    private synchronized CCNxServiceControl getCcnd() throws IOException {
        if(ccnd != null) return ccnd;
        init();
        if(ccnd == null) throw new IOException("start ccnd failed");
        return ccnd;
    }
    
    public synchronized void init() {
        if(ccnd == null) {
            String host = DataSource.getInstance().getCcnHost();
            logger.info("ccn host="+host);
            CCNxConfiguration.config(OmediaApplication.getInstance().getApplicationContext());
            ccnd = new CCNxServiceControl(OmediaApplication.getInstance().getApplicationContext());
            ccnd.registerCallback(this);
            ccnd.connect();
            ccnd.startAll();
            ccndc("ccnx:/", host);
        }
    }

    @Override
    public void newCCNxStatus(SERVICE_STATUS st) {
        OmediaApplication.getInstance().getCurrentActivity().toast(st.toString());
    }
    
}
