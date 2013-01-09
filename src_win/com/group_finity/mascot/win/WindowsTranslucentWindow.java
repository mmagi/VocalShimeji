package com.group_finity.mascot.win;

import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.image.TranslucentWindow;
import com.group_finity.mascot.win.jna.GDI32Ex;
import com.group_finity.mascot.win.jna.User32Ex;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinUser;

import javax.swing.*;

final class WindowsTranslucentWindow extends JWindow implements TranslucentWindow {
    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) {
            hWnd = new WinDef.HWND(Native.getComponentPointer(this));
            final int exStyle = User32Ex.INSTANCE.GetWindowLongW(hWnd, WinUser.GWL_EXSTYLE);
            if ((exStyle & WinUser.WS_EX_LAYERED) == 0) {
                User32Ex.INSTANCE.SetWindowLongW(hWnd, WinUser.GWL_EXSTYLE, exStyle | WinUser.WS_EX_LAYERED);
            }
            final WinDef.HDC clientDC = User32Ex.INSTANCE.GetDC(hWnd);
            if (null != hdcSrc)
                GDI32Ex.INSTANCE.DeleteDC(hdcSrc);
            hdcSrc = GDI32Ex.INSTANCE.CreateCompatibleDC(clientDC);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (null != hdcSrc)
            GDI32Ex.INSTANCE.DeleteDC(hdcSrc);
    }

    private static final long serialVersionUID = 1L;

    @Override
    public JWindow asJWindow() {
        return this;
    }

    final static WinUser.BLENDFUNCTION blendFunction = new WinUser.BLENDFUNCTION();

    static {
        blendFunction.BlendOp = WinUser.AC_SRC_OVER;
        blendFunction.BlendFlags = 0;
        blendFunction.SourceConstantAlpha = (byte) 255; // 濃度を設定
        blendFunction.AlphaFormat = WinUser.AC_SRC_ALPHA;
    }

    final WinUser.POINT pptSrc = new WinUser.POINT(); //zero

    @Override
    final public String toString() {
        return "LayeredWindow[hashCode=" + hashCode() + ",bounds=" + getBounds() + "]";
    }

    /**
     * BOOL WINAPI UpdateLayeredWindow(
     * _In_      HWND hWnd,
     * _In_opt_  HDC hdcDst,
     * _In_opt_  POINT *pptDst,
     * _In_opt_  SIZE *pSize,
     * _In_opt_  HDC hdcSrc,
     * _In_opt_  POINT *pptSrc,
     * _In_      COLORREF crKey,
     * _In_opt_  BLENDFUNCTION *pblend,
     * _In_      DWORD dwFlags
     * );
     */

    WinDef.HWND hWnd;
    WinUser.POINT pptDst;
    WinDef.HDC hdcSrc;
    final WinUser.SIZE pSize = new WinUser.SIZE();
    WinDef.HBITMAP imageHandle;

    private WindowsNativeImage lastImage;
    public final void setImage(final NativeImage image) {
        if (image != this.lastImage) {
            this.lastImage = (WindowsNativeImage) image;
            imageHandle = this.lastImage.getHandle();
            pSize.cx = this.lastImage.getWidth();
            pSize.cy = this.lastImage.getHeight();
        }
    }
    final private WinUser.POINT lastPosition = new WinUser.POINT();
    public final void setPosition(int x, int y) {
        if(lastPosition.x != x || lastPosition.y != y){
            lastPosition.x = x;
            lastPosition.y = y;
            pptDst = lastPosition;
        }
    }

    public final void updateWindow() {
        if (null != imageHandle) {
            final WinNT.HANDLE oldBmp = GDI32Ex.INSTANCE.SelectObject(hdcSrc, imageHandle);
            imageHandle = null;
            User32Ex.INSTANCE.UpdateLayeredWindow(hWnd, null, pptDst, pSize, hdcSrc, pptSrc, 0x00000000, blendFunction, WinUser.ULW_ALPHA);
            GDI32Ex.INSTANCE.SelectObject(hdcSrc, oldBmp);
        } else if (null != pptDst) {
            User32Ex.INSTANCE.UpdateLayeredWindow(hWnd, null, pptDst, null, null, pptSrc, 0x00000000, blendFunction, WinUser.ULW_ALPHA);
            pptDst = null;
        }
    }

}
