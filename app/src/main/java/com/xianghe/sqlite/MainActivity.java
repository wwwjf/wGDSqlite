
package com.xianghe.sqlite;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.xianghe.sqlite.db.User;
import com.xianghe.sqlite.db.dao.UserDao;
import com.xianghe.sqlite.manager.DbManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Button mBtnAdd;
    private TextView mTvData;

    private UserDao mUserDao;
    private List<User> mUserList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBtnAdd = findViewById(R.id.add_btn);
        mTvData = findViewById(R.id.data_tv);

        mUserDao = DbManager.getInstance().getUserDao();
        mUserList = new ArrayList<>();

        updateData();
        mBtnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User user = new User();
                user.setName("张三-"+System.currentTimeMillis());
                user.setAge(20);
                if (mUserDao != null) {
                    mUserDao.insert(user);
                }
                updateData();
            }
        });
    }

    private void updateData() {
        StringBuilder sbData = new StringBuilder();
        mUserList.clear();
        if (mUserDao == null){
            return;
        }
        mUserList.addAll(mUserDao.queryBuilder().list());
        for (User user : mUserList) {
            sbData.append(user.getName()).append(":").append(user.getAge()).append("\n");
        }
        mTvData.setText(sbData.toString());
    }
}
