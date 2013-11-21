package com.group_finity.mascot;

import com.group_finity.mascot.environment.Environment;
import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.image.TranslucentWindow;
import com.sun.jna.Platform;

import java.awt.image.BufferedImage;

/**
 * ネイティブ環境へのアクセスを提供する.
 * {@link #getInstance()} は実行環境によって Windows 用あるいは汎用のサブクラスのインスタンスを返す.
 *
 * @author Yuki
 */
public abstract class NativeFactory {

    private static final NativeFactory instance;

    /**
     * サブクラスのインスタンスを作成しておく.
     */
    static {
        final String basePackage = NativeFactory.class.getName().substring(0, NativeFactory.class.getName().lastIndexOf('.'));

        final String subPackage = Platform.isWindows() ? "win" : "mac";

        try {
            @SuppressWarnings("unchecked")
            final Class<? extends NativeFactory> impl = (Class<? extends NativeFactory>) Class.forName(basePackage + "." + subPackage + ".NativeFactoryImpl");

            instance = impl.newInstance();

        } catch (final ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (final InstantiationException e) {
            throw new RuntimeException(e);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static NativeFactory getInstance() {
        return instance;
    }

    /**
     * 環境オブジェクトを取得する.
     *
     * @return 環境オブジェクト.
     */
    public abstract Environment getEnvironment();

    public abstract NativeImage newNativeImage(BufferedImage src);

    public abstract TranslucentWindow newTransparentWindow();
}
