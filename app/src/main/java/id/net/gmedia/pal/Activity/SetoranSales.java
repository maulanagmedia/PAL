package id.net.gmedia.pal.Activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.fxn.pix.Pix;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.leonardus.irfan.ApiVolleyManager;
import com.leonardus.irfan.AppLoading;
import com.leonardus.irfan.AppRequestCallback;
import com.leonardus.irfan.Converter;
import com.leonardus.irfan.DateTimeChooser;
import com.leonardus.irfan.DialogFactory;
import com.leonardus.irfan.JSONBuilder;
import com.otaliastudios.zoom.ZoomLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import id.net.gmedia.pal.Model.CaraBayarModel;
import id.net.gmedia.pal.Model.SetoranModel;
import id.net.gmedia.pal.Model.UploadModel;
import id.net.gmedia.pal.R;
import id.net.gmedia.pal.Adapter.SetoranSalesAdapter;
import id.net.gmedia.pal.Util.AppSharedPreferences;
import id.net.gmedia.pal.Util.Constant;

public class SetoranSales extends AppCompatActivity {

    //variabel global id upload bukti
    private final int UPLOAD_BUKTI = 999;

    //Variabel global dialog upload
    private Dialog dialog_upload;
    //Variabel global upload
    private UploadModel upload;
    private Button btn_next, btn_previous;

    private List<String> listFotoBukti = new ArrayList<>();
    private List<String> listImage = new ArrayList<>();

    //Variabel UI galeri (animasi, foto tampil)
    private Animation anim_popin, anim_popout;
    private int selectedImage = 0;
    private ImageView img_galeri_selected;
    private int imgHeight = 0;
    private int imgWidth = 0;
    private ZoomLayout layout_zoom;
    private ConstraintLayout layout_overlay;

    //variabel global total setoran sales
    private double total = 0;
    private String pembayaran = "";
    private String date_start = "";
    private String date_end = "";
    private String search = "";
    //variabel global flag apakah filter muncul atau tidak
    private boolean panel_filter = false;

    //flag apakah galeri sedang menampilkan foto detail secara popup atau tidak
    private boolean detail = false;

    //Variabel UI
    private TextView txt_total;
    private TextView txt_tgl_mulai, txt_tgl_selesai;
    private SlidingUpPanelLayout slidingPanel;
    private AppCompatSpinner spn_pembayaran;
    private EditText txt_nominal, txt_keterangan;
    private ProgressBar bar_bukti;
    private ImageView overlay_bukti, img_bukti;
    private Spinner spn_bank;
    private Activity activity;

    //Variabel data setoran
    private List<SetoranModel> listSetoran = new ArrayList<>();
    private SetoranSalesAdapter adapter;
    private List<CaraBayarModel> listBank = new ArrayList<>();
    private ArrayAdapter adapterBank;
    private String selectedBank = "";
    private LinearLayout layout_galeri_selected;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setoran_sales);

        activity = this;

        //Inisialisasi Toolbar
        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle("Daftar Setoran");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //Inisialisasi UI
        txt_total = findViewById(R.id.txt_total);
        slidingPanel = findViewById(R.id.layout_parent);
        txt_tgl_mulai = findViewById(R.id.txt_tgl_mulai);
        txt_tgl_selesai = findViewById(R.id.txt_tgl_selesai);
        spn_pembayaran = findViewById(R.id.spn_pembayaran);
        txt_tgl_mulai.setText(Converter.DToString(new Date()));
        txt_tgl_selesai.setText(Converter.DToString(new Date()));



        //button menampilkan datepicker untuk set filter tanggal mulai
        findViewById(R.id.img_tgl_mulai).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateTimeChooser.getInstance().selectDate(SetoranSales.this, new DateTimeChooser.DateTimeListener() {
                    @Override
                    public void onFinished(String dateString) {
                        txt_tgl_mulai.setText(dateString);
                    }
                });
            }
        });

        //button menampilkan datepicker untuk set filter tanggal selesai
        findViewById(R.id.img_tgl_selesai).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateTimeChooser.getInstance().selectDate(SetoranSales.this, new DateTimeChooser.DateTimeListener() {
                    @Override
                    public void onFinished(String dateString) {
                        txt_tgl_selesai.setText(dateString);
                    }
                });
            }
        });




        //button memuat data berdasarkan filter yang sudah diisi
        findViewById(R.id.btn_proses).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                date_start = txt_tgl_mulai.getText().toString();
                date_end = txt_tgl_selesai.getText().toString();
                pembayaran = spn_pembayaran.getSelectedItemPosition() == 0 ? "" : spn_pembayaran.getSelectedItem().toString();

                //tutup panel filter
                slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                panel_filter = false;

                //muat ulang data setoran
                loadSetoran();
            }
        });

        //button untuk memulai upload data transfer
        findViewById(R.id.btn_transfer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Inisialisasi dialog upload
                listFotoBukti.clear();
                dialog_upload = DialogFactory.getInstance().createDialog(SetoranSales.this, R.layout.popup_setoran_sales,
                        93, 90);

                txt_nominal = dialog_upload.findViewById(R.id.txt_nominal);
                txt_keterangan = dialog_upload.findViewById(R.id.txt_keterangan);
                overlay_bukti = dialog_upload.findViewById(R.id.overlay_bukti);
                layout_galeri_selected = dialog_upload.findViewById(R.id.layout_galeri_selected);
                img_galeri_selected = dialog_upload.findViewById(R.id.img_galeri_selected);
                layout_zoom = dialog_upload.findViewById(R.id.layout_zoom);
                img_bukti =dialog_upload.findViewById(R.id.img_bukti);
                bar_bukti = dialog_upload.findViewById(R.id.bar_bukti);
                spn_bank = (Spinner) dialog_upload.findViewById(R.id.spn_bank);
                layout_overlay = dialog_upload.findViewById(R.id.layout_overlay);
                btn_next = dialog_upload.findViewById(R.id.btn_next);
                btn_previous = dialog_upload.findViewById(R.id.btn_previous);
                spn_bank.setAdapter(adapterBank);

                spn_bank.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        CaraBayarModel item = (CaraBayarModel) parent.getItemAtPosition(position);
                        selectedBank = item.getId();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

               /* dialog_upload.findViewById(R.id.overlay_bukti).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog_upload = new Dialog(activity);
                        dialog_upload.setContentView(R.layout.popup_galery);
                        dialog_upload.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                    }
                });*/
                txt_nominal.setText(String.format(Locale.getDefault(), "%.0f", total));

                //button upload gambar bukti
                dialog_upload.findViewById(R.id.img_upload_bukti).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Pix.start(SetoranSales.this, UPLOAD_BUKTI, 5 - listFotoBukti.size());
                    }
                });


                img_bukti.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                     initView(listFotoBukti);

                    }
                });


                dialog_upload.findViewById(R.id.btn_batal).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog_upload.cancel();
                    }
                });

                //button kirim data bukti
                dialog_upload.findViewById(R.id.btn_kirim).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Validasi input
                        if(upload == null){
                            Toast.makeText(SetoranSales.this, "Upload bukti transfer terlebih dahulu", Toast.LENGTH_SHORT).show();
                        }
                        else if(!upload.isUploaded()){
                            Toast.makeText(SetoranSales.this, "Foto bukti transfer belum selesai ter-upload", Toast.LENGTH_SHORT).show();
                        }
                        else if(txt_nominal.getText().toString().equals("")){
                            Toast.makeText(SetoranSales.this, "Nominal transfer tidak boleh kosong", Toast.LENGTH_SHORT).show();
                        }
                        else if(Double.parseDouble(txt_nominal.getText().toString()) <= 0){
                            Toast.makeText(SetoranSales.this, "Nominal transfer tidak boleh kosong", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            //kirim bukti setoran
                            uploadBukti();
                        }
                    }
                });
                dialog_upload.show();
                initGaleri();

            }
        });

        //Inisialisasi RecyclerView & Adapter
        RecyclerView rv_setoran = findViewById(R.id.rv_setoran);
        rv_setoran.setLayoutManager(new LinearLayoutManager(this));
        rv_setoran.setItemAnimator(new DefaultItemAnimator());
        adapter = new SetoranSalesAdapter(listSetoran, activity);
        rv_setoran.setAdapter(adapter);

        adapterBank = new ArrayAdapter(SetoranSales.this,
                R.layout.simple_list, R.id.text1, listBank);


        //muat data setoran
        loadSetoran();

    }

    private void initView(List<String> images){
        //Fungsi untuk menampilkan foto secara popup
        if(images.size() > 0){
            if(images.size() == 1){
                btn_next.setVisibility(View.INVISIBLE);
                btn_previous.setVisibility(View.INVISIBLE);
            }
            else{
                btn_next.setVisibility(View.VISIBLE);
                btn_previous.setVisibility(View.VISIBLE);
            }

            listImage = images;
            Glide.with(this).load(listImage.get(0)).apply(new RequestOptions().
                    override(imgWidth, imgHeight)).into(img_galeri_selected);
            layout_zoom.zoomTo(1, false);
            layout_overlay.setVisibility(View.VISIBLE);
           // detail = true;

            layout_galeri_selected.startAnimation(anim_popin);
            //img_galeri_selected.startAnimation(anim_popin);
        }

    }

    private void initGaleri(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        imgWidth = displayMetrics.widthPixels - displayMetrics.widthPixels/7;
        imgHeight = displayMetrics.heightPixels - displayMetrics.heightPixels/5;

        anim_popin = AnimationUtils.loadAnimation(this, R.anim.anim_pop_in);
        anim_popout = AnimationUtils.loadAnimation(this, R.anim.anim_pop_out);

        anim_popout.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                detail=false;
                layout_overlay.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        layout_overlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout_galeri_selected.startAnimation(anim_popout);

            }
        });

        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedImage < listImage.size() - 1){
                    selectedImage++;
                }
                else{
                    selectedImage = 0;
                }

                Glide.with(SetoranSales.this).load(listImage.get(selectedImage)).
                        apply(new RequestOptions().override(imgWidth, imgHeight)).into(img_galeri_selected);
            }
        });

        btn_previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedImage > 0){
                    selectedImage--;

                }
                else{
                    selectedImage = listImage.size() - 1;
                }

                Glide.with(SetoranSales.this).load(listImage.get(selectedImage)).
                        apply(new RequestOptions().override(imgWidth, imgHeight)).into(img_galeri_selected);
            }
        });


    }

    private void getBank() {

        AppLoading.getInstance().showLoading(this, R.layout.popup_loading);
        ApiVolleyManager.getInstance().addRequest(this, Constant.URL_BANK,
                ApiVolleyManager.METHOD_GET, Constant.getTokenHeader(AppSharedPreferences.getId(this)),
                new AppRequestCallback(new AppRequestCallback.RequestListener() {
                    @Override
                    public void onEmpty(String message) {

                        AppLoading.getInstance().stopLoading();
                    }

                    @Override
                    public void onSuccess(String result) {
                        try{
                            //Inisialisasi Header

                            JSONArray array = new JSONArray(result);
                            for(int i = 0; i < array.length(); i++){
                                JSONObject item = array.getJSONObject(i);
                                listBank.add(new CaraBayarModel(
                                        item.getString("id")
                                        ,item.getString("text")
                                ));

                                if(i == 0){

                                    selectedBank = item.getString("id");
                                }
                            }

                            adapterBank.notifyDataSetChanged();
                        }
                        catch (JSONException e){

                            Toast.makeText(SetoranSales.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e(Constant.TAG, e.getMessage());
                            e.printStackTrace();
                        }

                        AppLoading.getInstance().stopLoading();
                    }

                    @Override
                    public void onFail(String message) {
                        Toast.makeText(SetoranSales.this, message, Toast.LENGTH_SHORT).show();
                        AppLoading.getInstance().stopLoading();
                    }
                }));
    }

    private void loadSetoran(){
        pembayaran = spn_pembayaran.getSelectedItemPosition() == 0 ? "" : spn_pembayaran.getSelectedItem().toString();
        //Membaca data setoran dari Web Service
        AppLoading.getInstance().showLoading(this, R.layout.popup_loading);
        String parameter = String.format(Locale.getDefault(), "?date_start=%s&date_end=%s&search=%s&cara=%s", date_start, date_end, Converter.encodeURL(search), pembayaran);
        ApiVolleyManager.getInstance().addRequest(this, Constant.URL_SETORAN_SALES + parameter,
                ApiVolleyManager.METHOD_GET, Constant.getTokenHeader(AppSharedPreferences.getId(this)),
                new AppRequestCallback(new AppRequestCallback.RequestListener() {
                    @Override
                    public void onEmpty(String message) {
                        listSetoran.clear();
                        adapter.notifyDataSetChanged();

                        /*for (SetoranModel setor: listSetoran){
                            setor.isSelected();
                            if (setor.isSelected())total= setor.getJumlah()+total;
                        }
                        String str_total = "Total : " + Converter.doubleToRupiah(total);
                        txt_total.setText(str_total);*/

                        total = 0;
                        String str_total = "Total : " + Converter.doubleToRupiah(total);
                        txt_total.setText(str_total);

                        AppLoading.getInstance().stopLoading();

                        getBank();
                    }

                    @Override
                    public void onSuccess(String result) {
                        listSetoran.clear();

                        try{
                            total = 0;
                            JSONArray list_bayar = new JSONObject(result).getJSONArray("bayar_list");
                            for(int i = 0; i < list_bayar.length(); i++){
                                JSONObject obj = list_bayar.getJSONObject(i);
                                SetoranModel setoran = new SetoranModel(obj.getString("tanggal"), obj.getString("nama_pelanggan"),
                                        obj.getString("nomor_nota"), obj.getDouble("bayar"), obj.getString("cara_bayar"));
                               // total += setoran.getJumlah();
                                listSetoran.add(setoran);
                            }

                            String str_total = "Total : " + Converter.doubleToRupiah(0);
                            txt_total.setText(str_total);
                        }
                        catch (JSONException e){
                            Toast.makeText(SetoranSales.this, R.string.error_json, Toast.LENGTH_SHORT).show();
                            Log.e(Constant.TAG, e.getMessage());
                        }

                        adapter.notifyDataSetChanged();
                        AppLoading.getInstance().stopLoading();

                        getBank();
                    }

                    @Override
                    public void onFail(String message) {
                        Toast.makeText(SetoranSales.this, message, Toast.LENGTH_SHORT).show();
                        AppLoading.getInstance().stopLoading();
                    }
                }));
    }

    public void updateJumlah(){
        total = 0;
        for (SetoranModel setor: listSetoran){
            setor.isSelected();
            if (setor.isSelected())total= setor.getJumlah()+total;
        }
        String str_total = "Total : " + Converter.doubleToRupiah(total);
        txt_total.setText(str_total);

        /*total -=  update;
        if(total < 0){
            String jum = "Total : " + Converter.doubleToRupiah(0);
            txt_total.setText(jum);
        }

        else{
            String jum = "Total : " + Converter.doubleToRupiah(total);
            txt_total.setText(jum);
        }*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == UPLOAD_BUKTI) {
            if(data != null){
                //Inisialisasi data upload
                try{
                    ArrayList<String> listPath = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);

                    for (int i = 0; i < listPath.size(); i++){

                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),
                                Uri.fromFile(new File(data.getStringArrayListExtra(Pix.IMAGE_RESULTS).get(0))));
                        bitmap = Converter.resizeBitmap(bitmap, 750);

                        img_bukti.setImageBitmap(bitmap);
//                        overlay_bukti.setVisibility(View.VISIBLE);
                        //bar_bukti.setVisibility(View.VISIBLE);

                        upload = new UploadModel(bitmap, convert(bitmap));
                        upload.setUploaded(true);
                        upload.setUrl(Uri.fromFile(new File(data.getStringArrayListExtra(Pix.IMAGE_RESULTS).get(0))).toString());
                        listFotoBukti.add(upload.getUrl());
                    }

                   // uploadFotoBukti(upload);
                }
                catch (IOException e){
                    Log.e(Constant.TAG, e.getMessage());
                }
            }
        }
        else{
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void uploadFotoBukti(final UploadModel upload){
        //Upload gambar bukti transfer ke Web Service
        ApiVolleyManager.getInstance().addMultipartRequest(this, Constant.URL_UPLOAD_FOTO_BUKTI,
                Constant.getTokenHeader(AppSharedPreferences.getId(this)),
                "pic", Converter.getFileDataFromDrawable(upload.getBitmap()),
                new AppRequestCallback(new AppRequestCallback.RequestListener() {
                    @Override
                    public void onEmpty(String message) {
                        Toast.makeText(SetoranSales.this, message, Toast.LENGTH_SHORT).show();
                        bar_bukti.setVisibility(View.GONE);
                    }

                    @Override
                    public void onSuccess(String result) {
                        try{
                            String id = new JSONObject(result).getString("id");

                            upload.setUploaded(true);
                            upload.setId(id);

                            overlay_bukti.setVisibility(View.GONE);
                            bar_bukti.setVisibility(View.GONE);
                        }
                        catch (JSONException e){
                            Toast.makeText(SetoranSales.this, R.string.error_json, Toast.LENGTH_SHORT).show();
                            Log.e(Constant.TAG, e.getMessage());
                        }
                    }

                    @Override
                    public void onFail(String message) {
                        Toast.makeText(SetoranSales.this, message, Toast.LENGTH_SHORT).show();
                        bar_bukti.setVisibility(View.GONE);
                    }
                }));

    }

    private String convert(Bitmap bitmap)
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
    }


    private void uploadBukti(){
        //Kirim data bukti setoran ke Web Service
        AppLoading.getInstance().showLoading(this, R.layout.popup_loading);
        JSONBuilder body = new JSONBuilder();
        JSONArray bodyNota = new JSONArray();
        JSONArray bodyGambar = new JSONArray();
        body.add("kode_akun",selectedBank);
        body.add("total_setoran", Double.parseDouble(txt_nominal.getText().toString()));
        try {
            for (SetoranModel setor: listSetoran){
                JSONObject jsNota = new JSONObject();
                if (setor.isSelected()){
                    jsNota.put("nobukti", setor.getNota());
                    bodyNota.put(jsNota);
                }
            }
            JSONObject jsGambar = new JSONObject();
            jsGambar.put("gambar", upload.getEncoded());
            bodyGambar.put(jsGambar);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        body.add("gambar_bukti", bodyGambar);
        body.add("nomor_pelunasan", bodyNota);
        body.add("keterangan", txt_keterangan.getText().toString());
        Log.d("setoran", "uploadBukti: "+body.create());

        //body.add("id_bank", selectedBank);

        ApiVolleyManager.getInstance().addRequest(this, Constant.URL_UPLOAD_BUKTI_BARU, ApiVolleyManager.METHOD_POST,
                Constant.getTokenHeader(AppSharedPreferences.getId(this)), body.create(),
                new AppRequestCallback(new AppRequestCallback.RequestListener() {
                    @Override
                    public void onEmpty(String message) {
                        Toast.makeText(SetoranSales.this, message, Toast.LENGTH_SHORT).show();
                        AppLoading.getInstance().stopLoading();
                    }

                    @Override
                    public void onSuccess(String result) {
                        Toast.makeText(SetoranSales.this, result, Toast.LENGTH_SHORT).show();
                        AppLoading.getInstance().stopLoading();
                        dialog_upload.dismiss();
                        loadSetoran();
                    }

                    @Override
                    public void onFail(String message) {
                        Toast.makeText(SetoranSales.this, message, Toast.LENGTH_SHORT).show();
                        AppLoading.getInstance().stopLoading();
                    }
                }));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_setoran_sales, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                search = s;
                loadSetoran();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (!searchView.isIconified() && TextUtils.isEmpty(s)) {
                    search = "";
                    loadSetoran();
                }

                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_filter:
                //munculkan panel filter
                slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                panel_filter = true;
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(panel_filter){
            slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            panel_filter = false;
        }
        else{
            super.onBackPressed();
        }
    }
}
