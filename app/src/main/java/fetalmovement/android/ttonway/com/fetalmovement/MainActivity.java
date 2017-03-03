package fetalmovement.android.ttonway.com.fetalmovement;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fetalmovement.android.ttonway.com.fetalmovement.model.DayStatistic;
import fetalmovement.android.ttonway.com.fetalmovement.model.OneHourStatistic;
import io.realm.Realm;

public class MainActivity extends AppCompatActivity {

    private static final long FIVE_MINUTES = 5 * 60 * 1000L;

    @BindView(R.id.text_day_time)
    TextView mDayTimeTextView;
    @BindView(R.id.text_hour_time)
    TextView mHourTimeTextView;
    @BindView(R.id.text_day)
    TextView mDayTextView;
    @BindView(R.id.text_hour)
    TextView mHourTextView;
    @BindView(R.id.text_day_last_time)
    TextView mDayLastTextView;
    @BindView(R.id.text_hour_last_time)
    TextView mHourLastTextView;
    @BindView(R.id.btn_begin_one_hour)
    Button mBeginHourButton;
    @BindView(R.id.btn_add_move)
    Button mAddButton;

    Realm mRealm;
    DayStatistic mDayStatistic;
    OneHourStatistic mOneHourStatistic;

    DateFormat mDateFormat;
    DateFormat mTimeFormat;

    Handler mHandler;
    final Runnable mClockRunnable = new Runnable() {
        @Override
        public void run() {

            long now = System.currentTimeMillis();

            if (now >= mDayStatistic.getEndTime()) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(now);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                mRealm.beginTransaction();
                mDayStatistic = mRealm.createObject(DayStatistic.class);
                mDayStatistic.setBeginTime(calendar.getTimeInMillis());
                calendar.add(Calendar.HOUR_OF_DAY, 24);
                mDayStatistic.setEndTime(calendar.getTimeInMillis());
                mDayStatistic.setMovements(0);
                mRealm.commitTransaction();

                mDayTextView.setText(String.valueOf(mDayStatistic.getMovements()));
                mDayTimeTextView.setText(mDateFormat.format(new Date(mDayStatistic.getBeginTime())));
            }

            if (mOneHourStatistic != null && now >= mOneHourStatistic.getEndTime()) {
                mOneHourStatistic = null;
                mHourTextView.setText(null);
                mHourTimeTextView.setText(" ");
                mBeginHourButton.setVisibility(View.VISIBLE);
            }


            if (mDayStatistic == null || mDayStatistic.getLastTime() == 0) {
                mDayLastTextView.setText(" ");
            } else {
                mDayLastTextView.setText(getString(R.string.label_last_time_x, formatTimeInterval3(now - mDayStatistic.getLastTime())));
            }
            if (mOneHourStatistic == null || mOneHourStatistic.getLastTime() == 0) {
                mHourLastTextView.setText(" ");
            } else {
                mHourLastTextView.setText(getString(R.string.label_last_time_x, formatTimeInterval2(now - mOneHourStatistic.getLastTime())));
            }

            mHandler.postDelayed(mClockRunnable, 1000L);
        }
    };

    public String formatTimeInterval2(long interval) {
        int seconds = (int) (interval / 1000);
        int hour = seconds / 3600;
        int min = seconds / 60 - hour * 60;
        int sec = seconds - min * 60 - hour * 3600;
        return String.format("%02d:%02d", min, sec);
    }

    public String formatTimeInterval3(long interval) {
        int seconds = (int) (interval / 1000);
        int hour = seconds / 3600;
        int min = seconds / 60 - hour * 60;
        int sec = seconds - min * 60 - hour * 3600;
        return String.format("%02d:%02d:%02d", hour, min, sec);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mHandler = new Handler();
        mDateFormat = DateFormat.getDateInstance();
        mTimeFormat = DateFormat.getTimeInstance();

        setupRealm();

        mDayTextView.setText(String.valueOf(mDayStatistic.getMovements()));
        mDayTimeTextView.setText(mDateFormat.format(new Date(mDayStatistic.getBeginTime())));
        if (mOneHourStatistic == null) {
            mHourTextView.setText(null);
            mHourTimeTextView.setText(" ");
            mBeginHourButton.setVisibility(View.VISIBLE);
        } else {
            mHourTextView.setText(String.valueOf(mOneHourStatistic.getMovements()));
            mHourTimeTextView.setText(mTimeFormat.format(new Date(mOneHourStatistic.getBeginTime())) + " - " + mTimeFormat.format(new Date(mOneHourStatistic.getEndTime())));
            mBeginHourButton.setVisibility(View.GONE);
        }

        mHandler.post(mClockRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mHandler.removeCallbacks(mClockRunnable);

        mRealm.close();
    }

    void setupRealm() {
        mRealm = Realm.getDefaultInstance();

        long now = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(now);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        mDayStatistic = mRealm.where(DayStatistic.class).equalTo("beginTime", calendar.getTimeInMillis()).findFirst();
        if (mDayStatistic == null) {
            mRealm.beginTransaction();
            mDayStatistic = mRealm.createObject(DayStatistic.class);
            mDayStatistic.setBeginTime(calendar.getTimeInMillis());
            calendar.add(Calendar.HOUR_OF_DAY, 24);
            mDayStatistic.setEndTime(calendar.getTimeInMillis());
            mDayStatistic.setMovements(0);
            mRealm.commitTransaction();
        }

        mOneHourStatistic = mRealm.where(OneHourStatistic.class).greaterThanOrEqualTo("endTime", now).lessThan("beginTime", now).findFirst();
    }


    @OnClick(R.id.btn_begin_one_hour)
    public void beginOneHourStatistic() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        mRealm.beginTransaction();
        mOneHourStatistic = mRealm.createObject(OneHourStatistic.class);
        mOneHourStatistic.setBeginTime(calendar.getTimeInMillis());
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        mOneHourStatistic.setEndTime(calendar.getTimeInMillis());
        mOneHourStatistic.setMovements(0);
        mRealm.commitTransaction();

        mHourTextView.setText(String.valueOf(mOneHourStatistic.getMovements()));
        mHourTimeTextView.setText(mTimeFormat.format(new Date(mOneHourStatistic.getBeginTime())) + " - " + mTimeFormat.format(new Date(mOneHourStatistic.getEndTime())));
        mBeginHourButton.setVisibility(View.GONE);
    }

    @OnClick(R.id.btn_add_move)
    public void addMovement() {
        long now = System.currentTimeMillis();

        mRealm.beginTransaction();
        if (now - mDayStatistic.getLastTime() > FIVE_MINUTES) {
            mDayStatistic.setMovements(mDayStatistic.getMovements() + 1);
            mDayStatistic.setLastTime(now);
        }

        if (mOneHourStatistic != null) {
            if (now - mOneHourStatistic.getLastTime() > FIVE_MINUTES) {
                mOneHourStatistic.setMovements(mOneHourStatistic.getMovements() + 1);
                mOneHourStatistic.setLastTime(now);
            }
        }
        mRealm.commitTransaction();

        mDayTextView.setText(String.valueOf(mDayStatistic.getMovements()));
        mDayLastTextView.setText(getString(R.string.label_last_time_x, formatTimeInterval3(now - mDayStatistic.getLastTime())));
        if (mOneHourStatistic != null) {
            mHourTextView.setText(String.valueOf(mOneHourStatistic.getMovements()));
            mHourLastTextView.setText(getString(R.string.label_last_time_x, formatTimeInterval2(now - mOneHourStatistic.getLastTime())));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_history:
                startActivity(new Intent(this, HistoryActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
