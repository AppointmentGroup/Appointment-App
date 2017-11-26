package com.example.lightdance.appointment.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lightdance.appointment.Model.BrowserMsgBean;
import com.example.lightdance.appointment.Model.JoinedHistoryBean;
import com.example.lightdance.appointment.R;
import com.example.lightdance.appointment.adapters.ParticipantAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * @author pope
 */

public class AppointmentDetailActivity extends AppCompatActivity {

    @BindView(R.id.tv_detailed_info_title)
    TextView tvDetailedInfoTitle;
    @BindView(R.id.tv_detailed_info_place)
    TextView tvDetailedInfoPlace;
    @BindView(R.id.tv_detailed_info_starttime)
    TextView tvDetailedInfoStarttime;
    @BindView(R.id.tv_detailed_info_endtime)
    TextView tvDetailedInfoEndtime;
    @BindView(R.id.tv_detailed_info_description)
    TextView tvDetailedInfoDescription;
    @BindView(R.id.tv_detailed_info_connection)
    TextView tvDetailedInfoConnection;
    @BindView(R.id.toolbar_appointmentdetail)
    Toolbar mToolbar;
    @BindView(R.id.tv_detailed_info_margin)
    TextView tvDetailedInfoMargin;
    @BindView(R.id.tv_detailed_info_headcount)
    TextView tvDetailedInfoHeadcount;
    @BindView(R.id.detailed_info_delete)
    Button detailedInfoDelete;
    @BindView(R.id.detailed_info_take_part_in)
    Button detailedInfoTakePartIn;


    private ProgressDialog progressDialog;
    private String objectId;

    /**
     * 定义userType用来保存当前用户相对于当前帖子的身份
     * <p>
     * USER_INVITER = 1 发起人
     * USER_JOINED = 2 成员
     * USER_PASSERBY = 3 路人
     */
    private int userType;
    final private int USER_INVITER = 1;
    final private int USER_JOINED = 2;
    final private int USER_PASSERBY = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_detail);
        ButterKnife.bind(this);

        progressDialog = new ProgressDialog(AppointmentDetailActivity.this);
        progressDialog.setTitle("请稍等");
        progressDialog.setMessage("加载中...");
        progressDialog.show();

        //toolbar
        mToolbar.setTitle("活动详情");
        mToolbar.setTitleTextColor(Color.parseColor("#ffffff"));
        mToolbar.setNavigationIcon(R.mipmap.ic_back_white);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //获取该活动的ObjectId
        Intent intent = getIntent();
        objectId = intent.getStringExtra("objectId");

        //查询该活动表中的详细活动信息
        BmobQuery<BrowserMsgBean> query = new BmobQuery<>();
        query.getObject(objectId, new QueryListener<BrowserMsgBean>() {
            @Override
            public void done(BrowserMsgBean browserMsgBean, BmobException e) {
                loadMsg(browserMsgBean);
                loadParticipant(browserMsgBean.getMembers());
                loadBtn();
            }
        });
    }

    /**
     * 更新JoinedHistoryBean表数据方法
     *
     * @param browserObjectId 需要被添加的BrowserObjectId
     * @param userObjectId    用户的UserObjectId
     */
    private void updateJoinedHistory(final String browserObjectId, final String userObjectId) {
        //将该用户对应的JoinedHistoryBean表中存入当前发布的BrowserObjectId
        BmobQuery<JoinedHistoryBean> query1 = new BmobQuery<>();
        query1.addWhereEqualTo("userObjectId", userObjectId);
        query1.setLimit(10);
        query1.findObjects(new FindListener<JoinedHistoryBean>() {
            @Override
            public void done(List<JoinedHistoryBean> list, BmobException e) {
                if (e == null) {
                    //如果该用户未在表中建立数据，则创建 如果已经建立则添加
                    //e == null 即该用户在表中已创建数据 则完成添加即可
                    //当表中查不到该用户有创建过数据时，则创建
                    if (list.size() == 0) {
                        JoinedHistoryBean historyBean = new JoinedHistoryBean();
                        historyBean.setUserObjectId(userObjectId);
                        List<String> browserIdList = new ArrayList<>();
                        browserIdList.add(browserObjectId);
                        historyBean.setBrowserIdList(browserIdList);
                        historyBean.save(new SaveListener<String>() {
                            @Override
                            public void done(String s, BmobException e) {
                                if (e != null) {
                                    Toast.makeText(AppointmentDetailActivity.this, "创建错误：" + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    } else {
                        JoinedHistoryBean joinedHistoryBean = list.get(0);
                        JoinedHistoryBean joinedHistoryBean1 = new JoinedHistoryBean();
                        List<String> idList = joinedHistoryBean.getBrowserIdList();
                        idList.add(browserObjectId);
                        joinedHistoryBean1.setValue("browserIdList", idList);
                        joinedHistoryBean1.update(joinedHistoryBean.getObjectId(), new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
                                if (e != null) {
                                    Toast.makeText(AppointmentDetailActivity.this,
                                            "更新错误：" + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }

                } else if (e.getMessage().equals("object not found for JoinedHistoryBean.")) {
                    Toast.makeText(AppointmentDetailActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(AppointmentDetailActivity.this, "错误错误：" + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * 加载参与人员方法
     *
     * @param members
     */
    private void loadParticipant(List<String> members) {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView_detailed_info);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);
        ParticipantAdapter adapter = new ParticipantAdapter(this, members);
        recyclerView.setAdapter(adapter);
        adapter.setItemOnclickListener(new ParticipantAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                Intent i = new Intent(AppointmentDetailActivity.this, MemberDetailActivity.class);
                i.putExtra("objectId", objectId);
                startActivity(i);
            }
        });
        progressDialog.dismiss();
    }

    /**
     * 加载活动信息方法
     *
     * @param browserMsgBean
     */
    public void loadMsg(BrowserMsgBean browserMsgBean) {
        tvDetailedInfoTitle.setText(browserMsgBean.getTitle());
        tvDetailedInfoPlace.setText(browserMsgBean.getPlace());
        tvDetailedInfoStarttime.setText(browserMsgBean.getStartTime());
        tvDetailedInfoEndtime.setText(browserMsgBean.getEndTime());
        tvDetailedInfoDescription.setText(browserMsgBean.getContent());
        tvDetailedInfoConnection.setText(browserMsgBean.getContactWay());
        int personNumberHave = browserMsgBean.getPersonNumberHave();
        tvDetailedInfoHeadcount.setText("共" + personNumberHave + "人/");
        String personNumerNeed = browserMsgBean.getPersonNumberNeed();
        if (personNumerNeed == null) {
            Toast.makeText(this, "数据出错", Toast.LENGTH_LONG).show();
        } else if (personNumerNeed.equals("∞")) {
            tvDetailedInfoMargin.setText("能来多少人来多少");
        } else {
            int personNumNeed = Integer.valueOf(personNumerNeed);
            int x = personNumNeed - personNumberHave;
            if (x == 0) {
                tvDetailedInfoMargin.setText("人满啦！");
            } else {
                tvDetailedInfoMargin.setText("还差" + x + "人");
            }
        }
    }

    /**
     * 加载按钮方法
     */
    public void loadBtn() {
        //获取当前用户ObjectId
        SharedPreferences preferences = getSharedPreferences("loginData", MODE_PRIVATE);
        final String userObjectId = preferences.getString("userBeanId", "错误啦啊啊啊~");
        BmobQuery<BrowserMsgBean> query = new BmobQuery<>();
        query.getObject(objectId, new QueryListener<BrowserMsgBean>() {
            @Override
            public void done(BrowserMsgBean browserMsgBean, BmobException e) {
                if (e == null) {
                    //判断当前用户是否为发起人
                    if (browserMsgBean.getInviter().equals(userObjectId)) {
                        userType = USER_INVITER;
                        detailedInfoTakePartIn.setText("编辑");
                        detailedInfoDelete.setText("删除");
                    } else {
                        detailedInfoDelete.setVisibility(View.GONE);
                        //获取已参与人员列表
                        List<String> memberBeanList = browserMsgBean.getMembers();
                        int s = memberBeanList.size();
                        //检测当前用户是否已经在该活动已参与人员列表
                        boolean isJoined = false;
                        for (int i = 0; i < s; i++) {
                            String s1 = memberBeanList.get(i);
                            if (s1.equals(userObjectId)) {
                                isJoined = true;
                                break;
                            }
                        }
                        if (isJoined) {
                            userType = USER_JOINED;
                            detailedInfoTakePartIn.setText("取消应约");
                        } else {
                            userType = USER_PASSERBY;
                            detailedInfoTakePartIn.setText("应约");
                        }
                    }
                } else {
                    Toast.makeText(AppointmentDetailActivity.this, "错误 e=" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }

            }
        });
    }

    @OnClick({R.id.recyclerView_detailed_info, R.id.detailed_info_take_part_in, R.id.textView18,R.id.detailed_info_delete})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.detailed_info_delete:
                progressDialog.show();
                Toast.makeText(AppointmentDetailActivity.this,"删除尚待开发",Toast.LENGTH_LONG).show();
                break;
            case R.id.detailed_info_take_part_in:
                progressDialog.show();
                //通过objectId查询表内详细信息
                switch (userType) {
                    case USER_INVITER:
                        Toast.makeText(AppointmentDetailActivity.this,"编辑尚待开发",Toast.LENGTH_LONG).show();
                        break;
                    case USER_JOINED:
                        Toast.makeText(AppointmentDetailActivity.this,"取消应约尚待开发",Toast.LENGTH_LONG).show();
                        break;
                    case USER_PASSERBY:
                        BmobQuery<BrowserMsgBean> query = new BmobQuery<>();
                        query.getObject(objectId, new QueryListener<BrowserMsgBean>() {
                            @Override
                            public void done(BrowserMsgBean browserMsgBean, BmobException e) {
                                if (e == null) {
                                    //获取当前用户ObjectId
                                    SharedPreferences preferences = getSharedPreferences("loginData", MODE_PRIVATE);
                                    final String userObjectId = preferences.getString("userBeanId", "错误啦啊啊啊~");
                                    //获取已参与人员列表
                                    List<String> memberBeanList = browserMsgBean.getMembers();
                                    int s = memberBeanList.size();
                                    //未参与 将当前用户添加到该活动的参与人员名单
                                    int typeCode = browserMsgBean.getTypeCode();
                                    BrowserMsgBean browserMsgBean2 = new BrowserMsgBean();
                                    List<String> members = browserMsgBean.getMembers();
                                    members.add(userObjectId);
                                    browserMsgBean2.setMembers(members);
                                    browserMsgBean2.setValue("typeCode", typeCode);
                                    browserMsgBean2.setValue("personNumberHave", s + 1);
                                    browserMsgBean2.update(objectId, new UpdateListener() {
                                        @Override
                                        public void done(BmobException e) {
                                            if (e == null) {
                                            } else {
                                                Toast.makeText(AppointmentDetailActivity.this,
                                                        "更新数组失败" + e.getMessage(),
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                    //更改该活动的已参与人数+1
                                    BrowserMsgBean browserMsgBean1 = new BrowserMsgBean();
                                    browserMsgBean1.setValue("typeCode", typeCode);
                                    browserMsgBean1.setValue("personNumberHave", s + 1);
                                    browserMsgBean1.update(objectId, new UpdateListener() {
                                        @Override
                                        public void done(BmobException e) {
                                            if (e == null) {
                                                //应约成功后更新当前详情页数据
                                                BmobQuery<BrowserMsgBean> query = new BmobQuery<>();
                                                query.getObject(objectId, new QueryListener<BrowserMsgBean>() {
                                                    @Override
                                                    public void done(BrowserMsgBean browserMsgBean, BmobException e) {
                                                        loadMsg(browserMsgBean);
                                                        loadParticipant(browserMsgBean.getMembers());
                                                        loadBtn();
                                                    }
                                                });
                                                Toast.makeText(AppointmentDetailActivity.this,
                                                        "应约成功！别放别人鸽子哟~",
                                                        Toast.LENGTH_SHORT).show();
                                                updateJoinedHistory(objectId, userObjectId);
                                                progressDialog.dismiss();
                                            } else {
                                                Toast.makeText(AppointmentDetailActivity.this,
                                                        "人数更新失败 e=" + e.getMessage(),
                                                        Toast.LENGTH_SHORT).show();
                                                progressDialog.dismiss();
                                            }
                                        }
                                    });
                                } else {
                                    Toast.makeText(AppointmentDetailActivity.this, "错误 e=" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }

                            }
                        });
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }
}
