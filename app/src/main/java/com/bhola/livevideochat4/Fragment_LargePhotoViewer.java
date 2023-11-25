package com.bhola.livevideochat4;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.app.Fragment;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Map;


public class Fragment_LargePhotoViewer extends Fragment {

    Context context;
    View view;
    private ArrayList<Map<String, String>> imageList;
    private int adapterPosition;
    private int screenWidth;
    private int screenHeight;

    // Required empty public constructor
    public Fragment_LargePhotoViewer() {
    }

    // Create a new instance of MyFragment and pass data as arguments
    public static Fragment_LargePhotoViewer newInstance(ArrayList<Map<String, String>> imageList, int adapterPosition, int screenWidth, int screenHeight) {
        Fragment_LargePhotoViewer fragment = new Fragment_LargePhotoViewer();
        Bundle args = new Bundle();
        args.putSerializable("imageList", imageList);
        args.putInt("adapterPosition", adapterPosition);
        args.putInt("screenWidth", screenWidth);
        args.putInt("screenHeight", screenHeight);
        fragment.setArguments(args);
        return fragment;
    }

    public static Fragment_LargePhotoViewer newInstance(Context context, ArrayList<Map<String, String>> imageList, int absoluteAdapterPosition, int screenWidth, int screenHeight) {

        Fragment_LargePhotoViewer fragment = new Fragment_LargePhotoViewer();
        Bundle args = new Bundle();
        args.putSerializable("imageList", imageList);
        args.putInt("adapterPosition", absoluteAdapterPosition);
        args.putInt("screenWidth", screenWidth);
        args.putInt("screenHeight", screenHeight);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        fullscreenMode();
        getActivity().setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        setOrietationn();
        view = inflater.inflate(R.layout.fragment_large_photo_viewer, container, false);

        if (getArguments() != null) {
            imageList = (ArrayList<Map<String, String>>) getArguments().getSerializable("imageList");
            adapterPosition = getArguments().getInt("adapterPosition");
            screenWidth = getArguments().getInt("screenWidth");
            screenHeight = getArguments().getInt("screenHeight");
        }

        setupViewpager();
        context = getContext();
        return view;
    }

    private void setOrietationn() {

    }

    private void setupViewpager() {
        // Initialize ViewPager and its adapter
        ViewPager2 viewPager = view.findViewById(R.id.view_pager);
        ImageView cross = view.findViewById(R.id.cross);
        cross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager= ((Activity) context).getFragmentManager();
                fragmentManager.beginTransaction().remove(Fragment_LargePhotoViewer.this).commit();

                getActivity().setRequestedOrientation(
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        });
        ImagePagerAdapter pagerAdapter = new ImagePagerAdapter(context, imageList, screenWidth, screenHeight);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(adapterPosition);

    }


    private void fullscreenMode() {
        // Hide the status bar
        getActivity().getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

// Hide the navigation bar
        View decorView = getActivity().getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

    }

}

class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder> {

    private Context context;
    private ArrayList<Map<String, String>> imageUrls;
    private int screenWidth;
    private int screenHeight;


    public ImagePagerAdapter(Context context, ArrayList<Map<String, String>> imageUrls, int screenWidth, int screenHeight) {
        this.context = context;
        this.imageUrls = imageUrls;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image2, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {

        private PhotoView imageView;
        private LinearLayout lock;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            lock = itemView.findViewById(R.id.lock);
        }

        public void bind(int position) {

            Log.d("TAGA", "bind: "+imageUrls.get(position).get("url"));

            Picasso.get()
                    .load(imageUrls.get(position).get("url"))
                    .resize(screenWidth, 0)
                    .into(imageView);

            if (MyApplication.coins == 0) {
                if (imageUrls.get(position).get("type").equals("premium")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if(MyApplication.userLoggedIn && MyApplication.userLoggedIAs.equals("Google")){
                        imageView.setRenderEffect(RenderEffect.createBlurEffect(100, 100, Shader.TileMode.MIRROR));
                        }else{
                            imageView.setRenderEffect(RenderEffect.createBlurEffect(200, 200, Shader.TileMode.MIRROR));
                        }
                    }
                    lock.setVisibility(View.VISIBLE);
                    lock.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            lock.getContext().startActivity(new Intent(lock.getContext(), VipMembership.class));
                        }
                    });
                } else {
                    lock.setVisibility(View.GONE);
                }
            } else {
                lock.setVisibility(View.GONE);
            }
        }
    }

}