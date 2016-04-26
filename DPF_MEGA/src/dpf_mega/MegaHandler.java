package dpf_mega;

import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class MegaHandler {

    private int sequence_number;

    public MegaHandler() {
        Random rg = new Random();
        sequence_number = rg.nextInt(Integer.MAX_VALUE);
    }

    private String api_request(String data) {
        HttpURLConnection connection = null;
        try {
            String urlString = "https://g.api.mega.co.nz/cs?id=" + sequence_number;
            
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setAllowUserInteraction(false);
            connection.setRequestProperty("Content-Type", "text/xml");

            OutputStream out = connection.getOutputStream();
            try {
                OutputStreamWriter wr = new OutputStreamWriter(out);
                wr.write("[" + data + "]");
                wr.flush();
                wr.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (out != null)
                out.close();
            }

            InputStream in = connection.getInputStream();
            StringBuffer response = new StringBuffer();
            try {
                BufferedReader rd = new BufferedReader(new InputStreamReader(in));
                String line = "";
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                }
                rd.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (in != null)
                in.close();
            }
            return response.toString().substring(1, response.toString().length() - 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    public void download(String url, String path) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, FileNotFoundException, MalformedURLException, IOException{
        print("Download started");
        String[] s = url.split("!");
        String file_id = s[1];
        byte[] file_key = MegaCrypt.base64_url_decode_byte(s[2]);

        int[] intKey = MegaCrypt.aByte_to_aInt(file_key);
        JSONObject json = new JSONObject();
        try {
            json.put("a", "g");
            json.put("g", "1");
            json.put("p", file_id);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject file_data = new JSONObject(api_request(json.toString()));
        int[] keyNOnce = new int[] { intKey[0] ^ intKey[4], intKey[1] ^ intKey[5], intKey[2] ^ intKey[6], intKey[3] ^ intKey[7], intKey[4], intKey[5] };
        System.out.println("1...");
        byte[] key = MegaCrypt.aInt_to_aByte(keyNOnce[0], keyNOnce[1], keyNOnce[2], keyNOnce[3]);

        int[] iiv = new int[] { keyNOnce[4], keyNOnce[5], 0, 0 };
        byte[] iv = MegaCrypt.aInt_to_aByte(iiv);
        
        String attribs = (file_data.getString("at"));
        attribs = new String(MegaCrypt.aes_cbc_decrypt(MegaCrypt.base64_url_decode_byte(attribs), key));
        
        String file_name = attribs.substring(10,attribs.lastIndexOf("\""));
        String temporal_name = file_name;
        String [] temporal = temporal_name.split("\\,");
        String name_file = temporal[0].substring(0, temporal[0].length()-1); 
        print(name_file);
        final IvParameterSpec ivSpec = new IvParameterSpec(iv);
        final SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/CTR/nopadding");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivSpec);
        InputStream is = null;
        String file_url = file_data.getString("g");

        FileOutputStream fos = new FileOutputStream(path+File.separator+name_file);
        final OutputStream cos = new CipherOutputStream(fos, cipher);
        final Cipher decipher = Cipher.getInstance("AES/CTR/NoPadding");
        decipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivSpec);
        int read = 0;
        final byte[] buffer = new byte[32767];
        
        try {
            URLConnection urlConn = new URL(file_url).openConnection();
            print("Size: " + urlConn.getContentLength());
            
            print(file_url);
            is = urlConn.getInputStream();
            long siz = 0;
            while ((read = is.read(buffer)) > 0) {
                siz = siz + read;
                cos.write(buffer, 0, read);
                print("Downloading: " + siz);
            }
        } finally {
            try {
                cos.close();
                if (is != null) {
                    is.close();
                }
            } finally {
                if (fos != null) {
                    fos.close();
                }
            }
        }
        print("Download finished");
    }

    public static void print(Object o) {
        System.out.println(o);
    }
}