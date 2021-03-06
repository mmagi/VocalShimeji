package com.group_finity.mascot.image;

import com.group_finity.mascot.Main;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;


/**
 * 　画像ペアを読み込む.
 */
public class ImagePairLoader {

    private final Main main;

    public ImagePairLoader(Main main) {
        this.main = main;
    }

    /**
     * 画像ペアを読み込む.
     * <p/>
     * 左向き画像を読み込んで、右向き画像を自動生成する.
     *
     * @param name   読み込みたい左向き画像.
     * @param center 画像の中央座標.
     * @return 読み込んだ画像ペア.
     */
    public ImagePair load(final String name, final Point center) throws IOException {

        // flip では半透明にならない画像があるらしいので
        // shime1.png に対して shime1-r.png を反転画像として使用するようにして回避。
        String rightName = name.replaceAll("\\.[a-zA-Z]+$", "-r$0");
        try {
            final BufferedImage leftImage = ImageIO.read(main.getImageResource(name));
            final BufferedImage rightImage;
            if (main.getImageResource(rightName) == null) {
                rightImage = flip(leftImage);
            } else {
                rightImage = ImageIO.read(main.getImageResource(rightName));
            }
            return new ImagePair(new MascotImage(leftImage, center), new MascotImage(rightImage, new Point(rightImage.getWidth() - center.x, center.y)));
        } catch (IllegalArgumentException npe) {
            throw new RuntimeException("Error Reading file:" + name, npe);
        }
    }

    /**
     * 画像を左右反転させる.
     *
     * @param src 左右反転したい画像
     * @return 左右反転した
     */
    private static BufferedImage flip(final BufferedImage src) {

        final BufferedImage copy = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < src.getHeight(); ++y) {
            for (int x = 0; x < src.getWidth(); ++x) {
                copy.setRGB(copy.getWidth() - x - 1, y, src.getRGB(x, y));
            }
        }
        return copy;
    }

}
