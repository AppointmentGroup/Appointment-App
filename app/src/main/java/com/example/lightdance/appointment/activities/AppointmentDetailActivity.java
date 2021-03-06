package com.example.lightdance.appointment.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lightdance.appointment.Model.BrowserMsgBean;
import com.example.lightdance.appointment.Model.HistoryBean;
import com.example.lightdance.appointment.Model.UserBean;
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
    private String userObjectId;
    private int typeCode;

    /**
     * 定义userType用来保存当前用户相对于当前帖子的身份
     * <p>
     * USER_INVITER  = 1 发起人
     * USER_JOINED   = 2 成员
     * USER_PASSERBY = 3 路人
     */
    private int userType;
    private boolean isFull = false;
    final private int USER_INVITER = 1;
    final private int USER_JOINED = 2;
    final private int USER_PASSERBY = 3;
    final private int MANAGER_FIRSTLEVEL = 1001;
    final private int USER = 1000;
    final private int MODE_JOIN = 666;
    final private int MODE_QUIT = 999;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_detail);
        ButterKnife.bind(this);

        progressDialog = new ProgressDialog(AppointmentDetailActivity.this);
        progressDialog.setTitle("请稍等");
        progressDialog.setMessage("加载中...");
        progressDialog.show();

        SharedPreferences sharedPreferences = getSharedPreferences("loginData", MODE_PRIVATE);
        userObjectId = sharedPreferences.getString("userBeanId", "出错啦~");

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
                typeCode = browserMsgBean.getTypeCode();
                loadMsg(browserMsgBean);
            }
        });
    }

    /**
     * 更新HistoryBean表数据方法
     *
     * @param browserObjectId 需要被添加或删除的BrowserObjectId
     * @param userObjectId    用户的UserObjectId
     * @param mode            用户操作模式:添加/删除
     */
    private void updateHistoryBean(final String browserObjectId, final String userObjectId, int mode) {
        switch (mode) {
            //应约后 加入相应应约历史
            case MODE_JOIN:
                //将该用户对应的JoinedHistoryBean表中存入当前发布的BrowserObjectId
                BmobQuery<HistoryBean> q = new BmobQuery<>();
                q.addWhereEqualTo("userObjectId", userObjectId);
                q.findObjects(new FindListener<HistoryBean>() {
                    @Override
                    public void done(List<HistoryBean> list, BmobException e) {
                        if (e == null) {
                            //如果该用户未在表中建立数据，则创建 如果已经建立则添加
                            //e == null 即该用户在表中已创建数据 则完成添加即可
                            //当表中查不到该用户有创建过数据时，则创建
                            if (list.size() == 0 || list == null) {
                                final HistoryBean historyBean = new HistoryBean();
                                historyBean.setUserObjectId(userObjectId);
                                BmobQuery<UserBean> query = new BmobQuery<>();
                                query.getObject(userObjectId, new QueryListener<UserBean>() {
                                    @Override
                                    public void done(UserBean userBean, BmobException e) {
                                        if (e == null) {
                                            String userName = userBean.getUserName();
                                            historyBean.setValue("userName", userName);
                                            List<String> joinedList = new ArrayList<>();
                                            List<String> ongoingList = new ArrayList<>();
                                            joinedList.add(browserObjectId);
                                            ongoingList.add(browserObjectId);
                                            historyBean.setJoinedAppointment(joinedList);
                                            historyBean.setOngoingAppointment(ongoingList);
                                            historyBean.save(new SaveListener<String>() {
                                                @Override
                                                public void done(String s, BmobException e) {
                                                    if (e != null) {
                                                        Toast.makeText(AppointmentDetailActivity.this, "创建错误：" + e.getMessage(), Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });
                                        } else {
                                            Toast.makeText(AppointmentDetailActivity.this,
                                                    "创建时查询用户姓名出错：" + e.getMessage(),
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            } else {
                                HistoryBean historyBean = list.get(0);
                                List<String> joinedList = historyBean.getJoinedAppointment();
                                List<String> ongoingList = historyBean.getOngoingAppointment();
                                if (joinedList == null) {
                                    joinedList = new ArrayList<>();
                                }
                                if (ongoingList == null) {
                                    ongoingList = new ArrayList<>();
                                }
                                joinedList.add(browserObjectId);
                                ongoingList.add(browserObjectId);
                                historyBean.setValue("joinedAppointment", joinedList);
                                historyBean.setValue("ongoingAppointment", ongoingList);
                                historyBean.update(historyBean.getObjectId(), new UpdateListener() {
                                    @Override
                                    public void done(BmobException e) {
                                        if (e != null) {
                                            Toast.makeText(AppointmentDetailActivity.this,
                                                    "更新错误：" + e.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            }
                        } else {
                            Toast.makeText(AppointmentDetailActivity.this, "错误错误：" + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
                break;

            //取消某个应约 删掉相应应约历史
            case MODE_QUIT:
                //将该用户对应的HistoryBean表中joinedAppointment和ongoingAppointment中去除该活动记录
                BmobQuery<HistoryBean> q2 = new BmobQuery<>();
                q2.addWhereEqualTo("userObjectId", userObjectId);
                q2.findObjects(new FindListener<HistoryBean>() {
                    @Override
                    public void done(List<HistoryBean> list, BmobException e) {
                        if (e == null) {
                            if (list.size() != 0) {
                                //获取需要被操作的帖子的成员列表 查询其应约历史列表并删除该帖子的ObjectId
                                HistoryBean historyBean = list.get(0);
                                List<String> joinedList = historyBean.getJoinedAppointment();
                                List<String> ongoingList = historyBean.getOngoingAppointment();
                                joinedList = remove(joinedList, browserObjectId);
                                ongoingList = remove(ongoingList, browserObjectId);
                                historyBean.setValue("joinedAppointment", joinedList);
                                historyBean.setValue("ongoingAppointment", ongoingList);
                                historyBean.update(historyBean.getObjectId(), new UpdateListener() {
                                    @Override
                                    public void done(BmobException e) {
                                        if (e != null) {
                                            Toast.makeText(AppointmentDetailActivity.this,
                                                    "更新错误：" + e.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(AppointmentDetailActivity.this, "错误错误：" + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
                break;
            default:
                break;
        }
    }

    /**
     * 加载参与人员方法
     *
     * @param members 存了成员的UserBeanObjectId的List<String>
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
            isFull = false;
        } else {
            int personNumNeed = Integer.valueOf(personNumerNeed);
            int x = personNumNeed - personNumberHave;
            if (x == 0) {
                tvDetailedInfoMargin.setText("人满啦！");
                isFull = true;
            } else {
                tvDetailedInfoMargin.setText("还差" + x + "人");
                isFull = false;
            }
        }
        loadParticipant(browserMsgBean.getMembers());
        loadBtn();
    }

    /**
     * 加载按钮方法
     */
    public void loadBtn() {
        //获取当前用户ObjectId
        BmobQuery<UserBean> q = new BmobQuery<>();
        q.getObject(userObjectId, new QueryListener<UserBean>() {
            @Override
            public void done(UserBean userBean, BmobException e) {
                int levelCode = userBean.getLevelCode();
                switch (levelCode) {
                    case MANAGER_FIRSTLEVEL:
                        userType = MANAGER_FIRSTLEVEL;
                        detailedInfoTakePartIn.setBackgroundResource(R.drawable.bg_btn_press);
                        detailedInfoDelete.setText("删除");
                        break;
                    case USER:
                        BmobQuery<BrowserMsgBean> query = new BmobQuery<>();
                        query.getObject(objectId, new QueryListener<BrowserMsgBean>() {
                            @Override
                            public void done(BrowserMsgBean browserMsgBean, BmobException e) {
                                if (e == null) {
                                    //判断当前用户是否为发起人
                                    if (browserMsgBean.getInviter().equals(userObjectId)) {
                                        userType = USER_INVITER;
                                        detailedInfoTakePartIn.setText("编辑");
                                        detailedInfoDelete.setText("取消约人");
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
                                            if (isFull) {
                                                detailedInfoTakePartIn.setBackgroundResource(R.drawable.bg_btn_press);
                                                detailedInfoTakePartIn.setText("人满啦！");
                                            } else {
                                                detailedInfoTakePartIn.setText("应约");
                                            }
                                        }
                                    }
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
            }
        });
    }

    private void deleteByManager() {
        AlertDialog.Builder dialog1 = new AlertDialog.Builder(AppointmentDetailActivity.this);
        dialog1.setTitle("注意！");
        dialog1.setMessage("您正在删除本约人帖，请认真核对确认该帖为违法帖后再进行删除");
        dialog1.setCancelable(true);
        dialog1.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog1.setPositiveButton("确认删除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progressDialog.show();
                BmobQuery<BrowserMsgBean> query = new BmobQuery<>();
                query.getObject(objectId, new QueryListener<BrowserMsgBean>() {
                    @Override
                    public void done(BrowserMsgBean browserMsgBean, BmobException e) {
                        if (e == null) {
                            //获取成员信息并删除这些成员的本活动的应约记录方法
                            deleteMembersHistory(browserMsgBean);
                            int typeCode = browserMsgBean.getTypeCode();
                            int have = browserMsgBean.getPersonNumberHave();
                            BrowserMsgBean browserMsgBean1 = new BrowserMsgBean();
                            browserMsgBean1.setValue("typeCode", typeCode + 1000);
                            browserMsgBean1.setValue("personNumberHave", have);
                            browserMsgBean1.update(objectId, new UpdateListener() {
                                @Override
                                public void done(BmobException e) {
                                    if (e == null) {
                                        progressDialog.dismiss();
                                        Toast.makeText(AppointmentDetailActivity.this,
                                                "删除成功",
                                                Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        progressDialog.dismiss();
                                        Toast.makeText(AppointmentDetailActivity.this,
                                                "删除失败" + e.getMessage(),
                                                Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(AppointmentDetailActivity.this,
                                    "获取数据失败" + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });

            }
        });
        dialog1.show();
    }

    @OnClick({R.id.recyclerView_detailed_info, R.id.detailed_info_take_part_in, R.id.textView18, R.id.detailed_info_delete})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.detailed_info_delete:
                switch (userType) {
                    case MANAGER_FIRSTLEVEL:
                        deleteByManager();
                        break;
                    default:
                        deleteAppointment();
                        break;
                }
                break;
            case R.id.detailed_info_take_part_in:
                //通过objectId查询表内详细信息
                switch (userType) {
                    case USER_INVITER:
                        editAppointment();
                        break;
                    case USER_JOINED:
                        quitAppointment();
                        break;
                    case USER_PASSERBY:
                        joinAppointment();
                        break;
                    case MANAGER_FIRSTLEVEL:
                        Toast.makeText(this, "管理人员无法应约", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }

    /**
     * 发起人“编辑”功能逻辑
     */
    private void editAppointment() {
        Intent intent = new Intent(this, BrowserActivity.class);
        intent.putExtra("editObjectId", objectId);
        intent.putExtra("from", 3);
        startActivity(intent);
        finish();
    }

    /**
     * 路人“应约”逻辑代码
     */
    private void joinAppointment() {
        progressDialog.show();
        //应约前再次执行检查是否满员操作 保证数据实时性
        BmobQuery<BrowserMsgBean> q = new BmobQuery<>();
        q.getObject(objectId, new QueryListener<BrowserMsgBean>() {
            @Override
            public void done(BrowserMsgBean browserMsgBean, BmobException e) {
                if (e == null) {
                    String need1 = browserMsgBean.getPersonNumberNeed();
                    int have = browserMsgBean.getPersonNumberHave();
                    if (need1.equals("∞")) {
                        isFull = false;
                    } else {
                        int need = Integer.valueOf(need1);
                        if (have >= need) {
                            isFull = true;
                        } else {
                            isFull = false;
                        }
                    }
                    //根据检查结果执行不同的数据逻辑
                    if (isFull) {
                        Toast.makeText(AppointmentDetailActivity.this, "来晚了啊，已经满员发车了", Toast.LENGTH_LONG).show();
                        BmobQuery<BrowserMsgBean> query = new BmobQuery<>();
                        query.getObject(objectId, new QueryListener<BrowserMsgBean>() {
                            @Override
                            public void done(BrowserMsgBean browserMsgBean, BmobException e) {
                                typeCode = browserMsgBean.getTypeCode();
                                loadMsg(browserMsgBean);
                            }
                        });
                        progressDialog.dismiss();
                    } else {
                        BmobQuery<BrowserMsgBean> query = new BmobQuery<>();
                        query.getObject(objectId, new QueryListener<BrowserMsgBean>() {
                            @Override
                            public void done(BrowserMsgBean browserMsgBean, BmobException e) {
                                if (e == null) {
                                    //获取已参与人员列表
                                    List<String> memberBeanList = browserMsgBean.getMembers();
                                    int s = memberBeanList.size();
                                    //未参与 将当前用户添加到该活动的参与人员名单
                                    int typeCode = browserMsgBean.getTypeCode();
                                    BrowserMsgBean browserMsgBean2 = new BrowserMsgBean();
                                    List<String> members = browserMsgBean.getMembers();
                                    List<String> noCommentUser = browserMsgBean.getNoCommentUser();
                                    noCommentUser.add(userObjectId);
                                    members.add(userObjectId);
                                    //将当前用户添加到未反馈成员列表中
                                    browserMsgBean2.setNoCommentUser(noCommentUser);
                                    browserMsgBean2.setMembers(members);
                                    browserMsgBean2.setValue("typeCode", typeCode);
                                    //更改该活动的已参与人数+1
                                    browserMsgBean2.setValue("personNumberHave", s + 1);
                                    browserMsgBean2.update(objectId, new UpdateListener() {
                                        @Override
                                        public void done(BmobException e) {
                                            if (e == null) {
                                                //应约成功后更新当前详情页数据
                                                BmobQuery<BrowserMsgBean> query = new BmobQuery<>();
                                                query.getObject(objectId, new QueryListener<BrowserMsgBean>() {
                                                    @Override
                                                    public void done(BrowserMsgBean browserMsgBean, BmobException e) {
                                                        loadMsg(browserMsgBean);
                                                    }
                                                });
                                                Toast.makeText(AppointmentDetailActivity.this,
                                                        "应约成功！别放别人鸽子哟~",
                                                        Toast.LENGTH_SHORT).show();
                                                //更新应约历史记录数据
                                                updateHistoryBean(objectId, userObjectId, MODE_JOIN);
                                                progressDialog.dismiss();
                                            } else {
                                                Log.i("调试", "更新数组失败" + e.getMessage());
                                                Toast.makeText(AppointmentDetailActivity.this,
                                                        "出错在AppointmentDetailActivity的joinAppointment方法",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                } else {
                                    Toast.makeText(AppointmentDetailActivity.this, "错误 e=" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }

                            }
                        });
                    }
                } else {
                    Toast.makeText(AppointmentDetailActivity.this, "AppointmentDetailActivity的checkIsFull出错", Toast.LENGTH_LONG).show();
                    Log.i("调试", "出错" + e.getMessage());
                }
            }
        });
    }

    /**
     * 活动成员“取消应约”逻辑代码
     */
    private void quitAppointment() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(AppointmentDetailActivity.this);
        dialog.setTitle("注意");
        dialog.setMessage("你确定不去了嘛？想清楚了嘛？真的？");
        dialog.setCancelable(true);
        dialog.setNegativeButton("我再想想惹", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.setPositiveButton("真的不去了", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progressDialog.show();
                BmobQuery<BrowserMsgBean> query1 = new BmobQuery<>();
                query1.getObject(objectId, new QueryListener<BrowserMsgBean>() {
                    @Override
                    public void done(BrowserMsgBean browserMsgBean, BmobException e) {
                        if (e == null) {
                            //获取已参与人员列表
                            List<String> memberBeanList = browserMsgBean.getMembers();
                            int s = memberBeanList.size();
                            int typeCode = browserMsgBean.getTypeCode();
                            BrowserMsgBean browserMsgBean2 = new BrowserMsgBean();
                            //从参与成员列表中移除当前用户
                            List<String> members = browserMsgBean.getMembers();
                            members = remove(members, userObjectId);
                            browserMsgBean2.setMembers(members);
                            //从未反馈成员列表中移除当前用户
                            List<String> noCommentUser = browserMsgBean.getNoCommentUser();
                            noCommentUser = remove(noCommentUser, userObjectId);
                            browserMsgBean2.setNoCommentUser(noCommentUser);
                            browserMsgBean2.setValue("typeCode", typeCode);
                            browserMsgBean2.setValue("personNumberHave", s - 1);
                            browserMsgBean2.update(objectId, new UpdateListener() {
                                @Override
                                public void done(BmobException e) {
                                    if (e == null) {
                                        //取消应约成功后更新当前详情页数据
                                        BmobQuery<BrowserMsgBean> query = new BmobQuery<>();
                                        query.getObject(objectId, new QueryListener<BrowserMsgBean>() {
                                            @Override
                                            public void done(BrowserMsgBean browserMsgBean, BmobException e) {
                                                loadMsg(browserMsgBean);
                                            }
                                        });
                                        Toast.makeText(AppointmentDetailActivity.this,
                                                "取消应约成功！",
                                                Toast.LENGTH_SHORT).show();
                                        //更新应约历史记录数据
                                        updateHistoryBean(objectId, userObjectId, MODE_QUIT);
                                        progressDialog.dismiss();
                                    } else {
                                        Toast.makeText(AppointmentDetailActivity.this,
                                                "更新数组失败" + e.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(AppointmentDetailActivity.this, "编辑尚待开发", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
        dialog.show();
    }

    /**
     * 发起人“取消约人”逻辑代码
     */
    private void deleteAppointment() {
        AlertDialog.Builder dialog1 = new AlertDialog.Builder(AppointmentDetailActivity.this);
        dialog1.setTitle("注意！");
        dialog1.setMessage("有的人可能很期待，有的人可能正在热情准备着，你确定要取消本约人帖了嘛？");
        dialog1.setCancelable(true);
        dialog1.setNegativeButton("容我再考虑考虑", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog1.setPositiveButton("狠心取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AlertDialog.Builder dialog1 = new AlertDialog.Builder(AppointmentDetailActivity.this);
                dialog1.setTitle("取消约人须知");
                dialog1.setMessage("您正在取消本约人帖，一旦取消，该约人记录将被清除，无法恢复\n产生的任何后果自负");
                dialog1.setCancelable(true);
                dialog1.setNegativeButton("我再想想", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog1.setPositiveButton("我知道了\n依旧取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        progressDialog.show();
                        BmobQuery<HistoryBean> q = new BmobQuery<>();
                        q.addWhereEqualTo("userObjectId", userObjectId);
                        q.findObjects(new FindListener<HistoryBean>() {
                            @Override
                            public void done(List<HistoryBean> list, BmobException e) {
                                if (e == null) {
                                    if (list.size() != 0) {
                                        HistoryBean historyBean = list.get(0);
                                        List<String> organizeAppointment = historyBean.getOrganizeAppointment();
                                        List<String> ongoingAppointment = historyBean.getOngoingAppointment();
                                        for (int i = 0; i < organizeAppointment.size(); i++) {
                                            String item = organizeAppointment.get(i);
                                            if (item.equals(objectId)) {
                                                organizeAppointment.remove(objectId);
                                                break;
                                            }
                                        }
                                        for (int j = 0; j < ongoingAppointment.size(); j++) {
                                            String item = ongoingAppointment.get(j);
                                            if (item.equals(objectId)) {
                                                ongoingAppointment.remove(objectId);
                                                break;
                                            }
                                        }
                                        historyBean.setValue("organizeAppointment", organizeAppointment);
                                        historyBean.setValue("ongoingAppointment", ongoingAppointment);
                                        historyBean.update(historyBean.getObjectId(), new UpdateListener() {
                                            @Override
                                            public void done(BmobException e) {
                                                if (e != null) {
                                                    Toast.makeText(AppointmentDetailActivity.this, "出错啦", Toast.LENGTH_LONG).show();
                                                    Log.i("调试", "AppointmentDetailActivity更新HistoryBean出错" + e.getMessage());
                                                }
                                            }
                                        });
                                    }
                                } else {
                                    Log.i("调试", "取消约人时更改组织历史数据失败" + e.getMessage());
                                    Toast.makeText(AppointmentDetailActivity.this,
                                            "取消约人时更改组织历史数据失败",
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                        BmobQuery<BrowserMsgBean> query = new BmobQuery<>();
                        query.getObject(objectId, new QueryListener<BrowserMsgBean>() {
                            @Override
                            public void done(BrowserMsgBean browserMsgBean, BmobException e) {
                                if (e == null) {
                                    //获取成员信息并删除这些成员的本活动的应约记录方法
                                    deleteMembersHistory(browserMsgBean);
                                    BrowserMsgBean browserMsgBean1 = new BrowserMsgBean();
                                    browserMsgBean1.setObjectId(objectId);
                                    browserMsgBean1.delete(new UpdateListener() {
                                        @Override
                                        public void done(BmobException e) {
                                            if (e == null) {
                                                progressDialog.dismiss();
                                                Toast.makeText(AppointmentDetailActivity.this,
                                                        "取消约人成功",
                                                        Toast.LENGTH_SHORT).show();
                                                finish();
                                            } else {
                                                progressDialog.dismiss();
                                                Log.i("调试", "取消约人失败" + e.getMessage());
                                                Toast.makeText(AppointmentDetailActivity.this,
                                                        "取消约人失败" + e.getMessage(),
                                                        Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                                } else {
                                    progressDialog.dismiss();
                                    Toast.makeText(AppointmentDetailActivity.this,
                                            "获取数据失败" + e.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        });

                    }
                });
                dialog1.show();
            }
        });
        dialog1.show();
    }

    /**
     * 获取成员信息并删除这些成员的本活动的应约记录方法
     *
     * @param browserMsgBean 需要清理成员应约记录的BrowserBean
     */
    private void deleteMembersHistory(BrowserMsgBean browserMsgBean) {
        List<String> members = browserMsgBean.getMembers();
        for (int j = 0; j < members.size(); j++) {
            String item = members.get(j);
            if (item.equals(userObjectId)) {
                members.remove(userObjectId);
                break;
            }
        }
        for (int i = 0; i < members.size(); i++) {
            String member = members.get(i);
            updateHistoryBean(objectId, member, MODE_QUIT);
        }
    }

    /**
     * 删除List<String>中指定String方法
     *
     * @param list   需要被操作的List
     * @param target 需要被删除的目标String
     * @return 返回操作完成的List<String>
     */
    private List<String> remove(List<String> list, String target) {
        for (int i = list.size() - 1; i >= 0; i--) {
            String item = list.get(i);
            if (target.equals(item)) {
                list.remove(item);
            }
        }
        return list;
    }

}
