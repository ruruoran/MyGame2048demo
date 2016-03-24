package com.mer.mygame2048_complete;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mer.View.GameView;

public class Home extends ActionBarActivity implements View.OnClickListener {

    private GameView mGameView;
    Button restart;
    Button revert;
    Button option;

    private static Home mActivity;
    private TextView tv_home_score;
    private TextView tv_home_record;
    private TextView tv_home_target;
    private MyApplication application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.i("home", "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mActivity=this;

        RelativeLayout  rl_home_center = (RelativeLayout) findViewById(R.id.rl_home_center);

        mGameView = new GameView(this);

        rl_home_center.addView(mGameView);

        //初始化button
        restart = (Button) findViewById(R.id.restart);
        revert = (Button) findViewById(R.id.revert);
        option = (Button) findViewById(R.id.option);
        restart.setOnClickListener(this);
        revert.setOnClickListener(this);
        option.setOnClickListener(this);

        //初始化显示当前分数的tv
        tv_home_score = (TextView) findViewById(R.id.tv_home_score);
        tv_home_record = (TextView) findViewById(R.id.tv_home_record);
        tv_home_target = (TextView) findViewById(R.id.tv_home_target);

        application = (MyApplication) getApplication();

        //此处有坑。
        tv_home_target.setText(application.getTarget()+"");
        tv_home_record.setText(application.getHighestRecord()+"");

        Log.i("Tag", application.geti() + "");
    }


    public static Home getActivity(){

        return mActivity;
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.revert:
                revert();
                break;

            case R.id.restart:
                restart();
                break;


            case R.id.option:
                option();
                break;
        }

    }

    public void option() {

        startActivityForResult(new Intent(this, OptionActivity.class), 100);
    }

    private void restart() {

        new AlertDialog.Builder(this)
                .setTitle("确认")
                .setMessage("真的要重新开始吗？")
                .setPositiveButton("是的", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mGameView.restart();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();


    }

    private void revert() {
        mGameView.revert();
    }


    public void updateCurrentScore(int score){

        tv_home_score.setText(score + "");

    }

    public void updateHighestScore(int score){

        tv_home_record.setText(score+"");

    }

    public void updateTargetScore(int score){

        tv_home_target.setText(score+"");

    }


        //
       /* mGameView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return false;
            }
        });
*/
       /* GridLayout gl_home_content = (GridLayout) findViewById(R.id.gl_home_content);

        WindowManager wm   = (WindowManager) getSystemService(WINDOW_SERVICE);

        final Display defaultDisplay = wm.getDefaultDisplay();

        //之前
*//*        int height =  defaultDisplay.getHeight();
        int width=  defaultDisplay.getWidth();
        Log.i("home",height+":"+  width+"");*//*

        //之后
        DisplayMetrics metrix = new DisplayMetrics();
        defaultDisplay.getMetrics(metrix);
        int width2= metrix.widthPixels;

        Log.i("home",width2+"");



        //初始化中间的内容布局

        for (int i=0;i<4;i++)
            for (int j=0;j<4;j++){


                 NumberItem item = new NumberItem(this,0);
                 //这里的50 应该变成动态获取屏幕宽度，然后除以gridlayout 的列数
                 gl_home_content.addView(item,width2/4,width2/4); //可以指定增加的子控件宽，高

            }
*/



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i("Tag","onActivityResult");
//        if (requestCode==100&&resultCode==RESULT_CANCELED){

        updateTargetScore(application.getTarget());
        mGameView.restart();
        Log.i("Tag", "onActivityResult restart");

//        }


    }
}

