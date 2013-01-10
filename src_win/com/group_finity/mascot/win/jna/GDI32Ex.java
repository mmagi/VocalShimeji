package com.group_finity.mascot.win.jna;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinGDI;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.PointerByReference;

public class GDI32Ex {
    static {
        Native.register("gdi32");
    }

    public static final GDI32Ex INSTANCE = new GDI32Ex();//(GDI32Ex) Native.loadLibrary("gdi32", GDI32Ex.class);

    public native int GetObjectW(WinDef.HBITMAP hgdiobj, int cbBuffer, Structure lpvObject);

    public native int GetRgnBox(WinDef.HRGN hrgn, WinDef.RECT lprc);

    public native boolean DeleteObject(WinNT.HANDLE paramHANDLE);

    public native WinDef.HRGN CreateRectRgn(int paramInt1, int paramInt2, int paramInt3, int paramInt4);

    public native WinDef.HBITMAP CreateDIBSection(WinDef.HDC paramHDC, WinGDI.BITMAPINFO paramBITMAPINFO, int paramInt1, PointerByReference paramPointerByReference, Pointer paramPointer, int paramInt2);

    public native WinDef.HDC CreateCompatibleDC(WinDef.HDC paramHDC);

    public native WinNT.HANDLE SelectObject(WinDef.HDC paramHDC, WinNT.HANDLE paramHANDLE);

    public native boolean DeleteDC(WinDef.HDC paramHDC);

}
