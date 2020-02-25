package id.net.gmedia.pal.Adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.leonardus.irfan.Converter;

import java.util.List;

import id.net.gmedia.pal.Activity.Piutang.PiutangDetail;
import id.net.gmedia.pal.Activity.SetoranSales;
import id.net.gmedia.pal.Model.SetoranModel;
import id.net.gmedia.pal.R;

public class SetoranSalesAdapter extends RecyclerView.Adapter<SetoranSalesAdapter.SetoranSalesViewHolder> {

    private List<SetoranModel> listSetoran;
    private Activity activity;

    public SetoranSalesAdapter(List<SetoranModel> listSetoran, Activity activity){
        this.activity = activity;
        this.listSetoran = listSetoran;
    }

    @NonNull
    @Override
    public SetoranSalesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new SetoranSalesViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_setoran_sales, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SetoranSalesViewHolder setoranSalesViewHolder, final int i) {
        final SetoranModel setoran = listSetoran.get(i);
        final CheckBox cb_setoran = setoranSalesViewHolder.cb_setoran;

        setoranSalesViewHolder.txt_tanggal.setText(setoran.getTanggal());
        setoranSalesViewHolder.txt_customer.setText(setoran.getCustomer());
        setoranSalesViewHolder.txt_nota.setText(setoran.getNota());
        setoranSalesViewHolder.txt_jumlah.setText(Converter.doubleToRupiah(setoran.getJumlah()));
        setoranSalesViewHolder.txt_pembayaran.setText(setoran.getPembayaran());

        if (setoran.isSelected()) {
            setoranSalesViewHolder.cb_setoran.setChecked(true);
        }else {
            setoranSalesViewHolder.cb_setoran.setChecked(false);
        }

        setoranSalesViewHolder.cb_setoran.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                listSetoran.get(i).setSelected(isChecked);
                ((SetoranSales)activity).updateJumlah();
            }
        });
    }

    @Override
    public int getItemCount() {
        return listSetoran.size();
    }

    class SetoranSalesViewHolder extends RecyclerView.ViewHolder{

        TextView txt_customer, txt_nota, txt_jumlah, txt_tanggal, txt_pembayaran;
        CheckBox  cb_setoran;

        SetoranSalesViewHolder(@NonNull View itemView) {
            super(itemView);
            txt_customer = itemView.findViewById(R.id.txt_customer);
            txt_nota = itemView.findViewById(R.id.txt_nota);
            cb_setoran = itemView.findViewById(R.id.cb_piutang);
            txt_jumlah = itemView.findViewById(R.id.txt_jumlah);
            txt_tanggal = itemView.findViewById(R.id.txt_tanggal);
            txt_pembayaran = itemView.findViewById(R.id.txt_pembayaran);
        }
    }
}
