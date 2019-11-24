package id.net.gmedia.pal.Activity.DispensasiPiutangAct;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.leonardus.irfan.ApiVolleyManager;
import com.leonardus.irfan.AppLoading;
import com.leonardus.irfan.AppRequestCallback;
import com.leonardus.irfan.Converter;
import com.leonardus.irfan.LoadMoreScrollListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import id.net.gmedia.pal.Activity.DispensasiPiutangAct.Adapter.RiwayatDispensasiPiutangAdapter;
import id.net.gmedia.pal.Model.CustomModel;
import id.net.gmedia.pal.R;
import id.net.gmedia.pal.Util.AppSharedPreferences;
import id.net.gmedia.pal.Util.Constant;

public class RiwayatDispensasiPiutang extends AppCompatActivity {

    private String search = "";
    private RiwayatDispensasiPiutangAdapter adapter;
    private List<CustomModel> listData = new ArrayList<>();
    private LoadMoreScrollListener loadManager;
    private RecyclerView rv_surat_jalan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riwayat_dispensasi_piutang);

        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle("Riwayat Dispensasi Piutang");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        rv_surat_jalan = (RecyclerView) findViewById(R.id.rv_dispensasi);
        rv_surat_jalan.setItemAnimator(new DefaultItemAnimator());
        rv_surat_jalan.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RiwayatDispensasiPiutangAdapter(this, listData);
        rv_surat_jalan.setAdapter(adapter);
        loadManager = new LoadMoreScrollListener() {
            @Override
            public void onLoadMore() {
                loadDispensasiPiutang(false);
            }
        };
        rv_surat_jalan.addOnScrollListener(loadManager);

        loadDispensasiPiutang(true);
    }

    private void loadDispensasiPiutang(final boolean init){
        if(init){
            AppLoading.getInstance().showLoading(this, R.layout.popup_loading);
            loadManager.initLoad();
        }

        String parameter = String.format(Locale.getDefault(), "?start=%d&limit=%d&search=%s",
                loadManager.getLoaded(), 10, Converter.encodeURL(search));
        ApiVolleyManager.getInstance().addRequest(this, Constant.URL_PIUTANG_DISPENSASI + parameter,
                ApiVolleyManager.METHOD_GET, Constant.getTokenHeader(AppSharedPreferences.getId(this)),
                new AppRequestCallback(new AppRequestCallback.RequestListener() {
                    @Override
                    public void onEmpty(String message) {
                        if(init){
                            listData.clear();
                            adapter.notifyDataSetChanged();
                        }

                        loadManager.finishLoad(0);
                        AppLoading.getInstance().stopLoading();
                    }

                    @Override
                    public void onSuccess(String result) {
                        try{
                            if(init){
                                listData.clear();
                            }

                            JSONArray surat_jalan_list = new JSONObject(result).getJSONArray("dispensasi_list");
                            for(int i = 0; i < surat_jalan_list.length(); i++){
                                JSONObject surat_jalan = surat_jalan_list.getJSONObject(i);
                                listData.add(new CustomModel(
                                        surat_jalan.getString("id")
                                        ,surat_jalan.getString("tanggal_pengajuan")
                                        ,surat_jalan.getString("nama_pelanggan")
                                        ,surat_jalan.getString("total_pengajuan")
                                        ,surat_jalan.getString("keterangan_dispensasi")
                                        ,surat_jalan.getString("status_approval")
                                ));
                            }

                            loadManager.finishLoad(surat_jalan_list.length());
                            adapter.notifyDataSetChanged();
                        }
                        catch (JSONException e){
                            loadManager.finishLoad(0);
                            Toast.makeText(RiwayatDispensasiPiutang.this, R.string.error_json, Toast.LENGTH_SHORT).show();
                            Log.e(Constant.TAG, e.getMessage());
                        }

                        AppLoading.getInstance().stopLoading();
                    }

                    @Override
                    public void onFail(String message) {
                        Toast.makeText(RiwayatDispensasiPiutang.this, message, Toast.LENGTH_SHORT).show();
                        loadManager.finishLoad(0);
                        AppLoading.getInstance().stopLoading();
                    }
                }));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_search, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                search = s;
                loadDispensasiPiutang(true);

                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (!searchView.isIconified() && TextUtils.isEmpty(s)) {
                    search = "";
                    loadDispensasiPiutang(true);
                }
                return true;
            }
        });

        return true;
    }
}
