package id.net.gmedia.pal.Activity.ActivityRetur.Adapter;

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

public class HistoryPengajuanReturAdapter extends ArrayAdapter {

    private Activity context;
    private List<CustomItem> items;
    private ItemValidation iv = new ItemValidation();

    public HistoryPengajuanReturAdapter(Activity context, List<CustomItem> items) {
        super(context, R.layout.adapter_pengajuan_stok, items);
        this.context = context;
        this.items = items;
    }

    private static class ViewHolder {
        private TextView tvNobukti, tvTanggal, tvStatus, tvKeterangan, tvTanggalUpdate, tvUpdateBy, tvAlasan;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        HistoryPengajuanReturAdapter.ViewHolder holder = new HistoryPengajuanReturAdapter.ViewHolder();

        if(convertView == null){
            LayoutInflater inflater = context.getLayoutInflater();
            convertView = inflater.inflate(R.layout.adapter_pengajuan_stok, null);
            holder.tvNobukti = convertView.findViewById(R.id.tv_nobukti);
            holder.tvTanggal = convertView.findViewById(R.id.tv_tanggal);
            holder.tvStatus = convertView.findViewById(R.id.tv_status);
            holder.tvKeterangan = convertView.findViewById(R.id.tv_keterangan);
            holder.tvTanggalUpdate = convertView.findViewById(R.id.tv_tanggal_update);
            holder.tvUpdateBy = convertView.findViewById(R.id.tv_update_by);
            holder.tvAlasan = convertView.findViewById(R.id.tv_alasan);
            convertView.setTag(holder);
        }else{
            holder = (HistoryPengajuanReturAdapter.ViewHolder) convertView.getTag();
        }

        final CustomItem itemSelected = items.get(position);
        holder.tvNobukti.setText(itemSelected.getListItem1());
        holder.tvTanggal.setText(iv.ChangeFormatDateString(itemSelected.getListItem2(), FormatItem.formatDate, FormatItem.formatDateDisplay));
        holder.tvStatus.setText(itemSelected.getListItem3());
        holder.tvKeterangan.setText(itemSelected.getListItem4());
        holder.tvTanggalUpdate.setText(iv.ChangeFormatDateString(itemSelected.getListItem5(), FormatItem.formatTimestamp, FormatItem.formatDateDisplay));
        holder.tvUpdateBy.setText(itemSelected.getListItem6());
        holder.tvAlasan.setText(itemSelected.getListItem7());

        return convertView;

    }
}
