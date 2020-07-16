package id.net.gmedia.pal.Activity.ActivityStok.Adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.leonardus.irfan.ItemValidation;

import java.util.List;

import id.net.gmedia.pal.R;
import id.net.gmedia.pal.Util.CustomItem;
import id.net.gmedia.pal.Util.FormatItem;

/**
 * Created by Shin on 1/8/2017.
 */

public class DetailPengajuanStokAdapter extends ArrayAdapter {

    private Activity context;
    private List<CustomItem> items;
    private ItemValidation iv = new ItemValidation();

    public DetailPengajuanStokAdapter(Activity context, List<CustomItem> items) {
        super(context, R.layout.adapter_detail_ajuan_stok, items);
        this.context = context;
        this.items = items;
    }

    private static class ViewHolder {
        private TextView tvNama, tvKode, tvJumlah;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        DetailPengajuanStokAdapter.ViewHolder holder = new DetailPengajuanStokAdapter.ViewHolder();

        if(convertView == null){
            LayoutInflater inflater = context.getLayoutInflater();
            convertView = inflater.inflate(R.layout.adapter_detail_ajuan_stok, null);
            holder.tvNama = convertView.findViewById(R.id.tv_nama);
            holder.tvKode = convertView.findViewById(R.id.tv_kode);
            holder.tvJumlah = convertView.findViewById(R.id.tv_jumlah);
            convertView.setTag(holder);
        }else{
            holder = (DetailPengajuanStokAdapter.ViewHolder) convertView.getTag();
        }

        final CustomItem itemSelected = items.get(position);
        holder.tvNama.setText(itemSelected.getListItem2());
        holder.tvKode.setText(itemSelected.getListItem3());
        holder.tvJumlah.setText(itemSelected.getListItem4());

        return convertView;

    }
}
