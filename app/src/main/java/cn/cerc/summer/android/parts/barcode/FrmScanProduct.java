package cn.cerc.summer.android.parts.barcode;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.mimrc.vine.R;

import java.util.Timer;
import java.util.TimerTask;

import cn.cerc.summer.android.basis.core.MyApp;
import cn.cerc.summer.android.basis.tools.DataSet;
import cn.cerc.summer.android.basis.tools.ListViewAdapter;
import cn.cerc.summer.android.basis.tools.ListViewInterface;
import cn.cerc.summer.android.basis.tools.Record;

import static cn.cerc.summer.android.parts.music.FrmCaptureMusic.url;

public class FrmScanProduct extends AppCompatActivity implements View.OnClickListener, ListViewInterface {
    TextView lblTitle;
    EditText edtBarcode;
    Button btnSave;
    ListView lstView;
    WebView webView;

    private String viewUrl;
    private String postUrl;
    private DataSet dataSet = new DataSet();
    private ListViewAdapter adapter;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            edtBarcode.requestFocus();
        }
    };


    /**
     * @param context FrmMain
     * @param title   页面标题
     * @param postUrl 数据上传到指定的url
     * @param viewUrl 显示相应的记录之url
     */
    public static void startForm(Context context, String title, String postUrl, String viewUrl) {
        Intent intent = new Intent();
        intent.putExtra("title", title);
        intent.putExtra("postUrl", postUrl);
        intent.putExtra("viewUrl", viewUrl);
        intent.setClass(context, FrmScanProduct.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frm_scan_product);
        Intent intent = getIntent();

        lblTitle = (TextView) findViewById(R.id.lblTitle);
        lblTitle.setText(intent.getStringExtra("title"));

        this.postUrl = intent.getStringExtra("postUrl");
        this.viewUrl = intent.getStringExtra("viewUrl");

        edtBarcode = (EditText) findViewById(R.id.edtBarcode);
        btnSave = (Button) findViewById(R.id.btnSave);
        btnSave.setOnClickListener(this);

        for (int i = 0; i < 2; i++) {
            dataSet.append();
            dataSet.setField("barcode", "123424123412");
            dataSet.setField("num", 1 + i);
        }

        adapter = new ListViewAdapter(this, R.layout.activity_list_scan_product, dataSet, this);
        lstView = (ListView) findViewById(R.id.lstView);
        lstView.setAdapter(adapter);

        webView = (WebView) findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
                view.loadUrl(url);
                return true;
            }
        });
        //启用支持javascript
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        //打开指定的网页
        webView.loadUrl(MyApp.HOME_URL);

        //关闭软键盘
        edtBarcode.setInputType(InputType.TYPE_NULL);
        edtBarcode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    btnSave.callOnClick();
                }
                return false;
            }
        });

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Message msg = new Message();
                handler.sendMessage(msg);
            }
        }, 1000, 200);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSave:
                dataSet.insert(0);
                dataSet.setField("barcode", edtBarcode.getText().toString());
                dataSet.setField("num", 1);
                adapter.notifyDataSetChanged();
                edtBarcode.setText("");
                edtBarcode.requestFocus();
                break;
            case R.id.lblBarcode:
            case R.id.lblNum:
                int recordIndex = (Integer) view.getTag();
                Record item = dataSet.get((Integer) view.getTag());
                DlgScanProduct.startFormForResult(this, recordIndex, item.getInt("num"));
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        edtBarcode.requestFocus();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            int index = data.getIntExtra("recordIndex", -1);
            int num = data.getIntExtra("num", 0);
            dataSet.get(index).setField("num", num);
            if (num == 0)
                dataSet.remove(index);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onGetText(View view, Record item, int position) {
        TextView lblBarcode = (TextView) view.findViewById(R.id.lblBarcode);
        lblBarcode.setText(item.getString("barcode"));
        lblBarcode.setOnClickListener(this);
        lblBarcode.setTag(position);

        TextView lblNum = (TextView) view.findViewById(R.id.lblNum);
        lblNum.setText("" + item.getInt("num"));
        lblNum.setOnClickListener(this);
        lblNum.setTag(position);

//      ImageView imageView = (ImageView) view.findViewById(R.id.imgView);
    }
}