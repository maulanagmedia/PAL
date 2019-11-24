package id.net.gmedia.pal.Activity.DispensasiPiutangAct.Adapter;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.leonardus.irfan.Converter;

import java.util.List;

import id.net.gmedia.pal.Activity.DispensasiPiutangAct.RiwayatDispensasiPiutang;
import id.net.gmedia.pal.Activity.SuratJalan;
import id.net.gmedia.pal.Activity.SuratJalanDetail;
import id.net.gmedia.pal.Model.CustomModel;
import id.net.gmedia.pal.Model.NotaModel;
import id.net.gmedia.pal.R;
import id.net.gmedia.pal.Util.Constant;

public class RiwayatDispensasiPiutangAdapter extends RecyclerView.Adapter<RiwayatDispensasiPiutangAdapter.SuratJalanViewHolder> {

    private RiwayatDispensasiPiutang activity;
    private List<CustomModel> listItem;

    public RiwayatDispensasiPiutangAdapter(RiwayatDispensasiPiutang activity, List<CustomModel> listItem){
        this.activity = activity;
        this.listItem = listItem;
    }

    @NonNull
    @Override
    public SuratJalanViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new SuratJalanViewHolder(LayoutInflater.from(activity).
                inflate(R.layout.item_riwayat_dispensasi_piutang, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SuratJalanViewHolder holder, int i) {
        final CustomModel n = listItem.get(i);

        /*listData.add(new CustomModel(
                surat_jalan.getString("id")
                ,surat_jalan.getString("tanggal_pengajuan")
                ,surat_jalan.getString("nama_pelanggan")
                ,surat_jalan.getString("total_pengajuan")
                ,surat_jalan.getString("keterangan_dispensasi")
                ,surat_jalan.getString("status_approval")
        ));*/
        holder.item1.setText(n.getItem2());
        holder.item2.setText(n.getItem3());
        holder.item3.setText(n.getItem4());
        holder.item4.setText(n.getItem5());
        holder.item5.setText(n.getItem6());
    }

    @Override
    public int getItemCount() {
        return listItem.size();
    }

    class SuratJalanViewHolder extends RecyclerView.ViewHolder{

        View layout_root;
        TextView item1, item2, item3, item4, item5;

        SuratJalanViewHolder(@NonNull View itemView) {
            super(itemView);
            layout_root = itemView.findViewById(R.id.layout_root);
            item1 = itemView.findViewById(R.id.item1);
            item2 = itemView.findViewById(R.id.item2);
            item3 = itemView.findViewById(R.id.item3);
            item4 = itemView.findViewById(R.id.item4);
            item5 = itemView.findViewById(R.id.item5);
        }
    }
}
