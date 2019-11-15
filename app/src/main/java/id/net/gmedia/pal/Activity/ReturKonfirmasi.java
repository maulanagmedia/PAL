package id.net.gmedia.pal.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.leonardus.irfan.ApiVolleyManager;
import com.leonardus.irfan.AppLoading;
import com.leonardus.irfan.AppRequestCallback;
import com.leonardus.irfan.JSONBuilder;
import com.leonardus.irfan.SimpleObjectModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import id.net.gmedia.pal.Adapter.ApprovalReturAdapter;
import id.net.gmedia.pal.Model.CustomerModel;
import id.net.gmedia.pal.Model.NotaPenjualanModel;
import id.net.gmedia.pal.R;
import id.net.gmedia.pal.Util.AppSharedPreferences;
import id.net.gmedia.pal.Util.Constant;

public class ReturKonfirmasi extends AppCompatActivity {

    private List<NotaPenjualanModel> listNota = new ArrayList<>();
    private List<SimpleObjectModel> listApproval = new ArrayList<>();
    private ApprovalReturAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retur_konfirmasi);

        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle("Konfirmasi Retur");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //Inisialisasi Recycler View & Adapter
        RecyclerView rv_nota = findViewById(R.id.rv_nota);
        rv_nota.setItemAnimator(new DefaultItemAnimator());
        rv_nota.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ApprovalReturAdapter(this, listNota);
        rv_nota.setAdapter(adapter);

        loadRetur();
        loadApproval();
    }

    private void loadRetur(){
        AppLoading.getInstance().showLoading(this, R.layout.popup_loading);
        ApiVolleyManager.getInstance().addRequest(this, Constant.URL_RETUR_KONFIRMASI_LIST,
                ApiVolleyManager.METHOD_POST, Constant.getTokenHeader(AppSharedPreferences.getId(this)),
                new AppRequestCallback(new AppRequestCallback.RequestListener() {
                    @Override
                    public void onEmpty(String message) {
                        listNota.clear();
                        adapter.notifyDataSetChanged();

                        Toast.makeText(ReturKonfirmasi.this, message, Toast.LENGTH_SHORT).show();
                        AppLoading.getInstance().stopLoading();
                    }

                    @Override
                    public void onSuccess(String result) {
                        try{
                            listNota.clear();

                            JSONArray retur_list = new JSONArray(result);
                            for(int i = 0; i < retur_list.length(); i++){
                                JSONObject nota = retur_list.getJSONObject(i);
                                listNota.add(new NotaPenjualanModel(nota.getString("nomor"), nota.getString("nota_jual"),
                                        new CustomerModel(nota.getString("kode_pelanggan"),
                                                nota.getString("nama_pelanggan")), nota.getDouble("total_harga")));
                            }

                            adapter.notifyDataSetChanged();
                        }
                        catch (JSONException e){
                            Toast.makeText(ReturKonfirmasi.this, R.string.error_json, Toast.LENGTH_SHORT).show();
                            Log.e(Constant.TAG, e.getMessage());
                        }

                        AppLoading.getInstance().stopLoading();
                    }

                    @Override
                    public void onFail(String message) {
                        Toast.makeText(ReturKonfirmasi.this, message, Toast.LENGTH_SHORT).show();
                        AppLoading.getInstance().stopLoading();
                    }
                }));
    }

    public void loadApproval(){
        //Memuat data respon approval dari Web Service
        ApiVolleyManager.getInstance().addRequest(this,
                Constant.URL_MASTER_APPROVAL + "/retur_jual", ApiVolleyManager.METHOD_GET,
                Constant.getTokenHeader(AppSharedPreferences.getId(this)),
                new AppRequestCallback(new AppRequestCallback.RequestListener() {
                    @Override
                    public void onEmpty(String message) {
                        listApproval.clear();
                        adapter.setListApproval(listApproval);
                    }

                    @Override
                    public void onSuccess(String result) {
                        try{
                            listApproval.clear();

                            JSONArray approval = new JSONObject(result).getJSONArray("approval");
                            for(int i = 0; i < approval.length(); i++){
                                listApproval.add(new SimpleObjectModel(approval.getJSONObject(i).getString("kode"),
                                        approval.getJSONObject(i).getString("nama")));
                            }

                            adapter.setListApproval(listApproval);
                        }
                        catch (JSONException e){
                            Toast.makeText(ReturKonfirmasi.this, R.string.error_json, Toast.LENGTH_SHORT).show();
                            Log.e(Constant.TAG, e.getMessage());
                        }
                    }

                    @Override
                    public void onFail(String message) {
                        Toast.makeText(ReturKonfirmasi.this, message, Toast.LENGTH_SHORT).show();
                    }
                }));
    }

    public void responRetur(String nota, String approval){
        //Kirim respon retur berdasarkan respon approval yang dipilih
        AppLoading.getInstance().showLoading(this, R.layout.popup_loading);
        JSONBuilder body = new JSONBuilder();
        body.add("nomor", nota);
        body.add("approval",approval);

        ApiVolleyManager.getInstance().addRequest(this, Constant.URL_APPROVAL_RETUR, ApiVolleyManager.METHOD_POST,
                Constant.getTokenHeader(AppSharedPreferences.getId(this)), body.create(),
                new AppRequestCallback(new AppRequestCallback.RequestListener() {
                    @Override
                    public void onEmpty(String message) {
                        Toast.makeText(ReturKonfirmasi.this, message, Toast.LENGTH_SHORT).show();
                        AppLoading.getInstance().stopLoading();
                    }

                    @Override
                    public void onSuccess(String result) {
                        Toast.makeText(ReturKonfirmasi.this, result, Toast.LENGTH_SHORT).show();
                        AppLoading.getInstance().stopLoading();

                        loadRetur();
                    }

                    @Override
                    public void onFail(String message) {
                        Toast.makeText(ReturKonfirmasi.this, message, Toast.LENGTH_SHORT).show();
                        AppLoading.getInstance().stopLoading();
                    }
                }));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
