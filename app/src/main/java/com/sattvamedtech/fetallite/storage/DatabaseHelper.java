package com.sattvamedtech.fetallite.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.sattvamedtech.fetallite.helper.Logger;
import com.sattvamedtech.fetallite.model.Hospital;
import com.sattvamedtech.fetallite.model.Patient;
import com.sattvamedtech.fetallite.model.Test;
import com.sattvamedtech.fetallite.model.User;

import java.util.ArrayList;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
    private static final String DATABASE_NAME = "fetalLite.db";
    private static final int DATABASE_VERSION = 10;
    private static DatabaseHelper mDataBaseHelper;
    private static Dao<Hospital, Integer> mHospital = null;
    private static Dao<User, Integer> mUser = null;
    private static Dao<Patient, Integer> mPatient = null;
    private static Dao<Test, Integer> mTest = null;

    public DatabaseHelper(Context context, String databaseName) {
        super(context, databaseName, null, DATABASE_VERSION);
    }

    public static DatabaseHelper getInstance(Context context) {
        if (null == mDataBaseHelper) {
            mDataBaseHelper = new DatabaseHelper(context, DATABASE_NAME);
            try {
                mHospital = mDataBaseHelper.getHospitalDao();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                mUser = mDataBaseHelper.getUserDao();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                mPatient = mDataBaseHelper.getPatientDao();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                mTest = mDataBaseHelper.getTestDao();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return mDataBaseHelper;
    }

    /**
     * This is called when the database is first created. Usually you should
     * call createTable statements here to create the tables that will store
     * your data.
     */
    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        Logger.logInfo(DatabaseHelper.class.getName(), "onCreate");
        try {
            TableUtils.createTable(connectionSource, Hospital.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            TableUtils.createTable(connectionSource, User.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            TableUtils.createTable(connectionSource, Patient.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            TableUtils.createTable(connectionSource, Test.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This is called when your application is upgraded and it has a higher
     * version number. This allows you to adjust the various data to match the
     * new version number.
     */
    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        Logger.logInfo(DatabaseHelper.class.getName(), "onUpgrade");
        if (oldVersion == 9 && newVersion == 10) {
            try {
                TableUtils.dropTable(connectionSource, Test.class, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                TableUtils.createTable(connectionSource, Test.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                TableUtils.dropTable(connectionSource, Hospital.class, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                TableUtils.dropTable(connectionSource, User.class, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                TableUtils.dropTable(connectionSource, Patient.class, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                TableUtils.dropTable(connectionSource, Test.class, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            onCreate(database, connectionSource);
        }
    }

    /**
     * Returns the Database Access Object (DAO) for our Hospital class. It
     * will create it or just give the cached value.
     */
    private Dao<Hospital, Integer> getHospitalDao() throws Exception {
        if (mHospital == null) {
            mHospital = getDao(Hospital.class);
        }
        return mHospital;
    }

    /**
     * Returns the Database Access Object (DAO) for our User class. It
     * will create it or just give the cached value.
     */
    private Dao<User, Integer> getUserDao() throws Exception {
        if (mUser == null) {
            mUser = getDao(User.class);
        }
        return mUser;
    }

    /**
     * Returns the Database Access Object (DAO) for our Patient class. It
     * will create it or just give the cached value.
     */
    private Dao<Patient, Integer> getPatientDao() throws Exception {
        if (mPatient == null) {
            mPatient = getDao(Patient.class);
        }
        return mPatient;
    }

    /**
     * Returns the Database Access Object (DAO) for our Test class. It
     * will create it or just give the cached value.
     */
    private Dao<Test, Integer> getTestDao() throws Exception {
        if (mTest == null) {
            mTest = getDao(Test.class);
        }
        return mTest;
    }

    /**
     * Close the database connections and clear any cached DAOs.
     */
    @Override
    public void close() {
        super.close();
        mDataBaseHelper = null;
        mHospital = null;
        mUser = null;
        mPatient = null;
    }

    //************************ Hospital Data ************************

    public ArrayList<Hospital> getAllHospital() {
        try {
            QueryBuilder<Hospital, Integer> qb = mHospital.queryBuilder();
            return (ArrayList<Hospital>) qb.query();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public void addHospital(Hospital iUserData) {
        try {
            mHospital.createOrUpdate(iUserData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Hospital getHospitalById(int iId) {
        try {
            return mHospital.queryBuilder().where().eq("hospitalId", iId).queryForFirst();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void deleteHospital(int iId) {
        DeleteBuilder<Hospital, Integer> deleteBuilder = mHospital.deleteBuilder();
        try {
            deleteBuilder.where().eq("hospitalId", iId);
            deleteBuilder.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //************************ User Data ************************

    public ArrayList<User> getAllUsers() {
        try {
            QueryBuilder<User, Integer> qb = mUser.queryBuilder();
            return (ArrayList<User>) qb.where().eq("type", User.TYPE_USER).query();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public int getAllUsersCount() {
        return getAllUsers().size();
    }

    public ArrayList<User> getAllUsers(Hospital iHospital) {
        try {
            QueryBuilder<User, Integer> qb = mUser.queryBuilder();
            return (ArrayList<User>) qb.where().eq("type", User.TYPE_USER).and().eq("hospital_hospitalId", iHospital.hospitalId).query();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public int getAllUsersCount(Hospital iHospital) {
        return getAllUsers(iHospital).size();
    }

    public ArrayList<User> getAllDoctors() {
        try {
            QueryBuilder<User, Integer> qb = mUser.queryBuilder();
            return (ArrayList<User>) qb.where().eq("type", User.TYPE_DOCTOR).query();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public int getAllDoctorsCount() {
        return getAllDoctors().size();
    }

    public ArrayList<User> getAllDoctors(Hospital iHospital) {
        try {
            QueryBuilder<User, Integer> qb = mUser.queryBuilder();
            return (ArrayList<User>) qb.where().eq("type", User.TYPE_DOCTOR).and().eq("hospital_hospitalId", iHospital.hospitalId).query();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public int getAllDoctorsCount(Hospital iHospital) {
        return getAllDoctors(iHospital).size();
    }

    public void addUserDoctor(User iUserData) {
        try {
            mUser.createOrUpdate(iUserData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public User getUserDoctorById(int iId) {
        try {
            return mUser.queryBuilder().where().eq("id", iId).queryForFirst();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void deleteUserDoctor(int iId) {
        DeleteBuilder<User, Integer> deleteBuilder = mUser.deleteBuilder();
        try {
            deleteBuilder.where().eq("id", iId);
            deleteBuilder.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteAllOfHospital(Hospital iHospital) {
        DeleteBuilder<User, Integer> deleteBuilder = mUser.deleteBuilder();
        try {
            deleteBuilder.where().eq("hospital_hospitalId", iHospital.hospitalId);
            deleteBuilder.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean usernameExists(String iUsername) {
        try {
            QueryBuilder<User, Integer> qb = mUser.queryBuilder();
            return (qb.where().eq("username", iUsername).countOf() > 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public User validUserCredentials(String iUsername, String iPassword) {
        try {
            QueryBuilder<User, Integer> qb = mUser.queryBuilder();
            return qb.where().eq("username", iUsername).and().eq("password", iPassword).queryForFirst();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //************************ Patient Data ************************

    public ArrayList<Patient> getAllPatient() {
        try {
            QueryBuilder<Patient, Integer> qb = mPatient.queryBuilder();
            return (ArrayList<Patient>) qb.query();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public void addPatient(Patient iUserData) {
        try {
            mPatient.createOrUpdate(iUserData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Patient getPatientById(int iId) {
        try {
            return mPatient.queryBuilder().where().eq("id", iId).queryForFirst();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Patient getPatientByIdNameDob(String iId, long iDob, String iFirstName, String iLastName) {
        try {
            QueryBuilder<Patient, Integer> qb = mPatient.queryBuilder();
            return qb.where().eq("id", iId).and().eq("dob", iDob).and().like("firstName", iFirstName).and().like("lastName", iLastName).queryForFirst();
//            if (!TextUtils.isEmpty(iId))
//                return qb.where().eq("id", iId).queryForFirst();
//            else if (iDob > 0)
//                return qb.where().eq("dob", iDob).queryForFirst();
//            else
//                return qb.where().like("firstName", iFirstName).and().like("lastName", iLastName).queryForFirst();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void deletePatient(int iId) {
        DeleteBuilder<Patient, Integer> deleteBuilder = mPatient.deleteBuilder();
        try {
            deleteBuilder.where().eq("id", iId);
            deleteBuilder.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //************************ Test Data ************************

    public ArrayList<Test> getAllTest() {
        try {
            QueryBuilder<Test, Integer> qb = mTest.queryBuilder();
            return (ArrayList<Test>) qb.orderBy("timeStamp", false).query();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public ArrayList<Test> getAllTestByHospital(Hospital iHospital) {
        try {
            QueryBuilder<Test, Integer> qb = mTest.queryBuilder();
            if (iHospital == null)
                return (ArrayList<Test>) qb.orderBy("timeStamp", false).query();
            else
                return (ArrayList<Test>) qb.orderBy("timeStamp", false).where().eq("hospital_hospitalId", iHospital.hospitalId).query();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public ArrayList<Test> getAllTestByPatient(Patient iPatient, Hospital iHospital) {
        try {
            QueryBuilder<Test, Integer> qb = mTest.queryBuilder();
            return (ArrayList<Test>) qb.where().eq("patient_id", iPatient.id).and().eq("hospital_hospitalId", iHospital.hospitalId).query();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public ArrayList<Test> getAllTestByPatient(String iPatientDetail, long iTestTimeStamp, Hospital iHospital) {
        try {
            QueryBuilder<Test, Integer> qbTest = mTest.queryBuilder();
            if (iTestTimeStamp > 0)
                qbTest.where().eq("timeStamp", iTestTimeStamp).and().eq("hospital_hospitalId", iHospital.hospitalId);
            else
                qbTest.where().eq("hospital_hospitalId", iHospital.hospitalId);

            if (!TextUtils.isEmpty(iPatientDetail)) {
                QueryBuilder<Patient, Integer> qbPatient = mPatient.queryBuilder();
                qbPatient.where().eq("id", iPatientDetail).or().eq("firstName", iPatientDetail).or().eq("lastName", iPatientDetail);
                qbTest.join(qbPatient);
            }

            return (ArrayList<Test>) qbTest.orderBy("timeStamp", false).query();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public int getAllTestCount() {
        return getAllTest().size();
    }

    public void addTest(Test iUserData) {
        try {
            mTest.createOrUpdate(iUserData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Test getTestById(int iId) {
        try {
            return mTest.queryBuilder().where().eq("id", iId).queryForFirst();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void deleteTest(int iId) {
        DeleteBuilder<Test, Integer> deleteBuilder = mTest.deleteBuilder();
        try {
            deleteBuilder.where().eq("id", iId);
            deleteBuilder.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
