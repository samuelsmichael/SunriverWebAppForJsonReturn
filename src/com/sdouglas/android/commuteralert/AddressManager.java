package com.sdouglas.android.commuteralert;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class AddressManager {

    public static List<Address> getFromLocationName(String address, Context ctx) throws Exception {
        String addressTextText="410 Williams St Denver CO 80209";
        List<Address> addressList=null;
        try {
            addressTextText=address;
            addressTextText=addressTextText.replace("\n"," ");
            int x=4;
            if(x==3) throw new Exception("Try other method");
            Geocoder g=new Geocoder(ctx);
            addressList = g.getFromLocationName(addressTextText,5);
            if(addressList==null || addressList.size()==0) {
                throw new Exception("Try other method");
            }
            return addressList;
        } catch (Exception e) {
            String url="http://local.yahooapis.com/MapsService/V1/geocode?appid=yDSMLAbV34EUyy1AJrHKqbb1gL4A4xvchBWqr4MaNharntRqZTcCfm5Qs.ugfgTyrdoe4eoGxpM-&location=" +
                    addressTextText;
            url = url.replace(" ", "%20");
            URL u = new URL(url);
            HttpURLConnection conn=(HttpURLConnection)u.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.connect();
            OutputStream out=conn.getOutputStream();
            PrintWriter pw=new PrintWriter(out);
            pw.close();
            InputStream is = conn.getInputStream();
            DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
            DocumentBuilder db=dbf.newDocumentBuilder();
            Document doc=db.parse(is);
            doc.getDocumentElement().normalize();
            Element rootElement=doc.getDocumentElement();
            Element elem=(Element)rootElement.getChildNodes().item(0);
            String stotalResultsReturned=rootElement.getAttribute("precision");
            addressList=new java.util.ArrayList<Address>();
            addressList.add(deriveAddressFromElement(rootElement));
            try {is.close();} catch (Exception eieiee) {}
            return addressList;
        }
    }
    public static Address deriveAddressFromElement(Element elem) {
        Address address=new Address(Locale.getDefault());
        String lat=getTextFromElement(elem,"Latitude");
        address.setLatitude(Double.valueOf(lat));
        address.setLongitude(Double.valueOf(getTextFromElement(elem,"Longitude")));
        address.setAddressLine(0, getTextFromElement(elem,"Address"));
        address.setAdminArea(getTextFromElement(elem,"State"));
        address.setLocality(getTextFromElement(elem,"City"));
        address.setCountryCode(getTextFromElement(elem,"Country"));
        return address;
    }
    public static String getTextFromElement(Element elem,String name) {
        try {
            NodeList nl=elem.getElementsByTagName(name);
            Element resultaddress=(Element) nl.item(0);
            resultaddress.normalize();
            NodeList array=resultaddress.getChildNodes();
            return array.item(0).getNodeValue();
        } catch (Exception ee33d) {
            return "";
        }
    }


}
