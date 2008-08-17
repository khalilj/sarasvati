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

    Copyright 2008 Paul Lorenz
*/
package com.googlecode.sarasvati.event;

import com.googlecode.sarasvati.GuardResponse;
import com.googlecode.sarasvati.NodeToken;
import com.googlecode.sarasvati.Process;

public class NodeTokenEvent extends ExecutionEvent
{
  protected NodeToken nodeToken;
  protected GuardResponse guardResponse;

  public NodeTokenEvent (ExecutionEventType eventType, NodeToken nodeToken, GuardResponse guardResponse)
  {
    super( eventType );
    this.nodeToken = nodeToken;
    this.guardResponse = guardResponse;
  }

  @Override
  public NodeToken getNodeToken()
  {
    return nodeToken;
  }

  @Override
  public GuardResponse getGuardResponse()
  {
    return guardResponse;
  }

  @Override
  public Process getProcess()
  {
    return nodeToken.getProcess();
  }
}
