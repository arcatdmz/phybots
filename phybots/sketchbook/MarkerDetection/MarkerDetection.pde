import com.phybots.p5.*;
import com.phybots.p5.andy.*;

import com.phybots.hakoniwa.*;
import com.phybots.service.*;

import com.phybots.Phybots;
import com.phybots.utils.ScreenPosition;
import com.phybots.utils.ScreenRectangle;
import jp.digitalmuseum.napkit.NapMarker;
import jp.digitalmuseum.napkit.NapDetectionResult;

Camera camera;
PhybotsImage img;
MarkerDetector md;
NapMarker marker;

void setup()
{
  // Start the camera.
  camera = new Camera();
  camera.start();
  img = new PhybotsImage(camera);

  // Start marker detection.
  marker = new NapMarker("4x4_45.patt", 55);
  md = new MarkerDetector();
  md.setImageProvider(camera);
  md.addMarker(marker);
  md.start();
  
  size(640, 480);

  // Show the runtime debug tool.
  Phybots.getInstance().showDebugFrame();
}

void draw()
{
  image(img, 0, 0);

  NapDetectionResult result = md.getResult(marker);
  if (result != null) {
    ScreenPosition p = result.getPosition();
    ScreenRectangle r = result.getSquare();

    // Show the rectangle in red.
    stroke(255, 0, 0);
    for (int i = 0; i < 4; i ++) {
      ScreenPosition a = r.get(i);
      ScreenPosition b = r.get((i + 1) % 4);
      line(a.getX(), a.getY(), b.getX(), b.getY());
    }

    // Show the detection confidence.
    fill(255);
    text("confidence: " + result.getConfidence(), p.getX(), p.getY());
  }
}
