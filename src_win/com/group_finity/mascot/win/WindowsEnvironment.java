package com.group_finity.mascot.win;

import com.group_finity.mascot.environment.Area;
import com.group_finity.mascot.environment.Environment;
import com.group_finity.mascot.win.jna.GDI32Ex;
import com.group_finity.mascot.win.jna.User32Ex;
import com.sun.jna.platform.win32.GDI32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;

import java.awt.*;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Java では取得が難しい環境情報をJNIを使用して取得する.
 */
class WindowsEnvironment extends Environment {

    /**
     * 作業領域を取得する. この領域はディスプレイ領域からタスクバーをのぞいたものになる.
     *
     * @return 作業領域.
     */
    private static Rectangle getWorkAreaRect() {
        final WinDef.RECT rect = new WinDef.RECT();
        User32Ex.INSTANCE.SystemParametersInfoW(User32Ex.SPI_GETWORKAREA, 0, rect, 0);
        return new Rectangle(rect.left, rect.top, rect.right - rect.left, rect.bottom - rect.top);
    }

    private static HashMap<WinDef.HWND, Boolean> ieCache = new LinkedHashMap<WinDef.HWND, Boolean>();

    private static boolean isIE(final WinDef.HWND ie) {

        final Boolean cache = ieCache.get(ie);
        if (cache != null) {
            return cache;
        }

        // ウィンドウタイトルで IE かどうか判断する
        final char[] title = new char[1024];

        final int titleLength = User32.INSTANCE.GetWindowText(ie, title, 1024);

        if (new String(title, 0, titleLength).contains("Internet Explorer")) {
            ieCache.put(ie, true);
            return true;
        }

        if (new String(title, 0, titleLength).contains("Google Chrome")) {
            ieCache.put(ie, true);
            return true;
        }

        if (new String(title, 0, titleLength).contains("Mozilla Firefox")) {
            ieCache.put(ie, true);
            return true;
        }


        // ウィンドウクラウスで MSNメッセンジャか判断する
        final char[] className = new char[1024];

        final int classNameLength = User32.INSTANCE.GetClassName(ie, className, 1024);

        if (new String(className, 0, classNameLength).contains("IMWindowClass")) {
            ieCache.put(ie, true);
            return true;
        }
        //        if (new String(className, 0, classNameLength).contains("TXGuiFoundation")) { //QQ2012 窗口类 允许桌宠趴在QQ窗口上 //被藏起来的QQ窗口拉不回来，待研究
        //            ieCache.put(ie, true);
        //            return true;
        //        }
        if (new String(className, 0, classNameLength).contains("SleipnirMainWindow")) {
            ieCache.put(ie, true);
            return true;
        }
        if (new String(className, 0, classNameLength).contains("OperaWindowClass")) {
            ieCache.put(ie, true);
            return true;
        }

        ieCache.put(ie, false);
        return false;
    }

    static int f;

    private static WinDef.HWND findActiveIE() {

        WinDef.HWND ie = User32.INSTANCE.GetWindow(User32.INSTANCE.GetForegroundWindow(), new WinDef.DWORD(WinUser.GW_HWNDFIRST));

        while (User32Ex.INSTANCE.IsWindow(ie) != 0) {

            if (User32.INSTANCE.IsWindowVisible(ie)) {
                if ((User32.INSTANCE.GetWindowLong(ie, WinUser.GWL_STYLE) & WinUser.WS_MAXIMIZE) != 0) {
                    // 最大化されているウィンドウが見つかったので中止
                    return null;
                }
                if (isIE(ie) && (User32Ex.INSTANCE.IsIconic(ie) == 0)) {
                    // IE が見つかった
                    break;
                }
            }

            ie = User32.INSTANCE.GetWindow(ie, new WinDef.DWORD(WinUser.GW_HWNDNEXT));

        }

        if (User32Ex.INSTANCE.IsWindow(ie) == 0) {
            // 見つからなかった
            return null;
        }

        return ie;
    }

    /**
     * アクティブなIEの領域を取得する.
     *
     * @return アクティブなIEの領域. 見つけられなかったときは null.
     */
    private static Rectangle getActiveIERect() {

        final WinDef.HWND ie = findActiveIE();

        // IE の矩形を取得
        final WinDef.RECT out = new WinDef.RECT();
        User32.INSTANCE.GetWindowRect(ie, out);
        final WinDef.RECT in = new WinDef.RECT();
        if (getWindowRgnBox(ie, in) == User32Ex.ERROR) {
            in.left = 0;
            in.top = 0;
            in.right = out.right - out.left;
            in.bottom = out.bottom - out.top;
        }

        // Rectangle オブジェクトを作って返す
        return new Rectangle(out.left + in.left, out.top + in.top, in.right - in.left, in.bottom - in.top);
    }

    private static int getWindowRgnBox(final WinDef.HWND window, final WinDef.RECT rect) {

        WinDef.HRGN hRgn = GDI32.INSTANCE.CreateRectRgn(0, 0, 0, 0);
        try {
            if (User32Ex.INSTANCE.GetWindowRgn(window, hRgn) == User32Ex.ERROR) {
                return User32Ex.ERROR;
            }
            GDI32Ex.INSTANCE.GetRgnBox(hRgn, rect);
            return 1;
        } finally {
            GDI32.INSTANCE.DeleteObject(hRgn);
        }
    }

    private static boolean moveIE(final WinDef.HWND ie, final Rectangle rect) {

        if (ie == null) {
            return false;
        }

        // IE の矩形を取得
        final WinDef.RECT out = new WinDef.RECT();
        User32.INSTANCE.GetWindowRect(ie, out);
        final WinDef.RECT in = new WinDef.RECT();
        if (getWindowRgnBox(ie, in) == User32Ex.ERROR) {
            in.left = 0;
            in.top = 0;
            in.right = out.right - out.left;
            in.bottom = out.bottom - out.top;
        }

        User32.INSTANCE.MoveWindow(ie, rect.x - in.left, rect.y - in.top, rect.width + (out.right - out.left) - (in.right - in.left), rect.height + (out.bottom - out.top) - (in.bottom - in.top), true);

        return true;
    }

    private static boolean restoreAllIEs() {
        boolean ret = false; //指示是否有窗口被还原了，用于判定使用哪个声音事件
        // ワークエリアの矩形を取得
        final WinDef.RECT workArea = new WinDef.RECT();
        User32Ex.INSTANCE.SystemParametersInfoW(User32Ex.SPI_GETWORKAREA, 0, workArea, 0);

        WinDef.HWND ie = User32.INSTANCE.GetWindow(User32.INSTANCE.GetForegroundWindow(), new WinDef.DWORD(WinUser.GW_HWNDFIRST));

        while (User32Ex.INSTANCE.IsWindow(ie) != 0) {
            if (isIE(ie)) {
                // IE が見つかった

                // IE の矩形を取得
                final WinDef.RECT rect = new WinDef.RECT();
                User32.INSTANCE.GetWindowRect(ie, rect);
                if ((rect.right <= workArea.left + 100) || (rect.bottom <= workArea.top + 100) || (rect.left >= workArea.right - 100) || (rect.top >= workArea.bottom - 100)) {
                    ret = true;
                    // 位置がおかしいような気がする

                    int offsetX = workArea.left + 100 - rect.left;
                    int offsetY = workArea.top + 100 - rect.top;
                    rect.left += offsetX;
                    rect.right += offsetX;
                    rect.top += offsetY;
                    rect.bottom += offsetY;
                    User32.INSTANCE.MoveWindow(ie, rect.left, rect.top, rect.right - rect.left, rect.bottom - rect.top, true);
                    User32Ex.INSTANCE.BringWindowToTop(ie);
                }

                break;
            }

            ie = User32.INSTANCE.GetWindow(ie, new WinDef.DWORD(WinUser.GW_HWNDNEXT));
        }
        return ret;
    }

    public static Area workArea = new Area();

    public static Area activeIE = new Area();

    @Override
    public void tick() {
        super.tick();
        workArea.set(getWorkAreaRect());

        final Rectangle ieRect = getActiveIERect();
        activeIE.setVisible((ieRect != null) && ieRect.intersects(getScreen().toRectangle()));
        activeIE.set(ieRect == null ? new Rectangle(-1, -1, 0, 0) : ieRect);

    }

    @Override
    public void moveActiveIE(final Point point) {
        moveIE(findActiveIE(), new Rectangle(point.x, point.y, activeIE.getWidth(), activeIE.getHeight()));
    }

    @Override
    public boolean restoreIE() {
        return restoreAllIEs();
    }

    @Override
    public Area getWorkArea() {
        return workArea;
    }

    @Override
    public Area getActiveIE() {
        return activeIE;
    }

}
