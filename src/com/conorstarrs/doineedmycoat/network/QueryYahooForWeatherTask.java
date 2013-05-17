package com.conorstarrs.doineedmycoat.network;

import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.ProgressDialog;
import android.net.ParseException;
import android.os.AsyncTask;

import com.conorstarrs.doineedmycoat.FullscreenActivity;

public class QueryYahooForWeatherTask extends AsyncTask <String, String, Document> { 
	
	private FullscreenActivity activityRef;
	public ProgressDialog dialog;

	public QueryYahooForWeatherTask(FullscreenActivity activityRef,
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
	public Document doInBackground(String... params) {
    	String yahooURL = "http://weather.yahooapis.com/forecastrss?w=";
    	yahooURL += params[0];
    	yahooURL += "&u=c";
   
    	Document doc = null;

	    try {     
	    	
	    	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    	DocumentBuilder db = dbf.newDocumentBuilder();
	    	doc = db.parse(new URL(yahooURL).openStream());
	    	
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
	    return doc;
	}
	
	@Override
	protected void onPostExecute(Document result) {
		activityRef.weatherObtained(result);
		activityRef.setYahooWeatherXML(result);
    	if (dialog.isShowing()) {
            dialog.dismiss();
        }
	}
}
