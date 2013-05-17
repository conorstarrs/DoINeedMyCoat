package com.conorstarrs.doineedmycoat;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.conorstarrs.doineedmycoat.network.GPSInformation;
import com.conorstarrs.doineedmycoat.network.LocationInfoVO;
import com.conorstarrs.doineedmycoat.network.QueryYahooForWOEIDTask;
import com.conorstarrs.doineedmycoat.network.QueryYahooForWeatherTask;
import com.conorstarrs.doineedmycoat.util.SystemUiHider;
import com.conorstarrs.doineedmycoat.util.WeatherCodeType;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class FullscreenActivity extends Activity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;
    
    QueryYahooForWOEIDTask yQ;
    QueryYahooForWeatherTask yW;

    boolean isUS = false;
    private String degreesType = "\u2103";
    private String temp = "";
    private Document yahooWeatherXML = null;

    private Button refreshButton;
    private TextView tempText;
    private GPSInformation gps;
    
    public static final String PREFS_NAME = "MyPrefsFile";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
    	setContentView(R.layout.activity_fullscreen);	
    	
    	this.refreshButton = (Button)this.findViewById(R.id.dummy_button);
    	this.refreshButton.setOnClickListener(new OnClickListener() {
    	    @Override
    	    public void onClick(View v) {
        		finish();
        		startActivity(getIntent());
    	    }
    	  });
	    	if(isNetworkConnected())
	    	{
    	  
    	Toast.makeText(getApplicationContext(), "Finding your location...", Toast.LENGTH_SHORT).show();
    	      	
    	LocationInfoVO locInfo = locationInfo();
    	
    	if(null != locInfo && locInfo.getAddress().size() > 0)
    	{
	    	String city = "";
	    	String country = "";
			if (null != locInfo.getAddress() && locInfo.getAddress().size() > 0) 
			{
			    city = locInfo.getAddress().get(0).getLocality();
			    country = locInfo.getAddress().get(0).getCountryName();
			}
			else
			{
				city = "Location not found :-(\n";
				country = "Country not found :-(";
			}

			if(locInfo.getAddress().get(0).getCountryCode().equals("US"))
			{
				this.isUS = true;
			}

	    	Toast.makeText(getApplicationContext(), "Found you in " + city + ".", Toast.LENGTH_SHORT).show();

			    Context context = FullscreenActivity.this;
			    ProgressDialog progressDialog = new ProgressDialog(context);

		        yQ = new QueryYahooForWOEIDTask(this, progressDialog);
		        yQ.execute(locInfo.getLatitude(), locInfo.getLongitude());

		    	TextView txtView = (TextView) findViewById(R.id.fullscreen_content);
		    	txtView.setText(city + ", " + country);        		
        		
        		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        		boolean dialogShown = settings.getBoolean("dialogShown", false);

        		if (!dialogShown) {
           			new AlertDialog.Builder(this)
       				.setTitle("Welcome!")
       				.setMessage("Thanks for downloading and installing \"Do I Need My Coat?\". " +
       						"You can toggle between Celsius and Fahrenheit " +
       						"by touching the temperature." +
       						"This notice will not show again. Enjoy. :-)")
       				.show();

        		     SharedPreferences.Editor editor = settings.edit();
        		     editor.putBoolean("dialogShown", true);
        		     editor.commit();  
        		}
        		

    			this.tempText = (TextView)this.findViewById(R.id.weatherText);
    	 		this.tempText.setOnClickListener(new OnClickListener() {
    	    	@Override
    	    	public void onClick(View v) 
    	    	{
    	    			String text = getYahooWeatherXML().getElementsByTagName("yweather:condition").item(0).
    	    				getAttributes().getNamedItem("text").getTextContent();	
    	    		    double tempConversion = 0.0;

    	    			if(getDegreesType().equals("\u2103"))
    	    			{
    	    				tempConversion = Double.parseDouble(temp) * 9 / 5 + 32;
    						DecimalFormat df = new DecimalFormat("#");
    						setTemp(df.format(tempConversion));
    						setDegreesType("\u2109");
    					}
    					else
    					{
    	    				tempConversion = (Double.parseDouble(temp) - 32) *5 / 9;
    						DecimalFormat df = new DecimalFormat("#");
    						setTemp(df.format(tempConversion));
    						setDegreesType("\u2103");
    					}

    					TextView weatherText = (TextView) findViewById(R.id.weatherText);
    					weatherText.setText(text + ", " + getTemp() + getDegreesType());	
    	    		}
    	  		});
		
	    	}
    	}
	    else
	    {
			ImageView imgView = (ImageView) findViewById(R.id._0);
			imgView.setImageResource(R.drawable._na);
			imgView.setVisibility(1); //visible	

	    	TextView noNetworkText = (TextView) findViewById(R.id.reason);
	    	noNetworkText.setText("No network connection :-(");
	   	}

        super.onCreate(savedInstanceState);

        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    int mControlsHeight;
                    int mShortAnimTime;

                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            // If the ViewPropertyAnimator API is available
                            // (Honeycomb MR2 and later), use it to animate the
                            // in-layout UI controls at the bottom of the
                            // screen.
                            if (mControlsHeight == 0) {
                                mControlsHeight = controlsView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }
                            controlsView.animate()
                                    .translationY(visible ? 0 : mControlsHeight)
                                    .setDuration(mShortAnimTime);
                        } else {
                            // If the ViewPropertyAnimator APIs aren't
                            // available, simply show or hide the in-layout UI
                            // controls.
                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                        }

                        if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });

        // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
    }
    
	private LocationInfoVO locationInfo() {
		LocationInfoVO locInfoVO = new LocationInfoVO();
		double latitude = 0.0;
		double longitude = 0.0;

		// create class object
        gps = new GPSInformation(FullscreenActivity.this);

        // check if GPS enabled     
        if(gps.canGetLocation()){
             
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
    
        } else {
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }

		Geocoder gcd = new Geocoder(this, Locale.getDefault());
		List<Address> addresses = null;

		try {
			addresses = gcd.getFromLocation(latitude, longitude, 1);
		} catch (IOException e) {
		}

		locInfoVO.setAddress(addresses);
		locInfoVO.setLatitude(String.valueOf(latitude));
		locInfoVO.setLongitude(String.valueOf(longitude));

		return locInfoVO;
	}

	@Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

//	@Override
//    protected void onRestart()
//	{   
//		super.onRestart();
//		finish();
//		startActivity(getIntent());
//	}

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

	public void woeidObtained(String result) {
	    Context context = FullscreenActivity.this;
	    ProgressDialog progressDialog = new ProgressDialog(context);
        yW = new QueryYahooForWeatherTask(this, progressDialog);
        yW.execute(result); 
	}

	public void weatherObtained(Document result) {
    	NodeList n = result.getElementsByTagName("yweather:condition");
    	String code = n.item(0).getAttributes().getNamedItem("code").getTextContent();
    	String text = n.item(0).getAttributes().getNamedItem("text").getTextContent();
    	setTemp(n.item(0).getAttributes().getNamedItem("temp").getTextContent());
    	String wearCoat = "";
    	String reason = "";
    	String sunrise = result.getElementsByTagName("yweather:astronomy").
    			item(0).getAttributes().getNamedItem("sunrise").getNodeValue();
    	String sunset = result.getElementsByTagName("yweather:astronomy").
    			item(0).getAttributes().getNamedItem("sunset").getNodeValue();
    	String windSpeed = result.getElementsByTagName("yweather:wind").
    			item(0).getAttributes().getNamedItem("speed").getNodeValue();
    	String windDirection = result.getElementsByTagName("yweather:wind").
    			item(0).getAttributes().getNamedItem("direction").getNodeValue();
    	double convertWindToMph = 0.0;
    	double directionDouble = 0.0;
		double tempToFarenheit = 0.0;

		if(this.isUS && getTemp() != null)
		{
			tempToFarenheit = Double.parseDouble(getTemp()) * 9 / 5 + 32;
    		DecimalFormat df = new DecimalFormat("#");
    		setTemp(df.format(tempToFarenheit));
    		this.degreesType = "\u2109";
		}    	

    	int weatherCode = 0;
    	
    	if(code != null)
    	{
    		weatherCode = Integer.parseInt(code);
    	}

    	if(windSpeed != null)
    	{
    		convertWindToMph = Double.parseDouble(windSpeed) * 0.621371;
    		DecimalFormat df = new DecimalFormat("#");
    		windSpeed = df.format(convertWindToMph);
    	}
    	
    	if(windDirection != null)
    	{
    		directionDouble = Double.parseDouble(windDirection);
    	}
    	
		if(isPrecipitation(weatherCode) && isCold(getTemp(), getDegreesType()))
		{
			wearCoat = "YES";
			reason = "It's cold and wet";
		}
		else if(isPrecipitation(weatherCode) && !isCold(getTemp(), getDegreesType()))
		{
			wearCoat = "YES";
			reason = "It's wet";
		}
		else if(!isPrecipitation(weatherCode) && isCold(getTemp(), getDegreesType()))
		{
			wearCoat = "YES";
			reason = "It's cold";
		}
		else if(!isPrecipitation(weatherCode) && !isCold(getTemp(), getDegreesType()))
		{
			wearCoat = "NO";
			reason = "Yay - good weather :-)";
		}
		else
		{
			wearCoat = "N/A";
			reason = "Weather not available :-(";
		}

		ImageView imgView = (ImageView) findViewById(R.id._0);
		imgView.setImageResource(getWeatherImage(weatherCode));
		imgView.setVisibility(1); //visible	

    	TextView weatherResult = (TextView) findViewById(R.id.weatherResult);
    	weatherResult.setText(wearCoat);
    	weatherResult.setPadding(0, 10, 0, 10);
    	
    	TextView weatherText = (TextView) findViewById(R.id.weatherText);
    	weatherText.setText(text + ", " + getTemp() + this.degreesType);
    	
    	TextView weatherText2 = (TextView) findViewById(R.id.weatherText2);
    	weatherText2.setText("\nWind: " + windSpeed + "mph, Direction: " + 
    	windDirection + (char) 0x00B0 + "(" + headingToString(directionDouble) + ")" +
    	"\nSunrise: " + sunrise + ", Sunset: " + sunset); 
    	weatherText2.setPadding(0, -45, 0, 0);
        
    	TextView reasonText = (TextView) findViewById(R.id.reason);
    	reasonText.setText(reason); 
	}

	private boolean isNetworkConnected() {
		  ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		  NetworkInfo ni = cm.getActiveNetworkInfo();
		  if (ni == null) {
		   // There are no active networks.
		   return false;
		  } else
		   return true;
		 }

	public static String headingToString(double x)
	{
		String directions[] = {"N", "NE", "E", "SE", "S", "SW", "W", "NW", "N"};
		return directions[ (int)Math.round(( ((double)x % 360) / 45)) ];
	}

	private int getWeatherImage(int weatherCode) {

		int image = 0;

        switch (weatherCode) {
            case 0:  image = R.drawable._0;
                     break;
            case 1:  image = R.drawable._1;
                     break;
            case 2:  image = R.drawable._2;
                     break;
            case 3:  image = R.drawable._3;
                     break;
            case 4:  image = R.drawable._4;
                     break;
            case 5:  image = R.drawable._5;
                     break;
            case 6:  image = R.drawable._6;
                     break;
            case 7:  image = R.drawable._7;
                     break;
            case 8:  image = R.drawable._8;
                     break;
            case 9:  image = R.drawable._9;
                     break;
            case 10: image = R.drawable._10;
                     break;
            case 11: image = R.drawable._11;
                     break;
            case 12: image = R.drawable._12;
		             break;
            case 13: image = R.drawable._13;
		             break;
            case 14: image = R.drawable._14;
		             break;
		    case 15: image = R.drawable._15;
		             break;
		    case 16: image = R.drawable._16;
		             break;
		    case 17: image = R.drawable._17;
		             break;
		    case 18: image = R.drawable._18;
		             break;
		    case 19: image = R.drawable._19;
		            break;
		    case 20: image = R.drawable._20;
		            break;
		    case 21: image = R.drawable._21;
		            break;
            case 22: image = R.drawable._22;
		            break;
            case 23: image = R.drawable._23;
		            break;
            case 24: image = R.drawable._24;
		            break;
            case 25: image = R.drawable._25;
		            break;
            case 26: image = R.drawable._26;
		            break;
            case 27: image = R.drawable._27;
		            break;
            case 28: image = R.drawable._28;
		            break;
            case 29: image = R.drawable._29;
		            break;
		   	case 30: image = R.drawable._30;
		            break;
		   	case 31: image = R.drawable._31;
		           break;
		   	case 32: image = R.drawable._32;
		           break;
		   	case 33: image = R.drawable._33;
		           break;
			case 34: image = R.drawable._34;
		           break;
			case 35: image = R.drawable._35;
		           break;
			case 36: image = R.drawable._36;
		           break;
			case 37: image = R.drawable._37;
		           break;
			case 38: image = R.drawable._38;
		           break;
			case 39: image = R.drawable._39;
		           break;
		  	case 40: image = R.drawable._40;
		           break;
		  	case 41: image = R.drawable._41;
		          break;
		  	case 42: image = R.drawable._42;
		  		  break;
			case 43: image = R.drawable._43;
					break;
			case 44: image = R.drawable._44;
					break;
			case 45: image = R.drawable._45;
					break;
			case 46: image = R.drawable._46;
					break;
		  	case 47: image = R.drawable._47;
		  		  break;
            default: image = R.drawable._na;
                     break;
        }

		return image;
	}

	private boolean isCold(String temp, String degreesType) {
		boolean isCold = false;

		if(Integer.parseInt(temp) <= 15 && degreesType.equals("\u2103"))
		{
			isCold = true;
		}
		
		if(Integer.parseInt(temp) <= 59 && degreesType.equals("\u2109"))
		{
			isCold = true;
		}

		return isCold;
	}

	private boolean isPrecipitation(int weatherCode) {
		boolean isPrecipitation = false;

		if(weatherCode == WeatherCodeType.SNOW
				|| weatherCode == WeatherCodeType.TORNADO
				|| weatherCode == WeatherCodeType.TROPICAL_STORM
				|| weatherCode == WeatherCodeType.HURRICANE
				|| weatherCode == WeatherCodeType.SEVERE_THUNDERSTORMS
				|| weatherCode == WeatherCodeType.THUNDERSTORMS
				|| weatherCode == WeatherCodeType.MIXED_RAIN_AND_SNOW
				|| weatherCode == WeatherCodeType.MIXED_RAIN_AND_SLEET
				|| weatherCode == WeatherCodeType.MIXED_SNOW_AND_SLEET
				|| weatherCode == WeatherCodeType.FREEZING_DRIZZLE
				|| weatherCode == WeatherCodeType.DRIZZLE
				|| weatherCode == WeatherCodeType.FREEZING_RAIN
				|| weatherCode == WeatherCodeType.SHOWERS
				|| weatherCode == WeatherCodeType.SHOWERS2
				|| weatherCode == WeatherCodeType.SNOW_FLURRIES
				|| weatherCode == WeatherCodeType.LIGHT_SNOW_SHOWERS
				|| weatherCode == WeatherCodeType.BLOWING_SNOW
				|| weatherCode == WeatherCodeType.SNOW
				|| weatherCode == WeatherCodeType.HAIL
				|| weatherCode == WeatherCodeType.SLEET
				|| weatherCode == WeatherCodeType.COLD
				|| weatherCode == WeatherCodeType.MIXED_RAIN_AND_HAIL
				|| weatherCode == WeatherCodeType.MIXED_RAIN_AND_SLEET
				|| weatherCode == WeatherCodeType.MIXED_RAIN_AND_SNOW
				|| weatherCode == WeatherCodeType.MIXED_SNOW_AND_SLEET
				|| weatherCode == WeatherCodeType.ISOLATED_THUNDERSHOWERS
				|| weatherCode == WeatherCodeType.ISOLATED_THUNDERSTORMS
				|| weatherCode == WeatherCodeType.SCATTERED_THUNDERSTORMS1
				|| weatherCode == WeatherCodeType.SCATTERED_THUNDERSTORMS2
				|| weatherCode == WeatherCodeType.SCATTERED_SHOWERS
				|| weatherCode == WeatherCodeType.HEAVY_SNOW
				|| weatherCode == WeatherCodeType.SCATTERED_SNOW_SHOWERS
				|| weatherCode == WeatherCodeType.HEAVY_SNOW2
				|| weatherCode == WeatherCodeType.THUNDERSHOWERS
				|| weatherCode == WeatherCodeType.SNOW_SHOWERS
				|| weatherCode == WeatherCodeType.ISOLATED_THUNDERSHOWERS)
		{
			isPrecipitation = true;
		}

		return isPrecipitation;
	}

	public Document getYahooWeatherXML()
    {
		return this.yahooWeatherXML;
    }

    public void setYahooWeatherXML(Document doc)
    {
    	this.yahooWeatherXML = doc;
    }
    
	public String getDegreesType()
    {
		return this.degreesType;
    }
	
	public void setDegreesType(String d)
    {
		this.degreesType = d;
    }
	
	public String getTemp()
    {
		return this.temp;
    }
	
	public void setTemp(String t)
    {
		this.temp = t;
    }

}
