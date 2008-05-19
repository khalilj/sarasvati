/**
 * Created on May 6, 2008
 */
package org.codemonk.wf.visual.painter;

import java.awt.Graphics;

import org.codemonk.wf.db.NodeRef;
import org.codemonk.wf.visual.Point;

public interface NodePainter
{
  void paintNode (Graphics g, NodeRef node, int x, int y);

  void paintLeftIncomingAnchor (Graphics g, NodeRef node, int x, int y);

  void paintRightIncomingAnchor (Graphics g, NodeRef node, int x, int y);

  Point getLeftAnchor (int originX, int originY);

  Point getRightAnchor (int originX, int originY);

  Point getTopAnchor (int originX, int originY);
}