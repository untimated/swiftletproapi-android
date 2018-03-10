package thesis.com.swiftletpro;

import android.app.Application;

/**
 * Created by michaelmr on 1/31/17.
 */

public class MyApplication extends Application {

    private String userToken;

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }
}
