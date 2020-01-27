package id.net.gmedia.pal.Adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.leonardus.irfan.Converter;

import java.util.List;

import id.net.gmedia.pal.Model.BarangPOModel;
import id.net.gmedia.pal.R;

public class ApprovalSOKirimDetailAdapter extends RecyclerView.Adapter<ApprovalSOKirimDetailAdapter.ApprovalPODetailViewHolder> {

    private Activity activity;
    private List<BarangPOModel> listPo;

    public ApprovalSOKirimDetailAdapter(Activity activity, List<BarangPOModel> listPo){
        this.activity = activity;
        this.listPo = listPo;
    }

    @NonNull
    @Override
    public ApprovalPODetailViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ApprovalPODetailViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_detail_kirim_so, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ApprovalPODetailViewHolder approvalPODetailViewHolder, int i) {
        final BarangPOModel barang = listPo.get(i);

        approvalPODetailViewHolder.txt_nama.setText(barang.getNama());
        approvalPODetailViewHolder.txt_kode_area.setText(barang.getId());
//        String jumlah = barang.getJumlah() + " " + barang.getSatuan();
        approvalPODetailViewHolder.txt_jumlah.setText(barang.getJml());
        approvalPODetailViewHolder.txt_nama_barang.setText(barang.getNama_barang());
        approvalPODetailViewHolder.txt_tonase.setText(barang.getTonase());
        approvalPODetailViewHolder.txt_total.setText(Converter.doubleToRupiah(barang.getSubtotal()));

        /*approvalPODetailViewHolder.img_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(activity, approvalPODetailViewHolder.img_edit, Gravity.END);
                //Inflating the Popup using xml file
                popup.getMenuInflater()
                        .inflate(R.menu.menu_item_penjualan, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        if(item.getItemId() == R.id.action_hapus){
                            ((ApprovalPODetail)activity).hapusBarangPO(barang.getId());
                            return true;
                        }
                        else if(item.getItemId() == R.id.action_edit){
                            Gson gson = new Gson();
                            Intent i = new Intent(activity, ApprovalPOEdit.class);
                            i.putExtra(Constant.EXTRA_BARANG_PO, gson.toJson(barang));
                            i.putExtra(Constant.EXTRA_NO_NOTA, ((ApprovalPODetail)activity).nota.getId());
                            activity.startActivity(i);

                            return true;
                        }

                        return false;
                    }
                });

                popup.show(); //showing popup menu
            }
        });*/
    }

    @Override
    public int getItemCount() {
        return listPo.size();
    }

    class ApprovalPODetailViewHolder extends RecyclerView.ViewHolder{

        TextView txt_nama, txt_nama_barang, txt_tonase, txt_kode_area, txt_jumlah, txt_total;
        //ImageView img_edit;

        ApprovalPODetailViewHolder(@NonNull View itemView) {
            super(itemView);
            txt_nama = itemView.findViewById(R.id.txt_nama);
            txt_nama_barang = itemView.findViewById(R.id.txt_nama_barang);
            txt_jumlah = itemView.findViewById(R.id.txt_jml);
            txt_kode_area = itemView.findViewById(R.id.txt_kode_area);
            txt_tonase = itemView.findViewById(R.id.txt_tonase);
            txt_total = itemView.findViewById(R.id.txt_total);
            //img_edit = itemView.findViewById(R.id.img_edit);
        }
    }
}
