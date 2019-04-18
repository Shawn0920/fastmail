package com.shawn.fastmail.loader;

import android.content.Context;
import android.widget.TextView;

import com.shawn.fastmail.R;
import com.shawn.fastmail.dialog.AppLoading;

import java.util.ArrayList;


/**
 * Created by shawn on 2017/12/15.
 */

public class AppLoader {
    private static final int LOADER_SIZE_SCALE = 10;
    private static final int LOADER_OFFSET_SCALE = 8;
    private static TextView message;
    //    private static final ArrayList<AppCompatDialog> LOADERS = new ArrayList<>();
    private static final ArrayList<AppLoading> LOADERS = new ArrayList<>();

    private static final String DEFAULT_LOADER = LoaderStyle.NORMAL.name();

    public static void showLoading(Context context, Enum<LoaderStyle> type) {
        showLoading(context, type.name());
    }

//    public static void showLoading(Context context, String type) {
//
//        final AppCompatDialog dialog = new AppCompatDialog(context, R.style.dialog);
//
//        //final AVLoadingIndicatorView avLoadingIndicatorView = LoaderCreator.getInstance(type, context);
//        View view = LayoutInflater.from(context).inflate(R.layout.dialog_layout, null);
//        message = view.findViewById(R.id.load_message);
//        dialog.setContentView(view);
//        dialog.setCancelable(false);
//        dialog.setCanceledOnTouchOutside(false);
//        int deviceWidth = CommonUtil.getScreenWidth();
//        int deviceHeight = CommonUtil.getScreenHeight();
//
//        final Window dialogWindow = dialog.getWindow();
//
//        if (dialogWindow != null) {
//            final WindowManager.LayoutParams lp = dialogWindow.getAttributes();
//            lp.width = deviceWidth / 3;
//            lp.height = lp.height + deviceHeight / LOADER_OFFSET_SCALE;
//            lp.gravity = Gravity.CENTER;
//            dialogWindow.setAttributes(lp);
//        }
//        LOADERS.add(dialog);
//        dialog.show();
//    }

    public static void showLoading(Context context, String type) {

        final AppLoading dialog = new AppLoading(context, R.style.loading_dialog);

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        LOADERS.add(dialog);
        dialog.show();
    }

    public static void showLoading(Context context) {
        showLoading(context, DEFAULT_LOADER);

    }

    public static void setMessage(String content) {
        if (message != null) {
            message.setText(content + "...");
        }
    }

    public static void stopLoading() {
        for (AppLoading dialog : LOADERS) {
            if (dialog != null) {
                if (dialog.isShowing()) {
                    dialog.cancel();
                }
            }
        }
    }
}
