package id.net.gmedia.pal.Activity.Approval;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.leonardus.irfan.ApiVolleyManager;
import com.leonardus.irfan.AppLoading;
import com.leonardus.irfan.AppRequestCallback;
import com.leonardus.irfan.Converter;
import com.leonardus.irfan.JSONBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import id.net.gmedia.pal.Adapter.ApprovalPODetailAdapter;
import id.net.gmedia.pal.Adapter.ApprovalSOKirimDetailAdapter;
import id.net.gmedia.pal.Model.BarangPOModel;
import id.net.gmedia.pal.Model.PurchaseOrderModel;
import id.net.gmedia.pal.Model.SatuanModel;
import id.net.gmedia.pal.R;
import id.net.gmedia.pal.Util.AppSharedPreferences;
import id.net.gmedia.pal.Util.Constant;

public class ApprovalSOKirimDetail extends AppCompatActivity {

    //Variabel global nomor nota
    public PurchaseOrderModel nota;

    //Variabel UI
    private TextView txt_nota, txt_total, txt_tgl, txt_margin, txt_sepuluh, tonase, txt_estimasi_biaya;

    private List<BarangPOModel> listPo = new ArrayList<>();
    private ApprovalSOKirimDetailAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approval_so_kirim_detail);

        //Inisialisasi Toolbar
        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle("Detail SO Kirim");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //Inisialisasi data global nota
        if(getIntent().hasExtra(Constant.EXTRA_NOTA)){
            Gson gson = new Gson();
            nota = gson.fromJson(getIntent().getStringExtra(Constant.EXTRA_NOTA), PurchaseOrderModel.class);
        }

        //Inisialisasi UI
        txt_nota = findViewById(R.id.txt_nota);
        txt_estimasi_biaya = findViewById(R.id.txt_estimasi_biaya);
        txt_total = findViewById(R.id.txt_total);
        txt_sepuluh = findViewById(R.id.txt_sepuluh_persen_margin);
        txt_tgl = findViewById(R.id.txt_tanggal);

        //Inisialisasi RecyclerView & Adapter
        RecyclerView rv_po = findViewById(R.id.rv_po);
        rv_po.setItemAnimator(new DefaultItemAnimator());
        rv_po.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ApprovalSOKirimDetailAdapter(this, listPo);
        rv_po.setAdapter(adapter);

        //muat data
        loadDetail();
    }

    private void loadDetail(){
        AppLoading.getInstance().showLoading(this, R.layout.popup_loading);
        String parameter = String.format(Locale.getDefault(), "?id_pengiriman=1&start=0&limit=10", nota.getId());
        ApiVolleyManager.getInstance().addRequest(this, Constant.URL_DETAIL_SO_KIRIM + parameter,
                ApiVolleyManager.METHOD_GET, Constant.getTokenHeader(AppSharedPreferences.getId(this)),
                new AppRequestCallback(new AppRequestCallback.RequestListener() {
                    @Override
                    public void onEmpty(String message) {
                        listPo.clear();
                        adapter.notifyDataSetChanged();
                        AppLoading.getInstance().stopLoading();
                    }

                    @Override
                    public void onSuccess(String result) {
                        try{
                            txt_tgl.setText(nota.getTanggal());
                            txt_estimasi_biaya.setText(Converter.doubleToRupiah(nota.getEstimasi_biaya()));
                            txt_total.setText(Converter.doubleToRupiah(nota.getTotal_nominal()));
                            txt_sepuluh.setText(nota.getTotal_sepuluh_maragin());
                            txt_nota.setText(nota.getTotal_margin());
                            listPo.clear();

                            JSONArray purchase_orders = new JSONObject(result).getJSONArray("data");
                            for(int i = 0; i < purchase_orders.length(); i++){
                                JSONObject po = purchase_orders.getJSONObject(i);
                                listPo.add(new BarangPOModel(po.getString("kode_area")
                                        ,po.getString("nama_pelanggan")
                                        , po.getString("nama_barang")
                                        ,po.getString("jumlah")
                                        , po.getString("tonase")
                                        , po.getDouble("total_nominal")));
                            }

                            adapter.notifyDataSetChanged();
                        }
                        catch (JSONException e){
                            Toast.makeText(ApprovalSOKirimDetail.this, R.string.error_json, Toast.LENGTH_SHORT).show();
                            Log.e(Constant.TAG, e.getMessage());
                        }

                        AppLoading.getInstance().stopLoading();
                    }

                    @Override
                    public void onFail(String message) {
                        Toast.makeText(ApprovalSOKirimDetail.this, message, Toast.LENGTH_SHORT).show();
                    }
                }));
    }

  /*  public void hapusBarangSO(String id){
        AppLoading.getInstance().showLoading(this, R.layout.popup_loading);
        JSONBuilder body = new JSONBuilder();
        body.add("id_detail", id);
        body.add("nomor_nota", nota.getId());

        ApiVolleyManager.getInstance().addRequest(this, Constant.URL_PO_HAPUS, ApiVolleyManager.METHOD_POST,
                Constant.getTokenHeader(AppSharedPreferences.getId(this)), body.create(),
                new AppRequestCallback(new AppRequestCallback.RequestListener() {
                    @Override
                    public void onEmpty(String message) {
                        Toast.makeText(ApprovalSOKirimDetail.this, message, Toast.LENGTH_SHORT).show();
                        AppLoading.getInstance().stopLoading();
                    }

                    @Override
                    public void onSuccess(String result) {
                        Toast.makeText(ApprovalSOKirimDetail.this, result, Toast.LENGTH_SHORT).show();
                        AppLoading.getInstance().stopLoading();

                        loadDetail();
                    }

                    @Override
                    public void onFail(String message) {
                        Toast.makeText(ApprovalSOKirimDetail.this, message, Toast.LENGTH_SHORT).show();
                        AppLoading.getInstance().stopLoading();
                    }
                }));
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
