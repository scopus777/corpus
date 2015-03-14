package com.corpus.sensor.kinect;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import kinect.geometry.Pixel;
import kinect.visual.Imager;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.corpus.controller.SceneController;
import com.corpus.scene.Joint;
import com.corpus.scene.Scene;
import com.corpus.sensor.Sensor;

/**
 * This class opens a debug window containing the captured video of the
 * <i>Microsoft Kinect</i>. It is also able to draw the bones and orientations
 * of the joints contained in the body model.
 * 
 * @author Matthias Weise
 * 
 */
@SuppressWarnings("serial")
public class KinectDebugWindow extends JFrame implements Runnable {

	BufferedImage img_video = Imager.getNewVideoImage();
	ImageIcon imageIcon = new ImageIcon();
	boolean running;

	boolean drawBones = false;
	boolean drawCoordinateSystems = false;

	/**
	 * C'tor of the class.
	 * 
	 * @param drawBones
	 *            Determines whether the bones of the body model will be drawn.
	 * @param drawCoordinateSystems
	 *            Determines whether the orientations of the joints a visualized
	 *            with the help of coordinate systems.
	 */
	public KinectDebugWindow(boolean drawBones, boolean drawCoordinateSystems) {
		super("Kinect Debug Window");
		JPanel jPanel = new JPanel();
		imageIcon.setImage(img_video);
		setSize(640, 480);
		jPanel.add(new JLabel(imageIcon));
		add(jPanel);
		pack();
		setVisible(true);
		running = true;
		this.drawBones = drawBones;
		this.drawCoordinateSystems = drawCoordinateSystems;
	}

	@Override
	public void run() {
		while (running) {
			processFrame();
			try {
				Thread.sleep(1000 / 30);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Updates the image, draws the body model and resizes the image if
	 * necessary.
	 */
	private void processFrame() {
		Imager.updateColour640ImageWithVideo(img_video);
		drawSkeleton(img_video);
		BufferedImage resized = resize(img_video, this.getWidth(), this.getHeight());
		imageIcon.setImage(resized);
		repaint();
	}

	/**
	 * Updates the width and the height of the drawn image if the frame is
	 * resized.
	 * 
	 * @param image
	 *            {@link BufferedImage} containing the current frame.
	 * @param width
	 *            New width of the image.
	 * @param height
	 *            New height of the image.
	 * @return new {@link BufferedImage}
	 */
	public static BufferedImage resize(BufferedImage image, int width, int height) {
		float widthRatio = width / 640f;
		float heightRatio = height / 480f;
		float ratio = Math.min(widthRatio, heightRatio);
		int newWidth = (int) (ratio * 640f);
		int newHeight = (int) (ratio * 480f);
		BufferedImage bi = new BufferedImage(newWidth, newHeight, BufferedImage.TRANSLUCENT);
		Graphics2D g2d = (Graphics2D) bi.createGraphics();
		g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
		g2d.drawImage(image, 0, 0, newWidth, newHeight, null);
		g2d.dispose();
		return bi;
	}

	/**
	 * Draws the body model.
	 * 
	 * @param image
	 *            {@link BufferedImage} containing the current frame.
	 */
	private void drawSkeleton(BufferedImage image) {

		// set bone strength
		Graphics2D g2 = (Graphics2D) image.getGraphics();
		g2.setStroke(new BasicStroke(5));

		// get copy of the current scene
		Scene sceneCopy = SceneController.getInstance().getCopyOfScene();

		// sort joints according the the z value of the their position
		List<Joint> joints = new ArrayList<Joint>(sceneCopy.getJoints().values());
		Collections.sort(joints, new Comparator<Joint>() {
			@Override
			public int compare(Joint o1, Joint o2) {
				if (o1.getAbsolutePosition().getZ() > o2.getAbsolutePosition().getZ())
					return -1;
				else if (o1.getAbsolutePosition().getZ() < o2.getAbsolutePosition().getZ())
					return 1;
				else
					return 0;
			}
		});

		// draw bones and orientations if requested
		if (drawBones)
			drawBones(g2, joints);
		if (drawCoordinateSystems)
			drawCoordinateSystems(g2, joints, sceneCopy.getSensors());
	}

	/**
	 * Draws the bones connecting the given <code>joints</code>.
	 * 
	 * @param g2
	 * @param joints
	 */
	private void drawBones(Graphics2D g2, List<Joint> joints) {
		for (Joint joint : joints) {
			g2.setColor(Color.YELLOW);
			Pixel pixel = getPixelValue(joint.getAbsolutePosition());
			g2.drawOval(pixel.col - 3, pixel.row - 3, 6, 6);
			g2.setColor(Color.RED);
			if (joint.getParent() != null)
				drawline(g2, joint.getAbsolutePosition(), joint.getParent().getAbsolutePosition());

		}
	}

	/**
	 * Draws coordinate systems representing the orientations of the given
	 * <code>joints</code> and <code>sensors</code>.
	 * 
	 * @param g2
	 * @param joints
	 * @param sensors
	 */
	private void drawCoordinateSystems(Graphics2D g2, List<Joint> joints, List<Sensor> sensors) {
		for (Joint joint : joints) {
			drawCoordSystem(g2, joint.getAbsolutePosition(), joint.getAbsoluteOrientation(), 2);
		}

		for (Sensor sensor : sensors)
			if (sensor.getParent() != null) {
				Vector3D pos = sensor.getAbsolutePosition();
				Rotation rot = sensor.getAbsoluteOrientation();
				drawCoordSystem(g2, pos, rot, 2);
			}
	}

	/**
	 * Draws a line connecting the given points <code>p1</code> and
	 * <code>p2</code>.
	 * 
	 * @param g2
	 * @param p1
	 *            start point
	 * @param p2
	 *            end point
	 */
	private void drawline(Graphics2D g2, Vector3D p1, Vector3D p2) {
		Pixel start = getPixelValue(p1);
		Pixel end = getPixelValue(p2);
		g2.drawLine(start.col, start.row, end.col, end.row);
	}

	/**
	 * Draws a cooridinate system representing the given orientation.
	 * 
	 * @param g2
	 * @param pos
	 * @param ori
	 * @param size
	 */
	private void drawCoordSystem(Graphics2D g2, Vector3D pos, Rotation ori, int size) {
		Map<Color, Vector3D> map = new HashMap<Color, Vector3D>();
		map.put(Color.RED, pos.add(ori.applyInverseTo(new Vector3D(size, 0, 0))));
		map.put(Color.GREEN, pos.add(ori.applyInverseTo(new Vector3D(0, size, 0))));
		map.put(Color.BLUE, pos.add(ori.applyInverseTo(new Vector3D(0, 0, size))));
		map = sortByValue(map);

		for (Color key : map.keySet()) {
			g2.setColor(key);
			drawline(g2, pos, map.get(key));
		}

	}

	/**
	 * Determine the the corresponding pixel to a 3d position.
	 * 
	 * @see Microsoft.Kinect.dll
	 * 
	 * @param p
	 *            position
	 * @return corresponding {@link Pixel}
	 */
	private Pixel getPixelValue(Vector3D p) {
		int x = (int) ((0.5 + (p.getX() / 100) * 285.630004882813 / ((p.getZ() / 100) * 320.0)) * 640.0);
		int y = (int) ((0.5 - (p.getY() / 100) * 285.630004882813 / ((p.getZ() / 100) * 240.0)) * 480.0);
		return new Pixel(y + 20, x);
	}

	/**
	 * Terminates the {@link KinectDebugWindow}.
	 */
	public void terminate() {
		running = false;
		setVisible(false);
		dispose();
	}

	/**
	 * Sorts a {@link Map} containing positions according to the z value of the
	 * positions.
	 * 
	 * @param map
	 * @return sorted {@link Map}
	 */
	public static Map<Color, Vector3D> sortByValue(Map<Color, Vector3D> map) {
		List<Map.Entry<Color, Vector3D>> list = new LinkedList<>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<Color, Vector3D>>() {
			@Override
			public int compare(Map.Entry<Color, Vector3D> o1, Map.Entry<Color, Vector3D> o2) {
				if (o1.getValue().getZ() >= o2.getValue().getZ())
					return -1;
				else
					return 1;
			}
		});

		Map<Color, Vector3D> result = new LinkedHashMap<>();
		for (Map.Entry<Color, Vector3D> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
}
