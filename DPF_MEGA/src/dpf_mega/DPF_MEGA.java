/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dpf_mega;

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.NoSuchPaddingException;

/**
 *
 * @author JOSE
 */
public class DPF_MEGA {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, MalformedURLException, IOException {

        MegaHandler mh = new MegaHandler();
        
        String link_MEGA = "https://mega.co.nz/.........................................";
        String path_save = "C:\\Users\\......\\Desktop";
        mh.download(link_MEGA, path_save);
       
    }
    
}
