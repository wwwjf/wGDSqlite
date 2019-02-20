package com.xianghe.sqlite.manager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


import com.xianghe.sqlite.db.dao.DaoMaster;
import com.xianghe.sqlite.db.dao.UserDao;

import org.greenrobot.greendao.database.Database;

import static com.xianghe.sqlite.db.dao.DaoMaster.dropAllTables;

public class IvySqliteOpenHelper extends DaoMaster.OpenHelper {

    public IvySqliteOpenHelper(Context context, String name) {
        super(context, name);
    }

    public IvySqliteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    @Override
    public void onCreate(Database db) {
        super.onCreate(db);
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        super.onUpgrade(db, oldVersion, newVersion);
        Log.e("IvySqliteOpenHelper", "Upgrading schema from version " + oldVersion + " to " + newVersion + " by dropping all tables");

        if (newVersion > oldVersion) {
            // 升级、数据库迁移操作
//            MigrationHelper.getInstance().migrate(db, UserDao.class);
            MigrationHelper2.migrate(db,UserDao.class);
        }else {
            // 默认操作
            dropAllTables(db, true);
            onCreate(db);
        }

        /*dropAllTables(db, true);
        onCreate(db);*/
    }
}
