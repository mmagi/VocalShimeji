package com.group_finity.mascot.win.jna;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.StdCallLibrary;

public interface User32Ex extends StdCallLibrary {

    User32Ex INSTANCE = (User32Ex) Native.loadLibrary("User32", User32Ex.class);

    int SPI_GETWORKAREA = 48;

    int SystemParametersInfoW(int uiAction, int uiParam, WinDef.RECT pvParam, int fWinIni);

    int IsWindow(WinDef.HWND hWnd);

    int IsIconic(WinDef.HWND hWnd);

    int ERROR = 0;

    int GetWindowRgn(WinDef.HWND hWnd, WinDef.HRGN hRgn);

    int BringWindowToTop(WinDef.HWND hWnd);
}
