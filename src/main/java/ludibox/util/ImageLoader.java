package ludibox.util;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

// resourcesフォルダにある画像を読み込むクラス
public class ImageLoader {
    // Image画像を読み込む
    public static Image loadImage(String path) {
        URL imageUrl = ImageLoader.class.getClassLoader().getResource(path);
        System.out.println("Image URL: " + imageUrl);
        if (imageUrl == null) {
            System.err.println("Image not found: " + path);
            return null;
        }
        return new ImageIcon(imageUrl).getImage();
    }
    // ImageIconを読み込む
    public static ImageIcon loadIcon(String path) {
        URL imageUrl = ImageLoader.class.getClassLoader().getResource(path);
        if (imageUrl == null) {
            System.err.println("Image not found: " + path);
            return null;
        }
        return new ImageIcon(imageUrl);
    }

    public static Image resizeByWidth(Image image, int width) {
        ImageIcon icon = new ImageIcon(image);
        int originalWidth = icon.getIconWidth();
        int originalHeight = icon.getIconHeight();
        int targetHeight = (int) ((double) originalHeight / originalWidth * width);

        return icon.getImage().getScaledInstance(width, targetHeight, Image.SCALE_SMOOTH);
    }
}
