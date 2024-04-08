package com.example.gallery.component;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.squareup.picasso.Transformation;

public class BrightnessTransformation implements Transformation {
    private float brightness;

    public BrightnessTransformation(float brightness) {
        this.brightness = brightness;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        Bitmap bitmap = Bitmap.createBitmap(source.getWidth(), source.getHeight(), source.getConfig());

        for (int x = 0; x < source.getWidth(); x++) {
            for (int y = 0; y < source.getHeight(); y++) {
                int pixel = source.getPixel(x, y);

                int alpha = Color.alpha(pixel);
                int red = Color.red(pixel);
                int green = Color.green(pixel);
                int blue = Color.blue(pixel);

                // Điều chỉnh độ sáng của mỗi pixel
                red = (int) (red * brightness);
                green = (int) (green * brightness);
                blue = (int) (blue * brightness);

                // Đảm bảo rằng giá trị red, green và blue không vượt quá giới hạn [0, 255]
                red = Math.min(red, 255);
                green = Math.min(green, 255);
                blue = Math.min(blue, 255);

                // Đặt pixel mới vào bitmap
                bitmap.setPixel(x, y, Color.argb(alpha, red, green, blue));
            }
        }

        // Giải phóng bộ nhớ
        source.recycle();

        return bitmap;
    }

    @Override
    public String key() {
        return "brightness(" + brightness + ")";
    }
}
