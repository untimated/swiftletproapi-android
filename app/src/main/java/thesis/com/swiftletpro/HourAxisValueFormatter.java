package thesis.com.swiftletpro;

/**
 * Created by michaelmr on 1/5/17.
 */

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Yasir on 02/06/16.
 */
public class HourAxisValueFormatter implements IAxisValueFormatter
{
    private long referenceTimestamp; // minimum timestamp in your data set
    private DateFormat mDataFormat;
    private Date mDate;

    public HourAxisValueFormatter(long referenceTimestamp) {
        this.referenceTimestamp = referenceTimestamp;
        this.mDataFormat = new SimpleDateFormat("HH:mm");
        this.mDate = new Date();
    }


    /**
     * Called when a value from an axis is to be formatted
     * before being drawn. For performance reasons, avoid excessive calculations
     * and memory allocations inside this method.
     *
     * @param value the value to be formatted
     * @param axis  the axis the value belongs to
     * @return
     */
    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        // convertedTimestamp = originalTimestamp - referenceTimestamp
        long convertedTimestamp = (long) value;

        // Retrieve original timestamp
        long originalTimestamp = referenceTimestamp + convertedTimestamp;
        // Convert timestamp to hour:minute
        return getHour(originalTimestamp);
    }


    public int getDecimalDigits() { return 0; }

    private String getHour(long timestamp){
        try{
            //mDate.setTime(timestamp*1000);
            //return mDataFormat.format(mDate);
            return getDate(timestamp);
        }
        catch(Exception ex){
            return "xx";
        }
    }

    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);
        String date = android.text.format.DateFormat.format("HH:mm", cal).toString();
        return date;
    }
}