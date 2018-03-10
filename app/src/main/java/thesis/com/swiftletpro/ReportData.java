package thesis.com.swiftletpro;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by michaelmr on 1/5/17.
 */

public class ReportData {
    private String mSerial;
    private int mHumidity;
    private int mTemperature;
    private Date date;

    public ReportData(){}

    public ReportData(String s, int h, int t, String d){
        mSerial = s;
        mHumidity = h;
        mTemperature = t;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(d);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public String getmSerial() {
        return mSerial;
    }

    public int getmHumidity() {
        return mHumidity;
    }

    public int getmTemperature() {
        return mTemperature;
    }

    public Date getDate() {
        return date;
    }
}
