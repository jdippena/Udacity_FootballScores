package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.Locale;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;
import barqsoft.footballscores.scoresAdapter;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class FootballWidgetRemoteService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new FootballRemoteViewFactory();
    }

    class FootballRemoteViewFactory implements RemoteViewsFactory {
        private final String LOG_TAG = getClass().getSimpleName();
        private Cursor mData;

        @Override
        public int getCount() {
            return mData == null ? 0 : mData.getCount();
        }

        @Override
        public void onCreate() {
            mData = getData();
        }

        @Override
        public void onDataSetChanged() {
            if (mData != null) {
                mData.close();
            }
            mData = getData();
        }

        @Override
        public void onDestroy() {
            if (mData != null) {
                mData.close();
            }
        }

        @Override
        public RemoteViews getViewAt(int position) {
            // bunch of boilerplate to set all the text and image views of the widget
            if (mData == null || !mData.moveToPosition(position)) {
                return null;
            };
            String homeName = mData.getString(scoresAdapter.COL_HOME);
            String awayName = mData.getString(scoresAdapter.COL_AWAY);
            String date = mData.getString(scoresAdapter.COL_MATCHTIME);
            int homeGoals = mData.getInt(scoresAdapter.COL_HOME_GOALS);
            int awayGoals = mData.getInt(scoresAdapter.COL_AWAY_GOALS);

            String contentDescription;
            if (homeGoals < 0) {
                contentDescription = String.format(Locale.getDefault(),
                        getString(R.string.match_not_played),
                        homeName, awayName);
            } else {
                contentDescription = String.format(Locale.getDefault(),
                        getString(R.string.score_content_description),
                        homeGoals, awayGoals, homeName, awayName);
            }

            RemoteViews rv = new RemoteViews(getPackageName(), R.layout.widget_list_item);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                rv.setContentDescription(R.id.widget_score_card, contentDescription);
            }
            rv.setTextViewText(R.id.widget_home_name, homeName);
            rv.setTextViewText(R.id.widget_away_name, awayName);
            rv.setTextViewText(R.id.widget_data_textview, date);
            rv.setTextViewText(
                    R.id.widget_score_textview,
                    Utilies.getScores(homeGoals, awayGoals)
            );
            rv.setImageViewResource(R.id.widget_home_crest,
                    Utilies.getTeamCrestByTeamName(homeName)
            );
            rv.setImageViewResource(R.id.widget_away_crest,
                    Utilies.getTeamCrestByTeamName(awayName)
            );
            return rv;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            if (mData.moveToPosition(position)) {
                return mData.getLong(scoresAdapter.COL_ID);
            }
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        private Cursor getData() {
            return getContentResolver().query(
                    DatabaseContract.scores_table.buildScoreWithDate(),
                    null,
                    null,
                    new String[] {Utilies.getFormattedDate()},
                    null);
        }
    }
}
