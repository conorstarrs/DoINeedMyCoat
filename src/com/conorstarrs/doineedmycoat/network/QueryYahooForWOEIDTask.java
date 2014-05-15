package com.conorstarrs.doineedmycoat.network;

import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import android.app.ProgressDialog;
import android.net.ParseException;
import android.os.AsyncTask;

import com.conorstarrs.doineedmycoat.FullscreenActivity;

public class QueryYahooForWOEIDTask extends AsyncTask <String, String, String> { 
	
	private FullscreenActivity activityRef;
	public ProgressDialog dialog;

	public QueryYahooForWOEIDTask(FullscreenActivity activityRef,
									ProgressDialog dialog) {
	     this.activityRef = activityRef;
	     this.dialog = dialog;
	}

    @Override
    protected void onPreExecute() {
        this.dialog.setMessage("Finding weather...");
        this.dialog.show();
    }
	
	@Override
	public String doInBackground(String... params) {
    	String yahooURL = "http://query.yahooapis.com/v1/public/yql?";
    	yahooURL += "q=select%20*%20from%20geo.placefinder%20where%20text%3D%22";
    	yahooURL += params[0];
    	yahooURL += ",";
    	yahooURL += params[1] + "%22";
    	yahooURL += "%20and%20gflags%3D\"R\"";

    	Document doc;
    	String woeid = "";

	    try {     
	    	
	    	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    	DocumentBuilder db = dbf.newDocumentBuilder();
	    	doc = db.parse(new URL(yahooURL).openStream());

	    	woeid = doc.getFirstChild().getChildNodes().item(0).
	    			getChildNodes().item(0).getChildNodes().item(28).
	    			getTextContent();
	    	
	    } catch (ParseException e) {
	    	// TODO Auto-generated catch block
	    	e.printStackTrace();
	    } catch (IOException e) {
	    	// TODO Auto-generated catch block
	    	e.printStackTrace();	
	    } catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return woeid;
	}
	
	@Override
	protected void onPostExecute(String result) {
		activityRef.woeidObtained(result);
    	if (dialog.isShowing()) {
            dialog.dismiss();
        }
	}
}