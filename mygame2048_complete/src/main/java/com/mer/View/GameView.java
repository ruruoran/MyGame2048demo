package com.mer.View;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.GridLayout;

import com.mer.mygame2048_complete.Home;
import com.mer.mygame2048_complete.MyApplication;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mer on 2016/3/22.
 */
public class GameView extends GridLayout {
    private static final String TAG = "GameView";
    private int mCollomNumber ;
    private int mRowNumber ;
    private  int mTarget;
    private Home mHome;


    //用于计算上下左右滑动
    float startx=0;
    float starty=0;
    float stopx;
    float stopy;

    //用于计算滑动之后每行或者每列合并后的数组

    List<Integer> caculorList;

    private NumberItem[][] mNumberItemMatrix;

    //记录上一步操作的矩阵
    private int[][]  histroyMatrix;

    private List<Point> blanklist;

    //决定是否可以撤销的标志位
    boolean canRevert =false;


    //保存当前分数的一个成员变量
    private int currentScore;
    private int HighestScore;
    SharedPreferences sp;
    private int width;


    public GameView(Context context) {
        super(context);
        init();//不管从哪个构造方法进来  都要 经过自定义初始化init()
    }

        public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    private void init() {

        //得到当前Activity的引用
        mHome =Home.getActivity();
        MyApplication app = (MyApplication) mHome.getApplication();
        mRowNumber=app.getLineNumber();
        mCollomNumber=mRowNumber;
        mTarget=app.getTarget();
        canRevert=false;
        blanklist= new ArrayList<Point>();
        caculorList = new ArrayList<Integer>();
        mNumberItemMatrix= new NumberItem[mRowNumber][mCollomNumber];

        histroyMatrix=new int[mRowNumber][mCollomNumber];

        currentScore=0;
        sp=getContext().getSharedPreferences("config",getContext().MODE_PRIVATE);
        HighestScore=sp.getInt("HighestScore",0);

        WindowManager wm   = (WindowManager) getContext().getSystemService(getContext().WINDOW_SERVICE);

        final Display defaultDisplay = wm.getDefaultDisplay();

        //之前
//      int height =  defaultDisplay.getHeight();
//      int width=  defaultDisplay.getWidth();

        //之后
        DisplayMetrics metrix = new DisplayMetrics();
        defaultDisplay.getMetrics(metrix);
        int width= metrix.widthPixels;


        setRowCount(mRowNumber);
        setColumnCount(mCollomNumber);

        for (int i=0;i<mRowNumber;i++)
            for (int j=0;j<mCollomNumber;j++){
                NumberItem item = new NumberItem(getContext(),0);
                //这里的50 应该变成动态获取屏幕宽度，然后除以gridlayout 的列数
//                item.setTextNumber(2);
                addView(item, width / mCollomNumber, width / mCollomNumber); //可以指定增加的子控件宽，高

                //把该item的引用保存在一个二维矩阵里面

                mNumberItemMatrix[i][j]=item;
                //初始化的时候记录当前空白的位置。

                Point p = new Point();
                p.x= i;
                p.y= j ;
                blanklist.add(p);



            }

        //继续初始化棋盘view，一开始看到的时候，里面应有随机出现的两个数字不为0
        //有一个东西来记录当前棋盘上的空白位置

        addRandomNumber();//一开启产生2个方块
        //表示随机找两个空白位置，产生一个数字
        addRandomNumber();
//        addRandomNumber();
//        addRandomNumber();

    }

    /*
       在棋盘的空白位置上，随机找到一个位置，给它的item设置一个数。

     */


    private void addRandomNumber() {
        updateBlanklist();
        final int size = blanklist.size();
        final int location = (int) Math.floor(Math.random()*size);
        final Point point = blanklist.get(location);
        //随机产生2 或4
        mNumberItemMatrix[point.x][point.y].setTextNumber(Math.random() > 0.5 ? 2 : 4);

    }

    private void updateBlanklist() {
        blanklist.clear();
        for (int i=0;i<mRowNumber;i++)
            for (int j=0;j<mCollomNumber;j++){
                NumberItem numberItem = mNumberItemMatrix[i][j];
                if (numberItem.getNumber()==0)
                    blanklist.add(new Point(i,j));
            }
    }

    public void restart(){
        removeAllViews();
        init();
        updateCurrentScore();
        Log.i(TAG, "restart GmaeView");
    }

    private void updateCurrentScore() {
        //更新当前的分数
        mHome.updateCurrentScore(currentScore);
    }

    //恢复上一步的状态
    public void revert(){

        //方法1，遍历一遍history矩阵，如果里面全是0，就直接return。


        //方法2，添加一个flag，当且仅当histroy矩阵有过赋值之后，才置位1.
        if (canRevert) {
            for (int i = 0; i < mRowNumber; i++)
                for (int j = 0; j < mCollomNumber; j++) {
                    mNumberItemMatrix[i][j].setTextNumber(histroyMatrix[i][j]);

                }
        }
    }

    //把当前的记录保存到history矩阵中
    private void saveHistroy() {

        for (int i=0;i<mRowNumber;i++)
            for (int j=0;j<mCollomNumber;j++){
                histroyMatrix[i][j] =  mNumberItemMatrix[i][j].getNumber() ;
            }

        canRevert=true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        return super.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.i(TAG, "ACTION_DOWN");
                startx = event.getX();
                starty = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                Log.i(TAG, "ACTION_MOVE");
                break;
            case MotionEvent.ACTION_UP:
                Log.i(TAG, "ACTION_UP");
                stopx = event.getX();
                stopy = event.getY();

                //手指离开触摸屏时  判断一段动作的方向
                judgeDirection(startx,starty,stopx,stopy);

                //离开时  同时要判断是否继续   这儿假设 1为继续 2为成功 3为gameover
                handleResult(isOver());
                break;
        }
        return true;//super.onTouchEvent(event);
        //Down MOve ...MOve UP
        //表示当前控件来处理这个触摸事件的序列

    }

    private  void handleResult(int result){
        if (result==2){//完成游戏
            new AlertDialog.Builder(getContext()).
                    setTitle("恭喜！！")
                    .setMessage("你已成功完成游戏")
                    .setPositiveButton("重新开始", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            restart();
                        }
                    })
                    .setNegativeButton("挑战更难", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).show();

         }else if(result==3){//gameover
            new     AlertDialog.Builder(getContext()).
                    setTitle("失败")
                    .setMessage("游戏结束")
                    .setPositiveButton("重新开始", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            restart();

                        }
                    })
                    .setNegativeButton("退出游戏", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .show();
        }else{//1 表示继续 则给出一个随机数
            addRandomNumber();
        }
    }

    //上面判断条件中result    // 2 成功了    1 可以继续玩   3 gameover
    private  int isOver(){
//遍历所有的格子 判断是否有一个满足条件
        for (int i=0;i<mRowNumber;i++){
            for (int j=0;j<mCollomNumber;j++){
                if (mNumberItemMatrix[i][j].getNumber()==32){//这里判断是否等于2048  为了便于测试 让其等于32
                    return 2;
                }
            }
        }

        updateBlanklist();
        if (blanklist.size()==0){

            //这种情况下如果还有可以合并的，则返回1

            for (int i=0;i<mRowNumber;i++)
                for (int j=0;j<mCollomNumber-1;j++){
                    int current =   mNumberItemMatrix[i][j].getNumber();
                    int next = mNumberItemMatrix[i][j+1] .getNumber();
                    if (current==next){
                        return  1;
                    }
                }
//             注意  这里有两种情况  上下  左右可合并 都可以
            for (int i=0;i<mRowNumber;i++)
                for (int j=0;j<mCollomNumber-1;j++){
                    int current =   mNumberItemMatrix[j][i].getNumber();
                    int next = mNumberItemMatrix[j+1][i] .getNumber();
                    if (current==next){
                        return  1;
                    }
                }
            //如果没有可以合并的了，返回3
            return 3;

        }

        return 1;
    }


    private void judgeDirection(float startx, float starty, float stopx, float stopy) {
       /* boolean flag =   Math.abs(startx-stopx)>Math.abs(starty-stopy)?true:false;//判断x方向的绝对值是否大于y方向的绝对值

        Log.i(TAG,  "flag="+flag+"startx:"+ startx+", starty: "+starty+"stopx"+stopx+"stopy:"+ stopy);

        if(flag){//x方向的绝对值大于y方向的绝对值  以x方向的移动为主  即为左右移动
            if (stopx>startx){//右滑
                slideRight();
            }else{//左滑
                slideLeft();
            }
        }else{//垂直方向的移动
            if(stopy>starty){
                //注意！！！这是下滑  因为远点在（0，0）处
                slideDown();
            }else{
                slideUp();
            }
        }*/


//      下面这种写法是为了防止 触屏过度敏感
        float dx= Math.abs(startx-stopx);
        float dy= Math.abs(starty-stopy);
        //制定必须划过 五分之一的宽度才算数
        float baselevel= width/5;

        if(dx>baselevel||dy>baselevel) {

            boolean flag = dx > dy ? true : false;

            Log.i(TAG, "flag=" + flag + "startx:" + startx + ", starty: " + starty + "stopx" + stopx + "stopy:" + stopy);
            if (flag) {//水平方向滑动

                if (stopx > startx) {
                    //右滑
                    slideRight();
                } else {
                    //左滑
                    slideLeft();
                }
            } else { //竖直方向滑动
                if (stopy > starty) {
                    //下滑
                    slideDown();
                } else {
                    //上滑
                    slideUp();
                }


            }


        }
    }

    private void slideRight() {
        int prenumber = -1;
        //右滑  一行行比较  从最右边的开始
        for (int i=0;i<mRowNumber;i++){
            for(int j=mCollomNumber-1;j>=0;j--){
                //依次获取每个格子里的值
                final int number = mNumberItemMatrix[i][j].getNumber();
                if(number!=0){
                    //不相等时   prenumber!=-1 防止越界
                    if (number!=prenumber&&prenumber!=-1){
                        caculorList.add(prenumber);
                    }else if(prenumber!=-1){
                        caculorList.add(number*2);
                        currentScore+=number*2;
                        prenumber=-1;
                        //跳出本次循环 寻找下一节点
                        continue;
                    }

                    prenumber = number;
                }
            }

            //把最后一个prenumber加入到集合中
            if (prenumber!=0&&prenumber!=-1){
                caculorList.add(prenumber);
            }
            //把通过计算后合并的数字放到矩阵中
            for (int p = mRowNumber-1;p>mRowNumber-caculorList.size();p--){
                mNumberItemMatrix[i][p].setTextNumber(caculorList.get(mRowNumber-1-p));
            }

            //合并长度之后的部分以0来填充
            for (int q=mRowNumber-caculorList.size()-1; q >=0;q--){
                mNumberItemMatrix[i][q].setTextNumber(0);
            }
            //重置中间变量，为下次循环做准备。
            caculorList.clear();
            prenumber=-1;

        }
        Log.i(TAG,"slide to Right");
    }

    private void slideLeft() {
        int prenumber = -1;
        //左滑  一行行比较  从最左边的开始
        for (int i=0;i<mRowNumber;i++){
            for(int j=0;j<mCollomNumber;j++){
                //依次获取每个格子里的值
                final int number = mNumberItemMatrix[i][j].getNumber();
                if(number!=0){
                    //不相等时   prenumber!=-1 防止越界
                    if (number!=prenumber&&prenumber!=-1){
                        caculorList.add(prenumber);
                    }else if(prenumber!=-1){
                        caculorList.add(number*2);
                        currentScore+=number*2;
                        prenumber=-1;
                        //跳出本次循环 寻找下一节点
                        continue;
                    }

                    prenumber = number;
                }
            }

            //把最后一个prenumber加入到集合中
            if (prenumber!=0&&prenumber!=-1){
                caculorList.add(prenumber);
            }
            //把通过计算后合并的数字放到矩阵中
            for (int p = 0;p<caculorList.size();p++){
                mNumberItemMatrix[i][p].setTextNumber(caculorList.get(p));
            }

            //合并长度之后的部分以0来填充
            for (int q=caculorList.size();q<mCollomNumber;q++){
                mNumberItemMatrix[i][q].setTextNumber(0);
            }
            //重置中间变量，为下次循环做准备。
            caculorList.clear();
            prenumber=-1;

        }
        Log.i(TAG,"slide to Left");
    }

    private void slideUp() {
        int prenumber = -1;
        //上滑  一列列比较  从最上边的开始
        for (int i=0;i<mRowNumber;i++) {
            for (int j = 0; j <mCollomNumber; j++) {
                //依次获取每个格子里的值
                //或者final int number = mNumberItemMatrix[i][j].getNumber(); 但上面i<mCollomNumbe,j<mRowNumber
                final int number = mNumberItemMatrix[j][i].getNumber();
                if (number != 0) {
                    //不相等时   prenumber!=-1 防止越界
                    if (number!= prenumber && prenumber!= -1) {
                        caculorList.add(prenumber);
                    } else if (prenumber != -1) {
                        caculorList.add(number * 2);
                        currentScore+=number*2;
                        prenumber = -1;
                        //跳出本次循环 寻找下一节点
                        continue;
                    }

                    prenumber = number;
                }
            }

            //把最后一个prenumber加入到集合中
            if (prenumber != 0 && prenumber != -1) {
                caculorList.add(prenumber);
            }
            //把通过计算后合并的数字放到矩阵中
            for (int p = 0; p < caculorList.size(); p++) {
                mNumberItemMatrix[p][i].setTextNumber(caculorList.get(p));
            }

            //合并长度之后的部分以0来填充
            for (int q=caculorList.size();q<mCollomNumber;q++){
                mNumberItemMatrix[q][i].setTextNumber(0);
            }

            //重置中间变量，为下次循环做准备。
            caculorList.clear();
            prenumber = -1;
        }
        Log.i(TAG,"slide to up");
    }

    private void slideDown() {
        int prenumber=-1;
        for(int i =0;i<mRowNumber;i++) {
            for (int j = mCollomNumber-1; j >=0; j--) {

                final int number = mNumberItemMatrix[j][i].getNumber();

                if (number!=0){
                    if (number!=prenumber&&prenumber!=-1){
                        caculorList.add(prenumber);

                    }else if(prenumber!=-1){
                        caculorList.add(number*2);
                        currentScore+=number*2;
                        prenumber=-1;
                        continue;
                    }
                    prenumber=number;
                }

            }

            //把最后一个prenumber加入到集合中
            if (prenumber!=0&&prenumber!=-1)
                caculorList.add(prenumber);


            //把通过计算后合并的数字放到矩阵中
            for(int p=mCollomNumber-1;p>=mCollomNumber-caculorList.size();p--){
                mNumberItemMatrix[p][i].setTextNumber(caculorList.get(mCollomNumber-1-p));
            }

            //合并长度之后的部分以0来填充
            for (int q=mCollomNumber-caculorList.size()-1;q>=0;q--){
                mNumberItemMatrix[q][i].setTextNumber(0);
            }

            //重置中间变量，为下次循环做准备。
            caculorList.clear();
            prenumber=-1;

        }
        Log.i(TAG,"slide to down");
    }


}
