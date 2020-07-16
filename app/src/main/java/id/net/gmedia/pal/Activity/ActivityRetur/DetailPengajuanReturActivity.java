package id.net.gmedia.pal.Activity.ActivityRetur;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.leonardus.irfan.ApiVolleyManager;
import com.leonardus.irfan.AppLoading;
import com.leonardus.irfan.AppRequestCallback;
import com.leonardus.irfan.Converter;
import com.leonardus.irfan.ItemValidation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import id.net.gmedia.pal.Activity.ActivityRetur.Adapter.DetailPengajuanReturAdapter;
import id.net.gmedia.pal.Activity.ActivityStok.Adapter.DetailPengajuanStokAdapter;
import id.net.gmedia.pal.R;
import id.net.gmedia.pal.Util.AppSharedPreferences;
import id.net.gmedia.pal.Util.Constant;
import id.net.gmedia.pal.Util.CustomItem;

public class DetailPengajuanReturActivity extends AppCompatActivity {

    private String keyword = "";
    private Activity activity;
    private String nobukti = "";
    private ItemValidation iv = new ItemValidation();
    private List<CustomItem> listItem = new ArrayList<>();
    private ListView lvBarang;
    private DetailPengajuanReturAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_pengajuan_retur);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){

            nobukti = bundle.getString("nobukti", "");
        }

        //Inisialisasi Toolbar
        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle("History ");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        activity = this;

        initUI();
        initData();
    }

    private void initUI() {

        lvBarang = (ListView) findViewById(R.id.lv_barang);

        adapter = new DetailPengajuanReturAdapter(activity, listItem);
        lvBarang.setAdapter(adapter);
    }

    private void initData() {

        //Membaca data piutang dari Web Service
        AppLoading.getInstance().showLoading(this, R.layout.popup_loading);

        String parameter = String.format(Locale.getDefault(), "?nobukti=%s&keyword=%s",nobukti ,
                Converter.encodeURL(keyword));
        ApiVolleyManager.getInstance().addRequest(this, Constant.URL_GET_DETAIL_RETUR + parameter, ApiVolleyManager.METHOD_GET,
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
                                                jo.getString("kodebrg")
                                                , jo.getString("namabrg")
                                                , jo.getString("jumlah")
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_search, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                keyword = s;
                initData();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (!searchView.isIconified() && TextUtils.isEmpty(s)) {

                    keyword = "";
                    initData();
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
        }
        return super.onOptionsItemSelected(item);
    }
}