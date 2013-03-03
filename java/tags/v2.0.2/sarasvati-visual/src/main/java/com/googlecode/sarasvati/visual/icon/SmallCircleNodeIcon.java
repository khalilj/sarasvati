/*
    This file is part of Sarasvati.

    Sarasvati is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    Sarasvati is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with Sarasvati.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2009 Cheong Chung Onn
*/
package com.googlecode.sarasvati.visual.icon;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;

import com.googlecode.sarasvati.visual.common.NodeDrawConfig;

public class SmallCircleNodeIcon extends AbstractNodeIcon
{
  protected final Color color;
  protected final boolean isJoin;
  protected final boolean isSelected;

  public SmallCircleNodeIcon ()
  {
    this.color = Color.BLACK.brighter();
    this.isJoin = false;
    this.isSelected = false;
    redrawImage();
  }

  public SmallCircleNodeIcon (final Color color,
                              final boolean isJoin,
                              final boolean isSelected)
  {
    this.color = color;
    this.isJoin = isJoin;
    this.isSelected = isSelected;
    redrawImage();
  }

  @Override
  public void redrawImage (final Graphics2D g)
  {
    g.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

    double diameter = HEIGHT;
    double outerCircle = diameter * 0.7;
    double innerCircle = diameter * 0.5;

    g.setColor( isSelected ? NodeDrawConfig.NODE_BORDER_SELECTED : NodeDrawConfig.NODE_BORDER );

    float[] dashes = isJoin ? new float[] { 10, 5 } : null;
    BasicStroke stroke = new BasicStroke( 3, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10, dashes, 0 );
    g.setStroke( stroke );
    int offset = 1;
    g.translate( (HEIGHT - outerCircle) / 2 , offset );
    g.draw( new Ellipse2D.Double( 0, 0, outerCircle, outerCircle ) );

    g.setColor( color );
    g.translate( (outerCircle - innerCircle) / 2  , ( outerCircle - innerCircle ) / 2 );
    g.fill( new Ellipse2D.Double( 0, 0, innerCircle, innerCircle ) );
  }

  @Override
  public int getIconWidth ()
  {
    return getIconHeight();
  }
}