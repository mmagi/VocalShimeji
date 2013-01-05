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
import java.awt.*;

/**
 * α値つき画像ウィンドウ.
 * {@link #setImage(com.group_finity.mascot.image.NativeImage)} で設定した {@link WindowsNativeImage} をデスクトップに表示できる.
 * <p/>
 */
class WindowsTranslucentWindow extends JWindow implements TranslucentWindow {

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


    /**
     * 表示する画像.
     */
    private WindowsNativeImage image;

    @Override
    public String toString() {
        return "LayeredWindow[hashCode=" + hashCode() + ",bounds=" + getBounds() + "]";
    }
    WinDef.HWND hWnd;
    WinDef.HDC clientDC;
    WinDef.HDC memDC;
    @Override
    public void paint(final Graphics g) {
        if (getImage() != null) {
            final WinDef.HBITMAP imageHandle = getImage().getHandle();
            if (null == hWnd){
               hWnd = new WinDef.HWND(Native.getComponentPointer(this));
            }
            //final WinDef.HWND hWnd = new WinDef.HWND(Native.getComponentPointer(this));
            if (User32Ex.INSTANCE.IsWindowVisible(hWnd)) {

                final int exStyle = User32Ex.INSTANCE.GetWindowLongW(hWnd, WinUser.GWL_EXSTYLE);
                if ((exStyle & WinUser.WS_EX_LAYERED) == 0) {
                    User32Ex.INSTANCE.SetWindowLongW(hWnd, WinUser.GWL_EXSTYLE, exStyle | WinUser.WS_EX_LAYERED);
                }

                // 画像の転送元DCを作成
                if (null == clientDC){
                    clientDC = User32Ex.INSTANCE.GetDC(hWnd);
                }
                //final WinDef.HDC clientDC = User32Ex.INSTANCE.GetDC(hWnd);
                if(null == memDC){
                    memDC = GDI32Ex.INSTANCE.CreateCompatibleDC(clientDC);
                }
                //final WinDef.HDC memDC = GDI32Ex.INSTANCE.CreateCompatibleDC(clientDC);
                final WinNT.HANDLE oldBmp = GDI32Ex.INSTANCE.SelectObject(memDC, imageHandle);

                //User32Ex.INSTANCE.ReleaseDC(hWnd, clientDC);

                // 転送先領域
//                final WinDef.RECT windowRect = new WinDef.RECT();
//                User32Ex.INSTANCE.GetWindowRect(hWnd, windowRect);

                // 転送
                //            final WinUser.POINT lt = new WinUser.POINT();
                //            lt.x = windowRect.left;
                //            lt.y = windowRect.top;
                /**
                 * 位置和大小都不在这里处理，全部传null
                 *
                 * BOOL WINAPI UpdateLayeredWindow(
                 * _In_      HWND hwnd,
                 * _In_opt_  HDC hdcDst,
                 * _In_opt_  POINT *pptDst,
                 * _In_opt_  SIZE *psize,
                 * _In_opt_  HDC hdcSrc,
                 * _In_opt_  POINT *pptSrc,
                 * _In_      COLORREF crKey,
                 * _In_opt_  BLENDFUNCTION *pblend,
                 * _In_      DWORD dwFlags
                 * );
                 */
                User32Ex.INSTANCE.UpdateLayeredWindow(hWnd, null, null, size, memDC, pptSrc, 0x00000000, blendFunction, WinUser.ULW_ALPHA);

                // ビットマップは元に戻しておく
                GDI32Ex.INSTANCE.SelectObject(memDC, oldBmp);
                //An application cannot select a single bitmap into more than one DC at a time.
                //释放掉当前图像,给其他mascot用

                //GDI32Ex.INSTANCE.DeleteDC(memDC);
            }

        }
    }

    private WindowsNativeImage getImage() {
        return this.image;
    }
    private boolean needRepaint = false;
    final WinUser.SIZE size = new WinUser.SIZE();
    public void setImage(final NativeImage image) {
        if (image != this.image){
            this.image = (WindowsNativeImage) image;
            size.cx = this.image.getWidth();
            size.cy = this.image.getHeight();
            needRepaint = true;
        }
    }

    public void updateImage() {
        if (needRepaint){
            needRepaint = false;
            repaint();
        }
    }

}
