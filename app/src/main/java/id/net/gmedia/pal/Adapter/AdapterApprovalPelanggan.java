package id.net.gmedia.pal.Adapter;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.leonardus.irfan.SimpleObjectModel;

import java.util.List;

import id.net.gmedia.pal.Activity.Approval.ApprovalPelanggan;
import id.net.gmedia.pal.Activity.CustomerDetail;
import id.net.gmedia.pal.Model.CustomerModel;
import id.net.gmedia.pal.R;
import id.net.gmedia.pal.Util.Constant;

public class AdapterApprovalPelanggan extends RecyclerView.Adapter<AdapterApprovalPelanggan.ViewHolderApprovalPelanggan> {

    private ApprovalPelanggan activity;
    private List<SimpleObjectModel> listCus;
    private List<CustomerModel> listCustomer;

    public void setListApproval(List<SimpleObjectModel> listApproval){
        this.listCus = listApproval;
    }

    public AdapterApprovalPelanggan(ApprovalPelanggan activity, List<CustomerModel> listCustomer){
        this.activity = activity;
        this.listCustomer = listCustomer;
    }

    @NonNull
    @Override
    public ViewHolderApprovalPelanggan onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolderApprovalPelanggan(LayoutInflater.from(activity).
                inflate(R.layout.item_approval_customer, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolderApprovalPelanggan viewHolderApprovalPelanggan, int i) {
        viewHolderApprovalPelanggan.bind(listCustomer.get(i));
        final CustomerModel cus = listCustomer.get(i);

        viewHolderApprovalPelanggan.img_apv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listCus != null){
                    showApproval(viewHolderApprovalPelanggan.img_apv, cus.getId());
                }
                else{
                    Toast.makeText(activity, "Data approval belum termuat", Toast.LENGTH_SHORT).show();
                    ((ApprovalPelanggan)activity).loadApproval();
                }
            }
        });

    }

    private void showApproval(View anchor_view, final String id){
        PopupMenu popup = new PopupMenu(activity, anchor_view, Gravity.END);

        for(int i = 0; i < listCus.size(); i++){
            popup.getMenu().add(0, i, i, listCus.get(i).getValue());
        }

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                ((ApprovalPelanggan)activity).responApproval1(id, listCus.get(menuItem.getItemId()).getId());
                return false;
            }
        });

        popup.show();
    }


    @Override
    public int getItemCount() {
        return listCustomer.size();
    }

    class ViewHolderApprovalPelanggan extends RecyclerView.ViewHolder{

        View layout_parent;
        TextView txt_nama, txt_alamat, txt_area;
        Button btn_approval;
        ImageView img_apv;

        ViewHolderApprovalPelanggan(@NonNull View itemView) {
            super(itemView);
            layout_parent = itemView.findViewById(R.id.layout_parent);
            txt_nama = itemView.findViewById(R.id.txt_nama);
            txt_alamat = itemView.findViewById(R.id.txt_alamat);
            btn_approval = itemView.findViewById(R.id.btn_approval);
            txt_area = itemView.findViewById(R.id.txt_area);
            img_apv = itemView.findViewById(R.id.img_approval);
        }


        void bind(final CustomerModel c){
            txt_nama.setText(c.getNama());
            String alamat = "Alamat : " + c.getAlamat();
            txt_alamat.setText(alamat);
            String area = "Area : " + c.getKode_area();
            txt_area.setText(area);

            layout_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(activity, CustomerDetail.class);
                    i.putExtra(Constant.EXTRA_ID_CUSTOMER, c.getId());
                    activity.startActivity(i);
                }
            });

           /* btn_approval.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.showApproval(c.getId());
                }
            });*/

        }
    }
}
