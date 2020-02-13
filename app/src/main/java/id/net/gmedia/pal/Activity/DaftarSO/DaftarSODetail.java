package id.net.gmedia.pal.Activity.DaftarSO;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.leonardus.irfan.ApiVolleyManager;
import com.leonardus.irfan.AppLoading;
import com.leonardus.irfan.AppRequestCallback;
import com.leonardus.irfan.Converter;
import com.leonardus.irfan.LoadMoreScrollListener;
import com.leonardus.irfan.bluetoothprinter.BluetoothPrinter;
import com.leonardus.irfan.bluetoothprinter.Model.Item;
import com.leonardus.irfan.bluetoothprinter.Model.Transaksi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import id.net.gmedia.pal.Adapter.BarangDetailAdapter;
import id.net.gmedia.pal.Adapter.DaftarSODetailAdapter;
import id.net.gmedia.pal.Model.BarangModel;
import id.net.gmedia.pal.Model.CustomerModel;
import id.net.gmedia.pal.Model.NotaPenjualanModel;
import id.net.gmedia.pal.Model.SatuanModel;
import id.net.gmedia.pal.R;
import id.net.gmedia.pal.Util.AppSharedPreferences;
import id.net.gmedia.pal.Util.Constant;
import id.net.gmedia.pal.Util.PalPrinter;

public class DaftarSODetail extends AppCompatActivity {

    //Variabel global customer & nota
    public CustomerModel customer;
    public NotaPenjualanModel nota;

    //variabel filter & loadmore data
    private String search = "";
    private LoadMoreScrollListener loadMoreScrollListener;

    //Variabel UI
    private TextView txt_nama, txt_piutang, txt_nota;

    //Variabel data barang
    private List<BarangModel> listBarang = new ArrayList<>();
    private RecyclerView.Adapter adapter;

    //Variabel bluetooth printer
    private PalPrinter printerManager;
    private String namaSales = "", kodeArea = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daftar_sodetail);

        //Inisialisasi Toolbar
        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle("Detail SO");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //Inisialisasi Ui
        txt_nama = findViewById(R.id.txt_nama);
        txt_piutang = findViewById(R.id.txt_piutang);
        txt_nota = findViewById(R.id.txt_nota);

        //Inisialisasi data nota
        if(getIntent().hasExtra(Constant.EXTRA_NOTA)){
            Gson gson = new Gson();
            nota = gson.fromJson(getIntent().getStringExtra(Constant.EXTRA_NOTA), NotaPenjualanModel.class);
        }

        //Inisialisasi RecyclerView & Adapter
        RecyclerView rv_riwayat = findViewById(R.id.rv_riwayat);
        rv_riwayat.setItemAnimator(new DefaultItemAnimator());
        rv_riwayat.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        if(getIntent().hasExtra(Constant.EXTRA_EDIT)){
            adapter = new DaftarSODetailAdapter(this, listBarang, Constant.PENJUALAN_SO);
        }
        else{
            adapter = new BarangDetailAdapter(this, listBarang, Constant.PENJUALAN_SO);
        }
        rv_riwayat.setAdapter(adapter);
        loadMoreScrollListener = new LoadMoreScrollListener() {
            @Override
            public void onLoadMore() {
                loadBarang(false);
            }
        };
        rv_riwayat.addOnScrollListener(loadMoreScrollListener);

        //Muat data barang
        loadBarang(true);
    }

    private void loadBarang(final boolean init){
        //membaca data barang dari Web Service
        AppLoading.getInstance().showLoading(this, R.layout.popup_loading);
        if(init){
            loadMoreScrollListener.initLoad();
        }

        String parameter = String.format(Locale.getDefault(), "%s?start=%d&limit=%d&search=%s", nota.getId(),
                loadMoreScrollListener.getLoaded(), 10, Converter.encodeURL(search));
        ApiVolleyManager.getInstance().addRequest(this, Constant.URL_DAFTAR_SO_DETAIL + parameter, ApiVolleyManager.METHOD_GET,
                Constant.getTokenHeader(AppSharedPreferences.getId(this)), new AppRequestCallback(new AppRequestCallback.RequestListener() {
                    @Override
                    public void onEmpty(String message) {
                        if(init){
                            listBarang.clear();
                            adapter.notifyDataSetChanged();
                        }

                        AppLoading.getInstance().stopLoading();
                        loadMoreScrollListener.finishLoad(0);
                    }

                    @Override
                    public void onSuccess(String result) {
                        try{
                            //Inisialisasi Header
                            JSONObject obj = new JSONObject(result).getJSONObject("nota");
                            customer = new CustomerModel(obj.getString("kode_pelanggan"),
                                    obj.getString("nama_pelanggan"));
                            namaSales = obj.getString("nama_sales");
                            kodeArea = obj.getString("kode_area");
                            txt_nama.setText(customer.getNama());
                            txt_piutang.setText(Converter.doubleToRupiah(obj.getDouble("total")));
                            txt_nota.setText(nota.getId());

                            if(init){
                                listBarang.clear();
                            }

                            JSONArray array = new JSONObject(result).getJSONArray("barang_list");
                            for(int i = 0; i < array.length(); i++){
                                JSONObject item = array.getJSONObject(i);
                                List<SatuanModel> satuan = new ArrayList<>();

                                JSONArray array_satuan = item.getJSONArray("satuan_list");

                                for(int j = 0; j < array_satuan.length(); j++){
                                    satuan.add(new SatuanModel(array_satuan.getString(j)));
                                }

                                BarangModel barang = new BarangModel(item.getString("id"), item.getString("kode_barang"),
                                        item.getString("nama_barang"), item.getDouble("harga_satuan"),
                                        item.getInt("jumlah"), item.getString("satuan"), item.getDouble("diskon_rupiah"),
                                        item.getDouble("harga_total"), item.getString("no_batch"));
                                barang.setListSatuan(satuan);

                                listBarang.add(barang);
                            }

                            loadMoreScrollListener.finishLoad(array.length());
                            adapter.notifyDataSetChanged();
                        }
                        catch (JSONException e){
                            Toast.makeText(DaftarSODetail.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e(Constant.TAG, e.getMessage());
                            e.printStackTrace();
                            loadMoreScrollListener.finishLoad(0);
                        }

                        AppLoading.getInstance().stopLoading();
                        loadMoreScrollListener.finishLoad(0);
                    }

                    @Override
                    public void onFail(String message) {
                        Toast.makeText(DaftarSODetail.this, message, Toast.LENGTH_SHORT).show();
                        AppLoading.getInstance().stopLoading();
                        loadMoreScrollListener.finishLoad(0);
                    }
                }));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_nota_detail,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_print:
                Bitmap logo = BitmapFactory.decodeResource(getResources(), R.drawable.ic_logo_print);
                printerManager = new PalPrinter(this, Bitmap.createScaledBitmap(
                        logo, 550, 180, false));
                printerManager.startService();
                printerManager.setListener(new BluetoothPrinter.BluetoothListener() {
                    @Override
                    public void onBluetoothConnected() {
                        List<Item> listItem = new ArrayList<>();
                        double total_diskon = 0;

                        for(BarangModel b : listBarang){
                            String nama = b.getNama();
                            //nama += b.getNo_batch().isEmpty()?" (-)" : " (" + b.getNo_batch() + "}";
                            listItem.add(new Item(nama, b.getJumlah(), b.getHarga()));
                            total_diskon += b.getDiskon();
                        }

                        Transaksi transaksi = new Transaksi(
                                txt_nama.getText().toString()
                                ,namaSales + " ( " + kodeArea + " )"
                                ,txt_nota.getText().toString()
                                ,new Date()
                                ,new Date()
                                ,listItem);
                        transaksi.setTunai(nota.getTotal());
                        transaksi.setDiskon(total_diskon);

                        printerManager.printNota(transaksi);
                    }

                    @Override
                    public void onBluetoothFailed(String message) {
                        Toast.makeText(DaftarSODetail.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if(printerManager != null){

            printerManager.stopService();
        }
        super.onDestroy();
    }
}
