package com.bhola.livevideochat4;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

public class Fragment_UserProfile extends Fragment {


    ImageView profileImage;
    TextView name, coins, id;
    LinearLayout logout;
    View view;
    Context context;


    public Fragment_UserProfile() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        view = inflater.inflate(R.layout.fragment_user__profile, container, false);

        context = getContext();


        setProfileDetails();


        LinearLayout memberShip = view.findViewById(R.id.memberShip);
        memberShip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(context, VipMembership.class));
            }
        });

        oprnPrivacy_Terms(view, context);

        LinearLayout about = view.findViewById(R.id.about);
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, About.class));
            }
        });

        profileEdit();

        notificationBar();


        return view;
    }

    private void setProfileDetails() {
        SharedPreferences sh = context.getSharedPreferences("UserInfo", MODE_PRIVATE);

        profileImage = view.findViewById(R.id.profileUrl);
        name = view.findViewById(R.id.profileName);
        coins = view.findViewById(R.id.coins);
        id = view.findViewById(R.id.id);
        int userId = sh.getInt("userId", 0);

        id.setText(String.valueOf(userId));
        coins.setText(String.valueOf("Coins: " + SplashScreen.coins));
        logout = view.findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GoogleSignInOptions gso;
                GoogleSignInClient gsc;
                if (SplashScreen.userLoggedIAs.equals("Google")) {
                    gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
                    gsc = GoogleSignIn.getClient(context, gso);
                    gsc.signOut().addOnCompleteListener(new com.google.android.gms.tasks.OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                            FirebaseAuth.getInstance().signOut();
                            clearUserInfo();
                        }
                    });

                } else {
                    clearUserInfo();
                }


                Toast.makeText(context, "Logged Out!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(context, LoginScreen.class));

            }
        });

        String urll = sh.getString("photoUrl", "");
        String Gender = sh.getString("Gender", "");
        String Birthday = sh.getString("Birthday", "");

        try {
            int age = new Utils().calculateAge(Birthday);
            TextView ageText = view.findViewById(R.id.ageText);
            ageText.setText(String.valueOf(age));
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }


        if (Gender.equals("male")) {
            ImageView genderIcon = view.findViewById(R.id.genderIcon);
            genderIcon.setImageResource(R.drawable.male);
            CardView genderCard = view.findViewById(R.id.genderCard);
            genderCard.setCardBackgroundColor(getResources().getColor(R.color.male_icon)); // Replace with your color resource ID
        }

        TextView location = view.findViewById(R.id.location);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                location.setText(SplashScreen.currentCity);
            }
        }, 2000);


        if (SplashScreen.userLoggedIn) {

            String fullname = sh.getString("nickName", "not set");
            name.setText(fullname);

            if (urll.startsWith("http")) {

                Picasso.get()
                        .load(urll)
                        .into(profileImage);
            } else {
                if (urll.length() > 0) {
                    profileImage.setImageURI(Uri.parse(urll));
                }
            }

        }


    }


    private void notificationBar() {
        Fragment_Messenger.updateUnreadmessageCount(context);
        CardView notificationCard = view.findViewById(R.id.notificationCard);
        if (Fragment_Messenger.count == 0) {
            notificationCard.setVisibility(View.INVISIBLE);
        } else {
            notificationCard.setVisibility(View.VISIBLE);

        }
        ImageView bellIcon = view.findViewById(R.id.bellIcon);
        ImageView crossIcon = view.findViewById(R.id.crossIcon);
        TextView notification_message = view.findViewById(R.id.notification_message);

        Animation scaleAnimation = AnimationUtils.loadAnimation(context, R.anim.scale_up_down);
        bellIcon.startAnimation(scaleAnimation);

        crossIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notificationCard.setVisibility(View.INVISIBLE);
            }
        });

        notification_message.setText(String.valueOf(Fragment_Messenger.count) + " new messages,click to read!");
        notification_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.viewPager2.setCurrentItem(2); // Switch to Fragment B

            }
        });
    }

    private void profileEdit() {
        TextView edit = view.findViewById(R.id.edit);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, UserProfileEdit.class));
            }
        });
    }

    private void oprnPrivacy_Terms(View view, Context context) {
        LinearLayout terms = view.findViewById(R.id.terms);
        terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(context, Terms_Conditions.class));
            }
        });

        LinearLayout privacy = view.findViewById(R.id.privacy);
        privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(context, PrivacyPolicy.class));
            }
        });
    }

    private void clearUserInfo() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserInfo", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

// Clear the SharedPreferences
        editor.clear();
        editor.apply();

    }

}