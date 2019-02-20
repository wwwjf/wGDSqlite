package com.xianghe.sqlite.manager;

import com.xianghe.sqlite.App;
import com.xianghe.sqlite.db.dao.DaoMaster;
import com.xianghe.sqlite.db.dao.DaoSession;
import com.xianghe.sqlite.db.dao.UserDao;


public class DbManager {
    private static final String mDbName = "GDDemo.db";
    private volatile static DbManager mDbManager;
    private DaoSession mDaoSession;

    public static DbManager getInstance() {
        if (mDbManager == null) {
            synchronized (DbManager.class) {
                if (mDbManager == null) {
                    mDbManager = new DbManager();
                }
            }
        }
        return mDbManager;
    }

    public void init() {
        mDaoSession = new DaoMaster(new IvySqliteOpenHelper(App.getInstance().getApplicationContext(), mDbName)
                .getWritableDb())
                .newSession();
    }

    public DaoSession getDaoSession() {
        return mDaoSession;
    }


    public UserDao getUserDao() {
        return mDaoSession != null ? mDaoSession.getUserDao() : null;
    }
}
