package com.yiwise.voip.test;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yiwise.voip.SSLClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.DigestSchemeFactory;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @Author: wangguomin
 * @Date: 2019-04-13 11:03
 */
public class TestSend {
    public static void main(String[] args) throws Exception{

        ArrayList<String> arrayList = new ArrayList<>();
        try {
            String path411= "/Users/guominwang/git-clone/voip-demo/src/test/java/com/yiwise/voip/test/4-11.txt";
            String path412= "/Users/guominwang/git-clone/voip-demo/src/test/java/com/yiwise/voip/test/4-12.txt";
            //FileReader fr = new FileReader(path411);
            FileReader fr = new FileReader(path412);
            BufferedReader bf = new BufferedReader(fr);
            String str;
            // 按行读取字符串
            while ((str = bf.readLine()) != null) {
                arrayList.add(str);
            }
            bf.close();
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int index = 0;
        int sum = 0;
        int size = arrayList.size();
        JSONArray array = new JSONArray();

        for(String item : arrayList) {
            index ++;
            sum ++;
            JSONObject put = new JSONObject();
            put.put("number", item);
            array.add(put);

            if(index > 100 || sum == size){

                JSONObject object = new JSONObject();
                object.put("text", "【爱又米】恭喜您获得新人专享福利！10000元免息券限量领，利率低至万3，秒到账，戳 http://t.cn/EiEjjvt 领，回T退订");
                Integer [] port = {1,2,3,4,6,7,16,18};
                object.put("port", port);

                object.put("param", array);
                System.out.println(object.toJSONString());
                try {
                   Thread.sleep(180000);
                }catch (Exception e){

                }

                System.out.println(doPost("https://192.168.120.111/api/send_sms", object.toJSONString(), "UTF-8"));

                index = 0;
                System.out.println(array.size());
                array = new JSONArray();
            }
        }
        System.out.println(size);
        /*
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";
        try {
            // 创建Http Post请求
            HttpPost httpPost = new HttpPost("https://192.168.120.111/api/send_sms");
            // 创建请求内容
            httpPost.setHeader("HTTP Method","POST");
            httpPost.setHeader("Connection","Keep-Alive");
            httpPost.setHeader("Content-Type","application/json;charset=utf-8");
            httpPost.setHeader("Authorization", ",Digest username=\"yiwise\", realm=\"Web Server\", nonce=\"1113dd98c2071a74a93b64a0911ac552\", uri=\"/api/send_sms\", algorithm=\"MD5\", qop=auth, nc=00000001, cnonce=\"7rapRrIN\", response=\"0cd5833e811a8760dcc8af82c9ffc906\", opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"");

            StringEntity entity = new StringEntity("{\n" +
                    "    \"text\": \"11\",\n" +
                    "    \"param\": [\n" +
                    "        {\n" +
                    "            \"number\": \"15209855706\"\n" +
                    "        }\n" +
                    "    ]\n" +
                    "}");

            entity.setContentType("application/json;charset=utf-8");
            httpPost.setEntity(entity);

            // 执行http请求
            response = httpClient.execute(httpPost);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                resultString = EntityUtils.toString(response.getEntity(), "UTF-8");
            }
            System.out.println(response.getStatusLine().getStatusCode());
            System.out.println(EntityUtils.toString(response.getEntity(), "UTF-8"));
            System.out.println(resultString);
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                if(response!=null){
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/

      }

    public static String doPost(String url,String jsonstr,String charset){
        HttpClient httpClient = null;
        HttpPost httpPost = null;
        String result = null;
        try{
            httpClient = new SSLClient();
            httpPost = new HttpPost(url);
            httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
            StringEntity se = new StringEntity(jsonstr, "UTF-8");
            se.setContentType("text/json; charset=UTF-8");
            se.setContentEncoding(new BasicHeader("Content-Type", "application/json;charset=UTF-8"));
            // httpPost.setHeader("Authorization", ",Digest username=\"yiwise\", realm=\"Web Server\", nonce=\"1113dd98c2071a74a93b64a0911ac552\", uri=\"/api/send_sms\", algorithm=\"MD5\", qop=auth, nc=00000001, cnonce=\"7rapRrIN\", response=\"0cd5833e811a8760dcc8af82c9ffc906\", opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"");
            httpPost.setEntity(se);
            Credentials credentials = new UsernamePasswordCredentials("yiwise","YiwiseGateway");
            ((SSLClient) httpClient).getCredentialsProvider().setCredentials(
                    new AuthScope(StringUtils.isBlank("") ? AuthScope.ANY_HOST : "", null == null ? AuthScope.ANY_PORT : 80),
                    credentials
            );

            httpClient.getParams().setParameter(AuthSchemes.DIGEST, Collections.singleton(AuthSchemes.DIGEST));
            ((SSLClient) httpClient).getAuthSchemes().register(AuthSchemes.DIGEST,new DigestSchemeFactory());

            HttpResponse response = httpClient.execute(httpPost);
            if(response != null){
                HttpEntity resEntity = response.getEntity();
                if(resEntity != null){
                    result = EntityUtils.toString(resEntity,charset);
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        System.out.println(result);
        return result;
    }
}
