package ec.gob.portoaguas.taxi.login;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.muddzdev.styleabletoastlibrary.StyleableToast;

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
import java.util.regex.Pattern;

import ec.gob.portoaguas.taxi.MainActivity;
import ec.gob.portoaguas.taxi.R;
import ec.gob.portoaguas.taxi.Utils.JSON;


/**
 * Created by PMAT-PROGRAMADOR_1 on 08/08/2017.
 */

public class LoginActivity extends AppCompatActivity{
    /*
    VARIABLES
     */
    TextView link_clave,link_registro;
    Button btn_iniciar;
    EditText txt_usuario, txt_clave;
    String nombres, cedula;

    //****************/
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        SharedPreferences PREF_USER_PROFILE = this.getSharedPreferences("PREF_USER_PROFILE", Context.MODE_PRIVATE);
        if (PREF_USER_PROFILE.getString("p_correo", null) != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            //overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
        } else {

        link_clave      =   (TextView)  findViewById(R.id.link_recuperarClave);
        link_registro   =   (TextView)  findViewById(R.id.link_registrar);
        btn_iniciar     =   (Button)    findViewById(R.id.btn_login);
        txt_usuario     =   (EditText)  findViewById(R.id.textUsuario);
        txt_clave       =   (EditText)  findViewById(R.id.txt_clave);

        btn_iniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (txt_usuario.getText().toString().isEmpty()) {
                    txt_usuario.setError("Por favor ingrese su correo");
                } else if (txt_clave.getText().toString().isEmpty()) {
                    txt_clave.setError("Por favor ingrese su Contraseña");
                } else if(!validarEmail(txt_usuario.getText().toString())) {
                    txt_usuario.setError("Correo no valido");
                }else{
                    new Login_cliente().execute(txt_usuario.getText().toString(), txt_clave.getText().toString());
                }

            }
        });

        link_registro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // Intent inte = new Intent(LoginActivity.this, Registrar.class);
              //  startActivity(inte);
               // finish();
                //overridePendingTransition(R.anim.left_in,R.anim.left_out);

            }
        });
        }

    }

    class Login_cliente extends AsyncTask<String,Void,String>{
        public String resul;
        public String datas;
        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(LoginActivity.this);
            pDialog.setMessage("Iniciando Sessión espere por favor!...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            final SharedPreferences PREF_DISPOSITIVO = getSharedPreferences("PREF_DISPOSITIVO", Context.MODE_PRIVATE);

            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("correo", params[0]));
            nameValuePairs.add(new BasicNameValuePair("clave", params[1]));
            nameValuePairs.add(new BasicNameValuePair("token_firebase", PREF_DISPOSITIVO.getString("p_token", null)));
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost("http://" + JSON.ipserver +"/Login");
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
                HttpResponse response = httpclient.execute(httppost);
                String status = String.valueOf(response.getStatusLine().getStatusCode());
               if (status.equals("500")) {
                    Log.e("ERROR 500 ", "ERROR INTERNO EN EL SERVIDOR ALMACENAR LOS TRAMITES GUARDADOS OFFLINE");
                    resul = "false";
               } else {
                    HttpEntity entity = response.getEntity();
                    datas = EntityUtils.toString(entity);
                    JSONObject obj = new JSONObject(datas);
                    String codigojson = obj.getString("RES");
                            resul = codigojson;
                   if(resul.equals("L_CORRECTO")){
                       nombres=obj.getString("nombres");
                       cedula = obj.getString("cedula");
                   }
               }
            } catch (Exception e) {
                Log.e("log_tag", "Error in http connection " + e.toString());
                resul = "false";
            }
            return resul;
        }

        @Override
        protected void onPostExecute(String aBoolean) {
            pDialog.dismiss();
            if(aBoolean.equals("false")){
                StyleableToast.makeText(LoginActivity.this, "Servidor no dsponible por el momentos!", Toast.LENGTH_LONG, R.style.StyledToastError).show();
            }else if(aBoolean.equals("L_DISPOSITIVO")){
                StyleableToast.makeText(LoginActivity.this, "El dispositivo no existe!", Toast.LENGTH_LONG, R.style.StyledToastError).show();

            }else if(aBoolean.equals("L_UINCORRECTO")){
                StyleableToast.makeText(LoginActivity.this, "Por favor verifique el correo!", Toast.LENGTH_LONG, R.style.StyledToastError).show();
                txt_usuario.setError("Correo es incorrecto. Registrese");
                txt_usuario.requestFocus();

            }else if(aBoolean.equals("L_CINCORRECTA")){
                StyleableToast.makeText(LoginActivity.this, "La clave es incorrecta!", Toast.LENGTH_LONG, R.style.StyledToastError).show();
                txt_clave.setError("Clave es incorrecta");
                txt_clave.requestFocus();
                txt_clave.setText("");

            }else if(aBoolean.equals("L_INACTIVO")){
                StyleableToast.makeText(LoginActivity.this, "La cuenta aun no ha sido activada!", Toast.LENGTH_LONG, R.style.StyledToastError).show();

            }else if(aBoolean.equals("L_CORRECTO")){
                SharedPreferences PREF_USER_PROFILE = getSharedPreferences("PREF_USER_PROFILE", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = PREF_USER_PROFILE.edit();
                editor.putString("p_correo", txt_usuario.getText().toString());
                editor.putString("p_nombre", nombres);
                editor.putString("p_cedula", cedula);
                editor.putString("p_sincro", "0");
                editor.commit();

                //StyleableToast.makeText(LoginActivity.this, "Bienvenido a Portoaguas EP App!", Toast.LENGTH_LONG, R.style.StyledToasOK).show();
                Intent inte = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(inte);
                finish();
            }
        }
    }
    private boolean validarEmail(String email) {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }
}
