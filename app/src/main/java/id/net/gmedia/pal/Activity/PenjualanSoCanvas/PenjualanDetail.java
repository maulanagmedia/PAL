package id.net.gmedia.pal.Activity.PenjualanSoCanvas;

import android.app.Dialog;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.leonardus.irfan.ApiVolleyManager;
import com.leonardus.irfan.AppLoading;
import com.leonardus.irfan.AppRequestCallback;
import com.leonardus.irfan.Converter;
import com.leonardus.irfan.ItemValidation;
import com.leonardus.irfan.JSONBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import id.net.gmedia.pal.Model.BarangModel;
import id.net.gmedia.pal.Model.SatuanModel;
import id.net.gmedia.pal.R;
import id.net.gmedia.pal.Util.AppKeranjangPenjualan;
import id.net.gmedia.pal.Util.AppSharedPreferences;
import id.net.gmedia.pal.Util.Constant;

public class PenjualanDetail extends AppCompatActivity {

    //Variabel global flag apakah mode edit
    private int edit;
    //Variabel global data barang
    private BarangModel barang;

    //Variabel global budget diskon
    private double budget_diskon = 0;
    private ItemValidation iv = new ItemValidation();

    //Variabel Ui

    private TextInputLayout btnJumlah;
    private Spinner spn_satuan, spn_popup;
    private Button popup_save;
    private ImageView btn_edit;
    private EditText  txt_diskon, txt_jumlah_canvas;
    private EditText popup_hargaAwal, txt_popup_jumlah, txt_popup_diskon;
    private TextView txt_nama_pelanggan, txt_nama_barang, txt_harga_satuan, txt_jumlah, txt_popup_stok;
    private TextView txt_stok, txt_budget, txt_stok_canvas;
    private EditText edtHargaSatuan;
    private String currentString = "", currentStringPopup = "";
    private String total = "0";
    private String hargaBarang = "";
    private boolean isValid = true;
    private Timer timerHarga;
    private boolean isTyping = false;
    private HashMap<String, String> listHarga = new HashMap<String, String>();
    private String lastInvalidMessage = "";

    int pos_spinnerpop=0;
    private boolean firstDialog = true;
    private boolean isLoading = false;
    private Double isiPCS = Double.valueOf(0);

    //TextView txt_total;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_penjualan_detail);

        //Inisialisasi Toolbar
        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle("Detail Penjualan");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //Inisialisasi UI
        spn_satuan = (Spinner) findViewById(R.id.spn_satuan);
        spn_satuan.setEnabled(false);

        txt_jumlah = findViewById(R.id.txt_jumlah);
        txt_nama_pelanggan = findViewById(R.id.txt_nama_pelanggan);
        txt_nama_barang = findViewById(R.id.txt_nama_barang);
        txt_stok = findViewById(R.id.txt_stok);
        txt_harga_satuan = findViewById(R.id.txt_harga_satuan);
        txt_diskon = findViewById(R.id.txt_diskon);
        txt_budget = findViewById(R.id.txt_budget);
        txt_jumlah_canvas = findViewById(R.id.txt_jumlah_canvas);
        txt_stok_canvas = findViewById(R.id.txt_stok_canvas);
        edtHargaSatuan = (EditText) findViewById(R.id.edt_harga_satuan);
        btn_edit = findViewById(R.id.btn_edit);
        /*txt_total = findViewById(R.id.txt_total);
        txt_total.setText(Converter.doubleToRupiah(total));*/

        //Inisialisasi data global jenis penjualan, customer, barang, mode edit
        Gson gson = new Gson();

        barang = gson.fromJson(getIntent().getStringExtra(Constant.EXTRA_BARANG), BarangModel.class);
        edit = getIntent().getIntExtra(Constant.EXTRA_EDIT, -1);
        if(AppKeranjangPenjualan.getInstance().getJENIS_PENJUALAN() == Constant.PENJUALAN_SO){
            findViewById(R.id.layout_potong_stok_canvas).setVisibility(View.VISIBLE);
        }

        //Inisialisasi nilai UI
        txt_nama_pelanggan.setText(AppKeranjangPenjualan.getInstance().getCustomer().getNama());
        txt_nama_barang.setText(barang.getNama());
        txt_harga_satuan.setText(Converter.doubleToRupiah(barang.getHarga()));

        // Inisialisasi data
        getDetailBarang();

        btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog popup = new Dialog(PenjualanDetail.this);
                popup.setContentView(R.layout.popup_edit_canvas);
                popup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                spn_popup = popup.findViewById(R.id.spn_satuan);
                popup_save = popup.findViewById(R.id.btn_simpan_popup);
                popup_hargaAwal = popup.findViewById(R.id.edt_harga_satuan);
                txt_popup_jumlah = popup.findViewById(R.id.txt_jumlah);
                txt_popup_diskon = popup.findViewById(R.id.txt_diskon);
                txt_popup_stok = popup.findViewById(R.id.txt_stok);
                final int pos = spn_popup.getSelectedItemPosition();

                //inisial
                initPopupSatuan();
                firstDialog = true;
                String hargaSatuan = edtHargaSatuan.getText().toString().replaceAll("[,.]", "");
                popup_hargaAwal.setText(hargaSatuan);
                txt_popup_jumlah.setText(txt_jumlah.getText().toString());
                txt_popup_diskon.setText(txt_diskon.getText().toString());

                txt_popup_jumlah.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                        timerHarga = new Timer();
                        timerHarga.schedule(new TimerTask() {
                            @Override
                            public void run() {

                                isTyping = true;
                                try {
                                    Thread.sleep(400);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        getHargaTotalPopup();
                                    }
                                });
                            }
                        }, 500);
                    }
                });

                popup_save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        edtHargaSatuan.setText(popup_hargaAwal.getText());
                        txt_jumlah.setText(txt_popup_jumlah.getText());
                        txt_diskon.setText(txt_popup_diskon.getText());
                        spn_satuan.setSelection(pos_spinnerpop);
                        getHargaTotal(false);
                        popup.dismiss();
                    }
                });
                popup.show();
            }
        });

        //button tambah barang ke nota penjualan
        findViewById(R.id.btn_beli).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //cek validasi input

                if(isLoading){

                    Toast.makeText(PenjualanDetail.this, "Harap tunggu proses selesai", Toast.LENGTH_LONG).show();
                    return;
                }
                double diskon = txt_diskon.getText().toString().equals("")?0:Double.parseDouble(txt_diskon.getText().toString());
                int jumlah_canvas = txt_jumlah_canvas.getText().toString().equals("")?0:Integer.parseInt(txt_jumlah_canvas.getText().toString());

                if(txt_jumlah.getText().toString().equals("")){
                    Toast.makeText(PenjualanDetail.this, "Jumlah barang tidak boleh kosong", Toast.LENGTH_SHORT).show();
                }
                else if(spn_satuan.getSelectedItem() == null || spn_satuan.getSelectedItem().toString().equals("")){
                    Toast.makeText(PenjualanDetail.this, "Satuan barang belum dipilih", Toast.LENGTH_SHORT).show();
                }
                else if(diskon > budget_diskon){
                    Toast.makeText(PenjualanDetail.this, "Diskon tidak boleh melebihi budget diskon", Toast.LENGTH_SHORT).show();
                }
                else if(!barang.getTipe().equals("move") && diskon > 0){

                    Toast.makeText(PenjualanDetail.this, "Maaf, barang ini tidak bisa di-diskon", Toast.LENGTH_SHORT).show();
                    txt_diskon.setText("0");
                }
                else if(Integer.parseInt(txt_jumlah.getText().toString()) > barang.getListSatuan().get(spn_satuan.getSelectedItemPosition()).getJumlah()){

                    txt_jumlah.setText(String.valueOf(barang.getListSatuan().get(spn_satuan.getSelectedItemPosition()).getJumlah()));
                    Toast.makeText(PenjualanDetail.this, "Jumlah barang tidak boleh melebihi stok barang", Toast.LENGTH_SHORT).show();
                }
                else if(AppKeranjangPenjualan.getInstance().getJENIS_PENJUALAN() == Constant.PENJUALAN_SO &&
                        jumlah_canvas > barang.getListSatuanCanvas().get(spn_satuan.getSelectedItemPosition()).getJumlah()){

                    txt_jumlah_canvas.setText(String.valueOf(barang.getListSatuanCanvas().get(spn_satuan.getSelectedItemPosition()).getJumlah()));
                    Toast.makeText(PenjualanDetail.this, "Jumlah barang tidak boleh melebihi stok canvas", Toast.LENGTH_SHORT).show();
                }
                else if(AppKeranjangPenjualan.getInstance().getJENIS_PENJUALAN() == Constant.PENJUALAN_SO &&
                        diskon > 0 && Integer.parseInt(txt_jumlah.getText().toString()) + jumlah_canvas > isiPCS){

                    Toast.makeText(PenjualanDetail.this, "Jumlah seluruh barang diskon tidak boleh lebih dari "+ iv.ChangeToCurrencyFormat(isiPCS), Toast.LENGTH_SHORT).show();
                }
                else if(diskon > 0 && Integer.parseInt(txt_jumlah.getText().toString()) > isiPCS){

                    Toast.makeText(PenjualanDetail.this, "Jumlah seluruh barang diskon tidak boleh lebih dari "+ iv.ChangeToCurrencyFormat(isiPCS), Toast.LENGTH_SHORT).show();
                }
                else if(!isValid){
                    if(lastInvalidMessage.isEmpty()){
                        lastInvalidMessage = "Harap tunggu hingga jumlah harga termuat";
                    }
                    Toast.makeText(PenjualanDetail.this, lastInvalidMessage, Toast.LENGTH_SHORT).show();
                }
                else{
                    //cek harga total barang
                    cekTotal();
                }
            }
        });

        //muat data budget
        initBudget();

        //muat data satuan barang
        initSatuan();

        edtHargaSatuan.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                if (timerHarga != null) {
                    timerHarga.cancel();
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if(!s.toString().equals(currentString)){

                    String cleanString = s.toString().replaceAll("[,.]", "");
                    edtHargaSatuan.removeTextChangedListener(this);

                    String formatted = iv.ChangeToCurrencyFormat(cleanString);

                    currentString = formatted;
                    edtHargaSatuan.setText(formatted);
                    edtHargaSatuan.setSelection(formatted.length());

                    edtHargaSatuan.addTextChangedListener(this);

                    timerHarga = new Timer();
                    timerHarga.schedule(new TimerTask() {
                        @Override
                        public void run() {

                            isTyping = true;
                            try {
                                Thread.sleep(400);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    //getHargaTotal(false);
                                }
                            });
                        }
                    }, 500);
                }
            }
        });

        edtHargaSatuan.setText(iv.doubleToStringRound(barang.getHarga()));

        txt_jumlah.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                //getHargaTotal();

                timerHarga = new Timer();
                timerHarga.schedule(new TimerTask() {
                    @Override
                    public void run() {

                        isTyping = true;
                        try {
                            Thread.sleep(400);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                //getHargaTotal(false);
                            }
                        });
                    }
                }, 500);
            }
        });

        txt_jumlah_canvas.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                timerHarga = new Timer();
                timerHarga.schedule(new TimerTask() {
                    @Override
                    public void run() {

                        isTyping = true;
                        try {
                            Thread.sleep(400);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                getHargaTotal(false);
                            }
                        });
                    }
                }, 500);
            }
        });
    }

    private void cekTotal(){

        double diskon = txt_diskon.getText().toString().equals("")?0:Double.parseDouble(txt_diskon.getText().toString());
        if(iv.parseNullDouble(total) < diskon){
            Toast.makeText(PenjualanDetail.this,
                    "Diskon tidak boleh melebihi total harga", Toast.LENGTH_SHORT).show();
        }
        else{
            tambahBarang(iv.parseNullDouble(total));
        }
    }

    private void getHargaTotal(final boolean firstLoad){
        //cek harga total barang yang akan ditambahkan
        isLoading = true;
        AppLoading.getInstance().showLoading(this, R.layout.popup_loading);
        JSONBuilder body = new JSONBuilder();
        body.add("kode_pelanggan", AppKeranjangPenjualan.getInstance().getCustomer().getId());
        body.add("kode_barang", edit == -1 ? barang.getId() : AppKeranjangPenjualan.getInstance().getBarang(edit).getId());
        if(AppKeranjangPenjualan.getInstance().getJENIS_PENJUALAN() == Constant.PENJUALAN_SO){
            int jumlah_potong = txt_jumlah_canvas.getText().toString().equals("")?0: iv.parseNullInteger(txt_jumlah_canvas.getText().toString());
            body.add("jumlah", iv.parseNullInteger(txt_jumlah.getText().toString()) + jumlah_potong);
        }
        else{
            body.add("jumlah", iv.parseNullInteger(txt_jumlah.getText().toString()));
        }
        body.add("satuan", spn_satuan.getSelectedItem().toString());
        body.add("harga_edit", edtHargaSatuan.getText().toString().replaceAll("[,.]", ""));

        ApiVolleyManager.getInstance().addRequest(this, Constant.URL_VALIDASI_EDIT_HARGA, ApiVolleyManager.METHOD_POST,
                Constant.getTokenHeader(AppSharedPreferences.getId(this)), body.create(),
                new AppRequestCallback(new AppRequestCallback.RequestListener() {
                    @Override
                    public void onEmpty(String message) {

                        isLoading = false;
                        isValid = false;
                        lastInvalidMessage = message;
                        Toast.makeText(PenjualanDetail.this, message, Toast.LENGTH_SHORT).show();
                        AppLoading.getInstance().stopLoading();
                    }

                    @Override
                    public void onSuccess(String result) {

                        try{

                            isLoading = false;
                            listHarga.clear();
                            isValid = true;
                            hargaBarang = new JSONObject(result).getString("harga_barang");
                            total = new JSONObject(result).getString("total_harga");

                            JSONArray ja = new JSONObject(result).getJSONArray("harga_satuan");

                            String selectedHarga = "0";
                            for(int i = 0; i < ja.length(); i++){

                                JSONObject jo = ja.getJSONObject(i);

                                listHarga.put(
                                        jo.getString("satuan")
                                        ,jo.getString("harga")
                                );

                                if(i == 0){
                                    selectedHarga = jo.getString("harga");
                                }
                            }

                            if(firstLoad){

                                edtHargaSatuan.setText(selectedHarga);
                            }else{
                                edtHargaSatuan.setText(hargaBarang);
                            }

                        }
                        catch (JSONException e){
                            Toast.makeText(PenjualanDetail.this, R.string.error_json, Toast.LENGTH_SHORT).show();
                            Log.e(Constant.TAG, e.getMessage());
                        }
                        AppLoading.getInstance().stopLoading();
                    }

                    @Override
                    public void onFail(String message) {

                        isLoading = false;
                        isValid = false;
                        lastInvalidMessage = message;
                        Toast.makeText(PenjualanDetail.this, message, Toast.LENGTH_SHORT).show();
                        AppLoading.getInstance().stopLoading();
                    }
                }));
    }

    private void getDetailBarang(){

        AppLoading.getInstance().showLoading(this, R.layout.popup_loading);
        JSONBuilder body = new JSONBuilder();
        body.add("kode", barang.getId());

        ApiVolleyManager.getInstance().addRequest(this, Constant.URL_GET_BARANG_BY_CODE, ApiVolleyManager.METHOD_POST,
                Constant.getTokenHeader(AppSharedPreferences.getId(this)), body.create(),
                new AppRequestCallback(new AppRequestCallback.RequestListener() {
                    @Override
                    public void onEmpty(String message) {

                        Toast.makeText(PenjualanDetail.this, message, Toast.LENGTH_SHORT).show();
                        AppLoading.getInstance().stopLoading();
                    }

                    @Override
                    public void onSuccess(String result) {

                        try {
                            JSONObject response = new JSONObject(result);
                            isiPCS = iv.parseNullDouble(response.getString("isi_kecil"));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        AppLoading.getInstance().stopLoading();
                    }

                    @Override
                    public void onFail(String message) {

                        Toast.makeText(PenjualanDetail.this, message, Toast.LENGTH_SHORT).show();
                        AppLoading.getInstance().stopLoading();
                    }
                }));
    }

    private void getHargaTotalPopup(){
        //cek harga total barang yang akan ditambahkan
        AppLoading.getInstance().showLoading(this, R.layout.popup_loading);
        JSONBuilder body = new JSONBuilder();
        body.add("kode_pelanggan", AppKeranjangPenjualan.getInstance().getCustomer().getId());
        body.add("kode_barang", edit == -1 ? barang.getId() : AppKeranjangPenjualan.getInstance().getBarang(edit).getId());
        if(AppKeranjangPenjualan.getInstance().getJENIS_PENJUALAN() == Constant.PENJUALAN_SO){
            int jumlah_potong = txt_jumlah_canvas.getText().toString().equals("")?0: iv.parseNullInteger(txt_jumlah_canvas.getText().toString());
            body.add("jumlah", iv.parseNullInteger(txt_popup_jumlah.getText().toString()) + jumlah_potong);
        }
        else{
            body.add("jumlah", iv.parseNullInteger(txt_popup_jumlah.getText().toString()));
        }
        body.add("satuan", spn_popup.getSelectedItem().toString());
        body.add("harga_edit", listHarga.get(spn_popup.getSelectedItem().toString()));

        ApiVolleyManager.getInstance().addRequest(this, Constant.URL_VALIDASI_EDIT_HARGA, ApiVolleyManager.METHOD_POST,
                Constant.getTokenHeader(AppSharedPreferences.getId(this)), body.create(),
                new AppRequestCallback(new AppRequestCallback.RequestListener() {
                    @Override
                    public void onEmpty(String message) {

                        isValid = false;
                        lastInvalidMessage = message;
                        Toast.makeText(PenjualanDetail.this, message, Toast.LENGTH_SHORT).show();
                        AppLoading.getInstance().stopLoading();
                    }

                    @Override
                    public void onSuccess(String result) {

                        try{

                            isValid = true;
                            String hargaBarangPopup = new JSONObject(result).getString("harga_barang");
                            /*total = new JSONObject(result).getString("total_harga");*/

                            JSONArray ja = new JSONObject(result).getJSONArray("harga_satuan");

                            for(int i = 0; i < ja.length(); i++){

                                JSONObject jo = ja.getJSONObject(i);

                                if (jo.getString("satuan").equals(spn_popup.getSelectedItem().toString())){

                                    popup_hargaAwal.setText(jo.getString("harga"));
                                }
                            }
                        }
                        catch (JSONException e){
                            Toast.makeText(PenjualanDetail.this, R.string.error_json, Toast.LENGTH_SHORT).show();
                            Log.e(Constant.TAG, e.getMessage());
                        }

                        AppLoading.getInstance().stopLoading();
                    }

                    @Override
                    public void onFail(String message) {

                        isValid = false;
                        lastInvalidMessage = message;
                        Toast.makeText(PenjualanDetail.this, message, Toast.LENGTH_SHORT).show();
                        AppLoading.getInstance().stopLoading();
                    }
                }));
    }

    private void validasiHarga(){
        //cek harga total barang yang akan ditambahkan
        AppLoading.getInstance().showLoading(this, R.layout.popup_loading);
        JSONBuilder body = new JSONBuilder();
        body.add("kode_pelanggan", AppKeranjangPenjualan.getInstance().getCustomer().getId());
        body.add("kode_barang", edit == -1 ? barang.getId() : AppKeranjangPenjualan.getInstance().getBarang(edit).getId());
        if(AppKeranjangPenjualan.getInstance().getJENIS_PENJUALAN() == Constant.PENJUALAN_SO){
            int jumlah_potong = txt_jumlah_canvas.getText().toString().equals("")?0: iv.parseNullInteger(txt_jumlah_canvas.getText().toString());
            body.add("jumlah", iv.parseNullInteger(txt_jumlah.getText().toString()) + jumlah_potong);
        }
        else{
            body.add("jumlah", iv.parseNullInteger(txt_jumlah.getText().toString()));
        }
        body.add("satuan", spn_satuan.getSelectedItem().toString());
        body.add("harga_edit", edtHargaSatuan.getText().toString().replaceAll("[,.]", ""));

        ApiVolleyManager.getInstance().addRequest(this, Constant.URL_VALIDASI_EDIT_HARGA, ApiVolleyManager.METHOD_POST,
                Constant.getTokenHeader(AppSharedPreferences.getId(this)), body.create(),
                new AppRequestCallback(new AppRequestCallback.RequestListener() {
                    @Override
                    public void onEmpty(String message) {

                        isValid = false;
                        lastInvalidMessage = message;
                        Toast.makeText(PenjualanDetail.this, message, Toast.LENGTH_SHORT).show();
                        AppLoading.getInstance().stopLoading();
                    }

                    @Override
                    public void onSuccess(String result) {

                        try{

                            listHarga.clear();
                            isValid = true;
                            hargaBarang = new JSONObject(result).getString("harga_barang");
                            total = new JSONObject(result).getString("total_harga");

                            JSONArray ja = new JSONObject(result).getJSONArray("harga_satuan");

                            for(int i = 0; i < ja.length(); i++){

                                JSONObject jo = ja.getJSONObject(i);
                                listHarga.put(
                                        jo.getString("satuan")
                                        ,jo.getString("harga")
                                );
                            }

                        }
                        catch (JSONException e){
                            Toast.makeText(PenjualanDetail.this, R.string.error_json, Toast.LENGTH_SHORT).show();
                            Log.e(Constant.TAG, e.getMessage());
                        }
                        AppLoading.getInstance().stopLoading();
                        firstDialog = false;
                    }

                    @Override
                    public void onFail(String message) {

                        isValid = false;
                        lastInvalidMessage = message;
                        Toast.makeText(PenjualanDetail.this, message, Toast.LENGTH_SHORT).show();
                        AppLoading.getInstance().stopLoading();
                    }
                }));
    }

    private void initBudget(){
        //Membaca data budget diskon dari Web Service
        ApiVolleyManager.getInstance().addRequest(this, Constant.URL_BUDGET_DISKON,
                ApiVolleyManager.METHOD_GET, Constant.getTokenHeader(AppSharedPreferences.getId(this)),
                new AppRequestCallback(new AppRequestCallback.RequestListener() {
                    @Override
                    public void onEmpty(String message) {
                        String budget = "Budget diskon : " + Converter.doubleToRupiah(budget_diskon);
                        txt_budget.setText(budget);
                    }

                    @Override
                    public void onSuccess(String result) {
                        //System.out.println(result);
                        try{
                            budget_diskon = new JSONObject(result).getJSONObject("budget_diskon").getDouble("sisa");
                            if(edit == -1){
                                String budget = "Budget diskon : " + Converter.doubleToRupiah
                                        (budget_diskon - AppKeranjangPenjualan.getInstance().getBudget_terpakai());
                                txt_budget.setText(budget);
                            }
                            else {
                                String budget = "Budget diskon : " + Converter.doubleToRupiah
                                        (budget_diskon - AppKeranjangPenjualan.getInstance().getBudget_terpakai()
                                                + AppKeranjangPenjualan.getInstance().getBarang(edit).getDiskon());
                                txt_budget.setText(budget);
                            }
                        }
                        catch (JSONException e){
                            Toast.makeText(PenjualanDetail.this, R.string.error_json, Toast.LENGTH_SHORT).show();
                            Log.e(Constant.TAG, e.getMessage());
                        }
                    }

                    @Override
                    public void onFail(String message) {
                        Toast.makeText(PenjualanDetail.this, message, Toast.LENGTH_SHORT).show();
                    }
                }));
    }

    private void initSatuan(){
        //Inisialisasi satuan barang
        List<String> spinnerItem = new ArrayList<>();
        for(SatuanModel s : barang.getListSatuan()){
            spinnerItem.add(s.getSatuan());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                PenjualanDetail.this, android.R.layout.simple_spinner_item, spinnerItem);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spn_satuan.setAdapter(adapter);

        spn_satuan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String stok = barang.getListSatuan().get(position).getJumlah() + " " + spn_satuan.getItemAtPosition(position).toString();
                txt_stok.setText(stok);

                if(AppKeranjangPenjualan.getInstance().getJENIS_PENJUALAN() == Constant.PENJUALAN_SO){
                    String stok_canvas = barang.getListSatuanCanvas().get(position).getJumlah() + " " + spn_satuan.getItemAtPosition(position).toString();
                    txt_stok_canvas.setText(stok_canvas);
                }

                String selectedHarga = listHarga.get(spn_satuan.getItemAtPosition(position).toString());

                if(selectedHarga != null){

                    barang.setHarga(iv.parseNullDouble(selectedHarga));
                    txt_harga_satuan.setText(Converter.doubleToRupiah(barang.getHarga()));
                    edtHargaSatuan.setText(selectedHarga);
                }else{

                    //getHargaTotal(false);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //set ketika barang sudah ada isinya/edit
        if (spinnerItem.size()>0 && barang.getSatuan() != null && !barang.getSatuan().equals("")){

            int position = 0;

            for ( String s : spinnerItem){
                if ( s.equals(barang.getSatuan())){
                    break;
                }
                position++;
            }
            spn_satuan.setSelection(position);

            txt_jumlah.setText(String.valueOf(barang.getJumlah()));
            //ini untuk ngecek potongan ada op orak
            if (barang.getJumlah_potong()!=0){
                txt_jumlah_canvas.setText(String.valueOf(barang.getJumlah_potong()));
            }
            if (barang.getDiskon()!=0){
                txt_diskon.setText(iv.doubleToStringRound(barang.getDiskon()));
            }

            validasiHarga();
        }else{

            //txt_harga_satuan.setText(Converter.doubleToRupiah(barang.getHarga()));
            edtHargaSatuan.setText(iv.doubleToStringRound(barang.getHarga()));
            getHargaTotal(true);
        }

        //getHargaTotal(true);

    }

    private void initPopupSatuan(){

        //Inisialisasi satuan barang
        List<String> spinnerItem = new ArrayList<>();
        for(SatuanModel s : barang.getListSatuan()){
            spinnerItem.add(s.getSatuan());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                PenjualanDetail.this, android.R.layout.simple_spinner_item, spinnerItem);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spn_popup.setAdapter(adapter);

        spn_popup.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String stok = barang.getListSatuan().get(position).getJumlah() + " " + spn_popup.getItemAtPosition(position).toString();
                txt_popup_stok.setText(stok);
                pos_spinnerpop = position;

                if(AppKeranjangPenjualan.getInstance().getJENIS_PENJUALAN() == Constant.PENJUALAN_SO){
                    String stok_canvas = barang.getListSatuanCanvas().get(position).getJumlah() + " " + spn_popup.getItemAtPosition(position).toString();
                    txt_popup_stok.setText(stok_canvas);
                }

                if(!firstDialog){
                    getHargaTotalPopup();
                }
                firstDialog = false;

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spn_popup.setSelection(spn_satuan.getSelectedItemPosition());
        pos_spinnerpop = spn_satuan.getSelectedItemPosition();

        //set ketika barang sudah ada isinya/edit
        if (spinnerItem.size()>0 && barang.getSatuan() != null && !barang.getSatuan().equals("")){

            validasiHarga();
        }

    }

    private void tambahBarang(double subtotal){
        //tambah barang ke nota penjualan
        if(edit == -1){
            //jika bukan edit, tambah ke keranjang penjualan
            if(AppKeranjangPenjualan.getInstance().getJENIS_PENJUALAN() == Constant.PENJUALAN_SO){
                int jumlah_potong = txt_jumlah_canvas.getText().toString().equals("")?0:
                        Integer.parseInt(txt_jumlah_canvas.getText().toString());
                barang.setJumlah_potong(jumlah_potong);
                barang.setJumlah(Integer.parseInt(txt_jumlah.getText().toString()));
            }
            else if(AppKeranjangPenjualan.getInstance().getJENIS_PENJUALAN() == Constant.PENJUALAN_CANVAS){
                barang.setJumlah(Integer.parseInt(txt_jumlah.getText().toString()));
            }

            barang.setDiskon(txt_diskon.getText().toString().equals("")?0:Double.parseDouble(txt_diskon.getText().toString()));
            barang.setSatuan(spn_satuan.getSelectedItem().toString());
            barang.setSubtotal(subtotal);
            barang.setHarga(iv.parseNullDouble(hargaBarang));
            barang.setHargaEdit(hargaBarang);
            barang.setTotal(total);
            AppKeranjangPenjualan.getInstance().pakai_budget(txt_diskon.getText().toString().equals("")?0:Double.parseDouble(txt_diskon.getText().toString()));

            AppKeranjangPenjualan.getInstance().addBarang(barang);
        }
        else{
            if(AppKeranjangPenjualan.getInstance().getJENIS_PENJUALAN() == Constant.PENJUALAN_SO){
                int jumlah_potong = txt_jumlah_canvas.getText().toString().equals("")?0:
                        Integer.parseInt(txt_jumlah_canvas.getText().toString());
                AppKeranjangPenjualan.getInstance().getBarang(edit).setJumlah_potong(jumlah_potong);
                AppKeranjangPenjualan.getInstance().getBarang(edit).setJumlah(Integer.parseInt(txt_jumlah.getText().toString()));
            }
            else if(AppKeranjangPenjualan.getInstance().getJENIS_PENJUALAN() == Constant.PENJUALAN_CANVAS){
                AppKeranjangPenjualan.getInstance().getBarang(edit).setJumlah(Integer.parseInt(txt_jumlah.getText().toString()));
            }

            AppKeranjangPenjualan.getInstance().getBarang(edit).setHargaEdit(hargaBarang);
            AppKeranjangPenjualan.getInstance().getBarang(edit).setHarga(iv.parseNullDouble(hargaBarang));
            AppKeranjangPenjualan.getInstance().getBarang(edit).setTotal(total);
            AppKeranjangPenjualan.getInstance().getBarang(edit).setDiskon(txt_diskon.getText().toString().equals("")?0:Double.parseDouble(txt_diskon.getText().toString()));
            AppKeranjangPenjualan.getInstance().getBarang(edit).setSatuan(spn_satuan.getSelectedItem().toString());
            AppKeranjangPenjualan.getInstance().getBarang(edit).setSubtotal(subtotal);
            AppKeranjangPenjualan.getInstance().edit_pakai_budget(AppKeranjangPenjualan.getInstance().getBarang(edit).getDiskon(), txt_diskon.getText().toString().equals("")?0:Double.parseDouble(txt_diskon.getText().toString()));
        }

        Intent i = new Intent(PenjualanDetail.this, PenjualanNota.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
