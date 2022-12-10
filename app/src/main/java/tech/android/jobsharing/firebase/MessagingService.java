package tech.android.jobsharing.firebase;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

import tech.android.jobsharing.R;
import tech.android.jobsharing.activities.ChatActivity;
import tech.android.jobsharing.models.User;

/***
 * Created by HoangRyan aka LilDua on 11/21/2022.
 */
public class MessagingService extends FirebaseMessagingService{

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        User user = new User();
        user.setUserId( remoteMessage.getData().get("userId"));
        user.setName(remoteMessage.getData().get("name"));
        user.setFcmToken(remoteMessage.getData().get("fcmToken"));

        int notificationId = new Random().nextInt();
        String channelId = "chat_message";

        Intent intent = new Intent(this, ChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("user",user);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,channelId);
        builder.setSmallIcon(R.drawable.ic_notifications);
        builder.setContentTitle(user.getName());
        builder.setContentText("New message");
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(
                remoteMessage.getData().get("message")
        ));
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence charName = "Chat Message";
            String channelDescription = "This notification channel is used for chat message notifications";
            int importance  = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId,charName,importance);
            channel.setDescription(channelDescription);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(notificationId, builder.build());
    }
}
