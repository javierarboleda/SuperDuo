package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;

/**
 * Created by Hype on 11/10/2015.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class WidgetRemoteViewsService extends RemoteViewsService {

    public static final String LOG_TAG = WidgetRemoteViewsService.class.getSimpleName();

    private static final String[] SCORES_COLUMNS = {
            DatabaseContract.scores_table.LEAGUE_COL,
            DatabaseContract.scores_table.DATE_COL,
            DatabaseContract.scores_table.TIME_COL,
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL,
            DatabaseContract.scores_table.MATCH_ID,
            DatabaseContract.scores_table.MATCH_DAY
    };
    static final int INDEX_LEAGUE_COL = 5;
    static final int INDEX_DATE_COL = 1;
    static final int INDEX_TIME_COL = 2;
    static final int INDEX_HOME_COL = 3;
    static final int INDEX_AWAY_COL = 4;
    static final int INDEX_HOME_GOALS_COL = 6;
    static final int INDEX_AWAY_GOALS_COL = 7;
    static final int INDEX_MATCH_ID = 0;
    static final int INDEX_MATCH_DAY = 9;

    private String[] fragmentdate = new String[5];

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        return new RemoteViewsFactory() {
            private Cursor mDataCursor = null;

            @Override
            public void onCreate() {
                Log.d(LOG_TAG, "onCreate()");
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                Log.d(LOG_TAG, "onDataSetChanged()");

                if (mDataCursor != null) {
                    mDataCursor.close();
                }

                final long identityToken = Binder.clearCallingIdentity();
//                Uri scoresUri = DatabaseContract.scores_table.buildScoreWithDate();
                Uri scoresUri = DatabaseContract.BASE_CONTENT_URI;
//                fragmentdate[0] = "2015-11-10";
//                fragmentdate[1] = "2015-11-11";
//                fragmentdate[2] = "2015-11-12";
//                fragmentdate[3] = "2015-11-13";
//                fragmentdate[4] = "2015-11-14";
//                mDataCursor = getContentResolver().query(scoresUri,
//                        SCORES_COLUMNS,
//                        null,
//                        fragmentdate,
//                        null);
                mDataCursor = getContentResolver().query(scoresUri,
                        null,
                        null,
                        null,
                        null);
                Binder.restoreCallingIdentity(identityToken);

            }

            @Override
            public RemoteViews getViewAt(int position) {
                Log.d(LOG_TAG, "getViewAt(int position): position=" + position);
                if (position == AdapterView.INVALID_POSITION ||
                        mDataCursor == null || !mDataCursor.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_games_list_item);

//                static final int INDEX_LEAGUE_COL = 0;
//                static final int INDEX_DATE_COL = 1;
//                static final int INDEX_TIME_COL = 2;
//                static final int INDEX_HOME_COL = 3;
//                static final int INDEX_AWAY_COL = 4;
//                static final int INDEX_HOME_GOALS_COL = 5;
//                static final int INDEX_AWAY_GOALS_COL = 6;
//                static final int INDEX_MATCH_ID = 7;
//                static final int INDEX_MATCH_DAY = 8;

                String league = mDataCursor.getString(INDEX_LEAGUE_COL);
                String date = mDataCursor.getString(INDEX_DATE_COL);
                String time = mDataCursor.getString(INDEX_TIME_COL);
                String home = mDataCursor.getString(INDEX_HOME_COL);
                String away = mDataCursor.getString(INDEX_AWAY_COL);
                String homeGoals = mDataCursor.getString(INDEX_HOME_GOALS_COL);
                String awayGoals = mDataCursor.getString(INDEX_AWAY_GOALS_COL);
                String matchId = mDataCursor.getString(INDEX_MATCH_ID);
                String matchDay = mDataCursor.getString(INDEX_MATCH_DAY);

                views.setTextViewText(R.id.widget_home_name, home);
                views.setTextViewText(R.id.widget_date_textview, date);
                views.setTextViewText(R.id.widget_score_textview, homeGoals + " - " + awayGoals);
                views.setTextViewText(R.id.widget_time_textview, time);
                views.setTextViewText(R.id.widget_away_name, away);

                return views;
            }
            
            @Override
            public void onDestroy() {
                Log.d(LOG_TAG, "onDestroy()");
                if (mDataCursor != null) {
                    mDataCursor.close();
                    mDataCursor = null;
                }
            }

            @Override
            public int getCount() {
                Log.d(LOG_TAG, "getCount(): count = " + mDataCursor.getCount());
                // totally have to update this method correctly
                return mDataCursor == null ? 0 : mDataCursor.getCount();
            }

            @Override
            public RemoteViews getLoadingView() {
                Log.d(LOG_TAG, "getLoadingView()");
                return new RemoteViews(getPackageName(), R.layout.scores_list_item);
            }

            @Override
            public int getViewTypeCount() {
                Log.d(LOG_TAG, "getViewTypeCount()");
                return 1;
            }

            @Override
            public long getItemId(int position) {
                Log.d(LOG_TAG, "getItemId(int position)");
                return position;
            }

            @Override
            public boolean hasStableIds() {
                Log.d(LOG_TAG, "hasStableIds()");
                return true;
            }
        };
    }





}
