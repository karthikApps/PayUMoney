package payumoney.payu.com.payumoneysdkapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.payUMoney.sdk.PayUmoneySdkInitilizer;
import com.payUMoney.sdk.SdkConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;


public class MyActivity extends Activity {


    EditText amt = null;
    Button pay = null;

    public static final String TAG = "PayUMoneySDK Sample";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        amt = (EditText) findViewById(R.id.amount);
        pay = (Button) findViewById(R.id.pay);
    }

    private boolean isDouble(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String getTxnId() {
        return ("0nf7" + System.currentTimeMillis());
    }

    private double getAmount() {


        Double amount = 10.0;

        if (isDouble(amt.getText().toString())) {
            amount = Double.parseDouble(amt.getText().toString());
            return amount;
        } else {
            Toast.makeText(getApplicationContext(), "Paying Default Amount ₹10", Toast.LENGTH_LONG).show();
            return amount;
        }
    }

    public void makePayment(View view) {

        PayUmoneySdkInitilizer.PaymentParam.Builder builder = new PayUmoneySdkInitilizer.PaymentParam.Builder();

        builder.setAmount(getAmount())
                .setTnxId(getTxnId())
                .setPhone("9843810893")
                .setProductName("product_name")
                .setFirstName("Karthik")
                .setEmail("karthik1info@gmail.com")
                .setsUrl("https://www.payumoney.com/mobileapp/payumoney/success.php")
                .setfUrl("https://www.payumoney.com/mobileapp/payumoney/failure.php")
                .setUdf1("")
                .setUdf2("")
                .setUdf3("")
                .setUdf4("")
                .setUdf5("")
                .setIsDebug(false)
                .setKey("")
                .setMerchantId("");// Debug Merchant ID

        PayUmoneySdkInitilizer.PaymentParam paymentParam = builder.build();

        // Recommended
        calculateServerSideHashAndInitiatePayment(paymentParam);
    }

    public static String hashCal(String str) {
        byte[] hashseq = str.getBytes();
        StringBuilder hexString = new StringBuilder();
        try {
            MessageDigest algorithm = MessageDigest.getInstance("SHA-512");
            algorithm.reset();
            algorithm.update(hashseq);
            byte messageDigest[] = algorithm.digest();
            for (byte aMessageDigest : messageDigest) {
                String hex = Integer.toHexString(0xFF & aMessageDigest);
                if (hex.length() == 1) {
                    hexString.append("0");
                }
                hexString.append(hex);
            }
        } catch (NoSuchAlgorithmException ignored) {
        }
        return hexString.toString();
    }

    private void calculateServerSideHashAndInitiatePayment(final PayUmoneySdkInitilizer.PaymentParam paymentParam) {
        StringRequest sr = new StringRequest(Request.Method.POST, "http://dealanzer.com/api/Credits/createHash", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, response.toString());

                try {
                    JSONObject obj = new JSONObject(response);
                    final String status = obj.getString("status");
                    final String value = obj.getString("value");
                    JSONObject value_object = obj.getJSONObject("value");

                    if (status.equals("false")) {
                        Toast.makeText(getApplicationContext(), value, Toast.LENGTH_LONG).show();
                    } else if (status.equals("true")) {
                        String hash = value_object.getString("hash");
                        Log.i("app_activity", "Server calculated Hash :  " + hash);
                        paymentParam.setMerchantHash(hash);

                        PayUmoneySdkInitilizer.startPaymentActivityForResult(MyActivity.this, paymentParam);

                    }
                } catch (JSONException exception) {
                    Log.e("--JSON EXCEPTION--", exception.toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Log.d(TAG, "" + error.getMessage() + "," + error.toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("key", "");
                params.put("txnid", "0nf71472603610087");
                params.put("amount", "10");
                params.put("productinfo", "payUmoney");
                params.put("salt", "");
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/x-www-form-urlencoded");
                headers.put("Api-Token", "992b52d860dd720ffaea133342088bdc");
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(sr);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PayUmoneySdkInitilizer.PAYU_SDK_PAYMENT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Log.i(TAG, "Success - Payment ID : " + data.getStringExtra(SdkConstants.PAYMENT_ID));
                String paymentId = data.getStringExtra(SdkConstants.PAYMENT_ID);
                showDialogMessage("Payment Success Id : " + paymentId);
            } else if (resultCode == RESULT_CANCELED) {
                Log.i(TAG, "failure");
                showDialogMessage("cancelled");
            } else if (resultCode == PayUmoneySdkInitilizer.RESULT_FAILED) {
                Log.i("app_activity", "failure");

                if (data != null) {
                    if (data.getStringExtra(SdkConstants.RESULT).equals("cancel")) {

                    } else {
                        showDialogMessage("failure");
                    }
                }
                //Write your code if there's no result
            } else if (resultCode == PayUmoneySdkInitilizer.RESULT_BACK) {
                Log.i(TAG, "User returned without login");
                showDialogMessage("User returned without login");
            }
        }
    }

    private void showDialogMessage(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(TAG);
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();

    }
}
