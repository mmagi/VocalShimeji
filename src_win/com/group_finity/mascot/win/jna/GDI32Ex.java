package com.group_finity.mascot.win.jna;

import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.StdCallLibrary;

public interface GDI32Ex extends StdCallLibrary {

    GDI32Ex INSTANCE = (GDI32Ex) Native.loadLibrary("Gdi32", GDI32Ex.class);

    int GetObjectW(WinDef.HBITMAP hgdiobj, int cbBuffer, Structure lpvObject);

    int GetRgnBox(WinDef.HRGN hrgn, WinDef.RECT lprc);
}
