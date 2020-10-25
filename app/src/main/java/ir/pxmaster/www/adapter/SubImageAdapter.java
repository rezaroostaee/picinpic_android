package ir.pxmaster.www.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import ir.pxmaster.www.R;
import ir.pxmaster.www.model.SubImage;

public class SubImageAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<SubImage> subImages;
    private int width;
    private boolean selectMode = false;

    public SubImageAdapter(Context context, ArrayList<SubImage> subImages, float width) {

        this.context = context;
        this.subImages = subImages;
        this.width = (int) width;
    }

    @Override
    public int getCount() {
        return subImages.size();
    }

    @Override
    public Object getItem(int position) {
        return subImages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder viewHolder;
        final int position_local = position;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_sub_image, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Glide.with(context).load(subImages.get(position).getUri()).into(viewHolder.imgView);
        viewHolder.imgView.setLayoutParams(new RelativeLayout.LayoutParams(width, width));
        viewHolder.radioSelected.setChecked(subImages.get(position).isSelected());

        if (selectMode){
            viewHolder.radioSelected.setVisibility(View.VISIBLE);
        }else {
            viewHolder.radioSelected.setVisibility(View.GONE);
        }

        viewHolder.imgView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (!selectMode) selectMode = true;
                viewHolder.radioSelected.setChecked(true);
                subImages.get(position_local).setSelected(true);
                notifyDataSetChanged();
                return false;
            }
        });

        viewHolder.imgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectMode) {
                    if (viewHolder.radioSelected.isChecked()) {
                        viewHolder.radioSelected.setChecked(false);
                        subImages.get(position_local).setSelected(false);
                    } else {
                        viewHolder.radioSelected.setChecked(true);
                        subImages.get(position_local).setSelected(true);
                    }
                }
            }
        });


        return convertView;
    }

    public static class ViewHolder {

        //        public TextView txtViewTitle;
        ImageView imgView;
        RadioButton radioSelected;

        ViewHolder(View itemLayoutView) {
//            txtViewTitle = (TextView) itemLayoutView.findViewById(R.id.item_title);
            imgView = (ImageView) itemLayoutView.findViewById(R.id.img_sub);
            radioSelected = (RadioButton) itemLayoutView.findViewById(R.id.radio_sub_selected);
        }
    }

    public void setWidth(int width){
        this.width = width;
    }

    public void setSelectMode(boolean mode){
        selectMode = mode;
    }

    public boolean isSelectedMode(){
        return selectMode;
    }

}