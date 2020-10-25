package ir.pxmaster.www;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import ir.pxmaster.www.R;
import ir.pxmaster.www.model.SubImage;

public class MosaicCreator {

    Bitmap masterBmp, mosaicBmp;
    ArrayList<SubImage> subImgArray = new ArrayList<>();
    ArrayList<Bitmap> subBmpArray = new ArrayList<>();
    ArrayList<float[]> avgRGB = new ArrayList<>();
    Activity context;
    float subSize = 50f;
    int masterAlphaValue = 80;
    boolean interruptFlag;
    String colorGreen = "#1c9901";

    ProgressBar createMosaicImagePrgBar;
    ImageView previewImg;
    LinearLayout previewLyt;
    LinearLayout mosaicCreateLoadingLyt;

    public MosaicCreator(Activity context, Bitmap masterBmp, ArrayList<SubImage> subImgArray,
                         ProgressBar createMosaicImagePrgBar,
                         LinearLayout previewLyt, LinearLayout mosaicCreateLoadingLyt, ImageView previewImg) {
        this.masterBmp = masterBmp;
        this.subImgArray = subImgArray;
        this.context = context;
        this.createMosaicImagePrgBar = createMosaicImagePrgBar;
        this.previewLyt = previewLyt;
        this.previewImg = previewImg;
        this.mosaicCreateLoadingLyt = mosaicCreateLoadingLyt;
        interruptFlag = false;

        this.createMosaicImagePrgBar.setMax(subImgArray.size());
    }

    public void analysisSubImages() {

        for (SubImage subImg : subImgArray) {
            Glide.with(context.getApplicationContext())
                    .asBitmap()
                    .load(subImg.getUri())
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap subBmp, @Nullable Transition<? super Bitmap> transition) {
                            if (interruptFlag) return;

                            subBmp = Bitmap.createScaledBitmap(subBmp, (int) subSize, (int) subSize, true);


                            float[] avg = new float[3];
                            for (int i = 0; i < subBmp.getWidth(); i++) {
                                for (int j = 0; j < subBmp.getHeight(); j++) {
                                    int color = subBmp.getPixel(i, j);
                                    int red = Color.red(color);
                                    int green = Color.green(color);
                                    int blue = Color.blue(color);
                                    avg[0] += red;
                                    avg[1] += green;
                                    avg[2] += blue;
                                }
                            }

                            int pxCnt = subBmp.getWidth() * subBmp.getHeight();
                            avg[0] /= pxCnt;
                            avg[1] /= pxCnt;
                            avg[2] /= pxCnt;

                            subBmpArray.add(subBmp);
                            avgRGB.add(avg);

                            createMosaicImagePrgBar.setProgress(createMosaicImagePrgBar.getProgress() + 1);

                            if (avgRGB.size() == subImgArray.size() && !interruptFlag) {
                                mosaicBmp = createMosaic();
                                previewImg.setImageBitmap(getDemoMosaicImage());
                                previewLyt.setVisibility(View.VISIBLE);
                                mosaicCreateLoadingLyt.setVisibility(View.GONE);
                                createMosaicImagePrgBar.setProgress(0);
                            }
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }
                    });
        }

    }

    private int closestSubImage(float[] rgb) {
        float minDif = 99999;
        int minDifIndex = -1;
        for (int i = 0; i < avgRGB.size(); i++) {
            float dif = Math.abs(avgRGB.get(i)[0] - rgb[0]) +
                    Math.abs(avgRGB.get(i)[1] - rgb[1]) +
                    Math.abs(avgRGB.get(i)[2] - rgb[2]);
            if (dif < minDif) {
                minDif = dif;
                minDifIndex = i;
            }
        }
        return minDifIndex;
    }

    private Bitmap createMosaic() {
        float[] px = new float[3];
        Bitmap mosaicBmp = Bitmap.createBitmap((int) (masterBmp.getWidth() * subSize),
                (int) (masterBmp.getHeight() * subSize), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mosaicBmp);

        createMosaicImagePrgBar.setMax(masterBmp.getWidth() * masterBmp.getHeight());

        for (int i = 0; i < masterBmp.getWidth(); i++) {
            for (int j = 0; j < masterBmp.getHeight(); j++) {
                int color = masterBmp.getPixel(i, j);
                int red = Color.red(color);
                int green = Color.green(color);
                int blue = Color.blue(color);
                px[0] = red;
                px[1] = green;
                px[2] = blue;
                int closestSubIndex = closestSubImage(px);
                Bitmap tmpBmp = subBmpArray.get(closestSubIndex);
                canvas.drawBitmap(tmpBmp, i * subSize, j * subSize, null);

//                createMosaicImagePrgBar.setProgress(mosaicCreatedCnt);
            }
        }
//        createMosaicImagePrgBar.getProgressDrawable().setColorFilter(
//                Color.parseColor(colorGreen), android.graphics.PorterDuff.Mode.SRC_IN);
        Bitmap masterAlphaBmp = Bitmap.createScaledBitmap(masterBmp, mosaicBmp.getWidth(),
                mosaicBmp.getHeight(), true);
        Paint alphaPaint = new Paint();
        alphaPaint.setAlpha(masterAlphaValue);
        canvas.drawBitmap(masterAlphaBmp, 0, 0, alphaPaint);

        return mosaicBmp;
    }


    public Bitmap getDemoMosaicImage() {
        Bitmap demoBmp = Bitmap.createScaledBitmap(mosaicBmp, (int) (subSize / 10) * masterBmp.getWidth(),
                (int) (subSize / 10) * masterBmp.getHeight(), true);
        Canvas canvas = new Canvas(demoBmp);

        Bitmap watermarkBmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.watermark);
        watermarkBmp = Bitmap.createScaledBitmap(watermarkBmp, demoBmp.getWidth(), demoBmp.getHeight(), true);
        canvas.drawBitmap(watermarkBmp, 0, 0, null);


        return demoBmp;
    }


    public Bitmap getLargeMosaicImage() {
        return mosaicBmp;
    }

    public void saveMosaicImage() {


        String file_path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES) +
                "/pxMaster";
        File dir = new File(file_path);
        if (!dir.exists())
            dir.mkdirs();
        File file = new File(dir, "Mosaic_" + new Random().nextInt() + ".png");
        if (file.exists()) file.delete();
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(file);

            mosaicBmp.compress(Bitmap.CompressFormat.PNG, 85, fOut);
            fOut.flush();
            fOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        MediaScannerConnection.scanFile(context, new String[]{file.toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });
    }

    public void setInterruptFlag() {
        interruptFlag = true;
    }
}
