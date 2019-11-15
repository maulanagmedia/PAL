package id.net.gmedia.pal.Model;

public class DispensasiPiutangModel {

    private String id;
    private CustomerModel customer;
    private double total;
    private String keterangan;
    private String tanggalPengajuan;
    private String kodeArea;

    public DispensasiPiutangModel(String id, CustomerModel customer, double total, String keterangan){
        this.id = id;
        this.customer = customer;
        this.total = total;
        this.keterangan = keterangan;
    }

    public String getId() {
        return id;
    }

    public String getKeterangan() {
        return keterangan;
    }

    public double getTotal() {
        return total;
    }

    public CustomerModel getCustomer() {
        return customer;
    }

    public String getTanggalPengajuan() {
        return tanggalPengajuan;
    }

    public void setTanggalPengajuan(String tanggalPengajuan) {
        this.tanggalPengajuan = tanggalPengajuan;
    }

    public String getKodeArea() {
        return kodeArea;
    }

    public void setKodeArea(String kodeArea) {
        this.kodeArea = kodeArea;
    }
}
