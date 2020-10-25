package ir.pxmaster.www;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.victor.loading.rotate.RotateLoading;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;

import ir.pxmaster.www.adapter.SubImageAdapter;
import ir.pxmaster.www.model.SubImage;
import util.IabHelper;
import util.IabResult;
import util.Inventory;
import util.Purchase;

public class MosaicOrderActivity extends AppCompatActivity {

    ImageView masterImageView;
    ImageView deleteMasterImageView;
    ImageView editMasterImageView;
    ImageView previewImageView;

    EditText offCodeEdtxt;

    LinearLayout addSubImageIconLyt;
    LinearLayout deleteSubImageIconLyt;
    LinearLayout mosaicCreateLoadingLyt;
    LinearLayout previewLyt;
    RelativeLayout subImageLoaderLyt;

    ProgressBar createMosaicImagePrgBar;
    RotateLoading rotateLoading;

    GridView subImagesGridView;

    ArrayList<SubImage> subImages = new ArrayList<>();

    MosaicCreator mosaicCreator;

    private SubImageAdapter subImageAdapter;

    Bitmap masterBmp;
    Uri masterUri;

    static int RC_PIC_MASTER_IMAGE = 12345;
    static int RC_PIC_SUB_IMAGES = 54321;
    static int RC_PURCHASE = 54637;

    float widthSubPicSize = 0;
    int minWidthHeightOfMaster = 80;

    String colorGray = "#a4a4a4";


    //    Purchase Params
// Debug tag, for logging
    static final String TAG = "MOSAIC ORDER ACTIVITY";

    // SKUs for our products: the premium upgrade (non-consumable)
    static final String SKU_PREMIUM = "pic_test_purchase";

    // Does the user have the premium upgrade?
    boolean mIsPremium = false;

    // (arbitrary) request code for the purchase flow


    // The helper object
    IabHelper mHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mosaic_order);

        initUI();

        initBazaarConfig();

    }

    public void initBazaarConfig() {
        String base64EncodedPublicKey = getString(R.string.purchase_key);

        mHelper = new IabHelper(this, base64EncodedPublicKey);

        Log.d(TAG, "Starting setup.");
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                Log.d(TAG, "Setup finished.");

                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    Log.d(TAG, "Problem setting up In-app Billing: " + result);
                }
                // Hooray, IAB is fully set up!
                mHelper.queryInventoryAsync(mGotInventoryListener);
            }
        });
    }

    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Log.d(TAG, "Query inventory finished.");
            if (result.isFailure()) {
                Log.d(TAG, "Failed to query inventory: " + result);
                return;
            } else {
                Log.d(TAG, "Query inventory was successful.");
                // does the user have the premium upgrade?
                mIsPremium = inventory.hasPurchase(SKU_PREMIUM);

                // update UI accordingly

                Log.d(TAG, "User is " + (mIsPremium ? "PREMIUM" : "NOT PREMIUM"));
            }

            Log.d(TAG, "Initial inventory query finished; enabling main UI.");
        }
    };

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            if (result.isFailure()) {
                Log.d(TAG, "Error purchasing: " + result);
                return;
            } else if (purchase.getSku().equals(SKU_PREMIUM)) {
                // give user access to premium content and update the UI
                new saveImage().execute();
                mHelper.consumeAsync(purchase, null);
            }
        }
    };

    public void initUI() {
        masterImageView = (ImageView) findViewById(R.id.img_master);
        subImagesGridView = (GridView) findViewById(R.id.grid_sub_imgs);
        rotateLoading = (RotateLoading) findViewById(R.id.rotateloading);
        subImageLoaderLyt = (RelativeLayout) findViewById(R.id.lyt_sub_images_loading);
        deleteMasterImageView = (ImageView) findViewById(R.id.img_delete_master);
        editMasterImageView = (ImageView) findViewById(R.id.img_edit_master);
        addSubImageIconLyt = (LinearLayout) findViewById(R.id.lyt_add_sub_image_icon);
        deleteSubImageIconLyt = (LinearLayout) findViewById(R.id.lyt_delete_sub_image_icons);
        mosaicCreateLoadingLyt = (LinearLayout) findViewById(R.id.lyt_creating_mosaic_loading);
        createMosaicImagePrgBar = (ProgressBar) findViewById(R.id.prg_create_mosaic_image);
        previewImageView = (ImageView) findViewById(R.id.img_mosaic_preview);
        previewLyt = (LinearLayout) findViewById(R.id.lyt_mosaic_preview);
        offCodeEdtxt = (EditText) findViewById(R.id.edtxt_offcode);

        initSubImagesGridView();

//        createMosaicImagePrgBar.getProgressDrawable().setColorFilter(
//                Color.parseColor(colorGray), android.graphics.PorterDuff.Mode.SRC_IN);

        mosaicCreateLoadingLyt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelCreating(view);
            }
        });


    }

    public void cancelCreating(View v) {
        mosaicCreateLoadingLyt.setVisibility(View.GONE);
        mosaicCreator.setInterruptFlag();
        createMosaicImagePrgBar.setProgress(0);
    }

    public void deleteMaster(View v) {
        deleteMasterImageView.setVisibility(View.GONE);
        editMasterImageView.setVisibility(View.GONE);
        masterImageView.setImageResource(R.drawable.add_pic_1);
        masterBmp.recycle();
        masterBmp = null;
    }

    public void chooseMasterFromGallery(View v) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, RC_PIC_MASTER_IMAGE);
    }

    public void chooseSubImagesFromGallery(View v) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), RC_PIC_SUB_IMAGES);
    }

    public void createMosaicImage(View v) {
        if (masterBmp == null) {
            Toast.makeText(this, "عکس اصلی را انتخاب کنید.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (subImages.size() == 0) {
            Toast.makeText(this, "عکس های موزاییکی خود را انتخاب کنید. (هر چه بیشتر، بهتر)", Toast.LENGTH_LONG).show();
            return;
        }


        mosaicCreator = new MosaicCreator(this, masterBmp, subImages,
                createMosaicImagePrgBar, previewLyt, mosaicCreateLoadingLyt, previewImageView);
        mosaicCreator.analysisSubImages();
        mosaicCreateLoadingLyt.setVisibility(View.VISIBLE);
    }

    public void updateSubImagesGridView() {
        subImageAdapter.setWidth(subImagesGridView.getMeasuredWidth() / subImagesGridView.getNumColumns());
        subImageAdapter.notifyDataSetChanged();
    }

    public void initSubImagesGridView() {

        subImageAdapter = new SubImageAdapter(getApplicationContext(), subImages, widthSubPicSize);
        subImagesGridView.setAdapter(subImageAdapter);

        subImagesGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (subImageAdapter.isSelectedMode()) {
                    addSubImageIconLyt.setVisibility(View.GONE);
                    deleteSubImageIconLyt.setVisibility(View.VISIBLE);
                }
                return false;
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_PIC_SUB_IMAGES && resultCode == RESULT_OK && null != data) {

            new subImagesLoader(data).execute();

        } else if (requestCode == RC_PIC_MASTER_IMAGE && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            loadMasterPreview(imageUri);
        } else if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            Uri imageUri = UCrop.getOutput(data);
            loadMasterPreview(imageUri);
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
        } else if (requestCode == RC_PURCHASE) {
            if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
                super.onActivityResult(requestCode, resultCode, data);
            } else {
                Log.d(TAG, "onActivityResult handled by IABUtil.");
            }
        }

    }

    public void loadMasterPreview(Uri uri) {
        try {
            InputStream imageStream = getContentResolver().openInputStream(uri);

            masterUri = uri;
            masterBmp = BitmapFactory.decodeStream(imageStream);

            masterBmp = resizeMaster(masterBmp);

            masterImageView.setImageBitmap(masterBmp);
            deleteMasterImageView.setVisibility(View.VISIBLE);
            editMasterImageView.setVisibility(View.VISIBLE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Bitmap resizeMaster(Bitmap bmp) {
        int dstWidth = 0;
        int dstHeight = 0;
        if (bmp.getHeight() >= bmp.getWidth()) {
            dstWidth = minWidthHeightOfMaster;
            dstHeight = (int) ((float) (minWidthHeightOfMaster) * (float) bmp.getHeight() / bmp.getWidth());
        } else {
            dstHeight = minWidthHeightOfMaster;
            dstWidth = (int) ((float) (minWidthHeightOfMaster) * (float) bmp.getWidth() / bmp.getHeight());

        }
        bmp = Bitmap.createScaledBitmap(bmp, dstWidth, dstHeight, true);
        return bmp;
    }

    public void editMaster(View v) {
        String destinationFileName = "PicMaster_cropped_" + new Random().nextInt() + ".png";
        Uri dstCrop = Uri.fromFile(new File(getCacheDir(), destinationFileName));
        UCrop.of(masterUri, dstCrop)
                .start(this);
    }

    private Bitmap centerCrop(Bitmap srcBmp) {
        Bitmap dstBmp;
        if (srcBmp.getWidth() >= srcBmp.getHeight()) {

            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    srcBmp.getWidth() / 2 - srcBmp.getHeight() / 2,
                    0,
                    srcBmp.getHeight(),
                    srcBmp.getHeight()
            );

        } else {

            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    0,
                    srcBmp.getHeight() / 2 - srcBmp.getWidth() / 2,
                    srcBmp.getWidth(),
                    srcBmp.getWidth()
            );
        }
        return dstBmp;
    }

    public void cancelSubImageSelecting(View v) {
        for (int i = 0; i < subImages.size(); i++) {
            subImages.get(i).setSelected(false);
        }
        subImageAdapter.setSelectMode(false);
        subImageAdapter.notifyDataSetChanged();

        addSubImageIconLyt.setVisibility(View.VISIBLE);
        deleteSubImageIconLyt.setVisibility(View.GONE);
    }

    public void selectAllSubImages(View v) {
        for (int i = 0; i < subImages.size(); i++) {
            subImages.get(i).setSelected(true);
        }
        subImageAdapter.notifyDataSetChanged();
    }

    public void deleteSubImages(View v) {
        for (int i = subImages.size() - 1; i >= 0; i--) {
            if (subImages.get(i).isSelected()) {
                subImages.remove(i);
            } else {
                subImages.get(i).setSelected(false);
            }
        }
        subImageAdapter.setSelectMode(false);
        subImageAdapter.notifyDataSetChanged();

        addSubImageIconLyt.setVisibility(View.VISIBLE);
        deleteSubImageIconLyt.setVisibility(View.GONE);
    }

    class subImagesLoader extends AsyncTask {

        Intent data;

        public subImagesLoader(Intent data) {
            this.data = data;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            // Get the Image from data
            if (data.getData() != null) {

                Uri uri = data.getData();

                SubImage subImage = new SubImage();
                subImage.setUri(uri);
                subImages.add(subImage);

            } else {
                if (data.getClipData() != null) {
                    ClipData mClipData = data.getClipData();
                    for (int i = 0; i < mClipData.getItemCount(); i++) {

                        ClipData.Item item = mClipData.getItemAt(i);
                        Uri uri = item.getUri();
                        SubImage subImage = new SubImage();
                        subImage.setUri(uri);
                        subImages.add(subImage);

                    }
                }
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            rotateLoading.start();
            subImageLoaderLyt.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Object o) {
            rotateLoading.stop();
            subImageLoaderLyt.setVisibility(View.GONE);

            updateSubImagesGridView();
        }
    }

    @Override
    public void onBackPressed() {
        if (previewLyt.getVisibility() == View.VISIBLE) {
            previewLyt.setVisibility(View.GONE);
        } else if (mosaicCreateLoadingLyt.getVisibility() == View.VISIBLE) {
            cancelCreating(null);
        } else {
            finish();
        }
    }

    public void back(View v) {
        onBackPressed();
    }

    public void saveHighResolution(View v) {
        isStoragePermissionGranted();
    }

    class saveImage extends AsyncTask<String, String, String> {

        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(MosaicOrderActivity.this, "",
                    "در حال ذخیره سازی تصویر در گالری...", true);
            dialog.setCancelable(false);
        }

        @Override
        protected String doInBackground(String... strings) {
            mosaicCreator.saveMosaicImage();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            dialog.dismiss();
            Toast.makeText(MosaicOrderActivity.this, "تصویر موزاییکی در گالری ذخیره شد.", Toast.LENGTH_LONG).show();
            previewLyt.setVisibility(View.GONE);
        }
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                mHelper.launchPurchaseFlow(this, SKU_PREMIUM, RC_PURCHASE, mPurchaseFinishedListener, "payload-string");
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else {
            mHelper.launchPurchaseFlow(this, SKU_PREMIUM, RC_PURCHASE, mPurchaseFinishedListener, "payload-string");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mHelper.launchPurchaseFlow(this, SKU_PREMIUM, RC_PURCHASE, mPurchaseFinishedListener, "payload-string");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHelper != null) mHelper.dispose();
        mHelper = null;
    }
}
