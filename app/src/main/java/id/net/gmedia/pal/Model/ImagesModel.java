package id.net.gmedia.pal.Model;

import android.graphics.Bitmap;

public class ImagesModel {

    private Bitmap bitmap;

    public ImagesModel(Bitmap bitmap){
        this.bitmap = bitmap;
    }

    public ImagesModel(){

    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}
