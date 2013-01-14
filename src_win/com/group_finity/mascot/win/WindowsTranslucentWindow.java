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

final class WindowsTranslucentWindow extends JWindow implements TranslucentWindow {
    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) {
            try {
                hWnd = new WinDef.HWND(Native.getComponentPointer(this));
                final int exStyle = User32Ex.INSTANCE.GetWindowLongW(hWnd, WinUser.GWL_EXSTYLE);
                if ((exStyle & WinUser.WS_EX_LAYERED) == 0) {
                    User32Ex.INSTANCE.SetWindowLongW(hWnd, WinUser.GWL_EXSTYLE, exStyle | WinUser.WS_EX_LAYERED);
                }
                final WinDef.HDC clientDC = User32Ex.INSTANCE.GetDC(hWnd);
                if (null != hdcSrc)
                    GDI32Ex.INSTANCE.DeleteDC(hdcSrc);
                hdcSrc = GDI32Ex.INSTANCE.CreateCompatibleDC(clientDC);
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (null != hdcSrc)
            GDI32Ex.INSTANCE.DeleteDC(hdcSrc);
    }

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("EmptyMethod")
    @Override
    public void paint(Graphics g) {
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    public void repaint() {
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    public void update(Graphics g) {
    }

    @Override
    public JWindow asJWindow() {
        return this;
    }

    private final static WinUser.BLENDFUNCTION blendFunction = new WinUser.BLENDFUNCTION();

    static {
        blendFunction.BlendOp = WinUser.AC_SRC_OVER;
        blendFunction.BlendFlags = 0;
        blendFunction.SourceConstantAlpha = (byte) 255; // 濃度を設定
        blendFunction.AlphaFormat = WinUser.AC_SRC_ALPHA;
    }

    private final WinUser.POINT pptSrc = new WinUser.POINT(); //zero

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

    private WinDef.HWND hWnd;
    private WinDef.HDC hdcSrc;
    private final WinUser.SIZE pSize = new WinUser.SIZE();

    private volatile WindowsNativeImage imageDst;
    private WindowsNativeImage lastImage;

    public final void setImage(final NativeImage image) {
        if (image != this.lastImage) {
            this.lastImage = (WindowsNativeImage) image;
            imageDst = this.lastImage;
        }
    }

    private volatile WinUser.POINT pptDst;
    final private WinUser.POINT lastPosition = new WinUser.POINT();

    public final void setPosition(final int x, final int y) {
        if (lastPosition.x != x || lastPosition.y != y) {
            lastPosition.x = x;
            lastPosition.y = y;
            pptDst = new WinUser.POINT(x, y);
        }
    }

    public final void updateWindow() {
        if (null == hWnd)
            return;
        final WindowsNativeImage imageDst = this.imageDst;
        final WinUser.POINT pptDst = this.pptDst;
        if (null != imageDst) {
            this.imageDst = null;
            final WinNT.HANDLE oldBmp = NSelectObj(imageDst.getHandle());
            pSize.cx = imageDst.getWidth();
            pSize.cy = imageDst.getHeight();
            NUpdateWindow();
            NSelectObj(oldBmp);
        } else if (null != pptDst) {
            NMoveWin(pptDst);
            this.pptDst = null;
        }
    }

    private void NMoveWin(WinUser.POINT pptDst) {
        User32Ex.INSTANCE.MoveWindow(hWnd, pptDst.x, pptDst.y, pSize.cx, pSize.cy, false);
    }

    private WinNT.HANDLE NSelectObj(WinNT.HANDLE oldBmp) {
        return GDI32Ex.INSTANCE.SelectObject(hdcSrc, oldBmp);
    }

    private void NUpdateWindow() {
        User32Ex.INSTANCE.UpdateLayeredWindow(hWnd, null, lastPosition, pSize, hdcSrc, pptSrc, 0x00000000, blendFunction, WinUser.ULW_ALPHA);
    }



}
