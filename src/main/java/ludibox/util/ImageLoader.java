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
}
