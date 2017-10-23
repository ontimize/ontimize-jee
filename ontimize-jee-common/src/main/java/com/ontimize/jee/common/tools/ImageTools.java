package com.ontimize.jee.common.tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog.ModalExclusionType;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ImageTools {

	private static final Logger	logger			= LoggerFactory.getLogger(ImageTools.class);

	private static final double	SCALE_PRECISION	= 0.001;

	private ImageTools() {
		super();
	}

	public static void showImage(Image image) {
		ImageTools.showImage(image, "image");
	}

	public static void showImage(Image image, String title) {
		ImageTools.showImage(image, title, Color.white);
	}

	public static void showImage(Image image, String title, Color backgroundColor) {
		JFrame frame = new JFrame(title);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(new JLabel(new ImageIcon(image)));
		frame.getContentPane().setBackground(backgroundColor);
		// frame.setPreferredSize(new Dimension(image.getWidth(null) + 20, image.getHeight(null) + 20));
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
		frame.setVisible(true);
	}

	public static Image scaleImage(Image im, int width, int height) throws IOException {
		try {
			double widthRatio = im.getWidth(null) / (float) width;
			double heightRatio = im.getHeight(null) / (double) height;

			if (((widthRatio - MathTools.ONE_FLOAT) < ImageTools.SCALE_PRECISION) && ((heightRatio - MathTools.ONE_FLOAT) < ImageTools.SCALE_PRECISION)) {
				return im;
			}

			Image imScaled = null;
			if (widthRatio > heightRatio) {
				imScaled = new ImageIcon(im.getScaledInstance(width, -1, Image.SCALE_SMOOTH)).getImage();
			} else {
				imScaled = new ImageIcon(im.getScaledInstance(-1, height, Image.SCALE_SMOOTH)).getImage();
			}

			int x = Math.abs((imScaled.getWidth(null) - width) / 2);
			int y = Math.abs((imScaled.getHeight(null) - height) / 2);
			BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = bi.createGraphics();
			g.drawImage(imScaled, x, y, null);
			return bi;
		} catch (Exception e) {
			ImageTools.logger.error("Error_scaling_image", e);
			return im;
		}
	}

	public static Image scaleImage2(Image im, int width, int height) throws IOException {
		if ((height == 0) || (width == 0)) {
			return im;
		}
		try {
			double widthRatio = im.getWidth(null) / (float) width;
			double heightRatio = im.getHeight(null) / (double) height;

			if (((widthRatio - MathTools.ONE_FLOAT) < ImageTools.SCALE_PRECISION) && ((heightRatio - MathTools.ONE_FLOAT) < ImageTools.SCALE_PRECISION)) {
				return im;
			}

			if (widthRatio > heightRatio) {
				return im.getScaledInstance(width, -1, Image.SCALE_SMOOTH);
			} else {
				return im.getScaledInstance(-1, height, Image.SCALE_SMOOTH);
			}
		} catch (Exception e) {
			ImageTools.logger.error("Error_scaling_image", e);
			return im;
		}
	}

	public static Icon scaleImageIcon(ImageIcon base, int width, int height) {

		try {
			return new ImageIcon(ImageTools.scaleImage(base.getImage(), width, height));
		} catch (IOException e) {
			ImageTools.logger.error("Error_scaling_image", e);
			return base;
		}
	}

	public static BufferedImage imageToBufferedImage(ImageIcon imageIcon)
	        throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, IOException {

		Class<? extends ImageIcon> clazz = imageIcon.getClass();
		Field urlField = clazz.getDeclaredField("location");
		urlField.setAccessible(true);

		URL location = (URL) urlField.get(imageIcon);
		BufferedImage image = ImageIO.read(new File(location.getPath()));
		return image;

	}

	public static BufferedImage imageToBufferedImage(Image im) {
		if ((im == null) || (im.getWidth(null) < 0) || (im.getHeight(null) < 0)) {
			return null;
		}
		BufferedImage bi = new BufferedImage(im.getWidth(null), im.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics bg = bi.getGraphics();
		bg.drawImage(im, 0, 0, null);
		bg.dispose();
		return bi;
	}
}
