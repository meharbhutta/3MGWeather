package com.muhammadmehar.mmmgweather;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Muhammad Mehar on 1/24/2017.
 */
public class ForecastAdapter extends CursorAdapter {

    private final int VIEW_TYPE_TODAY = 0;
    private final int VIEW_TYPE_FUTURE_DAY = 1;

    private boolean mUseTodayLayout;

    public ForecastAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
        }

    public void setUseTodayLayout(boolean useTodayLayout){
        mUseTodayLayout = useTodayLayout;
    }


    @Override
    public int getItemViewType(int position) {
        return (position == 0 && mUseTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;
        if (viewType == VIEW_TYPE_TODAY) {
            layoutId = R.layout.list_item_forecast_today;
        }else if (viewType == VIEW_TYPE_FUTURE_DAY){
            layoutId = R.layout.list_item_forecast;
        }
        View view = LayoutInflater.from(context).inflate( layoutId, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView dateTextView;
        public final TextView decrsTextView;
        public final TextView highTextView;
        public final TextView lowTextView;
        public ViewHolder(View view){
            iconView = (ImageView) view.findViewById(R.id.list_item_imageview);
            dateTextView = (TextView) view.findViewById(R.id.list_item_date_textview);
            decrsTextView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            highTextView = (TextView) view.findViewById(R.id.list_item_high_textview);
            lowTextView = (TextView) view.findViewById(R.id.list_item_low_textview);
        }
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        int weatherId = cursor.getInt(ForecastFragment.COL_WEATHERID);

        String dateText = cursor.getString(ForecastFragment.COL_DATE_TEXT);

        int viewType = getItemViewType(cursor.getPosition());

        int resId = -1;
        if (viewType == VIEW_TYPE_TODAY) {
            resId = Utility.getResourseId( weatherId, 1, dateText, context);
            viewHolder.dateTextView.setText(Utility.getFriendlyDayString( context, dateText));
        }else if (viewType == VIEW_TYPE_FUTURE_DAY){
            if (cursor.getPosition() == 0){
                viewHolder.dateTextView.setText(R.string.today);
            } else {
                viewHolder.dateTextView.setText(Utility.getFriendlyDayString(context, dateText));
            }
            resId = Utility.getResourseId( weatherId, 0, dateText, context);
        }
        viewHolder.iconView.setImageResource(resId);

        String decrs = cursor.getString(ForecastFragment.COL_SHORT_DESC);
        viewHolder.decrsTextView.setText(decrs);

        viewHolder.iconView.setContentDescription(decrs);

        double high = cursor.getDouble(ForecastFragment.COL_MAX_TEMP);
        viewHolder.highTextView.setText(Utility.formatTemperature(high, context));

        double low = cursor.getDouble(ForecastFragment.COL_MIN_TEMP);
        viewHolder.lowTextView.setText(Utility.formatTemperature( low, context));


    }
}
