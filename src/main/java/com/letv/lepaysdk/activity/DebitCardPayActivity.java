package com.letv.lepaysdk.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.letv.lepaysdk.ELePayState;
import com.letv.lepaysdk.model.PayCard;
import com.letv.lepaysdk.model.Paymodes;
import com.letv.lepaysdk.model.Result.ResultConstant;
import com.letv.lepaysdk.model.TradeInfo;
import com.letv.lepaysdk.network.CardPayHelper;
import com.letv.lepaysdk.network.CardPayHelper.BankInfo;
import com.letv.lepaysdk.task.TaskListener;
import com.letv.lepaysdk.task.TaskResult;
import com.letv.lepaysdk.utils.LOG;
import com.letv.lepaysdk.utils.ResourceUtil;
import com.letv.lepaysdk.utils.StringUtil;
import com.letv.lepaysdk.utils.ToastUtils;
import com.letv.lepaysdk.view.ClearEditText;
import com.letv.lepaysdk.view.ClearEditText.OnTextChangedListener;
import com.letv.lepaysdk.view.LePayActionBar;
import com.letv.lepaysdk.view.LePayCustomDialog.Builder;
import com.letv.lepaysdk.view.MontmorilloniteLayer;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

public class DebitCardPayActivity extends BaseActivity implements OnClickListener {
    private List<BankInfo> bankInfos = new ArrayList();
    private String bankcode;
    private String bankname;
    private Context context;
    final Handler handler = new Handler();
    boolean hasFirstPay = true;
    boolean hasToast = true;
    private Button lepay_bt_checkcode;
    private TextView lepay_cashier_moeny;
    private ClearEditText lepay_et_cardNo;
    private ClearEditText lepay_et_checkcode;
    private ClearEditText lepay_et_cvv2;
    private ClearEditText lepay_et_exp;
    private ClearEditText lepay_et_mobile;
    private ImageView lepay_iv_cardNo_clean;
    private ImageView lepay_iv_cardno_question;
    private ImageView lepay_iv_checkcode_clean;
    private ImageView lepay_iv_cvv2_question;
    private ImageView lepay_iv_exp_question;
    private ImageView lepay_iv_mobile_clean;
    private LinearLayout lepay_ll_parent;
    private TextView lepay_pay_ok;
    private MontmorilloniteLayer lepay_payload_layer;
    private CheckBox lepay_rb_select;
    private TextView lepay_tv_cardNo_hint;
    private TextView lepay_tv_checkcode_hint;
    private TextView lepay_tv_cvv2_hint;
    private TextView lepay_tv_exp_hint;
    private TextView lepay_tv_mobile_hint;
    private TextView lepay_tv_pay_protocol;
    private String logosmall;
    private LePayActionBar mActionBar;
    private MessageCountTimer mMessageTimer;
    boolean modifyChanged = true;
    CardPayHelper payHelper;
    private Paymodes paymodes;
    private ProgressBar progress;
    int runCount = 0;
    private String sendby;

    private class MessageCountTimer extends CountDownTimer {
        public MessageCountTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        public void onFinish() {
            DebitCardPayActivity.this.lepay_bt_checkcode.setClickable(true);
            DebitCardPayActivity.this.lepay_bt_checkcode.setText(ResourceUtil.getStringResource(DebitCardPayActivity.this.context, "lepay_creditCards_getcheckcode"));
            DebitCardPayActivity.this.lepay_bt_checkcode.setBackgroundResource(ResourceUtil.getDrawableResource(DebitCardPayActivity.this.context, "lepay_count_sms"));
        }

        public void onTick(long millisUntilFinished) {
            DebitCardPayActivity.this.lepay_bt_checkcode.setBackgroundResource(ResourceUtil.getDrawableResource(DebitCardPayActivity.this.context, "lepay_count_sms_gray"));
            DebitCardPayActivity.this.lepay_bt_checkcode.setText((millisUntilFinished / 1000) + "秒后继续");
            DebitCardPayActivity.this.lepay_bt_checkcode.setClickable(false);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;
        checkData();
        initCardPayHelper();
        getSupportBanklist();
        initContentView();
        initView();
        initText();
        setlisenner();
    }

    void checkData() {
        this.paymodes = (Paymodes) getIntent().getSerializableExtra(CashierAcitivity.PaymodesTAG);
        this.mTradeInfo = (TradeInfo) getIntent().getSerializableExtra(CashierAcitivity.TradeinfoTAG);
        if (this.paymodes == null || this.mTradeInfo == null) {
            LOG.logI("参数错误");
            onBackPressed();
        }
    }

    private void initContentView() {
        setContentView(ResourceUtil.getLayoutResource(this, "lepay_debitcard_verify_activity"));
    }

    private void initView() {
        this.lepay_payload_layer = (MontmorilloniteLayer) findViewById(ResourceUtil.getIdResource(this, "lepay_payload_layer"));
        this.lepay_ll_parent = (LinearLayout) findViewById(ResourceUtil.getIdResource(this, "lepay_ll_parent"));
        this.lepay_et_cardNo = (ClearEditText) findViewById(ResourceUtil.getIdResource(this, "lepay_et_cardNo"));
        this.lepay_et_exp = (ClearEditText) findViewById(ResourceUtil.getIdResource(this, "lepay_et_exp"));
        this.lepay_et_cvv2 = (ClearEditText) findViewById(ResourceUtil.getIdResource(this, "lepay_et_cvv2"));
        this.lepay_et_mobile = (ClearEditText) findViewById(ResourceUtil.getIdResource(this, "lepay_et_mobile"));
        this.lepay_et_checkcode = (ClearEditText) findViewById(ResourceUtil.getIdResource(this, "lepay_et_checkcode"));
        this.lepay_tv_cardNo_hint = (TextView) findViewById(ResourceUtil.getIdResource(this, "lepay_tv_cardNo_hint"));
        this.lepay_tv_exp_hint = (TextView) findViewById(ResourceUtil.getIdResource(this, "lepay_tv_exp_hint"));
        this.lepay_tv_cvv2_hint = (TextView) findViewById(ResourceUtil.getIdResource(this, "lepay_tv_cvv2_hint"));
        this.lepay_tv_mobile_hint = (TextView) findViewById(ResourceUtil.getIdResource(this, "lepay_tv_mobile_hint"));
        this.lepay_tv_checkcode_hint = (TextView) findViewById(ResourceUtil.getIdResource(this, "lepay_tv_checkcode_hint"));
        this.lepay_iv_cardNo_clean = (ImageView) findViewById(ResourceUtil.getIdResource(this, "lepay_iv_cardNo_clean"));
        this.lepay_iv_cardno_question = (ImageView) findViewById(ResourceUtil.getIdResource(this, "lepay_iv_cardno_question"));
        this.lepay_iv_exp_question = (ImageView) findViewById(ResourceUtil.getIdResource(this, "lepay_iv_exp_question"));
        this.lepay_iv_cvv2_question = (ImageView) findViewById(ResourceUtil.getIdResource(this, "lepay_iv_cvv2_question"));
        this.lepay_iv_mobile_clean = (ImageView) findViewById(ResourceUtil.getIdResource(this, "lepay_iv_mobile_clean"));
        this.lepay_iv_checkcode_clean = (ImageView) findViewById(ResourceUtil.getIdResource(this, "lepay_iv_checkcode_clean"));
        this.lepay_bt_checkcode = (Button) findViewById(ResourceUtil.getIdResource(this, "lepay_bt_checkcode"));
        this.lepay_pay_ok = (TextView) findViewById(ResourceUtil.getIdResource(this, "lepay_pay_ok"));
        this.lepay_cashier_moeny = (TextView) findViewById(ResourceUtil.getIdResource(this, "lepay_cashier_moeny"));
        this.lepay_rb_select = (CheckBox) findViewById(ResourceUtil.getIdResource(this, "lepay_rb_select"));
        this.lepay_tv_pay_protocol = (TextView) findViewById(ResourceUtil.getIdResource(this, "lepay_tv_pay_protocol"));
        this.progress = (ProgressBar) findViewById(ResourceUtil.getIdResource(this, "progress"));
        this.mActionBar = (LePayActionBar) findViewById(ResourceUtil.getIdResource(this, "lepay_actionbar"));
        this.mActionBar.setLeftButtonVisable(0);
    }

    private void setlisenner() {
        this.lepay_iv_cardNo_clean.setOnClickListener(this);
        this.lepay_iv_cardno_question.setOnClickListener(this);
        this.lepay_iv_exp_question.setOnClickListener(this);
        this.lepay_iv_cvv2_question.setOnClickListener(this);
        this.lepay_iv_mobile_clean.setOnClickListener(this);
        this.lepay_iv_checkcode_clean.setOnClickListener(this);
        this.lepay_bt_checkcode.setOnClickListener(this);
        this.lepay_pay_ok.setOnClickListener(this);
        this.lepay_tv_pay_protocol.setOnClickListener(this);
        this.mActionBar.setLeftButtonOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                DebitCardPayActivity.this.onBackPressed();
            }
        });
        this.mActionBar.setRightButtonOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
            }
        });
        this.lepay_et_cardNo.setOnTextChangedListener(new OnTextChangedListener() {
            public void onTextChanged(String s) {
                if (!DebitCardPayActivity.this.hasFirstPay) {
                    DebitCardPayActivity.this.modifyChanged = true;
                }
                if (TextUtils.isEmpty(s) || "".equals(s.trim())) {
                    DebitCardPayActivity.this.lepay_tv_cardNo_hint.setText("请输入银行卡号");
                    DebitCardPayActivity.this.lepay_tv_cardNo_hint.setVisibility(0);
                } else {
                    DebitCardPayActivity.this.lepay_tv_cardNo_hint.setVisibility(4);
                }
                DebitCardPayActivity.this.lepay_iv_cardNo_clean.setVisibility(8);
            }

            public void onFocusChange(boolean hasFocus) {
                LOG.logI("cardNo EditText hasFocus:" + hasFocus);
                if (!hasFocus) {
                    String cardNo = DebitCardPayActivity.this.lepay_et_cardNo.getText().toString();
                    if (TextUtils.isEmpty(cardNo) || "".equals(cardNo.trim())) {
                        DebitCardPayActivity.this.lepay_tv_cardNo_hint.setText("请输入银行卡号");
                        DebitCardPayActivity.this.lepay_tv_cardNo_hint.setVisibility(0);
                        return;
                    }
                    DebitCardPayActivity.this.payHelper.getCardInfo(DebitCardPayActivity.this.mTradeInfo.getMerchant_business_id(), cardNo.replaceAll(" ", ""), new 1(this));
                }
            }
        });
        this.lepay_et_mobile.setOnTextChangedListener(new OnTextChangedListener() {
            public void onTextChanged(String s) {
                int i = 0;
                if (TextUtils.isEmpty(s) || "".equals(s.trim())) {
                    DebitCardPayActivity.this.lepay_tv_mobile_hint.setText("请输入手机号码");
                    DebitCardPayActivity.this.lepay_tv_mobile_hint.setVisibility(0);
                } else if (s.length() >= 11) {
                    DebitCardPayActivity.this.lepay_tv_mobile_hint.setText("请输入正确的手机号");
                    TextView access$9 = DebitCardPayActivity.this.lepay_tv_mobile_hint;
                    if (StringUtil.hasMobile(s)) {
                        i = 4;
                    }
                    access$9.setVisibility(i);
                } else {
                    DebitCardPayActivity.this.lepay_tv_mobile_hint.setVisibility(4);
                }
                if (!DebitCardPayActivity.this.hasFirstPay) {
                    DebitCardPayActivity.this.modifyChanged = true;
                }
            }

            public void onFocusChange(boolean hasFocus) {
            }
        });
        this.lepay_et_exp.setOnTextChangedListener(new OnTextChangedListener() {
            public void onTextChanged(String s) {
                if (!DebitCardPayActivity.this.hasFirstPay) {
                    DebitCardPayActivity.this.modifyChanged = true;
                }
                if (TextUtils.isEmpty(s) || "".equals(s.trim())) {
                    DebitCardPayActivity.this.lepay_tv_exp_hint.setText("请输入姓名");
                    DebitCardPayActivity.this.lepay_tv_exp_hint.setVisibility(0);
                    return;
                }
                DebitCardPayActivity.this.lepay_tv_exp_hint.setVisibility(4);
            }

            public void onFocusChange(boolean hasFocus) {
            }
        });
        this.lepay_et_cvv2.setOnTextChangedListener(new OnTextChangedListener() {
            public void onTextChanged(String s) {
                if (!DebitCardPayActivity.this.hasFirstPay) {
                    DebitCardPayActivity.this.modifyChanged = true;
                }
                if (TextUtils.isEmpty(s) || "".equals(s.trim())) {
                    DebitCardPayActivity.this.lepay_tv_cvv2_hint.setText("请输入身份证号码");
                    DebitCardPayActivity.this.lepay_tv_cvv2_hint.setVisibility(0);
                    return;
                }
                DebitCardPayActivity.this.lepay_tv_cvv2_hint.setVisibility(4);
            }

            public void onFocusChange(boolean hasFocus) {
            }
        });
        this.lepay_et_checkcode.setOnTextChangedListener(new OnTextChangedListener() {
            public void onTextChanged(String s) {
                if (TextUtils.isEmpty(s) || "".equals(s.trim())) {
                    DebitCardPayActivity.this.lepay_tv_checkcode_hint.setText("请输入验证码");
                    DebitCardPayActivity.this.lepay_tv_checkcode_hint.setVisibility(0);
                    return;
                }
                DebitCardPayActivity.this.lepay_tv_checkcode_hint.setVisibility(4);
            }

            public void onFocusChange(boolean hasFocus) {
            }
        });
    }

    private void initText() {
        this.lepay_et_cardNo.showType = true;
        this.lepay_et_mobile.showType = true;
        this.lepay_et_mobile.showMobileType = true;
        this.lepay_pay_ok.setText(getString(ResourceUtil.getStringResource(this, "lepay_pay_ok")));
        this.lepay_rb_select.setChecked(true);
        String price = this.mTradeInfo.getPrice();
        TextView textView = this.lepay_cashier_moeny;
        if (TextUtils.isEmpty(price)) {
            price = "";
        }
        textView.setText(price);
        this.mActionBar.setTitle(getString(ResourceUtil.getStringResource(this.context, "lepay_leshi_debitcardpay")));
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == this.lepay_iv_cardNo_clean.getId()) {
            return;
        }
        if (id == this.lepay_iv_cardno_question.getId()) {
            if (this.bankInfos.size() >= 0) {
                this.payHelper.createSupportBanks(ResourceUtil.getStringResource(this.context, "lepay_debitCard_layer_bank"), this.bankInfos);
            }
        } else if (id == this.lepay_iv_exp_question.getId()) {
            layerHint(ResourceUtil.getDrawableResource(this.context, "lepay_icon_exp_hint"), ResourceUtil.getStringResource(this.context, "lepay_creditCards_layer_exptitle"));
        } else if (id == this.lepay_iv_cvv2_question.getId()) {
            layerHint(ResourceUtil.getDrawableResource(this.context, "lepay_icon_cvv2_hint"), ResourceUtil.getStringResource(this.context, "lepay_creditCards_layer_cvv2title"));
        } else if (id == this.lepay_iv_mobile_clean.getId()) {
            this.lepay_et_mobile.setText("");
        } else if (id == this.lepay_iv_checkcode_clean.getId()) {
            this.lepay_et_checkcode.setText("");
        } else if (id == this.lepay_bt_checkcode.getId()) {
            startCheckCode();
        } else if (id == this.lepay_pay_ok.getId()) {
            verifyText();
        } else if (id == this.lepay_tv_pay_protocol.getId()) {
            Intent intent = new Intent(this, ProtocolActivity.class);
            intent.putExtra("protocol", "http://minisite.letv.com/zt2016/5020/zt1003282241/index.shtml");
            startActivity(intent);
        }
    }

    void startTimer() {
        this.mMessageTimer = new MessageCountTimer(60000, 1000);
        this.mMessageTimer.start();
    }

    void layerHint(int iconResId, int titleResId) {
        Builder builder = new Builder(this.context);
        builder.setIconRes(iconResId);
        builder.setTitle(titleResId);
        builder.create().show();
    }

    void cardlayerHint(int titleResId) {
        View view = View.inflate(this.context, ResourceUtil.getLayoutResource(this.context, "lepay_debitcard_list"), null);
        Builder builder = new Builder(this.context);
        builder.setTitle(titleResId);
        builder.setContentView(view);
        builder.create().show();
    }

    void visablePanel(boolean b) {
        LOG.logI("visablePanel＝" + b);
        if (this.lepay_ll_parent.getVisibility() != 0) {
            return;
        }
        if (b) {
            this.lepay_pay_ok.setClickable(false);
            this.lepay_payload_layer.setVisibility(0);
            this.lepay_payload_layer.setFocusable(true);
            this.lepay_pay_ok.setText(ResourceUtil.getStringResource(this.context, "lepay_pay_wait"));
            this.progress.setVisibility(0);
            return;
        }
        this.lepay_pay_ok.setClickable(true);
        this.lepay_payload_layer.setVisibility(8);
        this.lepay_payload_layer.setFocusable(false);
        this.lepay_pay_ok.setText(ResourceUtil.getStringResource(this.context, "lepay_pay_ok"));
        this.progress.setVisibility(8);
    }

    void showPayingMode() {
        visablePanel(true);
    }

    protected void onDestroy() {
        super.onDestroy();
        if (this.mMessageTimer != null) {
            this.mMessageTimer.cancel();
        }
    }

    void initCardPayHelper() {
        this.payHelper = new CardPayHelper(this.context);
    }

    void verifyText() {
        if (!commonVerfiy() || !proticalVerify()) {
            return;
        }
        if (this.hasFirstPay) {
            this.hasFirstPay = false;
            showPayingMode();
            pay();
        } else if (this.modifyChanged) {
            ToastUtils.makeText(this.context, "银行卡信息已更改，请重新获取验证码");
        } else {
            showPayingMode();
            pay();
        }
    }

    boolean proticalVerify() {
        String checkcode = this.lepay_et_checkcode.getText().toString();
        if (TextUtils.isEmpty(checkcode) || "".equals(checkcode.trim())) {
            this.lepay_tv_checkcode_hint.setText("请输入验证码");
            this.lepay_tv_checkcode_hint.setVisibility(0);
            return false;
        } else if (this.lepay_rb_select.isChecked()) {
            return true;
        } else {
            ToastUtils.makeText(this.context, "请阅读乐视支付协议");
            return false;
        }
    }

    boolean commonVerfiy() {
        String exp = this.lepay_et_exp.getText().toString();
        String cvv2 = this.lepay_et_cvv2.getText().toString();
        String mobile = this.lepay_et_mobile.getText().toString();
        String cardNo = this.lepay_et_cardNo.getText().toString();
        if (TextUtils.isEmpty(cardNo) || "".equals(cardNo.trim())) {
            this.lepay_tv_cardNo_hint.setText("请输入银行卡号");
            this.lepay_tv_cardNo_hint.setVisibility(0);
            return false;
        } else if (TextUtils.isEmpty(exp) || "".equals(exp.trim())) {
            this.lepay_tv_exp_hint.setText("请输入姓名");
            this.lepay_tv_exp_hint.setVisibility(0);
            return false;
        } else if (TextUtils.isEmpty(cvv2) || "".equals(cvv2.trim())) {
            this.lepay_tv_cvv2_hint.setText("请输入身份证号码");
            this.lepay_tv_cvv2_hint.setVisibility(0);
            return false;
        } else if (TextUtils.isEmpty(mobile) || "".equals(mobile.trim())) {
            this.lepay_tv_mobile_hint.setText("请输入手机号码");
            this.lepay_tv_mobile_hint.setVisibility(0);
            return false;
        } else {
            String newMobile = mobile.trim().replaceAll(" ", "");
            if (newMobile.length() == 11 && StringUtil.hasMobile(newMobile)) {
                return true;
            }
            this.lepay_tv_mobile_hint.setText("请输入正确的手机号");
            this.lepay_tv_mobile_hint.setVisibility(0);
            return false;
        }
    }

    void startCheckCode() {
        if (commonVerfiy()) {
            PayCard payCard = createVerifyParams();
            payCard.setBankcode(this.bankcode);
            payCard.setBankname(this.bankname);
            payCard.setChange(this.modifyChanged ? "1" : "0");
            this.payHelper.checkCode(payCard, new TaskListener<String>() {
                public void onFinish(TaskResult<String> result) {
                    if (result.isOk()) {
                        DebitCardPayActivity.this.startTimer();
                        DebitCardPayActivity.this.sendby = (String) result.getResult();
                        return;
                    }
                    ToastUtils.makeText(DebitCardPayActivity.this.context, result.getDesc());
                }

                public void onPreExcuete() {
                }
            });
            this.modifyChanged = false;
        }
    }

    void pay() {
        final String code = this.lepay_et_checkcode.getText().toString();
        final String lepay_order_no = this.mTradeInfo.getLepay_order_no();
        final String merchant_business_id = this.mTradeInfo.getMerchant_business_id();
        this.payHelper.queryVerifyCode(lepay_order_no, merchant_business_id, this.lepay_et_mobile.getText().toString().replaceAll(" ", ""), code, this.sendby, new TaskListener<Void>() {
            public void onFinish(TaskResult<Void> result) {
                if (result.isOk()) {
                    String channelId = DebitCardPayActivity.this.paymodes.getChannel_id();
                    if (TextUtils.isEmpty(lepay_order_no) || TextUtils.isEmpty(merchant_business_id) || TextUtils.isEmpty(channelId)) {
                        ToastUtils.makeText(DebitCardPayActivity.this.context, "参数有误");
                        DebitCardPayActivity.this.visablePanel(false);
                        return;
                    }
                    DebitCardPayActivity.this.validPayOk(channelId, lepay_order_no, merchant_business_id, code);
                    return;
                }
                ToastUtils.makeText(DebitCardPayActivity.this.context, result.getDesc());
                DebitCardPayActivity.this.visablePanel(false);
            }

            public void onPreExcuete() {
            }
        });
        this.hasToast = true;
        this.runCount = 0;
    }

    void validPayOk(String channel_id, String lepay_order_no, String merchant_business_id, String verifycode) {
        this.payHelper.pay(channel_id, lepay_order_no, merchant_business_id, verifycode, this.sendby, new TaskListener<JSONObject>() {
            public void onFinish(TaskResult<JSONObject> result) {
                if (result.isOk()) {
                    JSONObject jsonObject = (JSONObject) result.getResult();
                    String lepay_payment_no = jsonObject.optString("lepay_payment_no");
                    DebitCardPayActivity.this.validOrderState(lepay_payment_no, jsonObject.optString("merchant_business_id"), lepay_payment_no);
                    return;
                }
                DebitCardPayActivity.this.visablePanel(false);
                DebitCardPayActivity.this.payHelper.showPayStatus(DebitCardPayActivity.this.mTradeInfo.getKey(), ELePayState.FAILT, result.getDesc());
            }

            public void onPreExcuete() {
            }
        });
    }

    void validOrderState(final String lepay_order_no, final String merchant_business_id, final String lepayPaymentNo) {
        this.payHelper.queryOrderState(lepay_order_no, merchant_business_id, lepayPaymentNo, new TaskListener<Bundle>() {
            public void onFinish(TaskResult<Bundle> result) {
                if (result.isOk()) {
                    DebitCardPayActivity.this.visablePanel(false);
                    DebitCardPayActivity.this.setResult(-1);
                    DebitCardPayActivity.this.finish();
                    return;
                }
                Bundle bundle = (Bundle) result.getResult();
                if (!bundle.containsKey(ResultConstant.errorcode)) {
                    if (DebitCardPayActivity.this.hasToast) {
                        DebitCardPayActivity.this.payHelper.showPayStatus(DebitCardPayActivity.this.mTradeInfo.getKey(), ELePayState.FAILT, result.getDesc());
                    }
                    DebitCardPayActivity.this.visablePanel(false);
                    DebitCardPayActivity.this.hasToast = false;
                } else if (bundle.getInt(ResultConstant.errorcode) == 2005) {
                    DebitCardPayActivity.this.getOrderStateTimerTask(lepay_order_no, merchant_business_id, lepayPaymentNo);
                } else {
                    if (DebitCardPayActivity.this.hasToast) {
                        DebitCardPayActivity.this.payHelper.showPayStatus(DebitCardPayActivity.this.mTradeInfo.getKey(), ELePayState.FAILT, result.getDesc());
                    }
                    DebitCardPayActivity.this.visablePanel(false);
                    DebitCardPayActivity.this.hasToast = false;
                }
            }

            public void onPreExcuete() {
            }
        });
    }

    PayCard createVerifyParams() {
        String lepay_order_no = this.mTradeInfo.getLepay_order_no();
        String merchant_business_id = this.mTradeInfo.getMerchant_business_id();
        String checkcode = this.lepay_et_checkcode.getText().toString();
        PayCard payCard = new PayCard();
        payCard.setLepay_order_no(lepay_order_no);
        payCard.setMerchant_business_id(merchant_business_id);
        payCard.setCardno(this.lepay_et_cardNo.getText().toString().replaceAll(" ", ""));
        payCard.setPhone(this.lepay_et_mobile.getText().toString().replaceAll(" ", ""));
        payCard.setOwner(this.lepay_et_exp.getText().toString());
        payCard.setIdcard(this.lepay_et_cvv2.getText().toString());
        payCard.setChannel_id(this.paymodes.getChannel_id());
        payCard.setVerifycode(checkcode);
        return payCard;
    }

    void getOrderStateTimerTask(final String lepay_order_no, final String merchant_business_id, final String lepayPaymentNo) {
        this.handler.postDelayed(new Runnable() {
            public void run() {
                if (DebitCardPayActivity.this.runCount > 10) {
                    Log.e("Ta", "removeCallbacks runCount: " + DebitCardPayActivity.this.runCount);
                    DebitCardPayActivity.this.visablePanel(false);
                    DebitCardPayActivity.this.handler.removeCallbacks(this);
                    return;
                }
                DebitCardPayActivity.this.validOrderState(lepay_order_no, merchant_business_id, lepayPaymentNo);
                Log.e("Ta", "runCount: " + DebitCardPayActivity.this.runCount);
                DebitCardPayActivity debitCardPayActivity = DebitCardPayActivity.this;
                debitCardPayActivity.runCount++;
            }
        }, 3000);
    }

    void getSupportBanklist() {
        this.payHelper.getSupportBanklist("2", new TaskListener<List<BankInfo>>() {
            public void onFinish(TaskResult<List<BankInfo>> result) {
                if (result.isOk()) {
                    DebitCardPayActivity.this.bankInfos = (List) result.getResult();
                }
            }

            public void onPreExcuete() {
            }
        });
    }
}
