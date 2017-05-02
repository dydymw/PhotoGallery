package com.tdme.android.photogallery;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/4/21.
 */

public class PhotoGalleryFragment extends Fragment {

    private static final String TAG = "PhotoGalleryFragment";

    private RecyclerView mPhotoRecyclerView;
    private List<GalleryItem.PhotosBean.PhotoBean> mItems = new ArrayList<>();
    private LinearLayoutManager mLinearLayoutManager;
    public static int page = 1;
    private PhotoAdapter adapter;
    private ThumbnailDownloader<PhotoHolder>mThumbnailDownloader;
    


    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        //mLinearLayoutManager = new LinearLayoutManager(getContext());
        new FetchItemsTask().execute();

        Handler responseHandler = new Handler();
        mThumbnailDownloader = new ThumbnailDownloader<>(responseHandler);
        mThumbnailDownloader.setThumbnailDownloadListener(new ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder>() {
            @Override
            public void onThumbnailDownloaded(PhotoHolder target, Bitmap thumBnail) {
                Drawable drawable = new BitmapDrawable(getResources(),thumBnail);
                target.bindDrawable(drawable);
            }
        });

        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
        Log.i(TAG, "Background thread started");

//        mPhotoRecyclerView.addOnScrollListener(new EndLessOnScrollListener(mLinearLayoutManager) {
//            @Override
//            public void onLoadMore(int currentPage) {
//
//                loadMoreData();
//
//            }
//        });
    }

    public void loadMoreData() {


        new FetchItemsTask().execute();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

        mPhotoRecyclerView = (RecyclerView) v.findViewById(R.id.fragment_photo_gallery_recycler_view);
        mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));


        adapter = new PhotoAdapter(mItems);
        mPhotoRecyclerView.setAdapter(adapter);

        mPhotoRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!ViewCompat.canScrollVertically(recyclerView, 1)) {
                    Log.i(TAG, "onScrolled: daodile");

                    loadMoreData();

                }
            }
        });

        return v;
    }




    private class PhotoHolder extends RecyclerView.ViewHolder {
        private ImageView mItemImageView;

        public PhotoHolder(View itemView) {
            super(itemView);

            mItemImageView = (ImageView) itemView.findViewById(R.id.fragment_photo_gallery_image_view);
        }

        public void bindDrawable(Drawable drawable){
            mItemImageView.setImageDrawable(drawable);
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {

        private List<GalleryItem.PhotosBean.PhotoBean> mGalleryItems;

        public PhotoAdapter(List<GalleryItem.PhotosBean.PhotoBean> galleryItems) {
            mGalleryItems = galleryItems;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.gallery_item,parent,false);
            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
            GalleryItem.PhotosBean.PhotoBean galleryItem = mGalleryItems.get(position);
            Drawable placeholder = getResources().getDrawable(R.drawable.big_image);
            holder.bindDrawable(placeholder);
            mThumbnailDownloader.queueThumbnail(holder,galleryItem.getUrl_s());
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }

    private class FetchItemsTask extends AsyncTask<Void, Void, List<GalleryItem.PhotosBean.PhotoBean>> {

        @Override
        protected List<GalleryItem.PhotosBean.PhotoBean> doInBackground(Void... params) {

            return new FlickrFetchr().fetchItems(page++);

        }

        @Override
        protected void onPostExecute(List<GalleryItem.PhotosBean.PhotoBean> galleryItems) {
            mItems.addAll(galleryItems);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailDownloader.cleaQueue();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        page=0;
        mThumbnailDownloader.quit();
        Log.i(TAG, "Background thread destoryed");
    }
}
