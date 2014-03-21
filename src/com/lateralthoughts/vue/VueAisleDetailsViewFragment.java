package com.lateralthoughts.vue;

//generic android & java goodies
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.android.volley.toolbox.NetworkImageView;
import com.lateralthoughts.vue.ShareDialog.ShareViaVueClickedListner;
import com.lateralthoughts.vue.ui.AisleContentBrowser.AisleDetailSwipeListener;
import com.lateralthoughts.vue.user.VueUser;
import com.lateralthoughts.vue.utils.EditTextBackEvent;
import com.lateralthoughts.vue.utils.OnInterceptListener;
import com.lateralthoughts.vue.utils.Utils;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

//java utils

//as soon as this fragment attaches to the activity we will go ahead and pull up the
//the list of current trending aisles.
//when the result is received we parse it and coalesce it into an array list of 
//AisleWindowContent objects. At this point we are ready to setup the adapter for the
//mTrendingAislesContentView.

public class VueAisleDetailsViewFragment extends Fragment {
    private Context mContext;
    public static final String SCREEN_NAME = "DETAILS_SCREEN";
    public static final String SWIPE_LEFT_TO_RIGHT = "LEFT";
    public static final String SWIPE_RIGHT_TO_LEFT = "RIGHT";
    private static final int AISLE_HEADER_SHOW_TIME = 5000;
    private int mDotMaxCount = 10;
    private int mDotsCount = 0;
    private int mHighlightPosition;
    private int mTotalPageCount;
    private int mListCount = 3;
    AisleDetailsViewAdapterPager mAisleDetailsAdapter;
    private AisleDetailsSwipeListner mSwipeListener;
    private View mDetailsContentView = null;
    private ImageView mDotOne, mDotTwo, mDotThree, mDotFour, mDotFive, mDotSix,
            mDotSeven, mDotEight, mDotNine, mDotTen;
    private NetworkImageView mVueUserPic;
    private TextView mLeftArrow, mRightArrow, mVueUserName;
    private ListView mAisleDetailsList;
    EditTextBackEvent mEditTextFindAt;
    private LinearLayout mDetailsFindAtPopup;
    private LinearLayout mLikeStatusImageLay;
    private ImageView mLikeIconView;
    private TextView vueAisleHeading;
    private String mOccasion;
    private String mFindAtUrl;
    private LinearLayout mEditIconLay;
    private AisleDetailsViewActivity mAisleDetailsActivity = null;
    private InputMethodManager mInputMethodManager;
    int mAdapterNotifyDelay = 500;
    EditTextBackEvent edtCommentView;
    private MixpanelAPI mixpanel;
    
    // TODO: define a public interface that can be implemented by the parent
    // activity so that we can notify it with an ArrayList of AisleWindowContent
    // once we have received the result and parsed it. The idea is that the
    // activity
    // can then initiate a worker in the background to go fetch more content and
    // get
    // ready to launch other activities/fragments within the application
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
        // adding test comment
        // without much ado lets get started with retrieving the trending aisles
        // list
        mSwipeListener = new AisleDetailsSwipeListner();
        mAisleDetailsAdapter = new AisleDetailsViewAdapterPager(mContext,
                mSwipeListener, mListCount, null, new ShareViaVueListner());
        
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mixpanel = MixpanelAPI.getInstance(mContext,
                VueApplication.getInstance().MIXPANEL_TOKEN);
        // TODO: any particular state that we want to restore?
        
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mHighlightPosition = VueApplication.getInstance()
                .getmAisleImgCurrentPos();
        mDetailsContentView = inflater.inflate(
                R.layout.aisles_detailed_view_fragment, container, false);
        mTotalPageCount = VueApplication.getInstance().getClickedWindowCount();
        mDetailsFindAtPopup = (LinearLayout) mDetailsContentView
                .findViewById(R.id.detaisl_find_at_popup);
        mEditTextFindAt = (EditTextBackEvent) mDetailsContentView
                .findViewById(R.id.detaisl_find_at_text);
        mVueUserPic = (NetworkImageView) mDetailsContentView
                .findViewById(R.id.vue_user_pic);
        mEditIconLay = (LinearLayout) mDetailsContentView
                .findViewById(R.id.editImage);
        mLikeStatusImageLay = (LinearLayout) mDetailsContentView
                .findViewById(R.id.starImage);
        mLikeIconView = (ImageView) mDetailsContentView
                .findViewById(R.id.staricon);
        mEditIconLay.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                editAisle();
            }
        });
        
        String detailsUrl = null;
        try {
            if (VueApplication.getInstance().getPedningAisle() != null) {
                detailsUrl = VueApplication.getInstance().getPedningAisle()
                        .getImageList().get(0).mDetailsUrl;
            } else {
                detailsUrl = VueTrendingAislesDataModel
                        .getInstance(getActivity())
                        .getAisleItem(
                                VueApplication.getInstance()
                                        .getClickedWindowID())
                        .getImageList()
                        .get(VueApplication.getInstance()
                                .getmAisleImgCurrentPos()).mDetailsUrl;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (detailsUrl != null) {
            detailsUrl = Utils.getUrlFromString(detailsUrl);
            if (detailsUrl != null) {
                
                mEditTextFindAt.setText(detailsUrl);
                mFindAtUrl = detailsUrl;
            } else {
                mEditTextFindAt.setText("");
                mFindAtUrl = "";
            }
        } else {
            mEditTextFindAt.setText("");
            mFindAtUrl = "";
        }
        String profileUrl = null;
        if (VueApplication.getInstance().getPedningAisle() != null) {
            profileUrl = VueApplication.getInstance().getPedningAisle()
                    .getAisleContext().mAisleOwnerImageURL;
        } else {
            if (VueTrendingAislesDataModel.getInstance(getActivity())
                    .getAisleItem(
                            VueApplication.getInstance().getClickedWindowID()) != null
                    && VueTrendingAislesDataModel
                            .getInstance(getActivity())
                            .getAisleItem(
                                    VueApplication.getInstance()
                                            .getClickedWindowID())
                            .getAisleContext() != null) {
                profileUrl = VueTrendingAislesDataModel
                        .getInstance(getActivity())
                        .getAisleItem(
                                VueApplication.getInstance()
                                        .getClickedWindowID())
                        .getAisleContext().mAisleOwnerImageURL;
            } else {
                // Some times when the user returning from the other apps or
                // from the browser
                // apps lost data to avoid force close checking the current
                // ailse.
                getActivity().finish();
                
            }
        }
        if (profileUrl != null && profileUrl.length() > 5) {
            mVueUserPic.setImageUrl(profileUrl, VueApplication.getInstance()
                    .getImageCacheLoader(), VueApplication.getInstance()
                    .getPixel(32), VueApplication.getInstance().getPixel(32),
                    NetworkImageView.BitmapProfile.ProfileLandingView);
        }
        mEditTextFindAt.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                String url = null;
                if (mEditTextFindAt != null) {
                    url = mEditTextFindAt.getText().toString();
                }
                if (mEditTextFindAt.getVisibility() == View.VISIBLE) {
                    mDetailsFindAtPopup.setVisibility(View.GONE);
                    final InputMethodManager inputMethodManager = (InputMethodManager) getActivity()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.toggleSoftInputFromWindow(
                            mEditTextFindAt.getApplicationWindowToken(),
                            InputMethodManager.SHOW_FORCED, 0);
                }
                
                if (url != null && url.startsWith("http")) {
                    
                } else {
                    Toast.makeText(mContext, "No source url found",
                            Toast.LENGTH_SHORT).show();
                }
                
            }
        });
        
        mEditTextFindAt.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                    KeyEvent event) {
                final InputMethodManager inputMethodManager = (InputMethodManager) getActivity()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.toggleSoftInputFromWindow(
                        mEditTextFindAt.getApplicationWindowToken(),
                        InputMethodManager.SHOW_FORCED, 0);
                mDetailsFindAtPopup.setVisibility(View.GONE);
                return false;
            }
        });
        mEditTextFindAt.setonInterceptListen(new OnInterceptListener() {
            
            @Override
            public void setFlag(boolean flag) {
            }
            
            @Override
            public void onKeyBackPressed() {
                if (mEditTextFindAt.getVisibility() == View.VISIBLE) {
                    mDetailsFindAtPopup.setVisibility(View.GONE);
                    final InputMethodManager inputMethodManager = (InputMethodManager) getActivity()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.toggleSoftInputFromWindow(
                            mEditTextFindAt.getApplicationWindowToken(),
                            InputMethodManager.SHOW_FORCED, 0);
                }
                
            }
            
            @Override
            public boolean getFlag() {
                return false;
            }
        });
        mAisleDetailsList = (ListView) mDetailsContentView
                .findViewById(R.id.aisle_details_list);
        
        View footerView = ((LayoutInflater) getActivity().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.addcomment,
                null, false);
        mAisleDetailsList.addFooterView(footerView);
        
        final RelativeLayout enterComentStaticTextLay = (RelativeLayout) footerView
                .findViewById(R.id.entercmentrellay);
        final FrameLayout edtCommentFrameLay = (FrameLayout) footerView
                .findViewById(R.id.edtcommentlay);
        edtCommentView = (EditTextBackEvent) footerView
                .findViewById(R.id.edtcomment);
        final ImageView sendCommentButon = (ImageView) footerView
                .findViewById(R.id.sendcomment);
        final TextView textCount = (TextView) footerView
                .findViewById(R.id.textcount);
        enterComentStaticTextLay.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                mAisleDetailsList
                        .setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
                mAisleDetailsList.setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        edtCommentView.getParent()
                                .requestDisallowInterceptTouchEvent(false);
                        return false;
                    }
                });
                edtCommentView.setOnTouchListener(new OnTouchListener() {
                    
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        // to provide touch scroll for edit text
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        return false;
                    }
                });
                final InputMethodManager inputMethodManager = (InputMethodManager) getActivity()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.toggleSoftInputFromWindow(
                        edtCommentView.getApplicationWindowToken(),
                        InputMethodManager.SHOW_FORCED, 0);
                enterComentStaticTextLay.setVisibility(View.GONE);
                edtCommentView.requestFocus();
                mInputMethodManager = (InputMethodManager) getActivity()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                mInputMethodManager.showSoftInput(edtCommentView, 0);
                
                edtCommentFrameLay.setVisibility(View.VISIBLE);
                edtCommentView.setVisibility(View.VISIBLE);
                edtCommentView.setCursorVisible(true);
                edtCommentView.setTextColor(Color.parseColor(getResources()
                        .getString(R.color.black)));
                edtCommentView.requestFocus();
                edtCommentFrameLay.setVisibility(View.VISIBLE);
                edtCommentView.setonInterceptListen(new OnInterceptListener() {
                    
                    @Override
                    public void setFlag(boolean flag) {
                        
                    }
                    
                    @Override
                    public void onKeyBackPressed() {
                        if (edtCommentView.getText().toString().length() < 1) {
                            edtCommentView.setText("");
                            edtCommentFrameLay.setVisibility(View.GONE);
                            enterComentStaticTextLay
                                    .setVisibility(View.VISIBLE);
                            mInputMethodManager.hideSoftInputFromWindow(
                                    edtCommentView.getWindowToken(), 0);
                            // notify the adapter after keybord gone
                            new Handler().postDelayed(new Runnable() {
                                
                                @Override
                                public void run() {
                                    mAisleDetailsAdapter.mSetPager = false;
                                    mAisleDetailsAdapter.notifyDataSetChanged();
                                    mAisleDetailsAdapter.setmSetPagerToTrue();
                                }
                            }, mAdapterNotifyDelay);
                            return;
                        }
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                mContext);
                        alertDialogBuilder.setMessage(getResources().getString(
                                R.string.discard_comment));
                        alertDialogBuilder.setTitle("Discad message");
                        alertDialogBuilder.setPositiveButton("Discard",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                            int id) {
                                        dialog.cancel();
                                        edtCommentView.setText("");
                                        edtCommentFrameLay
                                                .setVisibility(View.GONE);
                                        enterComentStaticTextLay
                                                .setVisibility(View.VISIBLE);
                                        // notify the adapter after keybord gone
                                        new Handler().postDelayed(
                                                new Runnable() {
                                                    
                                                    @Override
                                                    public void run() {
                                                        mInputMethodManager
                                                                .hideSoftInputFromWindow(
                                                                        edtCommentView
                                                                                .getWindowToken(),
                                                                        0);
                                                        mAisleDetailsAdapter
                                                                .notifyDataSetChanged();
                                                        
                                                    }
                                                }, mAdapterNotifyDelay);
                                    }
                                });
                        alertDialogBuilder.setNegativeButton("Continue",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                            int id) {
                                        dialog.cancel();
                                        mInputMethodManager.showSoftInput(
                                                edtCommentView, 0);
 
                                        
                                    }
                                });
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }
                    
                    @Override
                    public boolean getFlag() {
                        
                        return false;
                    }
                });
                
            }
        });
        edtCommentView.setFocusable(true);
        edtCommentView.setImeOptions(EditorInfo.IME_ACTION_GO);
        edtCommentView.setScroller(new Scroller(getActivity()));
        edtCommentView.setVerticalScrollBarEnabled(true);
        edtCommentView.setMovementMethod(new ScrollingMovementMethod());
        edtCommentView.setVisibility(View.GONE);
        
        edtCommentView.addTextChangedListener(new TextWatcher() {
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                    int count) {
                if (s != null && s.length() >= 1) {
                    sendCommentButon.setVisibility(View.VISIBLE);
                    textCount.setText(s.length() + "");
                } else {
                    sendCommentButon.setVisibility(View.GONE);
                    textCount.setText("");
                }
            }
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {
                
            }
            
            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && s.length() >= 1) {
                    sendCommentButon.setVisibility(View.VISIBLE);
                    textCount.setText(s.length() + "");
                } else {
                    sendCommentButon.setVisibility(View.GONE);
                    textCount.setText("");
                }
            }
        });
        
        sendCommentButon.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                mixpanel.track("Added Comment", null);
                String etText = edtCommentView.getText().toString();
                
                if (etText != null && etText.length() >= 1) {
                    addComment(edtCommentView, enterComentStaticTextLay);
                }
            }
        });
        
        mAisleDetailsList.setAdapter(mAisleDetailsAdapter);
        try {
            SharedPreferences sharedPreferencesObj = getActivity()
                    .getSharedPreferences(VueConstants.SHAREDPREFERENCE_NAME, 0);
            boolean isHelpShown = sharedPreferencesObj.getBoolean(
                    VueConstants.HELP_SCREEN_ACCES, false);
            boolean isAlreadyDetailsScreenShown = sharedPreferencesObj
                    .getBoolean(VueConstants.IS_ALREADY_VIEWED_DETAILS_SCREEN,
                            false);
            if (isHelpShown && isAlreadyDetailsScreenShown) {
                int count = sharedPreferencesObj.getInt(
                        VueConstants.DETAILS_USER_FINDFRIENDS_OPEN_COUNT, 0);
                final int SHOW_LIMIT = 3;
                if (count < SHOW_LIMIT) {
                    long showedTime = sharedPreferencesObj.getLong(
                            VueConstants.DETAILS_USER_FINDFRIENDS_OPEN_TIME, 0);
                    int hours = (int) Utils.dateDifference(showedTime);
                    final int DAY_LATER = 24;
                    if (hours > DAY_LATER) {
                        showInviteFriendsDialog();
                    }
                }
                
            } else {
                Editor edit = sharedPreferencesObj.edit();
                edit.putBoolean(VueConstants.IS_ALREADY_VIEWED_DETAILS_SCREEN,
                        true);
                edit.commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        mVueUserName = (TextView) mDetailsContentView
                .findViewById(R.id.vue_user_name);
        mVueUserName.setTextSize(Utils.MEDIUM_TEXT_SIZE);
        final LinearLayout dotIndicatorBg = (LinearLayout) mDetailsContentView
                .findViewById(R.id.dot_indicator_bg);
        vueAisleHeading = (TextView) mDetailsContentView
                .findViewById(R.id.vue_aisle_heading);
        if (mOccasion != null) {
            vueAisleHeading.setText(mOccasion);
        }
        vueAisleHeading.setTextSize(Utils.LARGE_TEXT_SIZE);
        mDotOne = (ImageView) mDetailsContentView.findViewById(R.id.one);
        mDotTwo = (ImageView) mDetailsContentView.findViewById(R.id.two);
        mDotThree = (ImageView) mDetailsContentView.findViewById(R.id.three);
        mDotFour = (ImageView) mDetailsContentView.findViewById(R.id.four);
        mDotFive = (ImageView) mDetailsContentView.findViewById(R.id.five);
        mDotSix = (ImageView) mDetailsContentView.findViewById(R.id.six);
        mDotSeven = (ImageView) mDetailsContentView.findViewById(R.id.seven);
        mDotEight = (ImageView) mDetailsContentView.findViewById(R.id.eight);
        mDotNine = (ImageView) mDetailsContentView.findViewById(R.id.nine);
        mDotTen = (ImageView) mDetailsContentView.findViewById(R.id.ten);
        mLeftArrow = (TextView) mDetailsContentView
                .findViewById(R.id.leftarrow);
        mRightArrow = (TextView) mDetailsContentView
                .findViewById(R.id.rightarrow);
        if (mAisleDetailsList != null) {
            mAisleDetailsList.setOnScrollListener(new OnScrollListener() {
                public void onScrollStateChanged(AbsListView view,
                        int scrollState) {
                    switch (scrollState) {
                    case OnScrollListener.SCROLL_STATE_IDLE:
                        
                        break;
                    case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                        
                        break;
                    case OnScrollListener.SCROLL_STATE_FLING:
                        
                        break;
                    default:
                        break;
                    }
                }
                
                public void onScroll(AbsListView view, int firstVisibleItem,
                        int visibleItemCount, int totalItemCount) {
                    
                }
            });
        }
        mAisleDetailsList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
                if (arg2 != 0 && arg2 != 1) {
                    int maxLinesCount = 2;
                    closeKeyboard();
                    // will be called when press on the user comment,
                    // comment text will be expand and collapse for
                    // alternative clicks
                    TextView v = (TextView) arg1
                            .findViewById(R.id.vue_user_comment);
                    if (v == null) {
                        return;
                    }
                    
                    int x = v.getLineCount();
                    if (x <= maxLinesCount) {
                        LinearLayout.LayoutParams params;
                        params = new LinearLayout.LayoutParams(
                                LayoutParams.MATCH_PARENT,
                                LayoutParams.WRAP_CONTENT);
                        // get the pixel equivalent to given dp value
                        int leftMargin = VueApplication.getInstance().getPixel(
                                8);
                        int rightMargin = VueApplication.getInstance()
                                .getPixel(14);
                        int topBottomMargin = VueApplication.getInstance()
                                .getPixel(12);
                        params.setMargins(leftMargin, 0, rightMargin,
                                topBottomMargin);
                        v.setLayoutParams(params);
                        v.setMaxLines(Integer.MAX_VALUE);
                    } else {
                        v.setMaxLines(maxLinesCount);
                    }
                } else if (arg2 == 0) {
                    closeKeyboard();
                    // will be called when press on the description, description
                    // text will be expand and collapse for
                    // alternative clicks
                    // get the pixel equivalent to given dp value
                    int descriptionMaxCount = 3;
                    int leftRightMargin = VueApplication.getInstance()
                            .getPixel(8);
                    int topBottomMargin = VueApplication.getInstance()
                            .getPixel(6);
                    TextView v = (TextView) arg1
                            .findViewById(R.id.vue_details_descreption);
                    int x = v.getLineCount();
                    if (x == descriptionMaxCount) {
                        LinearLayout.LayoutParams params;
                        params = new LinearLayout.LayoutParams(
                                LayoutParams.MATCH_PARENT,
                                LayoutParams.WRAP_CONTENT);
                        params.setMargins(leftRightMargin, topBottomMargin,
                                leftRightMargin, topBottomMargin);
                        v.setMaxLines(Integer.MAX_VALUE);
                    } else {
                        
                        v.setMaxLines(descriptionMaxCount);
                    }
                }
            }
        });
        dotIndicatorBg.getBackground().setAlpha(50);
        new Handler().postDelayed(new Runnable() {
            
            @Override
            public void run() {
                dotIndicatorBg.setVisibility(View.GONE);
            }
        }, AISLE_HEADER_SHOW_TIME);
        RelativeLayout vueShareLayout = (RelativeLayout) mDetailsContentView
                .findViewById(R.id.vuesharelayout);
        final int shareDelay = 200;
        vueShareLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                closeKeyboard();
                // to smoothen the touch response
                new Handler().postDelayed(new Runnable() {
                    
                    @Override
                    public void run() {
                        mAisleDetailsAdapter
                                .share(getActivity(), getActivity());
                    }
                }, shareDelay);
                
            }
        });
        vueShareLayout.setOnLongClickListener(new OnLongClickListener() {
            
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(mContext, "Share", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        RelativeLayout vue_aislelayout = (RelativeLayout) mDetailsContentView
                .findViewById(R.id.vue_aislelayout);
        vue_aislelayout.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                mixpanel.track("Find At", null);
                String url = mFindAtUrl;
                if (url != null
                        && (url.startsWith("www") || url.startsWith("WWW"))) {
                    url = "http://" + url;
                }
                if (url != null && url.startsWith("http")) {
                    closeKeyboard();
                    Uri uriUrl = Uri.parse(url.trim());
                    
                    Intent launchBrowser = new Intent(Intent.ACTION_VIEW,
                            uriUrl);
                    startActivity(launchBrowser);
                } else {
                    Toast.makeText(mContext, "No source url found",
                            Toast.LENGTH_SHORT).show();
                }
                
            }
        });
        vue_aislelayout.setOnLongClickListener(new OnLongClickListener() {
            
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(mContext, "Find at", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        
        RelativeLayout detailsAddImageLayout = (RelativeLayout) mDetailsContentView
                .findViewById(R.id.details_add_image_to_aisle_layout);
        detailsAddImageLayout.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                if (!VueApplication.getInstance().mInstalledAppsLoadStatus) {
                    Toast.makeText(getActivity(),
                            "Please try again... Installed apps are loading.",
                            Toast.LENGTH_LONG).show();
                } else {
                    closeKeyboard();
                    // to smoothen the touch response
                    int addAilseDelay = 200;
                    new Handler().postDelayed(new Runnable() {
                        
                        @Override
                        public void run() {
                            addImageToAisle();
                        }
                    }, addAilseDelay);
                }
            }
        });
        detailsAddImageLayout.setOnLongClickListener(new OnLongClickListener() {
            
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(mContext, "Add image to aisle",
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        return mDetailsContentView;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        upDatePageDots(mHighlightPosition, "right");
        ViewTreeObserver vto = mVueUserName.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            
            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                mVueUserName.getViewTreeObserver()
                        .removeGlobalOnLayoutListener(this);
                mVueUserName.setText(mAisleDetailsAdapter.mVueusername);
                
            }
        });
    }
    
    public void notifyAdapter() {
        mAisleDetailsAdapter.notifyAdapter();
    }
    
    /**
     * 
     * 
     * while swiping the views inside the AisleContentWindow onAisleSwipe method
     * will be called to indicate the current position of the image.
     */
    private class AisleDetailsSwipeListner implements AisleDetailSwipeListener {
        @Override
        public void setOccasion(String occasion) {
            mOccasion = occasion;
        }
        
        @Override
        public void onAisleSwipe(String direction, int position,
                boolean editLayVisibility, boolean starLayVisibility,
                boolean isMostLikedImage) {
            upDatePageDots(position, direction);
            showEditIcon(editLayVisibility);
            showLikeStatus(starLayVisibility, isMostLikedImage);
        }
        
        @Override
        public void onUpdateLikeStatus(boolean editLayVisibility,
                boolean starLayVisibility, boolean isMostLikedImage) {
            showEditIcon(editLayVisibility);
            showLikeStatus(starLayVisibility, isMostLikedImage);
            
        }
        
        @Override
        public void onResetAdapter() {
            VueApplication.getInstance().saveTrendingRefreshTime(0);
            if (VueApplication.getInstance().getClickedWindowCount() != 0) {
                upDatePageDots(0, "right");
                mAisleDetailsAdapter = new AisleDetailsViewAdapterPager(
                        mContext, mSwipeListener, mListCount, null,
                        new ShareViaVueListner());
                mAisleDetailsList.setAdapter(mAisleDetailsAdapter);
            } else {
                getActivity().finish();
                Toast.makeText(mContext, "No images left in aisle",
                        Toast.LENGTH_SHORT).show();
            }
            
        }
        
        /**
         * when user enters the comment it will be added to the comment list at
         * the top.
         */
        @Override
        public void onAddCommentClick(final EditText editText,
                final ImageView sendComment, final FrameLayout edtCommentLay,
                int position, final TextView textCount) {
        }
        
        @Override
        public void onDissAllowListResponse() {
            mAisleDetailsList.requestDisallowInterceptTouchEvent(true);
        }
        
        @Override
        public void onAllowListResponse() {
            mAisleDetailsList.requestDisallowInterceptTouchEvent(false);
        }
        
        @Override
        public void setFindAtText(String findAt) {
            findAt = Utils.getUrlFromString(findAt);
            if (findAt != null) {
                
                mEditTextFindAt.setText(findAt);
                mFindAtUrl = findAt;
            } else {
                mEditTextFindAt.setText("");
                mFindAtUrl = "";
            }
        }
        
        @Override
        public void hasToShowEditIcon(boolean hasToShow) {
            
            if (hasToShow) {
                mEditIconLay.setVisibility(View.VISIBLE);
            } else {
                mEditIconLay.setVisibility(View.GONE);
            }
            
        }
        
        @Override
        public void onEditAisle() {
            editAisle();
            
        }
        
        @Override
        public void onCloseKeyBoard() {
            closeKeyboard();
            
        }
        
        @Override
        public void onImageAddEvent() {
            addImageToAisle();
            
        }
        
        @Override
        public void onReceiveImageCount(int count) {
            // TODO Auto-generated method stub
            
        }
        
        @Override
        public void finishScreen() {
            getActivity().finish();
        }
        
    }
    
    private void addComment(EditText editText, RelativeLayout view) {
        String etText = editText.getText().toString().trim();
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInputFromWindow(
                editText.getApplicationWindowToken(),
                InputMethodManager.SHOW_FORCED, 0);
        if (etText != null) {
            if (loginChcecking()) {
                mAisleDetailsAdapter.updateListCount(etText);
                mAisleDetailsAdapter.createComment(etText);
            }
        }
        editText.setVisibility(View.GONE);
        editText.setText("");
        view.setVisibility(View.VISIBLE);
        // notify the adapter after keybord gone
        new Handler().postDelayed(new Runnable() {
            
            @Override
            public void run() {
                mAisleDetailsAdapter.mSetPager = false;
                mAisleDetailsAdapter.notifyDataSetChanged();
                mAisleDetailsAdapter.setmSetPagerToTrue();
                
            }
        }, mAdapterNotifyDelay);
    }
    
    protected MotionEvent mLastOnDownEvent = null;
    
    public void changeLikeCount(int position, String clickType) {
        mAisleDetailsAdapter.changeLikesCountFromCopmareScreen(position,
                clickType);
    }
    
    public void setAisleContentListenerNull() {
        if (mAisleDetailsAdapter != null)
            mAisleDetailsAdapter.setAisleBrowserObjectsNull();
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mDetailsContentView = null;
        mAisleDetailsAdapter = null;
        mContext = null;
        
    }
    
    @Override
    public void onDestroy() {
        mAisleDetailsAdapter = null;
        super.onDestroy();
    }
    
    public void addAisleToWindow() {
        mAisleDetailsAdapter.addAisleToContentWindow();
        
    }
    
    public AisleContext getAisleContext() {
        
        return mAisleDetailsAdapter.getAisleContext();
        
    }
    
    private void showLeftArrow() {
        mLeftArrow.setVisibility(View.VISIBLE);
    }
    
    private void showRightArrow() {
        mRightArrow.setVisibility(View.VISIBLE);
    }
    
    private void disableLeftArrow() {
        mLeftArrow.setVisibility(View.GONE);
    }
    
    private void disableRightArrow() {
        mRightArrow.setVisibility(View.GONE);
    }
    
    public ArrayList<AisleImageDetails> getAisleWindowImgList() {
        return mAisleDetailsAdapter.getImageList();
    }
    
    private void upDatePageDots(int currentPosition, String direction) {
        int maxDotsCout = 10;
        mHighlightPosition = currentPosition % maxDotsCout;
        hightLightCurrentPage(mHighlightPosition);
        mTotalPageCount = VueApplication.getInstance().getClickedWindowCount();
        if (mTotalPageCount > mDotMaxCount) {
            
            if (currentPosition < mDotMaxCount) {
                
                mDotsCount = mDotMaxCount;
                showDots(mDotsCount);
                disableLeftArrow();
                showRightArrow();
                
            } else {
                showLeftArrow();
                int remainingDots = mTotalPageCount - currentPosition;
                if (currentPosition % maxDotsCout == 0) {
                    if (remainingDots > mDotMaxCount) {
                        showRightArrow();
                        showDots(mDotMaxCount);
                    } else {
                        disableRightArrow();
                        if (remainingDots == 0) {
                            showDots(1);
                        } else {
                            showDots(remainingDots);
                        }
                    }
                } else {
                    // Swiping from left to right...
                    if (direction.equalsIgnoreCase(SWIPE_LEFT_TO_RIGHT)) {
                        if (mRightArrow.getVisibility() == View.VISIBLE) {
                            showRightArrow();
                            showDots(mDotMaxCount);
                        } else {
                            disableRightArrow();
                            if (mTotalPageCount % maxDotsCout == 0) {
                                showDots(maxDotsCout);
                            } else {
                                showDots(mTotalPageCount % maxDotsCout);
                            }
                        }
                    } else if (direction.equalsIgnoreCase(SWIPE_RIGHT_TO_LEFT)) {
                        if ((currentPosition + 1) % maxDotsCout == 0) {
                            showDots(mDotMaxCount);
                            if (remainingDots > 1) {
                                showRightArrow();
                            }
                        } else {
                            if (mRightArrow.getVisibility() == View.VISIBLE) {
                                showRightArrow();
                                showDots(mDotMaxCount);
                            } else {
                                disableRightArrow();
                                if (mTotalPageCount % maxDotsCout == 0) {
                                    showDots(maxDotsCout);
                                } else {
                                    showDots(mTotalPageCount % maxDotsCout);
                                }
                            }
                        }
                    }
                }
            }
            
        } else {
            disableLeftArrow();
            disableRightArrow();
            showDots(mTotalPageCount);
        }
        
    }
    
    private void hightLightCurrentPage(int position) {
        if (position == 0) {
            mDotOne.setImageResource(R.drawable.active_dot);
        } else {
            mDotOne.setImageResource(R.drawable.inactive_dot);
        }
        if (position == 1) {
            mDotTwo.setImageResource(R.drawable.active_dot);
        } else {
            mDotTwo.setImageResource(R.drawable.inactive_dot);
        }
        if (position == 2) {
            mDotThree.setImageResource(R.drawable.active_dot);
        } else {
            mDotThree.setImageResource(R.drawable.inactive_dot);
        }
        if (position == 3) {
            mDotFour.setImageResource(R.drawable.active_dot);
        } else {
            mDotFour.setImageResource(R.drawable.inactive_dot);
        }
        if (position == 4) {
            mDotFive.setImageResource(R.drawable.active_dot);
        } else {
            mDotFive.setImageResource(R.drawable.inactive_dot);
        }
        if (position == 5) {
            mDotSix.setImageResource(R.drawable.active_dot);
        } else {
            mDotSix.setImageResource(R.drawable.inactive_dot);
        }
        if (position == 6) {
            mDotSeven.setImageResource(R.drawable.active_dot);
        } else {
            mDotSeven.setImageResource(R.drawable.inactive_dot);
        }
        if (position == 7) {
            mDotEight.setImageResource(R.drawable.active_dot);
        } else {
            mDotEight.setImageResource(R.drawable.inactive_dot);
        }
        if (position == 8) {
            mDotNine.setImageResource(R.drawable.active_dot);
        } else {
            mDotNine.setImageResource(R.drawable.inactive_dot);
        }
        if (position == 9) {
            mDotTen.setImageResource(R.drawable.active_dot);
        } else {
            mDotTen.setImageResource(R.drawable.inactive_dot);
        }
        
    }
    
    private void showLikeStatus(boolean showStarLay, boolean isMostLikeImage) {
        if (showStarLay) {
            mLikeStatusImageLay.setVisibility(View.VISIBLE);
            if (isMostLikeImage) {
                mLikeIconView.setImageResource(R.drawable.vue_star_light);
            } else {
                mLikeIconView.setImageResource(R.drawable.vue_star_theme);
            }
        } else {
            mLikeStatusImageLay.setVisibility(View.GONE);
        }
        
    }
    
    private void showEditIcon(boolean showEditIcon) {
        if (showEditIcon) {
            mEditIconLay.setVisibility(View.VISIBLE);
        } else {
            mEditIconLay.setVisibility(View.GONE);
        }
    }
    
    private void showDots(int dotsCount) {
        for (int i = 0; i < dotsCount; i++) {
            if (i == 0) {
                mDotOne.setVisibility(View.VISIBLE);
            } else if (i == 1) {
                mDotTwo.setVisibility(View.VISIBLE);
            } else if (i == 2) {
                mDotThree.setVisibility(View.VISIBLE);
            } else if (i == 3) {
                mDotFour.setVisibility(View.VISIBLE);
            } else if (i == 4) {
                mDotFive.setVisibility(View.VISIBLE);
            } else if (i == 5) {
                mDotSix.setVisibility(View.VISIBLE);
            } else if (i == 6) {
                mDotSeven.setVisibility(View.VISIBLE);
            } else if (i == 7) {
                mDotEight.setVisibility(View.VISIBLE);
            } else if (i == 8) {
                mDotNine.setVisibility(View.VISIBLE);
            } else if (i == 9) {
                mDotTen.setVisibility(View.VISIBLE);
            }
        }
        for (int i = dotsCount; i < mDotMaxCount; i++) {
            if (i == 0) {
                mDotOne.setVisibility(View.GONE);
            } else if (i == 1) {
                mDotTwo.setVisibility(View.GONE);
            } else if (i == 2) {
                mDotThree.setVisibility(View.GONE);
            } else if (i == 3) {
                mDotFour.setVisibility(View.GONE);
            } else if (i == 4) {
                mDotFive.setVisibility(View.GONE);
            } else if (i == 5) {
                mDotSix.setVisibility(View.GONE);
            } else if (i == 6) {
                mDotSeven.setVisibility(View.GONE);
            } else if (i == 7) {
                mDotEight.setVisibility(View.GONE);
            } else if (i == 8) {
                mDotNine.setVisibility(View.GONE);
            } else if (i == 9) {
                mDotTen.setVisibility(View.GONE);
            }
        }
        
    }
    
    public class ShareViaVueListner implements ShareViaVueClickedListner {
        @Override
        public void onAisleShareToVue() {
            ((AisleDetailsViewActivity) getActivity()).shareViaVueClicked();
        }
    }
    
    public void updateAisleScreen() {
        
        mAisleDetailsAdapter.updateAisleListAdapter();
    }
    
    public void editAisle() {
        if (mAisleDetailsActivity == null) {
            mAisleDetailsActivity = (AisleDetailsViewActivity) getActivity();
        }
        mAisleDetailsActivity.sendDataToDataentryScreen(null);
    }
    
    public void closeKeyboard() {
        final InputMethodManager mInputMethodManager = (InputMethodManager) mContext
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        mInputMethodManager.hideSoftInputFromWindow(
                edtCommentView.getWindowToken(), 0);
        mAisleDetailsAdapter.mSetPager = false;
        notifyAdapter();
        mAisleDetailsAdapter.setmSetPagerToTrue();
    }
    
    private void addImageToAisle() {
        Intent intent = new Intent(getActivity(),
                CreateAisleSelectionActivity.class);
        Utils.putFromDetailsScreenToDataentryCreateAisleScreenPreferenceFlag(
                getActivity(), true);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        Bundle b = new Bundle();
        b.putBoolean(
                VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_FLAG,
                true);
        intent.putExtras(b);
        if (!CreateAisleSelectionActivity.isActivityShowing) {
            CreateAisleSelectionActivity.isActivityShowing = true;
            getActivity()
                    .startActivityForResult(
                            intent,
                            VueConstants.FROM_DETAILS_SCREEN_TO_CREATE_AISLE_SCREEN_ACTIVITY_RESULT);
        }
    }
    
    private void showInviteFriendsDialog() {
        SharedPreferences sharedPreferencesObj = mContext.getSharedPreferences(
                VueConstants.SHAREDPREFERENCE_NAME, 0);
        int count = sharedPreferencesObj.getInt(
                VueConstants.DETAILS_USER_FINDFRIENDS_OPEN_COUNT, 0);
        count = count + 1;
        Editor edit = sharedPreferencesObj.edit();
        edit.putInt(VueConstants.DETAILS_USER_FINDFRIENDS_OPEN_COUNT, count);
        edit.putLong(VueConstants.DETAILS_USER_FINDFRIENDS_OPEN_TIME,
                System.currentTimeMillis());
        edit.putBoolean(VueConstants.DETAILS_HELP_SCREEN_ACCES, true);
        edit.commit();
        
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                new ContextThemeWrapper(getActivity(), R.style.AppBaseTheme));
        alertDialogBuilder
                .setTitle(getResources().getString(R.string.app_name));
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton("Skip for now",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        alertDialogBuilder.setNegativeButton("Invite Friends",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        if (mAisleDetailsActivity == null) {
                            mAisleDetailsActivity = (AisleDetailsViewActivity) getActivity();
                        }
                        mAisleDetailsActivity.mDrawerLayout
                                .openDrawer(mAisleDetailsActivity.mDrawerLeft);
                    }
                });
        ArrayList<String> hint_array_list = new ArrayList<String>();
        hint_array_list.add("1. Help Friends make their shopping decisions");
        hint_array_list.add("2. Invite them to join Vue");
        hint_array_list.add("3. Earn $$ rewards");
        ListView listview = new ListView(getActivity());
        ListAdapter adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.select_dialog_item, android.R.id.text1,
                hint_array_list);
        listview.setAdapter(adapter);
        alertDialogBuilder.setView(listview);
        Dialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        Editor edit2 = sharedPreferencesObj.edit();
        edit2.putBoolean(VueConstants.DETAILS_HELP_SCREEN_ACCES, true);
        edit2.commit();
    }
    
    private boolean loginChcecking() {
        SharedPreferences sharedPreferencesObj = mContext.getSharedPreferences(
                VueConstants.SHAREDPREFERENCE_NAME, 0);
        boolean isUserLoggedInFlag = sharedPreferencesObj.getBoolean(
                VueConstants.VUE_LOGIN, false);
        if (isUserLoggedInFlag) {
            VueUser storedVueUser = null;
            try {
                storedVueUser = Utils.readUserObjectFromFile(mContext,
                        VueConstants.VUE_APP_USEROBJECT__FILENAME);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            if (storedVueUser != null && storedVueUser.getId() != null) {
                return true;
            } else {
                Toast.makeText(
                        mContext,
                        mContext.getResources().getString(
                                R.string.vue_server_login_mesg),
                        Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(
                    mContext,
                    mContext.getResources().getString(
                            R.string.vue_fb_gplus_login_mesg),
                    Toast.LENGTH_LONG).show();
        }
        return false;
    }
}
