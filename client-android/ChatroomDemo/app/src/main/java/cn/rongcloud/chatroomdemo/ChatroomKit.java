package cn.rongcloud.chatroomdemo;

import android.content.Context;
import android.os.Handler;

import java.util.ArrayList;
import java.util.HashMap;

import cn.rongcloud.chatroomdemo.messageview.AdminAddView;
import cn.rongcloud.chatroomdemo.messageview.AdminRemoveView;
import cn.rongcloud.chatroomdemo.messageview.BaseMsgView;
import cn.rongcloud.chatroomdemo.messageview.EndView;
import cn.rongcloud.chatroomdemo.messageview.FollowMsgView;
import cn.rongcloud.chatroomdemo.messageview.LikeMsgView;
import cn.rongcloud.chatroomdemo.messageview.StartMsgView;
import cn.rongcloud.chatroomdemo.messageview.TextMsgView;
import cn.rongcloud.chatroomdemo.messageview.UserBanView;
import cn.rongcloud.chatroomdemo.messageview.UserBlockView;
import cn.rongcloud.chatroomdemo.messageview.UserQuitMsgView;
import cn.rongcloud.chatroomdemo.messageview.UserUnBanView;
import cn.rongcloud.chatroomdemo.messageview.UserUnBlockView;
import cn.rongcloud.chatroomdemo.messageview.WelcomeMsgView;
import cn.rongcloud.chatroomdemo.model.BanWarnMessage;
import cn.rongcloud.chatroomdemo.model.BanWarnView;
import cn.rongcloud.chatroomdemo.ui.panel.EmojiManager;
import io.rong.imlib.AnnotationNotFoundException;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.imlib.model.UserInfo;
import io.rong.message.ChatroomAdminAdd;
import io.rong.message.ChatroomAdminRemove;
import io.rong.message.ChatroomBarrage;
import io.rong.message.ChatroomEnd;
import io.rong.message.ChatroomFollow;
import io.rong.message.ChatroomGift;
import io.rong.message.ChatroomLike;
import io.rong.message.ChatroomStart;
import io.rong.message.ChatroomUserBan;
import io.rong.message.ChatroomUserBlock;
import io.rong.message.ChatroomUserQuit;
import io.rong.message.ChatroomUserUnBan;
import io.rong.message.ChatroomUserUnBlock;
import io.rong.message.ChatroomWelcome;
import io.rong.message.TextMessage;

/**
 * ChatroomKit是融云聊天室Demo对IMLib库的接口封装类。目的是在IMLib库众多通用接口中，提炼出与直播聊天室应用相关的常用接口，
 * 方便开发者了解IMLib库的调用流程，降低学习成本。同时也开发者可以此为基础扩展，并快速移植到自己的应用中去。
 * <p/>
 * <strong>注意：</strong>此种封装并不是集成IMLib库的唯一方法，开发者可以根据自身需求添加修改，或者直接使用IMLib接口。
 */

public class ChatroomKit {

    /**
     * 事件代码
     */
    public static final int MESSAGE_ARRIVED = 0;
    public static final int MESSAGE_SENT = 1;

    /**
     * 事件错误代码
     */
    public static final int MESSAGE_SEND_ERROR = -1;

    /**
     * 消息类与消息UI展示类对应表
     */
    private static HashMap<Class<? extends MessageContent>, Class<? extends BaseMsgView>> msgViewMap = new HashMap<>();

    /**
     * 事件监听者列表
     */
    private static ArrayList<Handler> eventHandlerList = new ArrayList<>();

    /**
     * 当前登录用户id
     */
    private static UserInfo currentUser;

    /**
     * 当前聊天室房间id
     */
    private static String currentRoomId;

    /**
     * 消息接收监听者
     */
    private static RongIMClient.OnReceiveMessageListener onReceiveMessageListener = new RongIMClient.OnReceiveMessageListener() {
        @Override
        public boolean onReceived(Message message, int i) {
            handleEvent(MESSAGE_ARRIVED, message);
            return false;
        }
    };

    /**
     * 初始化方法，在整个应用程序全局只需要调用一次，建议在Application 继承类中调用。
     * <p/>
     * <strong>注意：</strong>其余方法都需要在这之后调用。
     *
     * @param context Application类的Context
     * @param appKey  融云注册应用的AppKey
     */
    public static void init(Context context, String appKey) {

        RongIMClient.init(context, appKey);
        EmojiManager.init(context);

        RongIMClient.setOnReceiveMessageListener(onReceiveMessageListener);

        registerMessageType(ChatroomWelcome.class);
        registerMessageView(ChatroomWelcome.class, WelcomeMsgView.class);

        registerMessageType(ChatroomFollow.class);
        registerMessageView(ChatroomFollow.class, FollowMsgView.class);

        registerMessageType(ChatroomBarrage.class);

        registerMessageType(ChatroomGift.class);

        registerMessageType(ChatroomLike.class);
        registerMessageView(ChatroomLike.class, LikeMsgView.class);

        registerMessageType(ChatroomUserQuit.class);
        registerMessageView(ChatroomUserQuit.class, UserQuitMsgView.class);

        registerMessageView(TextMessage.class, TextMsgView.class);

        registerMessageType(ChatroomStart.class);
        registerMessageView(ChatroomStart.class, StartMsgView.class);

        registerMessageType(ChatroomAdminAdd.class);
        registerMessageView(ChatroomAdminAdd.class, AdminAddView.class);

        registerMessageType(ChatroomAdminRemove.class);
        registerMessageView(ChatroomAdminRemove.class, AdminRemoveView.class);

        registerMessageType(ChatroomUserBan.class);
        registerMessageView(ChatroomUserBan.class, UserBanView.class);

        registerMessageType(ChatroomUserUnBan.class);
        registerMessageView(ChatroomUserUnBan.class, UserUnBanView.class);

        registerMessageType(ChatroomUserBlock.class);
        registerMessageView(ChatroomUserBlock.class, UserBlockView.class);

        registerMessageType(ChatroomUserUnBlock.class);
        registerMessageView(ChatroomUserUnBlock.class, UserUnBlockView.class);

        registerMessageType(ChatroomEnd.class);
        registerMessageView(ChatroomEnd.class, EndView.class);

        registerMessageType(BanWarnMessage.class);
        registerMessageView(BanWarnMessage.class, BanWarnView.class);

    }

    /**
     * 设置当前登录用户，通常由注册生成，通过应用服务器来返回。
     *
     * @param user 当前用户
     */
    public static void setCurrentUser(UserInfo user) {
        currentUser = user;
    }

    /**
     * 获得当前登录用户。
     *
     * @return
     */
    public static UserInfo getCurrentUser() {
        return currentUser;
    }

    public static String getCurrentRoomId() {
        return currentRoomId;
    }

    /**
     * 注册自定义消息。
     *
     * @param msgType 自定义消息类型
     */
    public static void registerMessageType(Class<? extends MessageContent> msgType) {
        try {
            RongIMClient.registerMessageType(msgType);
        } catch (AnnotationNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 注册消息展示界面类。
     *
     * @param msgContent 消息类型
     * @param msgView    对应的界面展示类
     */
    public static void registerMessageView(Class<? extends MessageContent> msgContent, Class<? extends BaseMsgView> msgView) {
        msgViewMap.put(msgContent, msgView);
    }

    /**
     * 获取注册消息对应的UI展示类。
     *
     * @param msgContent 注册的消息类型
     * @return 对应的UI展示类
     */
    public static Class<? extends BaseMsgView> getRegisterMessageView(Class<? extends MessageContent> msgContent) {
        return msgViewMap.get(msgContent);
    }

    /**
     * 连接融云服务器，在整个应用程序全局，只需要调用一次，需在 {@link #init(Context, String)} 之后调用。
     * </p>
     * 如果调用此接口遇到连接失败，SDK 会自动启动重连机制进行最多10次重连，分别是1, 2, 4, 8, 16, 32, 64, 128, 256, 512秒后。
     * 在这之后如果仍没有连接成功，还会在当检测到设备网络状态变化时再次进行重连。
     *
     * @param token    从服务端获取的用户身份令牌（Token）
     * @param callback 连接回调
     */
    public static void connect(String token, final RongIMClient.ConnectCallback callback) {
        RongIMClient.connect(token, callback);
    }

    /**
     * 断开与融云服务器的连接，并且不再接收 Push 消息。
     */
    public static void logout() {
        RongIMClient.getInstance().logout();
    }

    /**
     * 加入聊天室。如果聊天室不存在，sdk 会创建聊天室并加入，如果已存在，则直接加入。加入聊天室时，可以选择拉取聊天室消息数目。
     *
     * @param roomId          聊天室 Id
     * @param defMessageCount 默认开始时拉取的历史记录条数
     * @param callback        状态回调
     */
    public static void joinChatRoom(String roomId, int defMessageCount, final RongIMClient.OperationCallback callback) {
        currentRoomId = roomId;
        RongIMClient.getInstance().joinChatRoom(currentRoomId, defMessageCount, callback);
    }

    /**
     * 退出聊天室，不在接收其消息。
     */
    public static void quitChatRoom(final RongIMClient.OperationCallback callback) {
        RongIMClient.getInstance().quitChatRoom(currentRoomId, callback);
    }

    /**
     * 向当前聊天室发送消息。
     * </p>
     * <strong>注意：</strong>此函数为异步函数，发送结果将通过handler事件返回。
     *
     * @param msgContent 消息对象
     */
    public static void sendMessage(final MessageContent msgContent) {
        if (currentUser == null) {
            throw new RuntimeException("currentUser should not be null.");
        }

        Message msg = Message.obtain(currentRoomId, Conversation.ConversationType.CHATROOM, msgContent);

        RongIMClient.getInstance().sendMessage(msg, null, null, new IRongCallback.ISendMessageCallback() {
            @Override
            public void onAttached(Message message) {

            }

            @Override
            public void onSuccess(Message message) {
                handleEvent(MESSAGE_SENT, message);

            }

            @Override
            public void onError(Message message, RongIMClient.ErrorCode errorCode) {
                handleEvent(MESSAGE_SEND_ERROR, errorCode.getValue(), 0, message);
            }
        });
    }

    /**
     * 添加IMLib 事件接收者。
     *
     * @param handler
     */
    public static void addEventHandler(Handler handler) {
        if (!eventHandlerList.contains(handler)) {
            eventHandlerList.add(handler);
        }
    }

    /**
     * 移除IMLib 事件接收者。
     *
     * @param handler
     */
    public static void removeEventHandler(Handler handler) {
        eventHandlerList.remove(handler);
    }

    private static void handleEvent(int what) {
        for (Handler handler : eventHandlerList) {
            android.os.Message m = android.os.Message.obtain();
            m.what = what;
            handler.sendMessage(m);
        }
    }

    private static void handleEvent(int what, Object obj) {
        for (Handler handler : eventHandlerList) {
            android.os.Message m = android.os.Message.obtain();
            m.what = what;
            m.obj = obj;
            handler.sendMessage(m);
        }
    }

    private static void handleEvent(int what, int arg1, int arg2, Object obj) {
        for (Handler handler : eventHandlerList) {
            android.os.Message m = android.os.Message.obtain();
            m.what = what;
            m.arg1 = arg1;
            m.arg2 = arg2;
            m.obj = obj;
            handler.sendMessage(m);
        }
    }
}
