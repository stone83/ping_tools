package com.jj.game.boost.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.jj.game.boost.R;
import com.jj.game.boost.domain.ProcessInfo;

import java.util.List;

/**
 * Created by huzd on 2017/5/16.
 */

public class ProcessColumAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ViewGroup.OnClickListener{
    List<ProcessInfo> mList = null;
    private OnRecyclerViewItemClickListener mOnItemClickListener = null;
    private OnCheckNotAllNotify mNotify;
    private Context mContext;
    //item类型
    public static final int ITEM_TYPE_HEADER = 0;
    public static final int ITEM_TYPE_CONTENT = 1;
    public static final int ITEM_TYPE_BOTTOM = 2;
    private int mHeaderCount=0;//头部View个数
    private int mBottomCount=0;//底部View个数
    private LayoutInflater mLayoutInflater;
    private Typeface mTypeface;

    public ProcessColumAdapter(Context context, List<ProcessInfo> datas) {
        mLayoutInflater = LayoutInflater.from(context);
        mContext = context;
        mList = datas;
//        mTypeface = CommonUtil.getSourceTypeFont(mContext);
    }

    public void setList(List<ProcessInfo> list) {
        mList = list;
    }

    public void remove(int position) {
        mList.remove(position);
    }

    //内容长度
    public int getContentItemCount(){
        return mList.size();
    }
    //判断当前item是否是HeadView
    public boolean isHeaderView(int position) {
        return mHeaderCount != 0 && position < mHeaderCount;
    }
    //判断当前item是否是FooterView
    public boolean isBottomView(int position) {
        return mBottomCount != 0 && position >= (mHeaderCount + getContentItemCount());
    }

    @Override
    public int getItemViewType(int position) {
        int dataItemCount = getContentItemCount();
        if (isHeaderView(position)) {
            //头部View
            return ITEM_TYPE_HEADER;
        } else if (isBottomView(position)) {
            //底部View
            return ITEM_TYPE_BOTTOM;
        } else {
            //内容View
            return ITEM_TYPE_CONTENT;
        }
//        return super.getItemViewType(position);
    }

    //创建新View，被LayoutManager所调用
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewType ==ITEM_TYPE_HEADER) {
            return new HeaderViewHolder(mLayoutInflater.inflate(R.layout.processcolum_item_bottom,viewGroup,false));
        } else if (viewType == ITEM_TYPE_CONTENT) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.processcolum_item,viewGroup,false);
            ViewHolder vh = new ViewHolder(view);
            if (Build.VERSION.SDK_INT >= 21) {
                view.setBackgroundResource(R.drawable.recycleritem_bg);
            }
            return vh;
        } else if (getContentItemCount() > 5 && viewType == ITEM_TYPE_BOTTOM) {
            return new BottomViewHolder(mLayoutInflater.inflate(R.layout.processcolum_item_bottom,viewGroup,false));
        }
        return new BottomViewHolderNone(mLayoutInflater.inflate(R.layout.processcolum_item_bottom_none,viewGroup,false));

//        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.processcolum_item,viewGroup,false);
//        ViewHolder vh = new ViewHolder(view);
//        //将创建的View注册点击事件
//        view.setOnClickListener(this);
//
//        if (Build.VERSION.SDK_INT >= 21) {
//            view.setBackgroundResource(R.drawable.recycleritem_bg);
//        }
//
//        return vh;
    }
    //将数据与界面进行绑定的操作
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder_Recycler, int position) {

       if (viewHolder_Recycler instanceof ProcessColumAdapter.ViewHolder) {
            ProcessColumAdapter.ViewHolder viewHolder = (ProcessColumAdapter.ViewHolder)viewHolder_Recycler;

           // 将数据保存在button的Tag中,以便点击时进行获取.
           viewHolder.mBtnStop.setTag(position);

           // 将创建的View注册点击事件
           viewHolder.mBtnStop.setOnClickListener(this);

            viewHolder.mTextView.setClickable(false);
            viewHolder.mTextView.setFocusable(false);
            viewHolder.mTextView.setFocusableInTouchMode(false);

            viewHolder.mTextViewSize.setClickable(false);
            viewHolder.mTextViewSize.setFocusable(false);
            viewHolder.mTextViewSize.setFocusableInTouchMode(false);

            viewHolder.mImageView.setClickable(false);
            viewHolder.mImageView.setFocusable(false);
            viewHolder.mImageView.setFocusableInTouchMode(false);

            viewHolder.mTextView.setText(mList.get(position).getAppName());
//            viewHolder.mTextView.setTypeface(mTypeface);

           viewHolder.mImageView.setImageDrawable(mList.get(position).getAppIcon());
//           viewHolder.mImageView.setImageBitmap(BitmapUtil.fillet(BitmapUtil.ALL,
//                   ((BitmapDrawable) mList.get(position).getAppIcon()).getBitmap(), 30));
//           FlexibleRoundedBitmapDisplayer.setRoundedImage(null, 0,
//                   FlexibleRoundedBitmapDisplayer.CORNER_ALL,
//                   mList.get(position).getAppIcon(), viewHolder.mImageView);

           Long networkSpeed = mList.get(position).getNetworkSpeed();
           if (networkSpeed != 0) {
               viewHolder.mTextViewSize.setText(String.format("%s/s", Formatter.formatFileSize(mContext, networkSpeed)));
           } else {
               viewHolder.mTextViewSize.setText("0.00 B/s");
           }
//            viewHolder.mTextViewSize.setTypeface(mTypeface);
        }
    }
    //获取数据的数量
    @Override
    public int getItemCount() {
//        return mList.size();
        return mHeaderCount + getContentItemCount() + mBottomCount;
    }
    //自定义的ViewHolder，持有每个Item的的所有界面元素
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;
        public ImageView mImageView;
        public TextView mTextViewSize;
        public Button mBtnStop;
        public ViewHolder(View view){
            super(view);
            mTextView = (TextView) view.findViewById(R.id.text);
            mImageView = (ImageView) view.findViewById(R.id.img);
            mTextViewSize = (TextView)view.findViewById(R.id.speed_txt);
            mBtnStop = (Button) view.findViewById(R.id.mBtnStop);
        }
    }
    //头部 ViewHolder
    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        public HeaderViewHolder(View itemView) {
            super(itemView);
        }
    }
    //底部 ViewHolder
    public static class BottomViewHolder extends RecyclerView.ViewHolder {
        public BottomViewHolder(View itemView) {
            super(itemView);
        }
    }
    public static class BottomViewHolderNone extends RecyclerView.ViewHolder {
        public BottomViewHolderNone(View itemView) {
            super(itemView);
        }
    }

    public interface OnCheckNotAllNotify{
        void onNotifyFalse(boolean[] ischks);
    }

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view , int pos);
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public void setOnNotifyNotAll(OnCheckNotAllNotify notify){
        mNotify = notify;
    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            //注意这里使用getTag方法获取数据
            mOnItemClickListener.onItemClick(v, (int)v.getTag());
        }
    }
    public void addItem(String content, int position) {
//        datas.add(position, content);
//        notifyItemInserted(position); //Attention!
    }
    public void removeItem(String model) {
//        int position = datas.indexOf(model);
//        datas.remove(position);
//        notifyItemRemoved(position);//Attention!
    }
}
