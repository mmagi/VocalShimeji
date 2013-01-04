package com.group_finity.mascot.win.jna;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;

public class User32Ex {

    static {
        Native.register("user32");
    }

    public static final User32Ex INSTANCE = new User32Ex();//(User32Ex) Native.loadLibrary("user32", User32Ex.class);

    public static final int SPI_GETWORKAREA = 48;

    public static final int ERROR = 0;

    public native int SystemParametersInfoW(int uiAction, int uiParam, WinDef.RECT pvParam, int fWinIni);

    public native int IsWindow(WinDef.HWND hWnd);

    public native int IsIconic(WinDef.HWND hWnd);

    public native int GetWindowRgn(WinDef.HWND hWnd, WinDef.HRGN hRgn);

    public native int BringWindowToTop(WinDef.HWND hWnd);

    public native WinDef.HWND GetWindow(WinDef.HWND paramHWND, WinDef.DWORD paramDWORD);

    public native WinDef.HWND GetForegroundWindow();

    public native boolean IsWindowVisible(WinDef.HWND paramHWND);

    public native int GetWindowLongW(WinDef.HWND paramHWND, int paramInt);

    public native boolean GetWindowRect(WinDef.HWND paramHWND, WinDef.RECT paramRECT);

    public native int GetWindowTextW(WinDef.HWND paramHWND, char[] paramArrayOfChar, int paramInt);

    public native int GetClassNameW(WinDef.HWND paramHWND, char[] paramArrayOfChar, int paramInt);

    public native boolean MoveWindow(WinDef.HWND paramHWND, int paramInt1, int paramInt2, int paramInt3, int paramInt4, boolean paramBoolean);

    public native int SetWindowLongW(WinDef.HWND paramHWND, int paramInt1, int paramInt2);

    public native WinDef.HDC GetDC(WinDef.HWND paramHWND);

    public native int ReleaseDC(WinDef.HWND paramHWND, WinDef.HDC paramHDC);

    public native boolean UpdateLayeredWindow(WinDef.HWND paramHWND, WinDef.HDC paramHDC1, WinUser.POINT paramPOINT1, WinUser.SIZE paramSIZE, WinDef.HDC paramHDC2, WinUser.POINT paramPOINT2, int paramInt1, WinUser.BLENDFUNCTION paramBLENDFUNCTION, int paramInt2);
}
