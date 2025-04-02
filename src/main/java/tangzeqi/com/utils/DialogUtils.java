//package tangzeqi.com.utils;
//
//import com.intellij.notification.*;
//import com.intellij.openapi.actionSystem.AnAction;
//import com.intellij.openapi.actionSystem.AnActionEvent;
//import com.intellij.openapi.application.ApplicationManager;
//import com.intellij.openapi.ui.Messages;
//import org.jetbrains.annotations.NotNull;
//import tangzeqi.com.service.ChatService;
//
//public class DialogUtils {
//
//    public static void message(String message) {
//        ApplicationManager.getApplication().invokeLater(()->{
//            int result = Messages.showYesNoCancelDialog( message, "收到新信息", Messages.getQuestionIcon());
//            if (result == Messages.YES) {
//                // 用户点击了“是”，执行相应操作
//                System.out.println("用户选择了是");
//            } else {
//                // 用户点击了“否”，执行相应操作
//                System.out.println("用户选择了否");
//            }
//        });
//    }
//
//    public static void notifiy(String message) {
//        ApplicationManager.getApplication().invokeLater(()->{
//            // 创建通知组（确保类型为 BALLOON）
//            NotificationGroup group = new NotificationGroup(
//                    "Wchat Notifications",
//                    NotificationDisplayType.BALLOON,  // 关键：使用 BALLOON 类型
//                    true
//            );
//            // 创建并显示通知
//            Notification notification = group.createNotification(
//                    "收到新的消息",
//                    message,
//                    NotificationType.INFORMATION,
//                    null  // 可选动作
//            );
//            notification.addAction(new AnAction("查看详情") {
//                @Override
//                public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
//                    ChatService.toolWindow.show();
//                    ChatService.showContent("chat");
////                    messageArea.requestFocusInWindow();
//                    notification.expire();
//                }
//            });
//            Notifications.Bus.notify(notification, ChatService.project);
//        });
//    }
//
//}
