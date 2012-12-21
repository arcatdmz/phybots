package marker.robot;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.phybots.Phybots;
import com.phybots.entity.Robot;
import com.phybots.gui.*;
import com.phybots.message.Event;
import com.phybots.message.EventListener;
import com.phybots.message.ImageUpdateEvent;
import com.phybots.resource.WheelsController;
import com.phybots.service.Camera;
import com.phybots.service.MarkerDetector;
import com.phybots.task.Task;
import com.phybots.task.TracePathLoosely;
import com.phybots.utils.Position;
import com.phybots.utils.ScreenPosition;

import jp.digitalmuseum.napkit.NapMarker;
import jp.digitalmuseum.napkit.gui.TypicalMDCPane;

/**
 * Draw path to navigate a robot along the path.<br/>
 * 描かれた線をなぞるように動く
 *
 * @author Jun Kato
 */
public class DrawAndTrace {
	private Camera camera;
	private MarkerDetector detector;
	private List<ScreenPosition> screenPath = new ArrayList<ScreenPosition>();
	private Path2D path2D;
	protected Robot robot;

	private transient final Stroke stroke =
		new BasicStroke(2);
	private transient final AlphaComposite alphaComp =
		AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .7f);

	private TracePathLoosely tracePath = null;

	public static void main(String[] args) {
		new DrawAndTrace();
	}

	public DrawAndTrace() {

		// Run a camera.
		// Let users select a device to capture images.
		final String identifier = (String) JOptionPane.showInputDialog(null,
				"Select a device to capture images.", "Device list",
				JOptionPane.QUESTION_MESSAGE, null,
				Camera.queryIdentifiers(), null);
		if ((identifier != null) && (identifier.length() > 0)) {
			camera = new Camera(identifier);
		} else {
			camera = new Camera();
		}
		camera.setSize(800, 600);
		camera.setRealSize(80, 60);
		camera.start();

		// Run a marker detector.
		detector = new MarkerDetector();
		detector.setImageProvider(camera);
		detector.start();

		// Show a configuration window.
		final JFrame configFrame = new DisposeOnCloseFrame(new TypicalMDCPane(
				detector));

		// Initialize a robot.
		robot = RobotInfo.getRobot();

		// Initialize boxes.
		detector.addMarker(new NapMarker("markers\\4x4_48.patt", 5.5), robot);

		// Make a window for showing captured image.
		final DrawableFrame frame = new DrawableFrame() {
			private static final long serialVersionUID = 1L;

			@Override public void dispose() {
				super.dispose();
				configFrame.dispose();
				Phybots.getInstance().dispose();
			}

			@Override
			public void paint2D(Graphics2D g) {
				Task task = robot.getAssignedTask(WheelsController.class);
				g.setColor(Color.white);
				g.fillRect(0, 0, getFrameWidth(), getFrameHeight());

				// Draw hakoniwa.
				camera.drawImage(g);

				// Draw frame.
				Composite comp = g.getComposite();
					g.setComposite(alphaComp);
					g.setColor(Color.black);
					g.fillRect(0, 0, getFrameWidth(), 35);
				g.setComposite(comp);

				// Draw path.
				if (path2D != null) {
					drawPath(g, path2D);
				}

				// Draw status.
				g.drawLine(0, 35, getFrameWidth(), 35);
				g.setColor(Color.white);
				if (screenPath.isEmpty()) {
					g.drawString("Draw path.", 10, 30);
				} else {
					if (task != null) {
						g.drawString("Status: "+task, 10, 30);
					} else {
						g.drawString("Status: Stopped", 10, 30);
					}
				}
			}
		};
		frame.setResizable(false);
		frame.setFrameSize(camera.getWidth(), camera.getHeight());

		// Bring the clicked object to the location in front of the user.
		frame.getPanel().addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				int x = e.getX(), y = e.getY();
				screenPath.clear();
				screenPath.add(new ScreenPosition(x, y));
				path2D = new Path2D.Float();
				path2D.moveTo(x, y);
			}
			public void mouseReleased(MouseEvent e) {
				screenPath.add(new ScreenPosition(e.getX(), e.getY()));

				synchronized (robot) {
					resample(30);
					List<Position> path = new ArrayList<Position>();
					path2D.reset();
					boolean isFirst = true;
					for (ScreenPosition sp : screenPath) {
						path.add(camera.screenToReal(sp));
						if (isFirst) {
							path2D.moveTo(sp.getX(), sp.getY());
							isFirst = false;
						} else {
							path2D.lineTo(sp.getX(), sp.getY());
						}
					}
					onPathSpecified(path);
				}
			}
		});
		frame.getPanel().addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				int x = e.getX(), y = e.getY();
				screenPath.add(new ScreenPosition(x, y));
				path2D.lineTo(x, y);
			}
		});

		// Repaint the window every time the image is updated.
		camera.addEventListener(new EventListener() {
			public void eventOccurred(Event e) {
				if (e instanceof ImageUpdateEvent) {
					frame.repaint();
				}
			}
		});
	}

	/**
	 * Resample the drawn path according to the specified unit length.<br/>
	 * 描かれた線を指定された長さごとに区切ってリサンプルする
	 *
	 * @param unitLength
	 */
	private void resample(double unitLength) {
		if (screenPath.isEmpty()) {
			return;
		}

		List<ScreenPosition> newPath = new ArrayList<ScreenPosition>();
		final Iterator<ScreenPosition> it = screenPath.iterator();
		ScreenPosition currentPosition = it.next(), nextPosition = null;
		while(it.hasNext()) {
			nextPosition = it.next();
			double distance;
			while ((distance = currentPosition.distance(nextPosition)) > unitLength) {
				newPath.add(currentPosition);
				currentPosition = new ScreenPosition(
						(int)((nextPosition.getX()*unitLength +
								currentPosition.getX()*(distance-unitLength))/distance),
						(int)((nextPosition.getY()*unitLength +
								currentPosition.getY()*(distance-unitLength))/distance));
			}
		}

		if (nextPosition != null) {
			newPath.add(nextPosition);
			screenPath = newPath;
		}
	}

	/**
	 * When path is drawn by the user, start navigating the robot.<br/>
	 * ユーザがパスを描き終えたら、それをなぞるようにロボットを動かす
	 *
	 * @param path
	 */
	protected void onPathSpecified(List<Position> path) {

		// If a task instance already exists, update it.
		if (tracePath != null &&
				tracePath.isStarted()) {
			tracePath.updatePath(path);
		}

		// Otherwise instantiate task and assign it to the robot.
		else {
			tracePath = new TracePathLoosely(path);
			// tracePath.setAllowedDistance(5);
			if (tracePath.assign(robot)) {
				tracePath.start();
			} else {
				tracePath = null;
			}
		}
	}

	/**
	 * Draw the specified path.<br/>
	 * 指定されたパスを画面上に描く
	 *
	 * @param g
	 * @param path2D
	 */
	protected void drawPath(Graphics2D g, Path2D path2D) {
		Stroke s = g.getStroke();
			g.setStroke(stroke);
			g.setColor(Color.black);
			g.draw(path2D);
		g.setStroke(s);
	}
}
