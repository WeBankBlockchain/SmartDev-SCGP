package com.webank.solc.plugin.cl;

import com.webank.solc.plugin.Cleaner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class SolcjClassLoader extends ClassLoader {

    private File jarFile;

    public SolcjClassLoader(String jarName){
        try{
            copyToUserhome(jarName);
        }
        catch (IOException ex){
            throw new RuntimeException(ex);
        }
    }

    /**
     * DO NOT use parent-delegated, otherwise it will load from gradle local cache thus load from solc from web3sdk
     */
    @Override
    public URL getResource(String name) {
        return findResource(name);
    }

    @Override
    protected URL findResource(String name) {
        try{
            StringBuilder jarUrl = new StringBuilder();
            jarUrl.append("jar:");
            jarUrl.append(this.jarFile.toURI().toURL().toString());
            jarUrl.append("!/");
            jarUrl.append(name);
            return new URL(jarUrl.toString());
        }
        catch (MalformedURLException ex){
            System.out.println("Error: "+ ex.getMessage());
            return null;
        }
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        URL url = getResource(name);
        try {
            if(url == null) return null;
            JarURLConnection urlConnection = (JarURLConnection)url.openConnection();
            urlConnection.setUseCaches(false);//Make sure jar file and jar entry are both is closed
            return urlConnection.getInputStream();
        } catch (IOException e) {
            return null;
        }
    }

    private void copyToUserhome(String jarName) throws IOException {
        File tmp = new File(System.getProperty("user.home"), "solc"+File.separator+jarName);
        tmp.mkdirs();
        InputStream inputStream = this.getClass().getResourceAsStream("/solcJ/"+jarName);
        if(inputStream == null){
            throw new IOException("jar not found!" + jarName);
        }
        Files.copy(inputStream, tmp.toPath(), StandardCopyOption.REPLACE_EXISTING);
        inputStream.close();
        Cleaner.recordDelete(tmp);
        this.jarFile =tmp;
    }

}
