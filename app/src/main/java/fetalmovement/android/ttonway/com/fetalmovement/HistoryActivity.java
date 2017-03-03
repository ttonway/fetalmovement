package fetalmovement.android.ttonway.com.fetalmovement;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import fetalmovement.android.ttonway.com.fetalmovement.model.DayStatistic;
import fetalmovement.android.ttonway.com.fetalmovement.model.OneHourStatistic;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by ttonway on 2017/2/28.
 */

public class HistoryActivity extends AppCompatActivity {
    private static final String TAG = HistoryActivity.class.getSimpleName();

    @BindView(R.id.compactcalendar_view)
    CompactCalendarView mCalendarView;
    @BindView(R.id.list_view)
    ListView mListView;

    TextView mHeaderTextView1;
    TextView mHeaderTextView2;

    DateFormat mMonthFormat;
    DateFormat mDateFormat;
    DateFormat mTimeFormat;

    Realm mRealm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        ButterKnife.bind(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mMonthFormat = new SimpleDateFormat("yyyy-MMM");
        mDateFormat = DateFormat.getDateInstance();
        mTimeFormat = DateFormat.getTimeInstance();


        mRealm = Realm.getDefaultInstance();

        View header = getLayoutInflater().inflate(R.layout.list_header_statics, null);
        mHeaderTextView1 = (TextView) header.findViewById(R.id.text1);
        mHeaderTextView2 = (TextView) header.findViewById(R.id.text2);
        mListView.addHeaderView(header);

        mCalendarView.setFirstDayOfWeek(Calendar.MONDAY);
        mCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                Log.d(TAG, "Day was clicked: " + dateClicked);
                selectDate(dateClicked);
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                Log.d(TAG, "Month was scrolled to: " + firstDayOfNewMonth);
                setTitle(mMonthFormat.format(firstDayOfNewMonth));
            }
        });

        setTitle(mMonthFormat.format(mCalendarView.getFirstDayOfCurrentMonth()));
        selectDate(new Date());
    }

    void selectDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long begin = calendar.getTimeInMillis();
        calendar.add(Calendar.HOUR_OF_DAY, 24);
        long end = calendar.getTimeInMillis();

        DayStatistic dayStatistic = mRealm.where(DayStatistic.class).equalTo("beginTime", begin).findFirst();
        if (dayStatistic == null) {
            mHeaderTextView1.setText(null);
            mHeaderTextView2.setText(null);
        } else {
            mHeaderTextView1.setText(mDateFormat.format(new Date(dayStatistic.getBeginTime())));
            mHeaderTextView2.setText(String.valueOf(dayStatistic.getMovements()));
        }

        RealmResults<OneHourStatistic> hourStatistics = mRealm.where(OneHourStatistic.class)
                .greaterThan("beginTime", begin).lessThan("beginTime", end)
                .findAll().sort("beginTime", Sort.DESCENDING);

        ArrayAdapter<OneHourStatistic> adapter = new ArrayAdapter<OneHourStatistic>(this, 0, hourStatistics) {

            @NonNull
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.list_item_statics, parent, false);
                }
                TextView textView1 = (TextView) convertView.findViewById(R.id.text1);
                TextView textView2 = (TextView) convertView.findViewById(R.id.text2);

                OneHourStatistic s = getItem(position);
                textView1.setText(mTimeFormat.format(new Date(s.getBeginTime())) + " - " + mTimeFormat.format(new Date(s.getEndTime())));
                textView2.setText(String.valueOf(s.getMovements()));

                return convertView;
            }
        };
        mListView.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mRealm.close();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
