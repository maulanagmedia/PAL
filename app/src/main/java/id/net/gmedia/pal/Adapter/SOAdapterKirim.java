package id.net.gmedia.pal.Adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import androidx.annotation.NonNull;
import com.google.android.material.card.MaterialCardView;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.leonardus.irfan.Converter;
import com.leonardus.irfan.SimpleObjectModel;

import java.util.List;

import id.net.gmedia.pal.Activity.Approval.ApprovalPO;
import id.net.gmedia.pal.Activity.Approval.ApprovalSOKirim1;
import id.net.gmedia.pal.Activity.Approval.ApprovalSOKirimDetail;
import id.net.gmedia.pal.Model.PurchaseOrderModel;
import id.net.gmedia.pal.R;
import id.net.gmedia.pal.Util.Constant;


public class SOAdapterKirim extends RecyclerView.Adapter<SOAdapterKirim.ApprovalPOViewHolder> {

    private List<PurchaseOrderModel> listPO;
    private List<SimpleObjectModel> listApproval;
    private Activity activity;
    private final String TAG = "Testing";

    public SOAdapterKirim(Activity activity, List<PurchaseOrderModel> listPO){
        this.activity = activity;
        this.listPO = listPO;
    }

    public void setListApproval(List<SimpleObjectModel> listApproval){
        this.listApproval = listApproval;
    }

    @NonNull
    @Override
    public ApprovalPOViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ApprovalPOViewHolder(LayoutInflater.from(viewGroup.getContext()).
                inflate(R.layout.item_approval_kirim, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ApprovalPOViewHolder viewHolder, int i) {
        final PurchaseOrderModel po = listPO.get(i);

        viewHolder.txt_tgl.setText(po.getTanggal());
        viewHolder.txt_total.setText(Converter.doubleToRupiah(po.getTotal_nominal()));
        viewHolder.txt_estimasi_biaya.setText(Converter.doubleToRupiah(po.getEstimasi_biaya()));
        viewHolder.txt_margin.setText(po.getTotal_margin());
        viewHolder.txt_customers.setText(po.getCustomers());
        viewHolder.txt_tonase.setText(po.getTonase());
        viewHolder.txt_sepuluh_persen.setText(po.getTotal_sepuluh_maragin());
        viewHolder.txt_keterangan.setText(po.getStatus());
        viewHolder.txt_insert_at.setText(po.getInsert_at());

        viewHolder.layout_parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Gson gson = new Gson();
                Intent i = new Intent(activity, ApprovalSOKirimDetail.class);
                i.putExtra(Constant.EXTRA_NOTA, gson.toJson(po));
                activity.startActivity(i);
            }
        });

        viewHolder.img_approval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listApproval != null){
                    showApproval(viewHolder.img_approval, po.getId());
                }
                else{
                    Toast.makeText(activity, "Data approval belum termuat", Toast.LENGTH_SHORT).show();
                    ((ApprovalPO)activity).loadApproval();
                }
            }
        });
    }

    private void showApproval(View anchor_view, final String id){
        PopupMenu popup = new PopupMenu(activity, anchor_view, Gravity.END);

        for(int i = 0; i < listApproval.size(); i++){
            popup.getMenu().add(0, i, i, listApproval.get(i).getValue());
        }

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final MenuItem menuItem) {
                if (listApproval.get(menuItem.getItemId()).getId().equals("9")){
                    // jika Tolak
                    final Dialog dialog = new Dialog(activity);
                    dialog.setContentView(R.layout.pop_up_dialog_reject);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    final TextView btnKirim = dialog.findViewById(R.id.btn_kirim);
                    final EditText edtAlasan = dialog.findViewById(R.id.txt_alasan);
                    btnKirim.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((ApprovalSOKirim1)activity).responApproval1(id, listApproval.get(menuItem.getItemId()).getId(),edtAlasan.getText().toString());
                            dialog.dismiss();
                            Intent intent = new Intent(activity,ApprovalSOKirim1.class);
                        }

                    });

                    dialog.show();

                }
                else {
                    ((ApprovalSOKirim1)activity).responApproval1(id, listApproval.get(menuItem.getItemId()).getId(),"");

                }
                Log.d(TAG, "onMenuItemClick: "+listApproval.get(menuItem.getItemId()).getId());
                return false;
            }
        });

        popup.show();
    }

    @Override
    public int getItemCount() {
        return listPO.size();
    }

    class ApprovalPOViewHolder extends RecyclerView.ViewHolder{

        MaterialCardView layout_parent;
        TextView txt_tgl, txt_total, txt_margin, txt_customers,
        txt_sepuluh_persen, txt_tonase, txt_keterangan, txt_insert_at, txt_estimasi_biaya;
        ImageView img_approval;

        ApprovalPOViewHolder(@NonNull View itemView) {
            super(itemView);
            txt_tgl = itemView.findViewById(R.id.txt_tgl);
            txt_insert_at = itemView.findViewById(R.id.txt_insert);
            txt_customers = itemView.findViewById(R.id.txt_customer);
            txt_total = itemView.findViewById(R.id.txt_total);
            txt_margin = itemView.findViewById(R.id.txt_margin);
            txt_sepuluh_persen = itemView.findViewById(R.id.txt_sepuluh_persen_margin);
            txt_tonase = itemView.findViewById(R.id.txt_tonase);
            txt_estimasi_biaya = itemView.findViewById(R.id.txt_estimasi_biaya);
            txt_keterangan = itemView.findViewById(R.id.txt_keterangan);
            img_approval = itemView.findViewById(R.id.img_approval);
            layout_parent = itemView.findViewById(R.id.layout_parent);
        }
    }
}
