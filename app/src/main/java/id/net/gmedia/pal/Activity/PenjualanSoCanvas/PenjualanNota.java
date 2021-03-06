package id.net.gmedia.pal.Activity.PenjualanSoCanvas;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.TaskStackBuilder;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import id.net.gmedia.pal.Activity.DaftarSO.DaftarSO;
import id.net.gmedia.pal.Activity.Riwayat;
import id.net.gmedia.pal.MainActivity;
import id.net.gmedia.pal.Model.BarangModel;
import id.net.gmedia.pal.Adapter.PenjualanNotaAdapter;
import id.net.gmedia.pal.Model.CaraBayarModel;
import id.net.gmedia.pal.PetaOutlet;
import id.net.gmedia.pal.R;
import id.net.gmedia.pal.Util.AppKeranjangPenjualan;

import com.leonardus.irfan.ApiVolleyManager;
import com.leonardus.irfan.AppLoading;

import id.net.gmedia.pal.Util.AppSharedPreferences;
import id.net.gmedia.pal.Util.Constant;
import id.net.gmedia.pal.Util.GoogleLocationManager;
import id.net.gmedia.pal.Util.OptionItem;

import com.leonardus.irfan.AppRequestCallback;
import com.leonardus.irfan.Converter;
import com.leonardus.irfan.DateTimeChooser;
import com.leonardus.irfan.Haversine;
import com.leonardus.irfan.ItemValidation;
import com.leonardus.irfan.JSONBuilder;

public class PenjualanNota extends AppCompatActivity implements GoogleLocationManager.LocationUpdateListener {

    //Variabel UI
    private TextView txt_total, txt_jarak;
    public TextView txt_tempo;
    public AppCompatSpinner spn_bayar;
    private ProgressBar pb_map;
    private List<CaraBayarModel> listCaraBayar = new ArrayList<>();
    private List<OptionItem> listPaket = new ArrayList<>();

    //Variabel lokasi
    private GoogleLocationManager manager;
    private Location current_location;
    private ArrayAdapter adapterCB;
    private String crBayar = "";
    private Spinner spPaket;
    private ArrayAdapter adapterPaket;
    private String selectedKodePaket = "", selectedNamaPaket = "", selectedTokenPaket = "";
    private Button btnGunakanPaket;
    private TextView txt_nama, txt_alamat;
    public boolean isPaket = false;
    public boolean isPaketValid = false;
    private LinearLayout llPaket;
    private TextView tvPotongan, tvTotalAkhir;
    private ItemValidation iv = new ItemValidation();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_penjualan_nota);

        //Inisialisasi Toolbar
        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle("Nota Penjualan");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //Inisialisasi UI
        txt_nama = findViewById(R.id.txt_nama);
        txt_alamat = findViewById(R.id.txt_alamat);
        txt_total = findViewById(R.id.txt_total);
        txt_jarak = findViewById(R.id.txt_jarak);
        spn_bayar = findViewById(R.id.spn_bayar);
        txt_tempo = findViewById(R.id.txt_tempo);
        pb_map = findViewById(R.id.pb_map);
        btnGunakanPaket = (Button) findViewById(R.id.btn_gunakan_paket);

        // Paket
        spPaket = (Spinner) findViewById(R.id.sp_paket);
        adapterPaket = new ArrayAdapter(PenjualanNota.this, android.R.layout.simple_list_item_1, listPaket);
        spPaket.setAdapter(adapterPaket);
        paketStatus(false, false);

        // Cara Bayar
        adapterCB = new ArrayAdapter(PenjualanNota.this,
                R.layout.simple_list, R.id.text1, listCaraBayar);
        spn_bayar.setAdapter(adapterCB);
        //listCaraBayar.add("pilih salah satu..");

        //spn_bayar.setSelection(AppKeranjangPenjualan.getInstance().getCara_bayar());
        txt_tempo.setText(AppKeranjangPenjualan.getInstance().getTempo());

        //Inisialisasi nilai UI
        txt_nama.setText(AppKeranjangPenjualan.getInstance().getCustomer().getNama());
        txt_alamat.setText(AppKeranjangPenjualan.getInstance().getCustomer().getAlamat());
        txt_jarak.setText(R.string.penjualan_detail_jarak);
        llPaket = (LinearLayout) findViewById(R.id.ll_paket);
        tvPotongan = (TextView) findViewById(R.id.tv_potongan);
        tvTotalAkhir = (TextView) findViewById(R.id.tv_akhir);

        //Inisialisasi RecyclerView & Adapter
        RecyclerView rv_nota = findViewById(R.id.rv_nota);
        rv_nota.setItemAnimator(new DefaultItemAnimator());
        rv_nota.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        PenjualanNotaAdapter adapter = new PenjualanNotaAdapter(this,
                AppKeranjangPenjualan.getInstance().getBarang(), AppKeranjangPenjualan.getInstance().getJENIS_PENJUALAN());
        rv_nota.setAdapter(adapter);

        //Muat data barang
        initBarang();

        //Inisialisasi manager lokasi
        manager = new GoogleLocationManager(this, this);
        manager.startLocationUpdates();
        pb_map.setVisibility(View.VISIBLE);

        initEvent();
        getCaraBayar();
        getPaket();
    }

    private void initEvent() {

        //Button tambah barang baru
        findViewById(R.id.img_tambah).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(PenjualanNota.this, PenjualanBarang.class);
                AppKeranjangPenjualan.getInstance().setTempo(txt_tempo.getText().toString());
                AppKeranjangPenjualan.getInstance().setCara_bayar(spn_bayar.getSelectedItemPosition());
                startActivity(i);
            }
        });

        findViewById(R.id.img_kalender).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateTimeChooser.getInstance().selectDate(PenjualanNota.this, new DateTimeChooser.DateTimeListener() {
                    @Override
                    public void onFinished(String dateString) {
                        txt_tempo.setText(dateString);
                    }
                });
            }
        });

        //Button checkout barang
        findViewById(R.id.btn_checkout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Validasi input nota
                if(current_location == null){
                    Toast.makeText(PenjualanNota.this, "Lokasi tidak terdeteksi", Toast.LENGTH_SHORT).show();
                }

                if(crBayar.equals("0")){
                    Toast.makeText(PenjualanNota.this, "Silahkan pilih Cara Bayar terlebih dahulu", Toast.LENGTH_SHORT).show();
                    return;
                }
                /*else if(spn_bayar.getSelectedItemPosition() == 0){
                    Toast.makeText(PenjualanNota.this, "Pilih cara bayar terlebih dahulu", Toast.LENGTH_SHORT).show();
                }*/
                /*else if(spn_bayar.getSelectedItemPosition() > 1 && txt_tempo.getText().toString().equals("")){
                    Toast.makeText(PenjualanNota.this, "Tanggal tempo belum terisi", Toast.LENGTH_SHORT).show();
                }*/
                else{
                    //checkout barang
                    AlertDialog dialog = new AlertDialog.Builder(PenjualanNota.this)
                            .setTitle("Konfirmasi")
                            .setMessage("Apakah anda yakin ingin memproses penjualan?")
                            .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    checkoutBarang(AppKeranjangPenjualan.getInstance().getJENIS_PENJUALAN());
                                }
                            })
                            .setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();

                }
            }
        });

        findViewById(R.id.img_refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(manager != null){
                    manager.stopLocationUpdates();
                }

                manager = new GoogleLocationManager(PenjualanNota.this, PenjualanNota.this);
                manager.startLocationUpdates();
                pb_map.setVisibility(View.VISIBLE);
            }
        });

        spn_bayar.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                CaraBayarModel item = (CaraBayarModel) parent.getItemAtPosition(position);
                crBayar = item.getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spPaket.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                OptionItem item = (OptionItem) parent.getItemAtPosition(position);
                selectedKodePaket = item.getValue();
                selectedNamaPaket = item.getText();

                if(position == 0){
                    paketStatus(false, false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnGunakanPaket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!selectedKodePaket.equals("0")){

                    AlertDialog dialog = new AlertDialog.Builder(PenjualanNota.this)
                            .setTitle("Konfirmasi")
                            .setMessage("Gunakan kode paket "+selectedNamaPaket+" ?")
                            .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    GunakanPaket();
                                }
                            })
                            .setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                }

            }
        });
    }

    private void paketStatus(boolean isPaket, boolean isPaketValid){

        this.isPaket = isPaket;
        this.isPaketValid = isPaketValid;
    }

    private void GunakanPaket(){

        //simpan nota checkout ke Web service
        JSONObject jBody = new JSONObject();
        JSONArray jBarang = new JSONArray();
        paketStatus(false, false);

        for(BarangModel barang : AppKeranjangPenjualan.getInstance().getBarang()){
            JSONObject jBar = new JSONObject();
            try {
                jBar.put("kode_barang", barang.getId());
                jBar.put("jumlah", barang.getJumlah() + barang.getJumlah_potong());
                jBar.put("satuan", barang.getSatuan());
                jBar.put("diskon", barang.getDiskon());
                jBar.put("no_batch", barang.getNo_batch());
                jBar.put("harga", barang.getHargaEdit());
                jBar.put("total_harga", barang.getTotal());
                jBarang.put(jBar);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        try {
            jBody.put("kode_paket", selectedKodePaket);
            jBody.put("kode_pelanggan", AppKeranjangPenjualan.getInstance().getCustomer().getId());
            jBody.put("barang", jBarang);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        AppLoading.getInstance().showLoading(this, R.layout.popup_loading, new AppLoading.CancelListener() {
            @Override
            public void onCancel() {
                ApiVolleyManager.getInstance().cancelRequest();
            }
        });

        selectedTokenPaket = "";
        tvPotongan.setText("");
        tvTotalAkhir.setText("");

        ApiVolleyManager.getInstance().addRequest(this, Constant.URL_GUNAKAN_PAKET, ApiVolleyManager.METHOD_POST,
                Constant.getTokenHeader(AppSharedPreferences.getId(this)),
                jBody, new AppRequestCallback(new AppRequestCallback.RequestListener() {
                    @Override
                    public void onEmpty(String message) {
                        AppLoading.getInstance().stopLoading();
                        Toast.makeText(PenjualanNota.this, message, Toast.LENGTH_LONG).show();
                        spPaket.setSelection(0);
                    }

                    @Override
                    public void onSuccess(String result) {

                        AppLoading.getInstance().stopLoading();
                        //Toast.makeText(PenjualanNota.this, result, Toast.LENGTH_LONG).show();
                        paketStatus(true, true);
                        try {
                            JSONObject response = new JSONObject(result);
                            selectedTokenPaket = selectedKodePaket;
                            tvPotongan.setText(iv.ChangeToCurrencyFormat(response.getString("total_saving")));
                            tvTotalAkhir.setText(iv.ChangeToCurrencyFormat(response.getString("total_harga_paket")));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFail(String message) {
                        AppLoading.getInstance().stopLoading();
                        Toast.makeText(PenjualanNota.this, message, Toast.LENGTH_LONG).show();
                    }
                }));
    }

    private void getCaraBayar() {

        AppLoading.getInstance().showLoading(this, R.layout.popup_loading);

        ApiVolleyManager.getInstance().addRequest(this, Constant.URL_CARA_BAYAR,
                ApiVolleyManager.METHOD_GET, Constant.getTokenHeader(AppSharedPreferences.getId(this)),
                new AppRequestCallback(new AppRequestCallback.RequestListener() {
                    @Override
                    public void onEmpty(String message) {

                        AppLoading.getInstance().stopLoading();
                    }

                    @Override
                    public void onSuccess(String result) {

                        listCaraBayar.add(new CaraBayarModel(
                                "0"
                                ,"- Pilih Pembayaran -"
                        ));
                        try{
                            //Inisialisasi Header

                            JSONArray array = new JSONArray(result);
                            for(int i = 0; i < array.length(); i++){
                                JSONObject item = array.getJSONObject(i);
                                listCaraBayar.add(new CaraBayarModel(
                                        item.getString("id")
                                        ,item.getString("text")
                                ));

                                if(i == 0){

                                    crBayar = item.getString("id");
                                }
                            }

                            adapterCB.notifyDataSetChanged();
                        }
                        catch (JSONException e){

                            Toast.makeText(PenjualanNota.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e(Constant.TAG, e.getMessage());
                            e.printStackTrace();
                        }

                        AppLoading.getInstance().stopLoading();
                    }

                    @Override
                    public void onFail(String message) {
                        Toast.makeText(PenjualanNota.this, message, Toast.LENGTH_SHORT).show();
                        AppLoading.getInstance().stopLoading();
                    }
                }));
    }

    private void getPaket() {

        AppLoading.getInstance().showLoading(this, R.layout.popup_loading);

        JSONObject jBody = new JSONObject();
        try {
            jBody.put("kode_pelanggan", AppKeranjangPenjualan.getInstance().getCustomer().getId());
            /*jBody.put("start", "");
            jBody.put("limit", "");
            jBody.put("search", "")*/;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiVolleyManager.getInstance().addRequest(this
                , Constant.URL_PAKET
                , ApiVolleyManager.METHOD_POST
                , Constant.getTokenHeader(AppSharedPreferences.getId(this))
                , jBody
                , new AppRequestCallback(new AppRequestCallback.RequestListener() {
                    @Override
                    public void onEmpty(String message) {

                        AppLoading.getInstance().stopLoading();
                    }

                    @Override
                    public void onSuccess(String result) {
                        try{
                            //Inisialisasi Header
                            listPaket.clear();
                            listPaket.add(new OptionItem("0", "Pilih Paket"));
                            selectedKodePaket = "0";

                            JSONObject response = new JSONObject(result);
                            JSONArray array = response.getJSONArray("data");

                            for(int i = 0; i < array.length(); i++){
                                JSONObject item = array.getJSONObject(i);

                                listPaket.add(new OptionItem(
                                        item.getString("kode_paket")
                                        ,item.getString("nama_paket")
                                ));
                            }

                            adapterPaket.notifyDataSetChanged();
                        }
                        catch (JSONException e){

                            Toast.makeText(PenjualanNota.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e(Constant.TAG, e.getMessage());
                            e.printStackTrace();
                        }

                        AppLoading.getInstance().stopLoading();
                    }

                    @Override
                    public void onFail(String message) {
                        Toast.makeText(PenjualanNota.this, message, Toast.LENGTH_SHORT).show();
                        AppLoading.getInstance().stopLoading();
                    }
                }));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == GoogleLocationManager.ACTIVATE_LOCATION) {
            if (manager != null) {
                manager.startLocationUpdates();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == GoogleLocationManager.PERMISSION_LOCATION) {
            if (manager != null) {
                manager.startLocationUpdates();
            }
        }
        else{
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void checkoutBarang(final int type){
        //simpan nota checkout ke Web service
        String url;
        if(type == Constant.PENJUALAN_SO){
            url = Constant.URL_CHECKOUT_SO;
        }
        else if(type == Constant.PENJUALAN_CANVAS){
            url = Constant.URL_CHECKOUT_CANVAS;
        }
        else{
            return;
        }

        List<JSONObject> array_barang = new ArrayList<>();
        for(BarangModel b : AppKeranjangPenjualan.getInstance().getBarang()){
            JSONBuilder obj = new JSONBuilder();
            obj.add("kode_barang", b.getId());
            if(type == Constant.PENJUALAN_SO){
                obj.add("jumlah_gudang", b.getJumlah());
                obj.add("jumlah_canvas", b.getJumlah_potong());
            }
            else{
                obj.add("jumlah", b.getJumlah());
            }

            obj.add("diskon", b.getDiskon());
            obj.add("satuan", b.getSatuan());
            obj.add("no_batch", b.getNo_batch());
            obj.add("harga", b.getHargaEdit());
            obj.add("total_harga", b.getTotal());
            array_barang.add(obj.create());
        }

        JSONBuilder body = new JSONBuilder();
        body.add("kode_pelanggan", AppKeranjangPenjualan.getInstance().getCustomer().getId());
        body.add("barang", new JSONArray(array_barang));
        body.add("user_latitude", current_location.getLatitude());
        body.add("user_longitude", current_location.getLongitude());
        body.add("cara_bayar", crBayar);
        body.add("kode_paket", selectedTokenPaket);

        Log.d(Constant.TAG, body.create().toString());
        AppLoading.getInstance().showLoading(this, R.layout.popup_loading, new AppLoading.CancelListener() {
            @Override
            public void onCancel() {
                ApiVolleyManager.getInstance().cancelRequest();
            }
        });

        ApiVolleyManager.getInstance().addRequest(this, url, ApiVolleyManager.METHOD_POST,
                Constant.getTokenHeader(AppSharedPreferences.getId(this)),
                body.create(), new AppRequestCallback(new AppRequestCallback.RequestListener() {
            @Override
            public void onEmpty(String message) {
                AppLoading.getInstance().stopLoading();
                Toast.makeText(PenjualanNota.this, message, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSuccess(String result) {
                AppLoading.getInstance().stopLoading();
                Toast.makeText(PenjualanNota.this, "Data berhasil disimpan", Toast.LENGTH_LONG).show();
                AppKeranjangPenjualan.getInstance().clearPenjualan();

                if(type == Constant.PENJUALAN_SO){
                    Intent resultIntent = new Intent(PenjualanNota.this, DaftarSO.class);
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(PenjualanNota.this);
                    stackBuilder.addNextIntentWithParentStack(resultIntent);
                    stackBuilder.startActivities();
                }
                else{
                    Intent resultIntent = new Intent(PenjualanNota.this, Riwayat.class);
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(PenjualanNota.this);
                    stackBuilder.addNextIntentWithParentStack(resultIntent);
                    stackBuilder.startActivities();
                }
            }

            @Override
            public void onFail(String message) {
                AppLoading.getInstance().stopLoading();
                Toast.makeText(PenjualanNota.this, message, Toast.LENGTH_SHORT).show();
            }
        }));
    }

    public void initBarang(){
        //Hitung total barang
        double sum = 0;
        for(BarangModel b : AppKeranjangPenjualan.getInstance().getBarang()){
            sum += (b.getSubtotal() - b.getDiskon());
        }
        txt_total.setText(Converter.doubleToRupiah(sum));
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Batalkan Penjualan");
        builder.setMessage("Anda yakin ingin membatalkan penjualan?");
        builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AppKeranjangPenjualan.getInstance().clearPenjualan();
                Intent i = new Intent(PenjualanNota.this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_penjualan, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_map:
                //Buka aktivity map antara sales & customer/outlet
                Gson gson = new Gson();
                Intent i = new Intent(this, PetaOutlet.class);
                i.putExtra(Constant.EXTRA_LOKASI_OUTLET, gson.toJson
                        (new LatLng(AppKeranjangPenjualan.getInstance().getCustomer().getLatitude(),
                                AppKeranjangPenjualan.getInstance().getCustomer().getLongitude())));
                if(current_location != null){
                    i.putExtra(Constant.EXTRA_LOKASI_USER, gson.toJson
                            (new LatLng(current_location.getLatitude(),
                            current_location.getLongitude())));
                }
                startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        manager.stopLocationUpdates();
    }

    @Override
    public void onChange(Location location) {
        //Update jarak customer
        if(AppKeranjangPenjualan.getInstance().getCustomer() != null){
            double distance = Haversine.distance(location.getLatitude(),
                    location.getLongitude(), AppKeranjangPenjualan.getInstance().getCustomer().getLatitude(),
                    AppKeranjangPenjualan.getInstance().getCustomer().getLongitude());
            String string_lokasi = "( Jarak dengan outlet : ";

            if(distance >= 1){
                string_lokasi +=  String.format(Locale.getDefault(), "%.2f Km )", distance);
            }else{
                string_lokasi +=  String.format(Locale.getDefault(), "%.2f m )", distance * 1000);
            }

            txt_jarak.setText(string_lokasi);
            current_location = location;
        }

        pb_map.setVisibility(View.INVISIBLE);
    }
}
