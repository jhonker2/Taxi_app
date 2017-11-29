package ec.gob.portoaguas.taxi.Servicios;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.util.ArrayList;

import ec.gob.portoaguas.taxi.Utils.JSON;


/**
 * Created by PMAT-PROGRAMADOR_1 on 04/08/2017.
 */

public class GPS_Service extends Service  {

    private LocationListener listener;
    private LocationManager locationManager;



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(final Location location) {
                Intent i = new Intent("location_update");
                i.putExtra("coordinates", location.getLongitude()+" "+location.getLatitude());
                sendBroadcast(i);
                Log.e("Latlong",location.getLatitude()+"  "+location.getLongitude());
                 //Aki deberia mandar a guardar al servidor
                //latitud_r = location.getLatitude();
                //logintud_r = location.getLongitude();
                new GPS_store().execute(String.valueOf(location.getLatitude()),String.valueOf(location.getLongitude()));


            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };


        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        //noinspection MissingPermission
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,0,listener);
    }

    public class GPS_store extends AsyncTask<String , Void, Boolean>{
        SharedPreferences dato = getSharedPreferences("PREF_USER_PROFILE", Context.MODE_PRIVATE);
        public String dat,ID_ = "";
        public boolean resul;

        @Override
        protected Boolean doInBackground(String... params) {
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("latitud", params[0]));
            nameValuePairs.add(new BasicNameValuePair("longitud", params[1]));
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost("http://" + JSON.ipserver + "/localizacion");
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
                HttpResponse response = httpclient.execute(httppost);
                String status = String.valueOf(response.getStatusLine().getStatusCode());
                if (status.equals("500")) {
                    Log.e("ERROR 500 ", "ERROR INTERNO EN EL SERVIDOR ALMACENAR LOS TRAMITES GUARDADOS OFFLINE");
                    resul = false;
                } else {
                    HttpEntity entity = response.getEntity();
                    dat = EntityUtils.toString(entity);
                    JSONObject obj = new JSONObject(dat);
                    String codigojson = obj.getString("Actualizacion");
                    dat = codigojson;
                    ID_ = params[0];
                    resul = true;
                }
            } catch (Exception e) {
                Log.e("log_tag", "Error in http connection " + e.toString());
                resul = false;
            }
            return resul;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if(aBoolean){
                    Log.e("Posicion Almacenada","ok");
            }else{
                    Log.e("Posicion Almacenada","False");
            }
        }
    }


}
