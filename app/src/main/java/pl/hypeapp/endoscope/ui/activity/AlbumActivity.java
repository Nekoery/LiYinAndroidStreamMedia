package pl.hypeapp.endoscope.ui.activity;

import android.app.AppComponentFactory;
import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.FileChannel;
import java.nio.file.Files;

import pl.hypeapp.endoscope.R;
import pl.hypeapp.endoscope.presenter.AESPresenter;

public class AlbumActivity extends AppCompatActivity {
    private Bitmap rotateBitmap(Bitmap origin, float alpha) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.setRotate(alpha);
        // 围绕原地进行旋转
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (newBM.equals(origin)) {
            return newBM;
        }
        origin.recycle();
        return newBM;
    }

    public static byte[] toByteArray3(String filename) throws IOException {

        FileChannel fc = null;
        try {
            fc = new RandomAccessFile(filename, "r").getChannel();
            MappedByteBuffer byteBuffer = fc.map(FileChannel.MapMode.READ_ONLY, 0,
                    fc.size()).load();
            System.out.println(byteBuffer.isLoaded());
            byte[] result = new byte[(int) fc.size()];
            if (byteBuffer.remaining() > 0) {
                // System.out.println("remain");
                byteBuffer.get(result, 0, byteBuffer.remaining());
            }
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                fc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        final LinearLayout MainLinearLayout = (LinearLayout) findViewById(R.id.InsideA);
        File file = new File(Environment.getExternalStorageDirectory()+"/fingerprintimages");
        File files[] = file.listFiles();
        InputStream inputStream = null;
        Bitmap bitmap;
        ImageView imageView ;
        for(int i = 0 ;i < Math.ceil(files.length/2.0) ;i ++) {
            LinearLayout layoutIn = new LinearLayout(this);
            layoutIn.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams layoutParam = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT,1);
            MainLinearLayout.addView(layoutIn, layoutParam);
            for(int j = 0 ;j< 2 ;j++) {
                if(files.length%2==1&&i*2+j==files.length)
                    continue;
                byte b [] = null;
                try {
                    b =toByteArray3(files[i*2+j].getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                imageView = new ImageView(this);
                LinearLayout.LayoutParams layoutParamIV = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1);

                bitmap = rotateBitmap(
                        BitmapFactory.decodeByteArray(
                                AESPresenter.decryptByte2Byte(b,"kakuishdyshifncgyrsjdiosfnvjfeas","asadfdedwderfvgd"),
                                0,AESPresenter.decryptByte2Byte(b,"kakuishdyshifncgyrsjdiosfnvjfeas","asadfdedwderfvgd").length),
                        270);
                imageView.setImageBitmap(bitmap);
                layoutIn.addView(imageView, layoutParamIV);
            }
        }
    }
}
