package com.m1nx.proxy;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MainActivity extends Activity {
    private static final int BG = Color.rgb(8, 13, 24);
    private static final int CARD = Color.rgb(17, 27, 46);
    private static final int CARD_ALT = Color.rgb(20, 34, 57);
    private static final int BLUE = Color.rgb(77, 141, 255);
    private static final int MINT = Color.rgb(45, 224, 193);
    private static final int WHITE = Color.rgb(241, 246, 255);
    private static final int MUTED = Color.rgb(148, 164, 190);
    private final ExecutorService worker = Executors.newSingleThreadExecutor();

    private EditText hostInput;
    private EditText portInput;
    private Button checkButton;
    private TextView connectionStatus;
    private Switch motionSwitch;
    private boolean destroyed;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        Window window = getWindow();
        window.setStatusBarColor(BG);
        window.setNavigationBarColor(BG);
        window.getDecorView().setSystemUiVisibility(0);
        buildScreen();
    }

    private void buildScreen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);
        scroll.setClipToPadding(false);

        LinearLayout page = column();
        page.setPadding(dp(20), dp(18), dp(20), dp(28));
        scroll.addView(page, new ScrollView.LayoutParams(-1, -2));

        LinearLayout header = row();
        TextView logo = text("M", 23, WHITE, true);
        logo.setGravity(Gravity.CENTER);
        logo.setBackground(roundGradient(new int[]{BLUE, 0xff2854bd}, 20, 0, 0));
        header.addView(logo, new LinearLayout.LayoutParams(dp(54), dp(54)));

        LinearLayout titleBlock = column();
        titleBlock.setPadding(dp(13), dp(2), 0, 0);
        titleBlock.addView(text("M1NX PROXY", 19, WHITE, true));
        TextView subtitle = text("TIỆN ÍCH KẾT NỐI", 10, MUTED, true);
        subtitle.setLetterSpacing(0.14f);
        titleBlock.addView(subtitle);
        header.addView(titleBlock, new LinearLayout.LayoutParams(0, -2, 1));

        TextView badge = text("SẴN SÀNG", 10, MINT, true);
        badge.setPadding(dp(11), dp(8), dp(11), dp(8));
        badge.setBackground(roundSolid(0xff12352f, 30, 0xff225b50, 1));
        header.addView(badge);
        page.addView(header);

        TextView headline = text("Bảng điều khiển", 27, WHITE, true);
        LinearLayout.LayoutParams headlineParams = params(-2, -2);
        headlineParams.topMargin = dp(27);
        page.addView(headline, headlineParams);

        TextView intro = text("Giao diện tiếng Việt · thao tác đơn giản · hiệu ứng nhẹ", 12, MUTED, false);
        LinearLayout.LayoutParams introParams = params(-2, -2);
        introParams.topMargin = dp(5);
        page.addView(intro, introParams);

        page.addView(sectionLabel("MÃ TRUY CẬP", 23));
        LinearLayout keyCard = card();
        LinearLayout keyRow = row();
        LinearLayout keyText = column();
        keyText.addView(text("Khóa mặc định", 12, MUTED, false));
        TextView keyValue = text("Random", 20, WHITE, true);
        keyText.addView(keyValue, topParams(4));
        keyRow.addView(keyText, new LinearLayout.LayoutParams(0, -2, 1));

        Button copy = button("Sao chép", false);
        keyRow.addView(copy, new LinearLayout.LayoutParams(dp(112), dp(42)));
        copy.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                clipboard.setPrimaryClip(ClipData.newPlainText("Mã truy cập", "Random"));
            }
            toast("Đã sao chép: Random");
        });
        keyCard.addView(keyRow);
        keyCard.addView(text("Giá trị hiển thị mặc định, không phải mã xác thực máy chủ.", 11, MUTED, false), topParams(11));
        page.addView(keyCard);

        page.addView(sectionLabel("KIỂM TRA MÁY CHỦ PROXY", 20));
        LinearLayout proxyCard = card();
        proxyCard.addView(text("Địa chỉ máy chủ", 12, MUTED, true));
        hostInput = editText("Ví dụ: proxy.example.com",
                getPreferences(Context.MODE_PRIVATE).getString("host", ""));
        proxyCard.addView(hostInput, topParams(7));

        proxyCard.addView(text("Cổng", 12, MUTED, true), topParams(13));
        portInput = editText("1080",
                getPreferences(Context.MODE_PRIVATE).getString("port", "1080"));
        portInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        proxyCard.addView(portInput, topParams(7));

        checkButton = button("Kiểm tra kết nối", true);
        proxyCard.addView(checkButton, topParams(14));
        checkButton.setOnClickListener(v -> checkProxy());

        connectionStatus = text("Chưa thực hiện kiểm tra", 12, MUTED, false);
        proxyCard.addView(connectionStatus, topParams(12));
        page.addView(proxyCard);

        page.addView(sectionLabel("TÙY CHỈNH GIAO DIỆN", 20));
        LinearLayout settingsCard = card();
        LinearLayout settingRow = row();
        LinearLayout settingText = column();
        settingText.addView(text("Chuyển động nhẹ", 14, WHITE, true));
        settingText.addView(text("Hiệu ứng xuất hiện và nhấn nút", 11, MUTED, false), topParams(3));
        settingRow.addView(settingText, new LinearLayout.LayoutParams(0, -2, 1));

        motionSwitch = new Switch(this);
        motionSwitch.setChecked(getPreferences(Context.MODE_PRIVATE).getBoolean("motion", true));
        motionSwitch.setButtonTintList(android.content.res.ColorStateList.valueOf(BLUE));
        settingRow.addView(motionSwitch);
        settingsCard.addView(settingRow);
        motionSwitch.setOnCheckedChangeListener((buttonView, checked) -> {
            getPreferences(Context.MODE_PRIVATE).edit().putBoolean("motion", checked).apply();
            toast(checked ? "Đã bật chuyển động nhẹ" : "Đã tắt hiệu ứng chuyển động");
        });
        page.addView(settingsCard);

        LinearLayout note = card();
        note.setBackground(roundSolid(0xff10243a, 17, 0xff1a4260, 1));
        note.addView(text("LƯU Ý", 11, MINT, true));
        note.addView(text(
                "Công cụ này chỉ kiểm tra máy chủ có phản hồi TCP hay không. Nó chưa tạo VPN, chưa định tuyến lưu lượng và không can thiệp vào trò chơi.",
                12, WHITE, false), topParams(7));
        page.addView(note, topParams(18));

        TextView footer = text("M1NX · BẢN DỰNG MỚI 1.0.0", 10, 0xff65758f, true);
        footer.setGravity(Gravity.CENTER);
        page.addView(footer, topParams(24));

        setContentView(scroll);
        animateEntrance(page);
    }

    private void checkProxy() {
        String host = hostInput.getText().toString().trim();
        String portText = portInput.getText().toString().trim();

        if (host.isEmpty()) {
            hostInput.setError("Nhập địa chỉ máy chủ");
            return;
        }

        final int port;
        try {
            port = Integer.parseInt(portText);
            if (port < 1 || port > 65535) throw new NumberFormatException();
        } catch (NumberFormatException error) {
            portInput.setError("Cổng phải từ 1 đến 65535");
            return;
        }

        if (host.contains(" ")) {
            hostInput.setError("Địa chỉ không được chứa dấu cách");
            return;
        }

        getPreferences(Context.MODE_PRIVATE).edit()
                .putString("host", host).putString("port", portText).apply();
        checkButton.setEnabled(false);
        checkButton.setText("Đang kiểm tra…");
        connectionStatus.setTextColor(MUTED);
        connectionStatus.setText("Đang thử kết nối TCP đến " + host + ":" + port + "…");

        worker.execute(() -> {
            boolean reachable;
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(host, port), 3500);
                reachable = true;
            } catch (Exception ignored) {
                reachable = false;
            }

            final boolean result = reachable;
            runOnUiThread(() -> {
                if (destroyed || isFinishing()) return;
                checkButton.setEnabled(true);
                checkButton.setText("Kiểm tra kết nối");
                if (result) {
                    connectionStatus.setTextColor(MINT);
                    connectionStatus.setText("Máy chủ phản hồi TCP. Chưa xác thực SOCKS5.");
                } else {
                    connectionStatus.setTextColor(0xffff9b9b);
                    connectionStatus.setText("Không kết nối được. Kiểm tra lại máy chủ, cổng hoặc mạng.");
                }
            });
        });
    }

    private void animateEntrance(ViewGroup parent) {
        if (!getPreferences(Context.MODE_PRIVATE).getBoolean("motion", true)) return;
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            child.setAlpha(0f);
            child.setTranslationY(dp(10));
            child.animate().alpha(1f).translationY(0f)
                    .setStartDelay(i * 38L).setDuration(230L).start();
        }
    }

    private LinearLayout card() {
        LinearLayout layout = column();
        layout.setPadding(dp(16), dp(16), dp(16), dp(16));
        layout.setBackground(roundSolid(CARD, 18, 0xff263752, 1));
        layout.setElevation(dp(2));
        LinearLayout.LayoutParams p = params(-1, -2);
        p.topMargin = dp(9);
        layout.setLayoutParams(p);
        return layout;
    }

    private LinearLayout row() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER_VERTICAL);
        return layout;
    }

    private LinearLayout column() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    private TextView sectionLabel(String value, int top) {
        TextView label = text(value, 10, MUTED, true);
        label.setLetterSpacing(0.12f);
        LinearLayout.LayoutParams p = params(-2, -2);
        p.topMargin = dp(top);
        p.bottomMargin = dp(1);
        label.setLayoutParams(p);
        return label;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setIncludeFontPadding(false);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }

    private EditText editText(String hint, String value) {
        EditText field = new EditText(this);
        field.setSingleLine(true);
        field.setText(value);
        field.setHint(hint);
        field.setTextSize(14);
        field.setTextColor(WHITE);
        field.setHintTextColor(0xff70819c);
        field.setPadding(dp(13), dp(11), dp(13), dp(11));
        field.setBackground(roundSolid(CARD_ALT, 12, 0xff30445f, 1));
        return field;
    }

    private Button button(String label, boolean primary) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextSize(13);
        button.setAllCaps(false);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setTextColor(WHITE);
        button.setPadding(dp(12), 0, dp(12), 0);
        button.setBackground(primary
                ? roundGradient(new int[]{BLUE, 0xff315fc4}, 13, 0, 0)
                : roundSolid(0xff20334e, 13, 0xff39516f, 1));
        button.setStateListAnimator(null);
        button.setOnTouchListener((v, event) -> {
            if (getPreferences(Context.MODE_PRIVATE).getBoolean("motion", true)) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(75L).start();
                } else if (event.getAction() == MotionEvent.ACTION_UP
                        || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100L).start();
                }
            }
            return false;
        });
        return button;
    }

    private GradientDrawable roundSolid(int color, int radius, int strokeColor, int strokeWidth) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));
        if (strokeWidth > 0) drawable.setStroke(dp(strokeWidth), strokeColor);
        return drawable;
    }

    private GradientDrawable roundGradient(int[] colors, int radius, int strokeColor, int strokeWidth) {
        GradientDrawable drawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
        drawable.setCornerRadius(dp(radius));
        if (strokeWidth > 0) drawable.setStroke(dp(strokeWidth), strokeColor);
        return drawable;
    }

    private LinearLayout.LayoutParams params(int width, int height) {
        return new LinearLayout.LayoutParams(
                width < 0 ? width : dp(width),
                height < 0 ? height : dp(height));
    }

    private LinearLayout.LayoutParams topParams(int top) {
        LinearLayout.LayoutParams p = params(-1, -2);
        p.topMargin = dp(top);
        return p;
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (hostInput != null && portInput != null) {
            getPreferences(Context.MODE_PRIVATE).edit()
                    .putString("host", hostInput.getText().toString())
                    .putString("port", portInput.getText().toString())
                    .apply();
        }
    }

    @Override
    protected void onDestroy() {
        destroyed = true;
        worker.shutdownNow();
        super.onDestroy();
    }
}
