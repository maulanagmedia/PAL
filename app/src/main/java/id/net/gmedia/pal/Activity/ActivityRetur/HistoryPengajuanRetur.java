package id.net.gmedia.pal.Activity.ActivityRetur;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.leonardus.irfan.ApiVolleyManager;
import com.leonardus.irfan.AppLoading;
import com.leonardus.irfan.AppRequestCallback;
import com.leonardus.irfan.Converter;
import com.leonardus.irfan.DateTimeChooser;
import com.leonardus.irfan.ItemValidation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import id.net.gmedia.pal.Activity.ActivityRetur.Adapter.HistoryPengajuanReturAdapter;
import id.net.gmedia.pal.Activity.ActivityStok.Adapter.HistoryPengajuanStokAdapter;
import id.net.gmedia.pal.Activity.ActivityStok.DetailPengajuanStokActivity;
import id.net.gmedia.pal.R;
import id.net.gmedia.pal.Util.AppSharedPreferences;
import id.net.gmedia.pal.Util.Constant;
import id.net.gmedia.pal.Util.CustomItem;
import id.net.gmedia.pal.Util.FormatItem;

public class HistoryPengajuanRetur extends AppCompatActivity {

    private Activity activity;
    private ItemValidation iv = new ItemValidation();
    private String tanggalMulai = "";
    private String tanggalSelesai = "";
    private String status = "";
    private EditText edtSearch;
    private TextView tvTanggalMulai, tvTanggalSelesai;
    private Button btnProses;
    private ListView lvHistory;
    private List<CustomItem> listItem = new ArrayList<>();
    private HistoryPengajuanReturAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_pengajuan_retur);

        //Inisialisasi Toolbar
        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle("History Pengajuan Retur");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        activity = this;

        initUI();
        initData();
    }

    private void initUI() {

        edtSearch = (EditText) findViewById(R.id.edt_search);
        tvTanggalMulai = (TextView) findViewById(R.id.txt_tgl_mulai);
        tvTanggalSelesai = (TextView) findViewById(R.id.txt_tgl_selesai);
        btnProses = (Button) findViewById(R.id.btn_proses);
        lvHistory = (ListView) findViewById(R.id.lv_history);

        //Inisialisasi filter tanggal default
        Calendar c = Calendar.getInstance();
        tanggalMulai = iv.getCurrentDate(FormatItem.formatDate);
        tanggalSelesai = iv.getCurrentDate(FormatItem.formatDate);
        tvTanggalMulai.setText(tanggalMulai);
        tvTanggalSelesai.setText(tanggalSelesai);

        //tampilkan dialog datepicker untuk tanggal mulai
        findViewById(R.id.img_tgl_mulai).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateTimeChooser.getInstance().selectDate(activity, new DateTimeChooser.DateTimeListener() {
                    @Override
                    public void onFinished(String dateString) {
                        tanggalMulai = dateString;
                        tvTanggalMulai.setText(dateString);
                    }
                });
            }
        });

        //tampilkan dialog datepicker untuk tanggal selesai
        findViewById(R.id.img_tgl_selesai).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateTimeChooser.getInstance().selectDate(activity, new DateTimeChooser.DateTimeListener(){
                    @Override
                    public void onFinished(String dateString) {
                        tanggalSelesai = dateString;
                        tvTanggalSelesai.setText(dateString);
                    }
                });
            }
        });

        //muat ulang data SO berdasarkan filter dan pencarian
        findViewById(R.id.btn_proses).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initData();
            }
        });

        adapter = new HistoryPengajuanReturAdapter(activity, listItem);
        lvHistory.setAdapter(adapter);

        lvHistory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                CustomItem item = (CustomItem) parent.getItemAtPosition(position);
                Intent intent = new Intent(activity, DetailPengajuanReturActivity.class);
                intent.putExtra("nobukti", item.getListItem1());
                startActivity(intent);
            }
        });
    }

    private void initData() {

        //Membaca data piutang dari Web Service
        AppLoading.getInstance().showLoading(this, R.layout.popup_loading);

        String parameter = String.format(Locale.getDefault(), "?keyword=%s&startdate=%s&enddate=%s",
                Converter.encodeURL(edtSearch.getText().toString()), tanggalMulai, tanggalSelesai);
        ApiVolleyManager.getInstance().addRequest(this, Constant.URL_GET_HISTORY_RETUR + parameter, ApiVolleyManager.METHOD_GET,
                Constant.getTokenHeader(AppSharedPreferences.getId(this)),
                new AppRequestCallback(new AppRequestCallback.RequestListener() {
                    @Override
                    public void onEmpty(String message) {

                        AppLoading.getInstance().stopLoading();
                    }

                    @Override
                    public void onSuccess(String result) {
                        try{
                            listItem.clear();

                            JSONArray array = new JSONArray(result);
                            for(int i = 0; i < array.length(); i++){
                                JSONObject jo = array.getJSONObject(i);
                                listItem.add(new CustomItem(
                                                jo.getString("nobukti")
                                                , jo.getString("tanggal")
                                                , jo.getString("status")
                                                , jo.getString("keterangan")
                                                , jo.getString("update_at")
                                                , jo.getString("user_update")
                                                , jo.getString("alasan_penolakan")
                                        )
                                );
                            }
                            adapter.notifyDataSetChanged();
                        }
                        catch (JSONException e){

                            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e(Constant.TAG, e.getMessage());
                            e.printStackTrace();
                        }

                        AppLoading.getInstance().stopLoading();
                    }

                    @Override
                    public void onFail(String message) {
                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                        AppLoading.getInstance().stopLoading();
                    }
                }));
    }

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