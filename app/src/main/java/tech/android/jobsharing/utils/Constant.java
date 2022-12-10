package tech.android.jobsharing.utils;

import java.util.HashMap;

public class Constant {
    //Push notification
    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";

    public static HashMap<String, String> remoteMsgHeaders = null;
    public static HashMap<String, String> getRemoteMsgHeaders(){
        if(remoteMsgHeaders == null){
            remoteMsgHeaders = new HashMap<>();
            remoteMsgHeaders.put(
                    REMOTE_MSG_AUTHORIZATION,
                    "key=AAAANTNYzmU:APA91bHCR4f37rW_924axxN5CQg9QqXJpjWVrVW36W-Lr0ns9vGfmrQQIp1jwCQHZfKn5SSYcRpcoz-5zaOaStpm3SvW09XGe98KTsAdAZE5k7SGG6IUf9cMDRyYZ5YytOnMTgOKmu6-"
            );
            remoteMsgHeaders.put(
                    REMOTE_MSG_CONTENT_TYPE,
                    "application/json"
            );
        }
        return remoteMsgHeaders;
    }
}
