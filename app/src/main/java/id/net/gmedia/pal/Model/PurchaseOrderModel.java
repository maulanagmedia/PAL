package id.net.gmedia.pal.Model;

public class PurchaseOrderModel {
    private String id;
    private String tanggal;
    private String tanggal_tempo;
    private String kode_supplier;
    private String nama_supplier;
    private double total;
    private String customers;
    private double ppn;
    private double biaya_lain;
    private String keterangan;
    private String status;

    private String tgl="";
    private String total_margin = "";
    private String total_sepuluh_maragin = "";
    private String tonase ="";
    private double estimasi_biaya;
    private double total_nominal;
    private String insert_at = "";

    public PurchaseOrderModel(String id, String tanggal, double total, double ppn, double biaya_lain,
                              String kode_supplier, String nama_supplier, String tanggal_tempo, String keterangan){
        this.id = id;
        this.tanggal = tanggal;
        this.ppn = ppn;
        this.biaya_lain = biaya_lain;
        this.total = total;
        this.kode_supplier = kode_supplier;
        this.nama_supplier = nama_supplier;
        this.tanggal_tempo = tanggal_tempo;
        this.keterangan = keterangan;

    }

    public PurchaseOrderModel(String id, String tanggal, String customers, double estimasi_biaya, double total_nominal,
                              String tonase, String total_margin, String total_sepuluh_maragin, String insert_at, String status){
        this.id = id;
        this.tanggal = tanggal;
        this.customers = customers;
        this.estimasi_biaya = estimasi_biaya;
        this.total_nominal = total_nominal;
        this.tonase = tonase;
        this.total_margin = total_margin;
        this.total_sepuluh_maragin = total_sepuluh_maragin;
        this.insert_at = insert_at;
        this.status = status;
    }

    public  String getCustomers(){
        return customers;
    }

    public String getInsert_at(){
        return insert_at;
    }

    public String getTotal_sepuluh_maragin(){
        return total_sepuluh_maragin;
    }

    public String getTotal_margin () {
        return total_margin;
    }

    public String getTonase(){
        return tonase;
    }

    public double getTotal_nominal (){
        return total_nominal;
    }

    public String getNama_supplier() {
        return nama_supplier;
    }

    public String getTanggal() {
        return tanggal;
    }
    public String getStatus (){
        return status;
    }
    public double getEstimasi_biaya(){
        return estimasi_biaya;
    }

    public String getKeterangan() {
        return keterangan;
    }

    public String getId() {
        return id;
    }

    public double getBiaya_lain() {
        return biaya_lain;
    }

    public double getPpn() {
        return ppn;
    }

    public double getTotal() {
        return total;
    }

    public String getKode_supplier() {
        return kode_supplier;
    }

    public String getTanggal_tempo() {
        return tanggal_tempo;
    }
}
