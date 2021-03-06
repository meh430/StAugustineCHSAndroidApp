package ca.staugustinechs.staugustineapp.Activities;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ca.staugustinechs.staugustineapp.AppUtils;
import ca.staugustinechs.staugustineapp.AsyncTasks.GetBadgesTask;
import ca.staugustinechs.staugustineapp.AsyncTasks.GetClubsTask;
import ca.staugustinechs.staugustineapp.AsyncTasks.GetUserTask;
import ca.staugustinechs.staugustineapp.DialogFragments.ShowScheduleDialog;
import ca.staugustinechs.staugustineapp.Interfaces.BadgeGetter;
import ca.staugustinechs.staugustineapp.Interfaces.UserGetter;
import ca.staugustinechs.staugustineapp.Objects.Badge;
import ca.staugustinechs.staugustineapp.Objects.ClubItem;
import ca.staugustinechs.staugustineapp.Objects.UserProfile;
import ca.staugustinechs.staugustineapp.R;
import ca.staugustinechs.staugustineapp.RVAdapters.RViewAdapter_Badges;
import ca.staugustinechs.staugustineapp.RVAdapters.RViewAdapter_Clubs;

public class Profile extends AppCompatActivity implements UserGetter, BadgeGetter, View.OnClickListener {

    private LinearLayout layout;
    private ImageView profilePic;
    private TextView profileName;
    private Button profileSchedule;
    private SearchView searchView;
    private MenuItem searchItem;
    private RecyclerView rv, rv2;
    private LinearLayout profileClubsGroup, badgesGroup;
    private ProgressBar loadingCircle;
    private SwipeRefreshLayout profileRefresh;

    private UserProfile user;
    private List<ClubItem> clubs;
    private List<Badge> badges;
    private List<Integer> classesInCommon;
    private boolean isMainUser = false, BACK_ENABLED = true;

    private GetUserTask userTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_profile);

        //SET STATUS BAR COLOR
        getWindow().setNavigationBarColor(AppUtils.PRIMARY_DARK_COLOR);
        getWindow().setStatusBarColor(AppUtils.PRIMARY_DARK_COLOR);

        //SET ACTIVITY TITLE AND COLOR
        this.getSupportActionBar().setTitle("Profile");
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setBackgroundDrawable(new ColorDrawable(AppUtils.PRIMARY_COLOR));

        if(AppUtils.isNetworkAvailable(this)){
            //KEEP A REFERENCE OF ALL THE VIEWS
            loadingCircle = findViewById(R.id.pLoadingCircle);
            loadingCircle.getIndeterminateDrawable().setTint(AppUtils.ACCENT_COLOR);
            layout = findViewById(R.id.profileItems);

            profileName = (TextView) findViewById(R.id.profileName);
            profileName.setTextColor(AppUtils.PRIMARY_COLOR);

            badgesGroup = findViewById(R.id.pBadgesGroup);
            profileClubsGroup = (LinearLayout) findViewById(R.id.profileClubsGroup);

            profilePic = (ImageView) findViewById(R.id.profilePic);
            profilePic.setOnClickListener(this);

            profileSchedule = (Button) findViewById(R.id.profileSchedule);
            profileSchedule.setTextColor(AppUtils.PRIMARY_COLOR);
            profileSchedule.setOnClickListener(this);

            profileRefresh = findViewById(R.id.profileRefresh);
            profileRefresh.setColorSchemeColors(AppUtils.ACCENT_COLOR);
            profileRefresh.setEnabled(false);

            View profileUserGroup = findViewById(R.id.profileUserGroup);
            profileUserGroup.setBackgroundColor(AppUtils.PRIMARY_COLOR);
            View profileDivider1 = findViewById(R.id.profileDivider1);
            profileDivider1.setBackgroundColor(AppUtils.PRIMARY_COLOR);
            TextView profileBadgesHeader = findViewById(R.id.profileBadgesHeader);
            profileBadgesHeader.setTextColor(AppUtils.PRIMARY_COLOR);
            View profileDivider2 = findViewById(R.id.profileDivider2);
            profileDivider2.setBackgroundColor(AppUtils.PRIMARY_COLOR);
            TextView profileClubsHeader = findViewById(R.id.profileClubsHeader);
            profileClubsHeader.setTextColor(AppUtils.PRIMARY_COLOR);

            ////RECYCLER VIEWS////
            //BADGES
            rv = (RecyclerView) findViewById(R.id.rv2);
            rv.setHasFixedSize(true);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this){
                @Override
                public boolean canScrollVertically() {
                    return false;
                }

                @Override
                public boolean canScrollHorizontally() {
                    return false;
                }
            };
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            rv.setLayoutManager(layoutManager);

            //CLUBS
            rv2 = (RecyclerView) findViewById(R.id.rv);
            rv2.setHasFixedSize(true);
            LinearLayoutManager layoutManager2 = new LinearLayoutManager(this){
                @Override
                public boolean canScrollVertically() {
                    return false;
                }
            };
            rv2.setLayoutManager(layoutManager2);
            
            //IF PREVIOUS CLASS HASN'T PASSED DOWN A USER TO LOAD, LOAD THE
            //CURRENT USER'S PROFILE
            String userEmail = getIntent().getExtras().getString("USER_EMAIL");
            if(userEmail.isEmpty()){
                //LOAD USER'S PROFILE
                updateProfile(Arrays.asList(Main.PROFILE));
            }else{
                //SEARCH UP USER AND LOAD THEIR PROFILE
                BACK_ENABLED = false;
                searchProfile(userEmail, false);
            }
        }else{
            setOffline();
        }
    }

    @Override
    public void updateProfile(List<UserProfile> users){
        if(users != null && !users.isEmpty()){
            this.user = users.get(0);
            //IS THE USER WE HAVE SEARCHED UP THE CURRENT USER?
            isMainUser = user.getEmail().equals(Main.PROFILE.getEmail());

            if(Main.PROFILE == null && isMainUser){
                Main.PROFILE = user;
            }

            //GET USER'S CLUBS
            if(user.getClubs() != null && !user.getClubs().isEmpty()){
                if(user.showClubs() || isMainUser){
                    this.clubs = null;
                    GetClubsTask clubsTask = new GetClubsTask(this);
                    clubsTask.execute(user.getUid());
                }else{
                    this.clubs = new ArrayList<ClubItem>();
                }
            }else{
                this.clubs = new ArrayList<ClubItem>();
            }

            //GET USER'S BADGES
            if(user.getBadges() != null && !user.getBadges().isEmpty()){
                this.badges = null;
                GetBadgesTask task = new GetBadgesTask(this);
                task.execute();
            }else{
                this.badges = new ArrayList<Badge>();
                setViews();
            }
        }else{
            //COULDN'T FIND THE USER THAT WAS SEARCHED FOR
            Snackbar.make(profilePic, "Couldn't Find User :/", Snackbar.LENGTH_LONG).show();
            profileRefresh.setRefreshing(false);
        }
    }

    @Override
    public void updateBadges(List<Badge> badges) {
        //CALLED WHEN USER'S BADGES HAVE BEEN FETCHED
        if(badges != null){
            this.badges = badges;
            setViews();
        }
    }
    
    public void updateClubs(List<ClubItem> clubs){
        //CALLED WHEN USER'S CLUBS HAVE BEEN FETCHED
        if(clubs != null){
            this.clubs = clubs;
            setViews();
        }
    }

    public void setViews(){
        if(clubs != null && badges != null && user.getIcon() != null){
            //CHANGE USER'S NAME COLOR
            changeNameColor();

            //CHANGE USER'S PROFILE PICTURE
            profilePic.setImageBitmap(user.getIcon().getImg());
            profileName.setText(user.getName());

            //SHOW USER'S BADGES
            RViewAdapter_Badges adapter = new RViewAdapter_Badges(badges, this);
            rv.setAdapter(adapter);

            //SHOW USER'S CLUBS
            RViewAdapter_Clubs adapter2 = new RViewAdapter_Clubs(clubs, this, true);
            rv2.setAdapter(adapter2);

            if(isMainUser){
                //NO CLASSES IN COMMON WITH OURSELVES
                classesInCommon = null;
                profileSchedule.setText("View Your Schedule");
                profileSchedule.setVisibility(View.VISIBLE);

                if(!clubs.isEmpty()){
                    profileClubsGroup.setVisibility(View.VISIBLE);
                }else{
                    profileClubsGroup.setVisibility(View.GONE);
                }
            }else{
                if(user.showClasses()){
                    //IF THE SEARCHED FOR USER HAS ALLOWED HIS CLASSES TO BE SEEN BY OTHER USERS
                    //ALLOW HIS SCHEDULE TO BE SEEN AN FIND THE CLASSES WE HAVE IN COMMON WITH THEM
                    getClassesInCommon();
                    if(classesInCommon.size() == 0){
                        profileSchedule.setText("No Classes in Common");
                    }else if(classesInCommon.size() == 1){
                        profileSchedule.setText("1 Class in Common");
                    }else{
                        profileSchedule.setText(classesInCommon.size() + " Classes in Common");
                    }
                    profileSchedule.setVisibility(View.VISIBLE);
                }else{
                    profileSchedule.setVisibility(View.GONE);
                }

                //IF USER HAS CHOSEN TO SHOW THEIR CLUBS, DO SO
                if(user.showClubs() && !clubs.isEmpty()){
                    profileClubsGroup.setVisibility(View.VISIBLE);
                }else{
                    profileClubsGroup.setVisibility(View.GONE);
                }
            }

            //IF USER HAS BADGES, SHOW THEM
            if(!badges.isEmpty()){
                badgesGroup.setVisibility(View.VISIBLE);
            }else{
                badgesGroup.setVisibility(View.GONE);
            }

            //FINISH LOADING
            loadingCircle.setVisibility(View.GONE);
            layout.setVisibility(View.VISIBLE);

            profileRefresh.setRefreshing(false);
            profileRefresh.setEnabled(false);

            //SCROLL UP
            NestedScrollView scrollView = findViewById(R.id.profileScroll);
            scrollView.requestFocus();
            scrollView.fullScroll(View.FOCUS_UP);
        }
    }

    public void setOffline(){
        //REMOVE ALL VIEWS AND SHOW OFFLINE VIEW
        profileRefresh.setRefreshing(false);
        LinearLayout layout = findViewById(R.id.profileLayout);
        layout.removeAllViews();
        View offline = getLayoutInflater().inflate(R.layout.offline_layout, null);
        layout.addView(offline);
    }

    private void getClassesInCommon(){
        //LOOP THROUGH USER'S CLASSES TO FIND THE ONES THEY HAVE IN COMMON
        //DURING THE SAME TIMES
        classesInCommon = new ArrayList<Integer>();
        for(int i = 0; i < 8; i++){
            if(Main.PROFILE.getSchedule().get(i).equals(user.getSchedule().get(i))){
                classesInCommon.add(i);
            }
        }
    }

    @TargetApi(26)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //SETUP SEARCH BAR
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.profile_options, menu);

        searchItem = menu.findItem(R.id.action_search);

        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
            if (searchView != null) {
                //SET SEARCH BAR HINT
                searchView.setLayoutParams(new ActionBar.LayoutParams(Gravity.RIGHT));
                searchView.setQueryHint("YCDSBK12 Email");
                if(Build.VERSION.SDK_INT > 25){
                    searchView.setAutofillHints(SearchView.AUTOFILL_HINT_EMAIL_ADDRESS);
                    searchView.setTooltipText("YCDSBK12 Email");
                }

                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String s) {
                        //HIDE SEARCH BAR WHEN USER CLICKS SEARCH
                        if (!searchView.isIconified()) {
                            searchView.setIconified(true);
                        }
                        searchItem.collapseActionView();

                        //SEARCH FOR THE USER
                        searchProfile(s.toLowerCase(), true);

                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String s) {
                        return false;
                    }
                });

                searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if(!hasFocus){
                            //HIDE SEARCH BAR WHEN NO LONGER IN FOCUS
                            if(!searchView.isIconified()) {
                                searchView.setIconified(true);
                            }
                            searchItem.collapseActionView();
                        }
                    }
                });
            }
        }

        return super.onCreateOptionsMenu(menu);
    }

    public void searchProfile(String email, boolean showRefresh){
        //IF SEARCHED FOR USER DOESN'T CONTAIN "@YCDSBK12.CA" AT THE END, ADD IT IN
        if(!email.contains("@ycdsb")){
            email += "@ycdsbk12.ca";
        }

        //GET USER
        userTask = new GetUserTask(this, email);
        userTask.execute();

        //SHOW LOADING CIRCLE
        profileRefresh.setEnabled(showRefresh);
        profileRefresh.setRefreshing(showRefresh);
    }

    //IN THE FUTURE, THIS METHOD CAN EASILY BE MODIFIED TO ALLOW FOR PROFILE CUSTOMIZATION :D
    private void changeNameColor(){
        View profileUserGroup = findViewById(R.id.profileUserGroup);
        if(user.getStatus() == Main.DEV){
            //PICK RANDOM COLOR
            profileName.setTextColor(Color.rgb((int) (Math.random() * 255),
                    (int) (Math.random() * 255), (int) (Math.random() * 255)));
            //SET PROFILE NAME BACKGROUND COLOR TO SOMETHING COOL
            profileName.setBackgroundColor(0x00000000);
            profileUserGroup.setBackgroundColor(AppUtils.STATUS_TWO_COLOR);

            //CHANGE THE COLOR TO SOME OTHER RANDOM COLOR EVERY 100 MILLISECONDS
            profileName.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(!Profile.this.isDestroyed() && !profileRefresh.isRefreshing()){
                        changeNameColor();
                    }
                }
            }, 100L);
        }else{
            //IF THE USER IS NOT AN APP DEV, SET THEIR COLOR TO SOMETHING BORING :(
            profileName.setTextColor(AppUtils.PRIMARY_COLOR);
            profileName.setBackgroundColor(Color.WHITE);
            profileUserGroup.setBackgroundColor(AppUtils.PRIMARY_COLOR);
        }
    }

    public UserProfile getProfile(){
        return user;
    }

    @Override
    protected void onDestroy() {
        //WHEN ACTIVITY GETS DESTROYED, CANCEL RUNNING TASKS
        if(userTask != null){
            userTask.cancel(true);
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        //IF THE BACK BUTTON IS PRESSED AND WE HAVE LOOKED A USER UP,
        //GO BACK TO SHOWING OUR PROFILE
        if(!isMainUser && BACK_ENABLED){
            profileRefresh.setEnabled(true);
            profileRefresh.setRefreshing(true);
            updateProfile(Arrays.asList(Main.PROFILE));
        }else{
            //OTHERWISE JUST EXIT THE ACTIVITY
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View v) {
        if(v.equals(profileSchedule)){
            //SHOW SCHEDULE DIALOG
            ShowScheduleDialog showScheduleDialog = new ShowScheduleDialog();
            showScheduleDialog.setProfile(this);
            showScheduleDialog.setClassesInCommon(classesInCommon);
            showScheduleDialog.show(this.getSupportFragmentManager(), "showScheduleDialog");
        }else if(v.equals(profilePic)){
            if(isMainUser){
                //GO TO PROFILE PICTURE SHOP ACTIVITY
                Intent intent = new Intent(this, IconSelect.class);
                this.startActivity(intent);
            }
        }
    }

    @Override
    public void onResume(){
        //UPDATE PROFILE ICON IF IT HAS CHANGED SINCE THE LAST TIME THIS
        //ACTIVITY WAS ACTIVE (EX. THE USER HAS CHANGED THEIR PROFILE PIC
        //IN THE ICON SELECT ACTIVITY AND HAS COME BACK TO THE PROFILE ACTIVITY)
        if(Main.UPDATE_ICON){
            this.user.setLocalIcon(Main.PROFILE.getIcon());
            setViews();
        }
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //FINISH ACTIVITY WHEN BACK BUTTON ON TOP LEFT IS PRESSED
        switch(item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
