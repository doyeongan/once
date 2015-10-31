package com.example.OnceReceiver;

import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.*;
import android.transition.Scene;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {

    public Socket socket = null;
    public ChatThread cThread = null;
    public BufferedReader stream = null;

    public TextView tvMsg;
    public Button btnConnect;
    public EditText etIP;

    ArrayList<SellingItems> orderList;
    Map<Integer, View> viewTable;
    ViewGroup rootContainer;
    Scene loginScene;
    Scene viewScene;
    LinearLayout counter;

    int num = 0;

    private class ViewHolder{
        public TextView tvContent;
        public TextView tvQuantity;
    }

    class ListViewAdapter extends BaseAdapter{
        private Context mContext = null;
        private ArrayList<SellingItem> mListData = new ArrayList<SellingItem>();

        public ListViewAdapter(Context mContext) {
            super();
            this.mContext = mContext;

        }

        @Override
        public int getCount() {
            return mListData.size();
        }

        @Override
        public Object getItem(int position) {
            return mListData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void addItem(SellingItem sellingItem){
            mListData.add(sellingItem);
        }
        public void addItem(String content, int quantity, String temp){
            SellingItem addInfo = new SellingItem(content, quantity);
            addInfo.setTemperature(temp);
            mListData.add(addInfo);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();

                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.orderlist_item, null);

                holder.tvContent = (TextView) convertView.findViewById(R.id.tvContent);
                holder.tvQuantity = (TextView) convertView.findViewById(R.id.tvQuantity);

                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }

            SellingItem mData = mListData.get(position);
            if(mData.getTemperature() !=null) {
                if (mData.getTemperature().equals("I")) {
                    holder.tvContent.setBackgroundColor(Color.argb(70, 138, 214, 240));
                    holder.tvQuantity.setBackgroundColor(Color.argb(70, 138, 214, 240));
                } else if (mData.getTemperature().equals("H")){
                    holder.tvContent.setBackgroundColor(Color.argb(100, 255, 214, 214));
                    holder.tvQuantity.setBackgroundColor(Color.argb(100, 255, 214, 214));
                }
            }
            holder.tvContent.setText(mData.getContent());
            holder.tvQuantity.setText("" + mData.getQuantity());

            return convertView;
        }
    }

    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if(msg.what == 1){
                SellingItems val = (SellingItems) msg.obj;
                orderList.add(val);
                addOrder(val);
            }else if(msg.what == 2){
                DeleteOrder val = (DeleteOrder) msg.obj;
                int num = val.getId();
                View v = viewTable.get(num);
                counter = (LinearLayout) findViewById(R.id.counterlayout);
                counter.removeView(v);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        rootContainer = (ViewGroup) findViewById(R.id.rootContainer);
        counter = (LinearLayout) findViewById(R.id.counterlayout);
        loginScene = Scene.getSceneForLayout(rootContainer, R.layout.login, this);
        viewScene = Scene.getSceneForLayout(rootContainer, R.layout.view, this);


        loginScene.enter();
        orderList = new ArrayList<>();
        viewTable = new HashMap<>();

        // API9 이상부터는 네트워크 사용 시
        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        tvMsg = (TextView) findViewById(R.id.tvMsg);
        btnConnect = (Button) findViewById(R.id.btnConnect);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("onDestory", "");
    }

    public void Connect(View v) {
//                    TransitionManager.go(viewScene);

        try {
            etIP = (EditText) findViewById(R.id.etIP);
            String ip = etIP.getText().toString();
            Log.i("tcp ", "ip: " + ip);
            socket = new Socket(ip, 9051);
            stream = new BufferedReader(new InputStreamReader(socket.getInputStream()));


            Toast.makeText(getApplicationContext(), "연결성공", Toast.LENGTH_LONG).show();
            TransitionManager.go(viewScene);
            cThread = new ChatThread();
            cThread.start();

        } catch (IOException err) {
            System.out.println(err);
            Toast.makeText(getApplicationContext(), "연결실패", Toast.LENGTH_LONG).show();
            return;
        } catch (NullPointerException err){
            System.out.println(err);
            Toast.makeText(getApplicationContext(), "IP를 입력해주세요", Toast.LENGTH_LONG).show();
            return;
        }
    }

    class ChatThread extends Thread {
        public void run() {
            String sMsg;
            Message msg;

            while (true) {
                try {
                    sMsg = stream.readLine();
                    System.out.println(sMsg);
                    msg = new Message();

                    if(sMsg.charAt(0) == '1'){
                        sMsg = sMsg.substring(1,sMsg.length());
                        SellingItems sellingItems = new Gson().fromJson(sMsg, SellingItems.class);
                        msg.what = 1;
                        msg.obj = sellingItems;
                    }else if(sMsg.charAt(0) == '2'){
                        sMsg = sMsg.substring(1,sMsg.length());
                        DeleteOrder deleteOrder = new Gson().fromJson(sMsg, DeleteOrder.class);
                        msg.what = 2;
                        msg.obj = deleteOrder;
                    }

                    mHandler.sendMessage(msg);
                } catch (NullPointerException|IOException err) {
                    System.out.println(err);
                    break;
                }
            }
            msg = new Message();
            msg.obj="connection fail";
            mHandler.sendMessage(msg);
        }
    }

    public void addOrder(View arg){
        Log.i("add", "order: " + num);
        View v = getLayoutInflater().inflate(R.layout.order, null);
        ((TextView)v.findViewById(R.id.tvOrderNum)).setText("Order #"+num);
        ((Button)v.findViewById(R.id.btnConfirm)).setText(num+ "# " +"완료");
        ListView orderListView = (ListView)v.findViewById(R.id.listOrder);
        ListViewAdapter orderListViewAdpater = new ListViewAdapter(v.getContext());
        orderListView.setAdapter(orderListViewAdpater);

        orderListViewAdpater.addItem("americano", 1, "I");
        orderListViewAdpater.addItem("latte", 2, "H");

        RelativeLayout.LayoutParams parm = new RelativeLayout.LayoutParams(750, ViewGroup.LayoutParams.MATCH_PARENT);
        parm.setMargins(15,0,15,0);
        v.setLayoutParams(parm);


        counter = (LinearLayout) findViewById(R.id.counterlayout);
        counter.addView(v, 0);
        viewTable.put(num, v);
        num ++;

    }

    public void addOrder(SellingItems arg){
        int ordernum = arg.getId();
        String order="";

        View v = getLayoutInflater().inflate(R.layout.order, null);
        ((TextView)v.findViewById(R.id.tvOrderNum)).setText("Order #"+ordernum);
        ((Button)v.findViewById(R.id.btnConfirm)).setText(ordernum+ "# " +"완료");
        ListView orderListView = (ListView)v.findViewById(R.id.listOrder);
        ListViewAdapter orderListViewAdpater = new ListViewAdapter(v.getContext());
        orderListView.setAdapter(orderListViewAdpater);


        for (SellingItem m : arg.getSellingItems()) {
            orderListViewAdpater.addItem(m);
        }

        RelativeLayout.LayoutParams parm = new RelativeLayout.LayoutParams(750, ViewGroup.LayoutParams.MATCH_PARENT);
        parm.setMargins(15,0,15,0);
        v.setLayoutParams(parm);


        counter = (LinearLayout) findViewById(R.id.counterlayout);
        counter.addView(v, 0);

        viewTable.put(ordernum, v);


    }

    public void confirmOrder(View arg){

        int ordernum = Integer.parseInt(((Button)arg).getText().toString().split("#")[0]);
        Log.i("confirm: ", ""+ordernum);
        View v = viewTable.get(ordernum);
        counter = (LinearLayout) findViewById(R.id.counterlayout);
        counter.removeView(v);

    }
}
