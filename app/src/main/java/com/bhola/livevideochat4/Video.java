package com.bhola.livevideochat4;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import java.io.IOException;

public class Video extends AppCompatActivity {

    private VideoView videoView;
    private RelativeLayout playerLayout;
    int videoView_Height;


    //    String videoPath = Utils.decryption("myyux?44knwjgfxjxytwflj3lttlqjfunx3htr4{54g4qn{j2{nijt2hmfy2;<6593fuuxuty3htr4t4Ijjunpf*75Sfnw3ru9DfqyBrjinf+ytpjsB;<:=k9f<2<i=<29:;82=87i2k8<;7j>6=ijf"); // Replace with your actual video URL
//    String videoPath = Utils.decryption("myyux?44knwjgfxjxytwflj3lttlqjfunx3htr4{54g4qn{j2{nijt2hmfy2;<6593fuuxuty3htr4t4Pf{nyf*75Rfqmtywf3ru9DfqyBrjinf+ytpjsBj<ik<fik2j79g29==>2>h562=:hhk77fkk;5"); // Replace with your actual video URL
//    String videoPath = Utils.decryption("myyux?44knwjgfxjxytwflj3lttlqjfunx3htr4{54g4qn{j2{nijt2hmfy2;<6593fuuxuty3htr4t4Rf~f*75Lzuyf3ru9DfqyBrjinf+ytpjsBk<hhfjh;29=ig2957=2>:<k2kh<jk;h=6h:="); // Replace with your actual video URL
    String videoPath = "https://player.vimeo.com/external/477296956.sd.mp4?s=526c9ea3c3740e4375a1ac7f513a4afd56f844c1&profile_id=165&oauth2_token_id=57447761"; // Replace with your actual video URL

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);


        videoView = findViewById(R.id.videoView);
        playerLayout = findViewById(R.id.player);

        // Get the height of the VideoView area before playing the video
        playerLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int videoViewHeight = playerLayout.getHeight();
                int videoViewWidth = playerLayout.getWidth();

                // Remove the listener to avoid redundant calls
                playerLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                videoView_Height = videoViewHeight;

            }
        });


        // Set the video source
        Uri videoUri = Uri.parse(videoPath);
        videoView.setVideoURI(videoUri);

        // Start playing the video

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {

                mp.setVolume(0f, 0f);
                mp.setLooping(true);

                // Calculate the video's original dimensions
                int videoWidth = mp.getVideoWidth();
                int videoHeight = mp.getVideoHeight();

                // Calculate the view's dimensions
                int viewWidth = videoView.getWidth();
                int viewHeight = videoView.getHeight();

                float scale = (float) videoView_Height / viewHeight;


                videoView.setScaleY(scale);
                videoView.setScaleX(scale);


                videoView.start();


            }
        });


    }


}